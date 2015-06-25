package org.eclipse.heap3d.graph.layout;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;

import java.awt.*;

public class FRLayoutDecorator<V, E> extends JungLayout<V, E> {

  private FRLayout<V, E> sl;

  public FRLayoutDecorator(IGraph<V, E> graph) {
    sl = new FRLayout<>(graph);
    sl.setInitializer(new RandomLocationTransformer<>(new Dimension(1000, 1000), 0));
    this.layout = sl;
    setSize(new Dimension(1000, 1000));
  }

  public FRLayoutDecorator(IGraph<V, E> graph, float attraction, float repulsion) {
    sl = new FRLayout<>(graph);
    sl.setInitializer(new RandomLocationTransformer<>(new Dimension(1000, 1000), 0));
    sl.setAttractionMultiplier(attraction);
    sl.setRepulsionMultiplier(repulsion);
    this.layout = sl;
    setSize(new Dimension(1000, 1000));
  }

  @Override
  protected void run() {
    int i = 0;
    while (i < 100 && !sl.done()) {
      sl.step();
      i++;
    }
  }

  public void setRepulsionMultiplier(double repulsionMultiplier) {
    sl.setRepulsionMultiplier(repulsionMultiplier);
  }

  public void setAttractionMultiplier(double attractionMultiplier) {
    sl.setAttractionMultiplier(attractionMultiplier);
  }
}
