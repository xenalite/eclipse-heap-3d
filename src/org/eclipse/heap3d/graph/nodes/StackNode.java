package org.eclipse.heap3d.graph.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;
import org.imperial.eclipse3dheap.reference.Type;

public class StackNode implements INode {

  private int levelId;
  private IJavaVariable variable;
  private IJavaObject referenceObject;

  public StackNode(int levelId, IJavaVariable primitiveVariable) {
    this.levelId = levelId;
    this.variable = primitiveVariable;
  }

  public StackNode(int levelId, IJavaVariable referenceVariable, IJavaObject referenceObject) {
    this.levelId = levelId;
    this.variable = referenceVariable;
    this.referenceObject = referenceObject;
  }

  public StackNode(int levelId, IJavaObject thisObject) {
    this.levelId = levelId;
    this.referenceObject = thisObject;
  }

  @Override
  public Map<INode, Reference> expand(IJavaStackFrame jsf) {
    Map<INode, Reference> references = new HashMap<>();
    try {
      if (variable == null && referenceObject != null) {
        ReferenceAttributes attributes = new ReferenceAttributes("this", Type.createType(referenceObject.getJavaType()));
        attributes.isLocal = true;

        references.put(new ObjectNode(levelId, referenceObject.getUniqueId(), referenceObject), new Reference(
            attributes));
      } else if (referenceObject != null) {
        ReferenceAttributes attributes = ReferenceAttributes.createAttributes(variable);

        IJavaType javaType = referenceObject.getJavaType();
        INode n = null;
        if (javaType instanceof IJavaArrayType)
          n = new CollectionNode(levelId, referenceObject.getUniqueId(), referenceObject);
        else if (javaType instanceof IJavaClassType) {
          boolean found = false;
          for (IJavaInterfaceType it : ((IJavaClassType) javaType).getAllInterfaces()) {
            if (it.getName().equals("java.util.Collection")) {
              found = true;
              break;
            }
          }
          n = found ? 
          new CollectionNode(levelId, referenceObject.getUniqueId(), referenceObject)
          : new ObjectNode(levelId, referenceObject.getUniqueId(), referenceObject);
        } else
          n = new ObjectNode(levelId, referenceObject.getUniqueId(), referenceObject);

        references.put(n, new Reference(attributes));
      }
    } catch (DebugException e) {
      e.printStackTrace();
    }
    return references;
  }

  @Override
  public int getLevelId() {
    return levelId;
  }

  @Override
  public String toString() {
    return (variable == null) ? "this" : variable.toString();
  }

  @Override
  public void setLevelId(int id) {
    this.levelId = id;
  }

  @Override
  public Map<ReferenceAttributes, String> getPrimitiveValues() {
    try {
      return extractPrimitiveValues();
    } catch (DebugException e) {
    }
    return Collections.emptyMap();
  }

  private Map<ReferenceAttributes, String> extractPrimitiveValues() throws DebugException {
    Map<ReferenceAttributes, String> primitiveValuesMap = new LinkedHashMap<>();
    IVariable[] variables = (referenceObject != null) ? referenceObject.getVariables()
        : new IVariable[] { this.variable };
    for (IVariable variable : variables) {
      ReferenceAttributes attributes = ReferenceAttributes.createAttributes((IJavaVariable) variable);
      String valueRepresentation = variable.getValue().toString();
      primitiveValuesMap.put(attributes, valueRepresentation);
    }
    return primitiveValuesMap;
  }

  @Override
  public Type getDeclaredType() {
    try {
      return Type.createType((variable == null) ? referenceObject.getJavaType() : variable.getJavaType());
    } catch (DebugException e) {
      return Type.INVALID_TYPE;
    }
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public Map<INode, Reference> expandPartially(int[] collectionIndices) {
    return Collections.emptyMap();
  }
}