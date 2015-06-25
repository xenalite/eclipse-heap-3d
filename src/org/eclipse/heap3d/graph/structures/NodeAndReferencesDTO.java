package org.eclipse.heap3d.graph.structures;

import java.util.Map;

import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.reference.Reference;

public class NodeAndReferencesDTO {

  public final INode parentNode;
  public final Map<INode, Reference> outgoing;
  public final Map<INode, Reference> incoming;
  
  public NodeAndReferencesDTO(INode parentNode, 
      Map<INode, Reference> outgoing, Map<INode, Reference> incoming) {
    
    this.parentNode = parentNode;
    this.outgoing = outgoing;
    this.incoming = incoming;
  }
  
  @Override
  public String toString() {
    return "parentNode: " + parentNode
        + " outgoing: " + outgoing
        + " incoming: " + incoming;
  }
}
