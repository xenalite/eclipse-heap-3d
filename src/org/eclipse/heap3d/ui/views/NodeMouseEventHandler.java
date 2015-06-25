package org.eclipse.heap3d.ui.views;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

import org.eclipse.heap3d.resources.Resources;
import org.imperial.eclipse3dheap.layout.heapgraph.NodeManager;
import org.imperial.eclipse3dheap.nodes.INode;

public class NodeMouseEventHandler implements EventHandler<MouseEvent> {
  protected BooleanProperty isExpanded = new SimpleBooleanProperty(false);
  private final INode node;
  private final NodeManager manager;
  private final Shape3D shape;
  private int clicks = 0;
  private ContextMenu openMenu;

  public NodeMouseEventHandler(NodeManager manager, INode node, Shape3D shape) {
    this.node = node;
    this.manager = manager;
    this.shape = shape;
  }

  @Override
  public void handle(MouseEvent event) {
    EventType<? extends MouseEvent> eventType = event.getEventType();
    if (eventType.equals(MouseEvent.MOUSE_CLICKED)) {
    	if(openMenu != null)
            disposeOpenMenu();
          MenuItem hmi = new MenuItem("Help");
          hmi.setOnAction(e -> DialogService.openHelpDialog());

          PhongMaterial m = new PhongMaterial();
			m.setDiffuseColor(Color.GREEN);
			m.setSpecularColor(Color.WHITE);
			Box box = new Box(10,10,10);
			box.setMaterial(m);
			box.getTransforms().addAll(new Rotate(45, Rotate.Z_AXIS), new Rotate(45, Rotate.X_AXIS));
			
          
          MenuItem imi = new MenuItem("Inspect", new ImageView(Resources.SEARCH_ICON));
          imi.setOnAction(e -> {
            DialogService.openInspectDialog(node);
          });
          
          MenuItem emi = new MenuItem("Expand / Collapse", box);
          emi.setOnAction(e -> {
            handlePrimaryClick();
          });
          
          openMenu = new ContextMenu();
          openMenu.getItems().addAll(emi, imi,
              new SeparatorMenuItem(), hmi);
          openMenu.show(shape, event.getScreenX(), event.getScreenY());
    } else if (eventType.equals(MouseEvent.MOUSE_ENTERED)) {
      manager.submitTask(() -> manager.findReferingNodes(node, true));
    } else if (eventType.equals(MouseEvent.MOUSE_EXITED)) {
      manager.submitTask(() -> manager.findReferingNodes(node, false));
    }
  }
  
  private void handlePrimaryClick() {
    final int currentClicks = ++clicks;
    manager.submitTask(() -> manager.findReferingNodes(node, false));

    if (isExpanded.get()) {
      getMaterial().setDiffuseMap(Resources.SPINNER);
      manager.submitTask(() -> manager.collapseNode(node, () -> {
        if (clicks == currentClicks)
          collapseNodeCallback();
      }));
    } else {
      getMaterial().setDiffuseMap(Resources.SPINNER);
      manager.submitTask(() -> manager.expandNode(node, () -> {
        if (clicks == currentClicks)
          expandNodeCallback();
      }));
    }
    isExpanded.setValue(!isExpanded.get());
  }

  private void disposeOpenMenu() {
    openMenu.hide();
    openMenu = null;
  }
  
  private PhongMaterial getMaterial() {
    return (PhongMaterial) shape.getMaterial();
  }

  private void expandNodeCallback() {
    PhongMaterial m = getMaterial();
    m.setDiffuseMap(Resources.BLOB);
  }

  private void collapseNodeCallback() {
    PhongMaterial m = getMaterial();
    m.setDiffuseMap(null);
  }
}