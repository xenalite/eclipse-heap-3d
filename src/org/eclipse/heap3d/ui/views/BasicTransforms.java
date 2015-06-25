package org.eclipse.heap3d.ui.views;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class BasicTransforms extends Group {

  private Translate t;
  private Rotate rx;
  private Rotate ry;

  public BasicTransforms() {
    t = new Translate();
    ry = new Rotate(0, Rotate.Y_AXIS);
    rx = new Rotate(0, Rotate.X_AXIS);
    getTransforms().addAll(t, ry, rx);
  }

  public Point3D getTranslate() {
    return new Point3D(t.getX(), t.getY(), t.getZ());
  }

  public void offsetTranslate(double x, double y, double z) {
    t.setX(t.getX() + x);
    t.setY(t.getY() + y);
    t.setZ(t.getZ() + z);
  }

  public double getRyAngle() {
    return ry.getAngle();
  }

  public double getRxAngle() {
    return rx.getAngle();
  }

  public void setTranslate(Point3D p) {
    t.setX(p.getX());
    t.setY(p.getY());
    t.setZ(p.getZ());
  }

  public void setRy(double angle) {
    ry.setAngle(angle);
  }

  public void setRx(double angle) {
    rx.setAngle(angle);
  }
}
