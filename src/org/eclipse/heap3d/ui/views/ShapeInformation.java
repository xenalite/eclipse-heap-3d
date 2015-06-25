package org.eclipse.heap3d.ui.views;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.shape.Shape3D;

import org.imperial.eclipse3dheap.reference.Reference;

public class ShapeInformation {

  private Shape3D shape;
  private Map<Reference, EdgeGroup> references;

  public ShapeInformation(Shape3D shape) {
    this.shape = shape;
  }

  public Shape3D getShape() {
    return shape;
  }

  public Map<Reference, EdgeGroup> getReferenceMap() {
    if (references == null)
      references = new HashMap<>();
    return references;
  }
}
