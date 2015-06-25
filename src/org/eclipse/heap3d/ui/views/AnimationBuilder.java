package org.eclipse.heap3d.ui.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class AnimationBuilder implements IAnimationBuilder {

  private class AnimationFrame implements EventHandler<ActionEvent> {
    private double factor = grow ? 0.02D : -0.02D;
    private double scale = grow ? 0D : 1D;
    
    @Override
    public void handle(ActionEvent event) {
      nodes.values().forEach(s -> {
        s.setX(scale);
        s.setY(scale);
        s.setZ(scale);
      });
      scale += factor;
    }
    
    private void cleanup() {
      nodes.entrySet().forEach(e -> {
        e.getKey().getTransforms().remove(e.getValue());
      });
    }
  }
  
  private Map<Shape3D, Scale> nodes = new HashMap<>();
  private float FPS = 60F;
  private boolean grow;
  
  public AnimationBuilder(boolean grow) {
    this.grow = grow;
  }
  
  @Override
  public IAnimationBuilder addNode(Shape3D shape3d) {
    double scale = grow ? 0D : 1D;
    Scale s = new Scale(scale, scale, scale);
    
    nodes.put(shape3d, s);
    shape3d.getTransforms().add(s);
    return this;
  }

  @Override
  public Timeline build() {
    AnimationFrame af = new AnimationFrame();
    Timeline timeline = new Timeline();
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000F / FPS), af));
    timeline.setCycleCount(50);
    timeline.setOnFinished(e -> af.cleanup());
    
    return timeline;
  }

  @Override
  public Set<Shape3D> getNodes() {
    return nodes.keySet();
  }
}