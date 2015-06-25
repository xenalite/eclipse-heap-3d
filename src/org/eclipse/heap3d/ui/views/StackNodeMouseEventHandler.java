package org.eclipse.heap3d.ui.views;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape3D;

import org.imperial.eclipse3dheap.layout.heapgraph.NodeManager;
import org.imperial.eclipse3dheap.nodes.INode;

public class StackNodeMouseEventHandler extends NodeMouseEventHandler {

  private BasicTransforms text;
  
  public StackNodeMouseEventHandler(BasicTransforms text, NodeManager manager, INode node, Shape3D shape) {
    super(manager, node, shape);
    this.text = text;
  }
  
  @Override
  public void handle(MouseEvent event) {
    super.handle(event);
    if(event.getEventType() == MouseEvent.MOUSE_CLICKED) {
      text.getChildren().get(0).visibleProperty().bind(isExpanded.not());
    }
  }
}
