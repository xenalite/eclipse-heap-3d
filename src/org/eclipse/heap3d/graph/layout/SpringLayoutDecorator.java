package org.eclipse.heap3d.graph.layout;

import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;

import java.awt.*;

public class SpringLayoutDecorator<V, E> extends JungLayout<V, E> {

  private SpringLayout<V, E> sl;

  public SpringLayoutDecorator(IGraph<V, E> graph) {
    this(graph, 0.7f, 100, 1 / 3);
  }

  /***
   *
   * @param graph
   * @param stretch
   *          default value is 0.7 Values should be positive. value less than 1
   *          cause high degree nodes to move less than low degree ones values
   *          greater than 1 cause high degree nodes to move more than low
   *          degree ones
   * @param repulsionRange
   *          default value is 100 The repulsion range of a node Outside the
   *          range, nodes don't repel eachother
   * @param force
   *          how strongly an edge wants to be its default length Default: 1/3 0
   *          no attempt to conform to default lengths negative use at own risk
   */
  public SpringLayoutDecorator(IGraph<V, E> graph, float stretch, int repulsionRange, float force) {
    sl = new SpringLayout<V, E>(graph);
    sl.setInitializer(new RandomLocationTransformer<>(new Dimension(1000, 1000), 0));
    sl.setStretch(stretch);
    sl.setForceMultiplier(force);
    sl.setRepulsionRange(repulsionRange);
    this.layout = sl;
    setSize(new Dimension(1000, 1000));
  }

  @Override
  protected void run() {
    int i = 0;
    while (i < 100) {
      sl.step();
      i++;
    }
  }
}
