package org.eclipse.heap3d.graph.nodes;

import java.util.Map;

import org.eclipse.heap3d.graph.edges.Reference;
import org.eclipse.heap3d.graph.edges.ReferenceAttributes;
import org.eclipse.heap3d.graph.edges.Type;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public interface INode {

  Map<INode, Reference> expand(IJavaStackFrame jsf);
  
  Map<INode, Reference> expandPartially(int[] collectionIndices);

  Map<ReferenceAttributes, String> getPrimitiveValues();
  
  Type getDeclaredType();
  
  boolean isCollection();
  
  int getLevelId();

  void setLevelId(int id);
}