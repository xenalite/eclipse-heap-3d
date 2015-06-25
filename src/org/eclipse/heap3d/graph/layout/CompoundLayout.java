package org.eclipse.heap3d.graph.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.util.IterativeContext;

public class CompoundLayout<V, E> implements ILayout<V, E>, IterativeContext {

  protected ILayout<V, E> delegate;
  protected List<ILayout<V, E>> layouts = new LinkedList<ILayout<V, E>>();

  /**
   * Creates an instance backed by the specified {@code delegate}.
   * 
   * @param delegate
   */
  public CompoundLayout(ILayout<V, E> delegate) {
    this.delegate = delegate;
  }

  /**
   * @return the delegate
   */
  public ILayout<V, E> getDelegate() {
    return delegate;
  }

  /**
   * @param delegate
   *          the delegate to set
   */
  public void setDelegate(ILayout<V, E> delegate) {
    this.delegate = delegate;
  }

  public void add(ILayout<V, E> layout) {
    layouts.add(layout);
  }

  public ILayout<V, E> get(int index) {
    return layouts.get(index);
  }

  public void remove(ILayout<V, E> layout) {
    layouts.remove(layout);
  }

  public void removeAll() {
    layouts.clear();
  }

  @Override
  public edu.uci.ics.jung.graph.Graph<V, E> getGraph() {
    return delegate.getGraph();
  }

  /**
   * Returns the size of the underlying layout.
   * 
   * @return the size of the underlying layout
   * @see edu.uci.ics.jung.algorithms.layout.Layout#getSize()
   */
  @Override
  public Dimension getSize() {
    return delegate.getSize();
  }

  /**
   *
   * @see edu.uci.ics.jung.algorithms.layout.Layout#initialize()
   */
  @Override
  public void initialize() {
    delegate.initialize();
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      layout.initialize();
    }
  }

  /**
   * Override to test if the passed vertex is locked in any of the layouts.
   * 
   * @param v
   * @return true if v is locked in any of the layouts, and false otherwise
   * @see edu.uci.ics.jung.algorithms.layout.Layout#isLocked(java.lang.Object)
   */
  @Override
  public boolean isLocked(V v) {
    boolean locked = false;
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      locked |= layout.isLocked(v);
    }
    locked |= delegate.isLocked(v);
    return locked;
  }

  /**
   * override to lock or unlock this vertex in any layout with a subgraph
   * containing it
   * 
   * @param v
   * @param state
   * @see edu.uci.ics.jung.algorithms.layout.Layout#lock(java.lang.Object,
   *      boolean)
   */
  @Override
  public void lock(V v, boolean state) {
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      if (layout.getGraph().getVertices().contains(v)) {
        layout.lock(v, state);
      }
    }
    delegate.lock(v, state);
  }

  /**
   *
   * @see edu.uci.ics.jung.algorithms.layout.Layout#reset()
   */
  @Override
  public void reset() {
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      layout.reset();
    }
    delegate.reset();
  }

  /**
   * @param graph
   * @see edu.uci.ics.jung.algorithms.layout.Layout#setGraph(edu.uci.ics.jung.graph.Graph)
   */
  @Override
  public void setGraph(edu.uci.ics.jung.graph.Graph<V, E> graph) {
    delegate.setGraph(graph);
  }

  /**
   * @param initializer
   * @see edu.uci.ics.jung.algorithms.layout.Layout#setInitializer(org.apache.commons.collections15.Transformer)
   */
  @Override
  public void setInitializer(Transformer<V, Point2D> initializer) {
    delegate.setInitializer(initializer);
  }

  /**
   * @param v
   * @param location
   * @see edu.uci.ics.jung.algorithms.layout.Layout#setLocation(java.lang.Object,
   *      java.awt.geom.Point2D)
   */
  @Override
  public void setLocation(V v, Point2D location) {
    if (getGraph().getVertices().contains(v)) {
      delegate.setLocation(v, location);
    }
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      if (layout.getGraph().getVertices().contains(v)) {
        layout.setLocation(v, location);
      }
    }

  }

  /**
   * @param d
   * @see edu.uci.ics.jung.algorithms.layout.Layout#setSize(java.awt.Dimension)
   */
  @Override
  public void setSize(Dimension d) {
    for (ILayout<V, E> layout : layouts)
      layout.setSize(d);
    delegate.setSize(d);
  }

  /**
   * Returns a map from each {@code Layout} instance to its center point.
   */
  public List<ILayout<V, E>> getLayouts() {
    return layouts;
  }

  /**
   * Returns the location of the vertex. The location is specified first by the
   * sublayouts, and then by the base layout if no sublayouts operate on this
   * vertex.
   * 
   * @return the location of the vertex
   * @see org.apache.commons.collections15.Transformer#transform(java.lang.Object)
   */
  @Override
  public Point2D transform(V v) {
    if (delegate.getGraph().containsVertex(v)) {
      return delegate.transform(v);
    }
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      if (layout.getGraph().getVertices().contains(v)) {
        return layout.transform(v);
      }
    }
    return delegate.transform(v);
  }

  /**
   * Check all sublayouts.keySet() and the delegate layout, returning done ==
   * true iff all are done.
   */
  @Override
  public boolean done() {
    boolean done = true;
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      if (layout instanceof IterativeContext) {
        done &= ((IterativeContext) layout).done();
      }
    }
    if (delegate instanceof IterativeContext) {
      done &= ((IterativeContext) delegate).done();
    }
    return done;
  }

  /**
   * call step on any sublayout that is also an IterativeContext and is not done
   */
  @Override
  public void step() {
    for (edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout : layouts) {
      if (layout instanceof IterativeContext) {
        IterativeContext context = (IterativeContext) layout;
        if (context.done() == false) {
          context.step();
          for (V vertex : layout.getGraph().getVertices()) {
            delegate.setLocation(vertex, layout.transform(vertex));
          }
        }
      }
    }
    if (delegate instanceof IterativeContext) {
      IterativeContext context = (IterativeContext) delegate;
      if (context.done() == false) {
        context.step();
      }
    }
  }

  @Override
  public void setRootVertex(V rootVertex) {

  }

  @Override
  public void setRawPosition(V vertex, Point2D transform) {

  }

  @Override
  public Point2D getRawPosition(V vertex) {
    return null;
  }

  @Override
  public void layout() {
    // Initialize the graph
    // Actually not sure if this is correct
    initialize();

    // run the layout
    run();
  }

  protected void run() {
    int i = 0;
    while (i < 100) {
      step();
      i++;
    }
  }
}
