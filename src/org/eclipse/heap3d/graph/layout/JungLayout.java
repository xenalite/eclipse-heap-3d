package org.eclipse.heap3d.graph.layout;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Transformer;

import java.awt.*;
import java.awt.geom.Point2D;

public abstract class JungLayout<V, E> implements ILayout<V, E>, IterativeContext {

  protected AbstractLayout<V, E> layout;
  protected V root = null;

  public JungLayout() {

  }

  /**
   * Initializes fields in the node that may not have been set during the
   * constructor. Must be called before the iterations begin.
   */
  @Override
  public void initialize() {
    layout.initialize();
  }

  /**
   * provides initial locations for all vertices.
   *
   * @param initializer
   */
  @Override
  public void setInitializer(Transformer<V, Point2D> initializer) {
    layout.setInitializer(initializer);
  }

  /**
   * setter for graph
   *
   * @param graph
   */
  @Override
  public void setGraph(Graph<V, E> graph) {
    layout.setGraph(graph);
  }

  /**
   * Returns the full graph (the one that was passed in at construction time)
   * that this Layout refers to.
   */
  @Override
  public Graph<V, E> getGraph() {
    return layout.getGraph();
  }

  /**
     *
     *
     */
  @Override
  public void reset() {
    layout.reset();
  }

  /**
   * @param d
   */
  @Override
  public void setSize(Dimension d) {
    Dimension size = layout.getSize();
    if (size == null) {
      layout.setSize(d);
    } else if (size != null && size.getHeight() != d.getHeight() && size.getWidth() != d.getWidth()) {
      layout.setSize(d);
    }
  }

  /**
   * Returns the current size of the visualization's space.
   */
  @Override
  public Dimension getSize() {
    return layout.getSize();
  }

  /**
   * Sets a flag which fixes this vertex in place.
   *
   * @param v
   *          vertex
   * @param state
   */
  @Override
  public void lock(V v, boolean state) {
    layout.lock(v, state);
  }

  /**
   * Returns <code>true</code> if the position of vertex <code>v</code> is
   * locked.
   *
   * @param v
   */
  @Override
  public boolean isLocked(V v) {
    return layout.isLocked(v);
  }

  /**
   * set the location of a vertex
   *
   * @param v
   * @param location
   */
  @Override
  public void setLocation(V v, Point2D location) {
    layout.setLocation(v, convertToCoord(location));
  }

  @Override
  public Point2D transform(V v) {
    return convertFromCoord(layout.transform(v));
  }

  @Override
  public void setRawPosition(V vertex, Point2D transform) {
    layout.setLocation(vertex, transform);
  }

  @Override
  public Point2D getRawPosition(V vertex) {
    return layout.transform(vertex);
  }

  @Override
  public void layout() {
    // Initialize the graph
    initialize();
    // run the layout
    run();
  }

  public void setRootVertex(V rootVertex) {
    root = rootVertex;
  }

  protected Point2D convertToCoord(Point2D location) {
    if (root == null) {
      double midx = getSize().width / 2.0;
      double midy = getSize().height / 2.0;
      return new Point2D.Double(location.getX() + midx, location.getY() + midy);
    } else {
      Point2D rootposition = layout.transform(root);
      return new Point2D.Double(location.getX() + rootposition.getX(), location.getY() + rootposition.getY());
    }
  }

  protected Point2D convertFromCoord(Point2D location) {
    if (root == null) {
      double midx = getSize().width / 2.0;
      double midy = getSize().height / 2.0;
      return new Point2D.Double(location.getX() - midx, location.getY() - midy);
    } else {
      Point2D rootposition = layout.transform(root);
      return new Point2D.Double(location.getX() - rootposition.getX(), location.getY() - rootposition.getY());
    }
  }

  protected abstract void run();

  /**
   * Returns true if this iterative process is finished, and false otherwise.
   */
  @Override
  public boolean done() {
    return false;
  }

  @Override
  public void step() {
    if (layout instanceof IterativeContext)
      ((IterativeContext) layout).step();
  }
}
