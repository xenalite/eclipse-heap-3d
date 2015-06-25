package org.eclipse.heap3d.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;

import org.eclipse.heap3d.graph.edges.Reference;
import org.eclipse.heap3d.graph.edges.Type;
import org.eclipse.heap3d.graph.edges.Type.TypeKind;
import org.eclipse.heap3d.graph.nodes.INode;
import org.eclipse.heap3d.graph.structures.NodeAndReferencesDTO;
import org.eclipse.heap3d.graph.structures.NodeManager;
import org.eclipse.heap3d.resources.Resources;
import org.eclipse.heap3d.utils.GeometryUtils;
import org.eclipse.heap3d.utils.NodeConnection;
import org.eclipse.heap3d.utils.Utils;

public class Main3DScene {

  private static final int INITIAL_WIDTH = 1024;
  private static final int INITIAL_HEIGHT = 768;

  private Group world = new Group();
  private Group liveNodes = new Group();
  private Group deadNodes = new Group();
  private InputHandler inputHandler;
  private ShapeFactory shapeFactory;
  private Map<INode, ShapeInformation> nodesToShapes = new HashMap<>();
  private List<Timeline> timelines = new ArrayList<>();

  public Scene getScene() {
    Group root = new Group();
    Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT, true, SceneAntialiasing.BALANCED);
    scene.setFill(Color.WHITE);
    
