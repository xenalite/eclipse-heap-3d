package org.eclipse.heap3d.graph.layout;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;

public class CircularLayout<V, E> extends JungLayout<V, E> {

  private CircleLayout<V, E> sl;

  public CircularLayout(Graph<V, E> graph) {
    sl = new CircleLayout<V, E>(graph);
    this.layout = sl;
    setSize(new Dimension(1000, 1000));
  }

  @Override
  protected void run() {
  }
}
