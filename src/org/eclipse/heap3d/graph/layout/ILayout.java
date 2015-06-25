package org.eclipse.heap3d.graph.layout;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.Layout;

public interface ILayout<V, E> extends Layout<V, E> {

  public void layout();

  public void setRootVertex(V rootVertex);

  public void setRawPosition(V vertex, Point2D transform);

  public Point2D getRawPosition(V vertex);
}
