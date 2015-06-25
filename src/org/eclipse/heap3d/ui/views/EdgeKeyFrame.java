package org.eclipse.heap3d.ui.views;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import org.eclipse.heap3d.utils.GeometryUtils;

public class EdgeKeyFrame implements EventHandler<ActionEvent> {

  private Box node;
  private Box edge;
  
  private Point3D from;
  private Point3D to;
  private Point3D current = Point3D.ZERO;

  private Translate t;
  private Scale s;
  
  double factor = 0.0D;
  double scale = 0.0D;

  private EdgeKeyFrame(Box node, Box edge, Point3D from, Point3D to, Translate t, Scale s) {
    this.edge = edge;
    this.to = to;
    this.t = t;
    this.s = s;
  }

  @Override
  public void handle(ActionEvent e) {
    current = from.multiply(1D - factor).add(to.multiply(factor));
    factor += 0.01D;

    GeometryUtils.setTranslate(t, current);
    edge.setWidth(2 * from.distance(current));
    s.setX(scale);
    s.setY(scale);
    s.setZ(scale);
    scale += 0.02D;
  }

  public void finish() {
    node.getTransforms().remove(s);
  }
}
