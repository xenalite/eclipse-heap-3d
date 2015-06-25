package org.eclipse.heap3d.ui.views;

import java.util.Set;

import javafx.animation.Timeline;
import javafx.scene.shape.Shape3D;

public interface IAnimationBuilder {
  IAnimationBuilder addNode(Shape3D shape3d);

  Set<Shape3D> getNodes();
  
  Timeline build();
}
