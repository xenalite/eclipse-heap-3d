package org.eclipse.heap3d.graph.structures;

import java.util.Collection;

import org.eclipse.heap3d.graph.edges.Reference;
import org.eclipse.heap3d.graph.nodes.INode;

public class VertexEdges {

  public final INode vertex;
  public final Collection<Reference> references;

  public VertexEdges(INode vertex, Collection<Reference> references) {
    this.vertex = vertex;
    this.references = references;
  }

  @Override
  public String toString() {
    return vertex.toString() + " = " + references.toString();
  }
}