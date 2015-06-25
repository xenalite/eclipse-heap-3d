package org.eclipse.heap3d.ui.views;

import java.util.Collection;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class EdgeGroup extends Group {

  public void highlight(Color color) {
    highlight(color, getChildren());
  }

  private void highlight(Color color, Collection<Node> children) {
    for(Node n : children) {
      if(n instanceof Box) {
        ((PhongMaterial) ((Box) n).getMaterial()).setDiffuseColor(color);
      }
      else if(n instanceof Group) {
        highlight(color, ((Group) n).getChildren());
      }
    }
  }
}
