package org.eclipse.heap3d.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.eclipse.heap3d.graph.edges.ReferenceAttributes;
import org.eclipse.heap3d.graph.edges.Type;
import org.eclipse.heap3d.graph.edges.Type.TypeKind;
import org.eclipse.heap3d.graph.nodes.INode;
import org.eclipse.heap3d.resources.Resources;
import org.eclipse.heap3d.ui.views.Configuration.TypeNameDisplayMode;

public class DialogService {

  public static void openHelpDialog() {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    VBox vbox = new VBox();
    Text t1 = new Text("Controls" + System.lineSeparator());
    t1.setFont(Font.font("Ubuntu", 24));
    
    StringBuilder sb = new StringBuilder();
    sb.append("Hold left mouse button - Rotate camera" + System.lineSeparator());
    sb.append("W - move forward" + System.lineSeparator());
    sb.append("A - move left" + System.lineSeparator());
    sb.append("S - move back" + System.lineSeparator());
    sb.append("D - move right" + System.lineSeparator());
    sb.append("SPACE - go up" + System.lineSeparator());
    sb.append("C - go down" + System.lineSeparator());
    sb.append("Hold LSHIFT - increase movement speed" + System.lineSeparator());
    sb.append("R - reset camera" + System.lineSeparator());
    sb.append("Click left mouse button - Expand / collapse node" + System.lineSeparator());
    sb.append("Click right mouse button - Open menu" + System.lineSeparator());
    sb.append("Hover on node - Type information" + System.lineSeparator());
    
    Text c1 = new Text(sb.toString());
    c1.setFont(Font.font("Ubuntu", 16));
    
    vbox.getChildren().addAll(t1, c1);
    Scene dialogScene = new Scene(vbox, 600, 400);
    dialog.setScene(dialogScene);
    dialog.show();
  }

  public static void openInspectDialog(INode node) {
    List<Entry<ReferenceAttributes, String>> set = new ArrayList<>(node.getPrimitiveValues().entrySet());
    
    ListView<VBox> listView = new ListView<>();
    TextField tf = new TextField();
    tf.setEditable(false);
    listView.getSelectionModel().selectedIndexProperty().addListener(e -> {
      tf.setText(set.get(listView.getSelectionModel().getSelectedIndex()).toString());
    });
    
    for(Entry<ReferenceAttributes, String> e : set) {
      VBox vBox = new VBox();
      listView.getItems().add(vBox);
      
      Type type = e.getKey().type;
      TypeKind kind = type.getTypeKind();
      Image icon = (kind == TypeKind.PRIMITIVE ? null :
        kind  == TypeKind.INTERFACE ? Resources.INTERFACE_ICON : Resources.CLASS_ICON);

      Image icon2 = null;
      ReferenceAttributes attributes = e.getKey();
      if(attributes.isPrivate)
        icon2 = Resources.PRIVATE_ICON;
      else if(attributes.isPublic)
        icon2 = Resources.PUBLIC_ICON;
      else if(attributes.isPackage)
        icon2 = Resources.PACKAGE_ICON;
      else if(attributes.isProtected)
        icon2 = Resources.PROTECTED_ICON;
      else if(attributes.isLocal)
        icon2 = Resources.LOCALVAR_ICON;
      
      vBox.getChildren().addAll(new HBox(new ImageView(icon), new ImageView(icon2), 
          new Text(Configuration.getInstance().typeNameDisplayMode == TypeNameDisplayMode.FULL ?  
              type.getFullName() : type.getShortName())), 
              new HBox(new Text(e.getKey().name + " = " + e.getValue())));
    }
    
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    Scene dialogScene = new Scene(new VBox(tf, listView), 300, 200);
    dialog.setScene(dialogScene);
    dialog.show();
  }
}
