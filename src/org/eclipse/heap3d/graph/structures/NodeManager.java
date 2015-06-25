package org.eclipse.heap3d.graph.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Point3D;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.imperial.eclipse3dheap.layout.algorithms.ConcreteGraph;
import org.imperial.eclipse3dheap.layout.algorithms.IGraph;
import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.nodes.StackNode;
import org.imperial.eclipse3dheap.reference.EdgeType;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.utils.NodeConnection;
import org.imperial.eclipse3dheap.utils.Utils;
import org.imperial.eclipse3dheap.views.Main3DScene;
import org.imperial.eclipse3dheap.views.UpdateInformation;

public class NodeManager {

	private Main3DScene main3dScene;
	private Map<Integer, GraphLevel> graphLevels = new HashMap<>();
	private IGraph<INode, Reference> interLevelGraph = new ConcreteGraph<>();
	public static IJavaStackFrame jsf;

	private final Queue<Runnable> tasksQueue = new ArrayDeque<>();

	public NodeManager(Main3DScene main3dScene) {
		this.main3dScene = main3dScene;
		initialiseWorkerThread();
	}

	private void initialiseWorkerThread() {
		Thread t1 = new Thread(() -> {
			while (true) {
				try {
					Runnable currentTask;
					synchronized (tasksQueue) {
						while (tasksQueue.isEmpty())
							tasksQueue.wait();
						currentTask = tasksQueue.poll();
					}
					currentTask.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		t1.setDaemon(true);
		t1.setName("node-manager-worker-thread");
		t1.start();
	}

	public void resetWorkerAndRun(Runnable task) {
		synchronized (tasksQueue) {
			tasksQueue.clear();
			tasksQueue.add(task);
			tasksQueue.notify();
		}
	}

	public void notifyOfNewStack(IJavaStackFrame javaStackFrame) throws DebugException {
		clearDataStructures();
		Map<INode, Point3D> rootNodePositions = new HashMap<>();
		jsf = javaStackFrame;

		addThisObject(javaStackFrame.getThis(), rootNodePositions);
		addLocalVariables(javaStackFrame.getLocalVariables(), rootNodePositions);

		Platform.runLater(() -> {
			main3dScene.clearStackSpace();
			main3dScene.createRootNodes(this, rootNodePositions);
		});
	}

	private void addLocalVariables(IJavaVariable[] localVariables, Map<INode, Point3D> rootNodePositions)
			throws DebugException {
		int currentLevelId = 1;

		for (IJavaVariable localVariable : localVariables) {
			IJavaValue value = (IJavaValue) localVariable.getValue();
			StackNode stackNode = (value instanceof IJavaPrimitiveValue) ? new StackNode(currentLevelId, localVariable)
					: new StackNode(currentLevelId, localVariable, (IJavaObject) value);

			GraphLevel graphLevel = new GraphLevel(currentLevelId, stackNode);
			graphLevels.put(currentLevelId++, graphLevel);
			rootNodePositions.put(stackNode, graphLevel.getVertexPosition(stackNode));
		}
	}

	private void clearDataStructures() {
		jsf = null;
		graphLevels.clear();
		interLevelGraph = new ConcreteGraph<>();
	}

	private void addThisObject(IJavaObject thisObject, Map<INode, Point3D> rootNodePositions) {
		final int THIS_OBJECT_LEVEL_ID = 0;
		if (thisObject != null) {
			StackNode stackNode = new StackNode(THIS_OBJECT_LEVEL_ID, thisObject);
			GraphLevel graphLevel = new GraphLevel(THIS_OBJECT_LEVEL_ID, stackNode);
			graphLevels.put(THIS_OBJECT_LEVEL_ID, graphLevel);
			rootNodePositions.put(stackNode, graphLevel.getVertexPosition(stackNode));
		}
	}

	public void submitTask(Runnable r) {
		synchronized (tasksQueue) {
			tasksQueue.add(r);
			tasksQueue.notify();
		}
	}

	public void expandCollectionNode(INode parentNode, int[] indices, Runnable callback) {
		collapseNode(parentNode, () -> {
		});
		List<NodeConnection> connections = new ArrayList<>();
		Map<INode, Reference> children = parentNode.expandPartially(indices);

		for (Entry<INode, Reference> childEntry : children.entrySet()) {
			NodeConnection connection = findContainingLevel(parentNode, childEntry.getKey(), childEntry.getValue());

			if (connection == NodeConnection.EMPTY)
				connections.add(createNewConnection(parentNode, childEntry.getKey(), childEntry.getValue()));
			else
				connections.add(connection);
		}
		graphLevels.values().forEach(e -> e.runLayout());
		List<List<UpdateInformation>> updatesList = graphLevels.keySet().stream()
				.map(e -> updateNodeAndEdgePositions(e)).collect(Collectors.toList());

		Platform.runLater(() -> {
			callback.run();
			main3dScene.addChildren(this, connections);

			for (List<UpdateInformation> updates : updatesList)
				for (UpdateInformation update : updates)
					main3dScene.updateNodeAndEdges(update.parentNode, update.parentPosition, update.positionMap,
							update.reversePositionMap);
		});
	}

	public void expandNode(INode parentNode, Runnable callback) {
		List<NodeConnection> connections = new ArrayList<>();
		Map<INode, Reference> children = parentNode.expand(jsf);

		for (Entry<INode, Reference> childEntry : children.entrySet()) {
			NodeConnection connection = findContainingLevel(parentNode, childEntry.getKey(), childEntry.getValue());

			if (connection == NodeConnection.EMPTY)
				connections.add(createNewConnection(parentNode, childEntry.getKey(), childEntry.getValue()));
			else
				connections.add(connection);
		}
		graphLevels.values().forEach(e -> e.runLayout());
		List<List<UpdateInformation>> updatesList = graphLevels.keySet().stream()
				.map(e -> updateNodeAndEdgePositions(e)).collect(Collectors.toList());

		Platform.runLater(() -> {
			callback.run();
			if(connections.isEmpty())
				return;
			main3dScene.addChildren(this, connections);

			for (List<UpdateInformation> updates : updatesList)
				for (UpdateInformation update : updates)
					main3dScene.updateNodeAndEdges(update.parentNode, update.parentPosition, update.positionMap,
							update.reversePositionMap);
		});
	}

	private List<UpdateInformation> updateNodeAndEdgePositions(int levelId) {
		List<UpdateInformation> updates = new ArrayList<>();
		GraphLevel graphLevel = graphLevels.get(levelId);
		for (VertexEdges entry : graphLevel) {
			Map<Reference, Point3D> positionMap = new HashMap<>();
			Map<INode, Entry<Reference, Point3D>> reversePositionMap = new HashMap<>();
			INode vertex = entry.vertex;

			if (interLevelGraph.containsVertex(vertex)) {
				Collection<Reference> outs = interLevelGraph.getOutEdges(vertex);
				outs = (outs == null) ? new HashSet<>() : outs;
				Collection<Reference> ins = interLevelGraph.getInEdges(vertex);
				ins = (ins == null) ? new HashSet<>() : ins;

				for (Reference r : outs) {
					r.setEdgeType(EdgeType.SINGLE);
					INode dest = interLevelGraph.getDest(r);
					Reference opposite = interLevelGraph.findEdge(dest, vertex);
					if (opposite != null) {
						r.setEdgeType(EdgeType.PARALLEL);
						opposite.setEdgeType(EdgeType.PARALLEL);
					}
					positionMap.put(r, graphLevels.get(dest.getLevelId()).getVertexPosition(dest));
				}

				for (Reference r : ins) {
					INode src = interLevelGraph.getSource(r);
					reversePositionMap.put(src,
							Utils.createPair(r, graphLevels.get(src.getLevelId()).getVertexPosition(src)));
				}
			}

			for (Reference r : entry.references) {
				r.setEdgeType(EdgeType.SINGLE);
				INode dest = graphLevel.getGraph().getDest(r);
				if (dest == vertex)
					r.setEdgeType(EdgeType.LOOP);
				else {
					Reference opposite = graphLevel.getGraph().findEdge(dest, vertex);
					if (opposite != null) {
						r.setEdgeType(EdgeType.PARALLEL);
						opposite.setEdgeType(EdgeType.PARALLEL);
					}
				}
				positionMap.put(r, graphLevel.getVertexPosition(dest));
			}
			updates.add(new UpdateInformation(vertex, graphLevel.getVertexPosition(vertex), positionMap,
					reversePositionMap));
		}
		return updates;
	}

	private NodeConnection createNewConnection(INode parentNode, INode childNode, Reference reference) {
		GraphLevel parentLevel = graphLevels.get(childNode.getLevelId());
		parentLevel.addVertex(childNode);
		parentLevel.addEdge(reference, parentNode, childNode);

		return new NodeConnection(parentNode, parentLevel.getVertexPosition(parentNode), childNode,
				parentLevel.getVertexPosition(childNode), reference);
	}

	private NodeConnection findContainingLevel(INode parentNode, INode childNode, Reference reference) {
		for (Entry<Integer, GraphLevel> currentLevelEntry : graphLevels.entrySet()) {
			GraphLevel currentLevel = currentLevelEntry.getValue();

			if (currentLevel.containsVertex(childNode)) {
				INode equivalentChildNode = currentLevel.getVertices().stream().filter(e -> e.equals(childNode))
						.findFirst().get();

				if (currentLevel.getId() == childNode.getLevelId())
					currentLevel.addEdge(reference, parentNode, equivalentChildNode);
				else
					interLevelGraph.addEdge(reference, parentNode, equivalentChildNode);

				return new NodeConnection(parentNode,
						graphLevels.get(parentNode.getLevelId()).getVertexPosition(parentNode), equivalentChildNode,
						currentLevel.getVertexPosition(equivalentChildNode), reference);
			}
		}
		return NodeConnection.EMPTY;
	}

	public void collapseNode(INode parentNode, Runnable callback) {
		GraphLevel parentGraphLevel = graphLevels.get(parentNode.getLevelId());
		IGraph<INode, Reference> parentGraph = parentGraphLevel.getGraph();

		Collection<Reference> outEdgesParentLevel = parentGraph.getOutEdges(parentNode);
		while (outEdgesParentLevel != null && !outEdgesParentLevel.isEmpty())
			parentGraph.removeEdge(outEdgesParentLevel.iterator().next());

		Collection<Reference> outEdgesInterlevel = interLevelGraph.getOutEdges(parentNode);
		while (outEdgesInterlevel != null && !outEdgesInterlevel.isEmpty())
			interLevelGraph.removeEdge(outEdgesInterlevel.iterator().next());

		Iterator<VertexEdges> rootPartitionIterator = parentGraphLevel.iterator();
		Set<INode> rootPartitionNodes = new HashSet<>();
		while (rootPartitionIterator.hasNext())
			rootPartitionNodes.add(rootPartitionIterator.next().vertex);

		Map<INode, Boolean> partitionedNodes = parentGraph.getVertices().stream()
				.filter(vertex -> !rootPartitionNodes.contains(vertex)).collect(Collectors.toMap(k -> k, k -> false));

		for (Entry<INode, Boolean> partitionedNodeEntry : partitionedNodes.entrySet()) {
			INode partitionedNode = partitionedNodeEntry.getKey();
			if (partitionedNodeEntry.getValue())
				continue;
			if (!interLevelGraph.containsVertex(partitionedNode))
				continue;

			Collection<Reference> pNodeInEdgesIL = interLevelGraph.getInEdges(partitionedNode);
			if (pNodeInEdgesIL == null || pNodeInEdgesIL.isEmpty())
				continue;

			Reference inEdgeInterlevel = pNodeInEdgesIL.iterator().next();
			INode inEdgeSource = interLevelGraph.getSource(inEdgeInterlevel);
			GraphLevel siblingGraphLevel = graphLevels.get(inEdgeSource.getLevelId());
			IGraph<INode, Reference> siblingGraph = siblingGraphLevel.getGraph();
			int siblingLevelId = inEdgeSource.getLevelId();

			Iterator<VertexEdges> partitionIterator = parentGraphLevel.spanningTree(partitionedNode);
			while (partitionIterator.hasNext()) {
				VertexEdges currentVertexEdges = partitionIterator.next();
				INode vertex = currentVertexEdges.vertex;
				vertex.setLevelId(siblingLevelId);
				partitionedNodes.put(vertex, true);

				Collection<Reference> inEdgesIL = interLevelGraph.getInEdges(vertex);
				if (inEdgesIL != null && !inEdgesIL.isEmpty()) {
					inEdgesIL.stream().filter(k -> {
						return siblingLevelId == interLevelGraph.getSource(k).getLevelId();
					}).collect(Collectors.toSet()).forEach(k -> {
						INode source = interLevelGraph.getSource(k);
						siblingGraph.addEdge(k, source, vertex);
						interLevelGraph.removeEdge(k);
					});
				}

				Collection<Reference> outEdgesIL = interLevelGraph.getOutEdges(vertex);
				if (outEdgesIL != null && !outEdgesIL.isEmpty()) {
					outEdgesIL.stream().filter(k -> {
						return siblingLevelId == interLevelGraph.getDest(k).getLevelId();
					}).collect(Collectors.toSet()).forEach(k -> {
						INode dest = interLevelGraph.getDest(k);
						siblingGraph.addEdge(k, vertex, dest);
						interLevelGraph.removeEdge(k);
					});
				}

				for (Reference r : currentVertexEdges.references) {
					INode dest = parentGraph.getDest(r);

					if (rootPartitionNodes.contains(dest))
						interLevelGraph.addEdge(r, vertex, dest);
					else {
					  dest.setLevelId(siblingLevelId);
						siblingGraph.addEdge(r, vertex, dest);
					}
				}
			}
		}

		Set<INode> garbageNodes = partitionedNodes.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey())
				.collect(Collectors.toSet());

		partitionedNodes.entrySet().forEach(e -> parentGraph.removeVertex(e.getKey()));
		garbageNodes.forEach(e -> interLevelGraph.removeVertex(e));
		graphLevels.values().forEach(e -> e.runLayout());
		List<List<UpdateInformation>> updatesList = graphLevels.keySet().stream()
				.map(e -> updateNodeAndEdgePositions(e)).collect(Collectors.toList());

		Platform.runLater(() -> {
			callback.run();
			if(garbageNodes.isEmpty())
				return;
			main3dScene.removeGarbage(parentNode, garbageNodes);

			for (List<UpdateInformation> updates : updatesList) {
				for (UpdateInformation update : updates)
					main3dScene.updateNodeAndEdges(update.parentNode, update.parentPosition, update.positionMap,
							update.reversePositionMap);
			}
		});
	}

	public void notifyOfTermination() {
		clearDataStructures();
		Platform.runLater(() -> {
			main3dScene.clearStackSpace();
		});
	}

	public void findReferingNodes(INode parentNode, boolean isEntering) {
		IGraph<INode, Reference> parentGraph = graphLevels.get(parentNode.getLevelId()).getGraph();

		Map<INode, Reference> outgoingMap = new HashMap<>();
		Collection<Reference> outgoingReferences = parentGraph.getOutEdges(parentNode);
		Collection<Reference> outgoingReferencesIL = interLevelGraph.getOutEdges(parentNode);

		if (outgoingReferences != null)
			for (Reference r : outgoingReferences)
				outgoingMap.put(parentGraph.getOpposite(parentNode, r), r);
		if (outgoingReferencesIL != null)
			for (Reference r : outgoingReferencesIL)
				outgoingMap.put(interLevelGraph.getOpposite(parentNode, r), r);

		Map<INode, Reference> incomingMap = new HashMap<>();
		Collection<Reference> incomingReferences = parentGraph.getInEdges(parentNode);
		Collection<Reference> incomingReferencesIL = interLevelGraph.getInEdges(parentNode);

		if (incomingReferences != null)
			for (Reference r : incomingReferences)
				incomingMap.put(parentGraph.getOpposite(parentNode, r), r);
		if (incomingReferencesIL != null)
			for (Reference r : incomingReferencesIL)
				incomingMap.put(interLevelGraph.getOpposite(parentNode, r), r);

		NodeAndReferencesDTO dto = new NodeAndReferencesDTO(parentNode, outgoingMap, incomingMap);

		Platform.runLater(() -> main3dScene.highlight(dto, isEntering));
	}
}