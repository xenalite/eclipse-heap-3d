package org.eclipse.heap3d.ui.views;

import java.util.List;
import java.util.Map.Entry;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.eclipse.heap3d.resources.Resources;
import org.imperial.eclipse3dheap.layout.heapgraph.NodeManager;
import org.imperial.eclipse3dheap.nodes.CollectionNode;
import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;

public class ArrayNodeMouseEventHandler implements EventHandler<MouseEvent> {
	protected boolean isExpanded = false;
	private final INode node;
	private final NodeManager manager;
	private final Shape3D shape;
	private int clicks = 0;
	private ContextMenu openMenu;

	public ArrayNodeMouseEventHandler(NodeManager manager, INode node,
			Shape3D shape) {
		this.node = node;
		this.manager = manager;
		this.shape = shape;
	}

	@Override
	public void handle(MouseEvent event) {
		EventType<? extends MouseEvent> eventType = event.getEventType();
		if (eventType.equals(MouseEvent.MOUSE_CLICKED)) {
			if (openMenu != null)
				disposeOpenMenu();
			MenuItem hmi = new MenuItem("Help");
			hmi.setOnAction(e -> DialogService.openHelpDialog());

			MenuItem imi = new MenuItem("Inspect", new ImageView(Resources.SEARCH_ICON));
			imi.setOnAction(e -> {
				DialogService.openInspectDialog(node);
			});

			PhongMaterial m = new PhongMaterial();
			m.setDiffuseColor(Color.GREEN);
			m.setSpecularColor(Color.WHITE);
			Box box = new Box(10,10,10);
			box.setMaterial(m);
			box.getTransforms().addAll(new Rotate(45, Rotate.Z_AXIS), new Rotate(45, Rotate.X_AXIS));
			
			MenuItem emi = new MenuItem("Expand / Collapse", box);
			emi.setOnAction(e -> {
				handlePrimaryClick();
			});

			openMenu = new ContextMenu();
			openMenu.getItems().addAll(emi, imi, new SeparatorMenuItem(),
					hmi);
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

    if (isExpanded) {
      getMaterial().setDiffuseMap(Resources.SPINNER);
      manager.submitTask(() -> manager.collapseNode(node, () -> {
        if (clicks == currentClicks)
          collapseNodeCallback();
      }));
    } else {
      getMaterial().setDiffuseMap(Resources.SPINNER);

      ListView<HBox> listView = new ListView<>();
      listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      for(Entry<ReferenceAttributes, String> entry : ((CollectionNode) node).extractPrimitiveValues2().entrySet())
        listView.getItems().add(new HBox(new Text(entry.getKey().name + " = " + entry.getValue())));
      
      VBox vbox = new VBox(listView);
      
      Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setResizable(false);
      Scene dialogScene = new Scene(vbox, 300, 200);
      dialog.setScene(dialogScene);
      dialog.show();
      dialog.setOnHiding(e -> {
    	  List<Integer> indices = listView.getSelectionModel().getSelectedIndices();
    	  if(indices.isEmpty())
    		  return;
        manager.submitTask(() -> manager.expandCollectionNode(node, convertToIndices(indices), () -> {
          if (clicks == currentClicks)
            expandNodeCallback();
        }));
      });
      
      Button b = new Button("OK");
      b.setOnAction(e -> dialog.hide());
      vbox.getChildren().add(b);
    }
    isExpanded = !isExpanded;
  }

	private int[] convertToIndices(List<Integer> list) {
		int[] indices = new int[list.size()];
		for (int i = 0; i < list.size(); ++i)
			indices[i] = list.get(i);
		return indices;
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
		m.setDiffuseMap(null);
	}

	private void collapseNodeCallback() {
		PhongMaterial m = getMaterial();
		m.setDiffuseMap(null);
	}
}