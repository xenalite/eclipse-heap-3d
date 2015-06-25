package org.eclipse.heap3d.graph.structures;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;

import org.imperial.eclipse3dheap.layout.algorithms.IGraph;
import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.reference.Reference;

public class SpanningTreeIterator implements Iterator<VertexEdges> {

  private IGraph<INode, Reference> graph;
  private INode current;
  private Deque<INode> stack = new ArrayDeque<>();
  private Collection<INode> seen = new HashSet<>();

  public SpanningTreeIterator(INode root, IGraph<INode, Reference> graph) {
    this.graph = graph;
    stack.push(root);
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  public VertexEdges next() {
    current = stack.pop();
    seen.add(current);
    Collection<Reference> references = graph.getOutEdges(current);
    VertexEdges e = new VertexEdges(current, references);

    for (Reference reference : references) {
      INode dest = graph.getDest(reference);
      if (seen.contains(dest) || stack.contains(dest))
        continue;

      current = dest;
      stack.push(dest);
    }
    return e;
  }
}