package org.eclipse.heap3d.ui.views;

import java.util.Collections;
import java.util.Set;

import javafx.animation.Timeline;
import javafx.scene.shape.Shape3D;

public class NullAnimationBuilder implements IAnimationBuilder {

  public static final NullAnimationBuilder INSTANCE = new NullAnimationBuilder();
  
  @Override
  public IAnimationBuilder addNode(Shape3D box) {
    return this;
  }

  @Override
  public Timeline build() {
    return new Timeline();
  }

  @Override
  public Set<Shape3D> getNodes() {
    return Collections.emptySet();
  }
}