    root.getChildren().add(world);
    world.getChildren().addAll(liveNodes, deadNodes);
    inputHandler = new InputHandler(scene, Configuration.getInstance());
    shapeFactory = new ShapeFactory(inputHandler);
    Utils.hackTooltipTiming();
    return scene;
  }

  public void clearStackSpace() {
    timelines.clear();
    liveNodes.getChildren().clear();
    deadNodes.getChildren().clear();
    inputHandler.getLabels().clear();
    nodesToShapes.clear();
  }

  public void createRootNodes(NodeManager nodeManager, Map<INode, Point3D> rootNodePositions) {
    IAnimationBuilder ab = new AnimationBuilder(true);
    
    for (Entry<INode, Point3D> entry : rootNodePositions.entrySet().stream()
    		.sorted((a,b) -> { return Integer.compare(a.getKey().getLevelId(), b.getKey().getLevelId()); })
    		.collect(Collectors.toList())) {
      Box rootNodeBox = shapeFactory.createRootNodeBox(entry.getValue());
    
      Type t = entry.getKey().getDeclaredType();
      String tooltipText = (Configuration.getInstance().typeNameDisplayMode == Configuration.TypeNameDisplayMode.FULL
          ? t.getFullName() : t.getShortName());
      
      if(tooltipText.length() != 0) {
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(rootNodeBox, tooltip);
      }
      
      nodesToShapes.put(entry.getKey(), new ShapeInformation(rootNodeBox));
      ab.addNode(rootNodeBox);
    
      BasicTransforms text = shapeFactory.createRootText(entry.getKey().toString(), entry.getValue());
      liveNodes.getChildren().addAll(rootNodeBox, text);
      
      StackNodeMouseEventHandler handler = 
          new StackNodeMouseEventHandler(text, nodeManager, entry.getKey(), rootNodeBox);
      rootNodeBox.setOnMouseClicked(handler);
      rootNodeBox.setOnMouseEntered(handler);
      rootNodeBox.setOnMouseExited(handler);
      
      text.setOnMouseClicked(handler);
      text.setOnMouseEntered(handler);
      text.setOnMouseExited(handler);
    }
    handleTimeline(ab.build());
  }

  public void updateNodeAndEdges(INode node, Point3D nodePosition, Map<Reference, Point3D> edgeEndPositions,
      Map<INode, Entry<Reference, Point3D>> reversePositionMap) {

    ShapeInformation shapeInformation = nodesToShapes.get(node);
    Shape3D shape = shapeInformation.getShape();
    GeometryUtils.setTranslate(shape, nodePosition);

    for (Entry<Reference, EdgeGroup> entry : shapeInformation.getReferenceMap().entrySet()) {
      Group oldGroup = entry.getValue();
      liveNodes.getChildren().remove(oldGroup);

      EdgeGroup newGroup = shapeFactory.createEdge(nodePosition, edgeEndPositions.get(entry.getKey()), entry.getKey());
      liveNodes.getChildren().add(newGroup);
      shapeInformation.getReferenceMap().put(entry.getKey(), newGroup);
    }

    for (Entry<INode, Entry<Reference, Point3D>> e : reversePositionMap.entrySet()) {
      ShapeInformation si = nodesToShapes.get(e.getKey());
      liveNodes.getChildren().remove(si.getReferenceMap().get(e.getValue().getKey()));

      EdgeGroup newGroup = shapeFactory.createEdge(e.getValue().getValue(), nodePosition, e.getValue().getKey());
      liveNodes.getChildren().add(newGroup);
      si.getReferenceMap().put(e.getValue().getKey(), newGroup);
    }
  }

  public void removeGarbage(INode parent, Collection<INode> garbage) {
    if(!nodesToShapes.containsKey(parent))
      return;
    ShapeInformation psi = nodesToShapes.get(parent);
    psi.getReferenceMap().entrySet().forEach(e -> liveNodes.getChildren().remove(e.getValue()));
    psi.getReferenceMap().clear();

    IAnimationBuilder ab = new AnimationBuilder(false);
    for (INode n : garbage) {
      ShapeInformation si = nodesToShapes.get(n);
      liveNodes.getChildren().remove(si.getShape());
      si.getShape().setOnMouseClicked(null);
      si.getShape().setOnMouseEntered(null);
      si.getShape().setOnMouseExited(null);
      si.getReferenceMap().entrySet().forEach(e -> liveNodes.getChildren().remove(e.getValue()));
      nodesToShapes.remove(n);
      
      deadNodes.getChildren().add(si.getShape());
      ab.addNode(si.getShape());
    }
    Timeline timeline = ab.build();
    timeline.setOnFinished(e -> {
      timeline.getOnFinished();
      deadNodes.getChildren().removeAll(ab.getNodes());
    });
    handleTimeline(timeline);
  }

  public void highlight(NodeAndReferencesDTO dto, boolean isEntering) {
    if(!nodesToShapes.containsKey(dto.parentNode))
      return;
    
    Image illuminationMap = (isEntering ? Resources.WHITE_ILLUMINATION_MAP : null);
    Color inEdgeColor = (isEntering ? ColourPicker.IN_EDGE_COLOR : ColourPicker.DEFAULT_EDGE_COLOR);
    Color outEdgeColor = (isEntering ? ColourPicker.OUT_EDGE_COLOR : ColourPicker.DEFAULT_EDGE_COLOR);
    
    ShapeInformation parentSi = nodesToShapes.get(dto.parentNode);
    setIllumination(parentSi.getShape(), illuminationMap);

    for(Entry<INode, Reference> e : dto.outgoing.entrySet()) {
      if(!nodesToShapes.containsKey(e.getKey()))
        continue;
      ShapeInformation si = nodesToShapes.get(e.getKey());
      setIllumination(si.getShape(), illuminationMap);
      
      parentSi.getReferenceMap().get(e.getValue()).highlight(outEdgeColor);
    }

    for(Entry<INode, Reference> e : dto.incoming.entrySet()) {
      if(!nodesToShapes.containsKey(e.getKey()))
        continue;
      ShapeInformation si = nodesToShapes.get(e.getKey());
      setIllumination(si.getShape(), illuminationMap);
      
      si.getReferenceMap().get(e.getValue()).highlight(inEdgeColor);
    }
  }
  
  private PhongMaterial getMaterial(Shape3D shape) {
    return ((PhongMaterial) shape.getMaterial());
  }
  
  private void setIllumination(Shape3D shape, Image map) {
    getMaterial(shape).setSelfIlluminationMap(map);
  }

  public void addChildren(NodeManager nodeManager, List<NodeConnection> connections) {
    IAnimationBuilder ab = new AnimationBuilder(true);
    for(NodeConnection connection : connections) {
      if (!nodesToShapes.containsKey(connection.toNode)) {
        Type t = connection.toNode.getDeclaredType();
        String tooltipText = (Configuration.getInstance().typeNameDisplayMode == Configuration.TypeNameDisplayMode.FULL
            ? t.getFullName() : t.getShortName());
        
        if(t.getTypeKind() == TypeKind.STRING)
      	  tooltipText = tooltipText + System.lineSeparator() + connection.toNode.toString();
        
        Shape3D childNodeBox = shapeFactory.createChildNodeBox(connection.toNode.isCollection(), tooltipText, connection.toPosition);
        ab.addNode(childNodeBox);
        if(tooltipText.length() != 0) {
          Tooltip tooltip = new Tooltip(tooltipText);
          Tooltip.install(childNodeBox, tooltip);
        }
        nodesToShapes.put(connection.toNode, new ShapeInformation(childNodeBox));
        liveNodes.getChildren().add(childNodeBox);
        
        EventHandler<MouseEvent> handler = connection.toNode.isCollection() ?
            new ArrayNodeMouseEventHandler(nodeManager, connection.toNode, childNodeBox)
            : new NodeMouseEventHandler(nodeManager, connection.toNode, childNodeBox);
        childNodeBox.setOnMouseClicked(handler);
        childNodeBox.setOnMouseEntered(handler);
        childNodeBox.setOnMouseExited(handler);
      }
      EdgeGroup edge = shapeFactory.createEdge(connection.fromPosition, connection.toPosition, connection.edge);
      liveNodes.getChildren().add(edge);
      nodesToShapes.get(connection.fromNode).getReferenceMap().put(connection.edge, edge);
    }
    handleTimeline(ab.build());
  }

  private void handleTimeline(Timeline t) {
	timelines.add(t);
    
	EventHandler<ActionEvent> ae = t.getOnFinished();
    t.setOnFinished(e -> {
      ae.handle(e);
      notifier();
    });
    
    if(timelines.size() == 1)
      t.play();
  }
  
  private void notifier() {
    Timeline t = timelines.get(0);
    if(t.getStatus() == Animation.Status.STOPPED) {
      timelines.remove(t);
      if(!timelines.isEmpty())
        timelines.get(0).play();
    }
  }
}