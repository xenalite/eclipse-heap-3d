package org.eclipse.heap3d.ui.views;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import org.eclipse.fx.ui.workbench3.FXViewPart;

public class Variables3DView extends FXViewPart {

  
  public Variables3DView() {
  }

  @Override
  protected Scene createFxScene() {
    Group g = new Group();
    Button t = new Button("hello");
    g.getChildren().add(t);
    Scene scene = new Scene(g, 1024, 768);
    return scene;
  }

  @Override
  protected void setFxFocus() {
    // TODO Auto-generated method stub
    
  }

}