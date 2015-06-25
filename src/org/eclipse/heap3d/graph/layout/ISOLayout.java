package org.eclipse.heap3d.graph.layout;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;

public class ISOLayout<V, E> extends JungLayout<V, E> {

  private ISOMLayout<V, E> sl;

  public ISOLayout(edu.uci.ics.jung.graph.Graph<V, E> graph) {
    sl = new ISOMLayout<V, E>(graph);
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

}
