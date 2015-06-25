package org.eclipse.heap3d.graph.layout;

import java.awt.*;

import edu.uci.ics.jung.algorithms.layout.KKLayout;

public class KKLayoutDecorator<V, E> extends JungLayout<V, E> {

  private KKLayout<V, E> sl;

  public KKLayoutDecorator(edu.uci.ics.jung.graph.Graph<V, E> graph) {
    sl = new KKLayout<V, E>(graph);
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
