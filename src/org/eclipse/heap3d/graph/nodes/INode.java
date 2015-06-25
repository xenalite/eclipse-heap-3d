package org.eclipse.heap3d.graph.nodes;

import java.util.Map;

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;
import org.imperial.eclipse3dheap.reference.Type;

public interface INode {

  Map<INode, Reference> expand(IJavaStackFrame jsf);
  
  Map<INode, Reference> expandPartially(int[] collectionIndices);

  Map<ReferenceAttributes, String> getPrimitiveValues();
  
  Type getDeclaredType();
  
  boolean isCollection();
  
  int getLevelId();

  void setLevelId(int id);
}