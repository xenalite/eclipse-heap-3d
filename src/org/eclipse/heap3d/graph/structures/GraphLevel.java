package org.eclipse.heap3d.graph.structures;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javafx.geometry.Point3D;

import org.eclipse.heap3d.graph.edges.Reference;
import org.eclipse.heap3d.graph.layout.ConcreteGraph;
import org.eclipse.heap3d.graph.layout.FRLayoutDecorator;
import org.eclipse.heap3d.graph.layout.IGraph;
import org.eclipse.heap3d.graph.layout.ILayout;
import org.eclipse.heap3d.graph.nodes.INode;
import org.eclipse.heap3d.graph.nodes.StackNode;
import org.eclipse.heap3d.utils.Utils;

public class GraphLevel implements Iterable<VertexEdges> {

  private final static float SPACING = 400F;
  private final static float ATTRACTION = 0.9F;
  private final static float REPULSION = 0.01F;
  private static final double MODIFIER = 25F;

  private int id;

  private IGraph<INode, Reference> graph;
  private ILayout<INode, Reference> layout;
  private StackNode root;

  public GraphLevel(int id, StackNode root) {
    this.id = id;
    graph = new ConcreteGraph<>();
    layout = new FRLayoutDecorator<>(graph, ATTRACTION, REPULSION);
    addRootVertex(root);
  }

  public boolean addRootVertex(StackNode vertex) {
    if (graph.addVertex(vertex)) {
      root = vertex;
      layout.setRootVertex(vertex);
      return true;
    }
    return false;
  }

  public Point3D getVertexPosition(INode vertex) {
    if (graph.containsVertex(vertex)) {
      Point2D point2d = layout.getRawPosition(vertex);
      Point2D rootOffset = layout.getRawPosition(root);
      return new Point3D(MODIFIER * (point2d.getX() - rootOffset.getX()), id * SPACING, MODIFIER * (point2d.getY() - rootOffset.getY()));
    }
    throw new NoSuchElementException();
  }

  public boolean addVertex(INode vertex) {
    if (graph.containsVertex(vertex))
      return false;
    graph.addVertex(vertex);
    return true;
  }

  public boolean addEdge(Reference edge, INode fromVertex, INode toVertex) {
    if (graph.containsEdge(edge))
      return false;
    graph.addEdge(edge, fromVertex, toVertex);
    return true;
  }

  public Collection<INode> getVertices() {
    return graph.getVertices();
  }

  public boolean containsVertex(INode node) {
    return graph.containsVertex(node);
  }

  public Entry<INode, INode> getSourceDestination(Reference reference) {
    if (!graph.containsEdge(reference))
      throw new IllegalStateException("getSourceDestination");
    INode src = graph.getSource(reference);
    INode dest = graph.getDest(reference);
    return Utils.createPair(src, dest);
  }

  public int getId() {
    return id;
  }

  public Collection<Reference> getEdges() {
    return graph.getEdges();
  }

  @Override
  public String toString() {
    return String.format("{id:%s name:%s}", id, root);
  }

  public Iterator<VertexEdges> iterator() {
    return spanningTree(root);
  }

  public void runLayout() {
    int w = (int) (1200 * Math.sqrt(graph.getVertexCount()));
    layout.setSize(new Dimension(w, w));
    layout.layout();
  }

  public IGraph<INode, Reference> getGraph() {
    return graph;
  }

  public Iterator<VertexEdges> spanningTree(INode vertex) {
    assert graph.containsVertex(vertex);

    return new SpanningTreeIterator(vertex, graph);
  }
}