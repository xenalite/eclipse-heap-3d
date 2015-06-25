package org.eclipse.heap3d.graph.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.heap3d.utils.Utils;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.imperial.eclipse3dheap.layout.heapgraph.NodeManager;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;
import org.imperial.eclipse3dheap.reference.Type;

public class CollectionNode implements INode {

  private final long uniqueID;
  private int levelId;
  private IJavaObject object;

  public CollectionNode(int levelId, long uniqueID, IJavaObject object) {
    this.object = object;
    this.uniqueID = uniqueID;
    this.levelId = levelId;
  }

  @Override
  public Map<INode, Reference> expand(IJavaStackFrame jsf) {
    try {
      return extractReferences();
    } catch (DebugException e) {
    }
    return new HashMap<>();
  }

  private Map<INode, Reference> extractReferences() throws DebugException {
    Map<INode, Reference> references = new HashMap<>();
    IVariable[] variables = object.getVariables();
    for (IVariable variable : variables) {
      IJavaValue value = (IJavaValue) variable.getValue();
      if (!(value instanceof IJavaPrimitiveValue || value.isNull())) {
        ReferenceAttributes attributes = ReferenceAttributes.createAttributes((IJavaVariable) variable);
        IJavaObject object = (IJavaObject) value;
        INode referenceNode = (object.getJavaType() instanceof IJavaArrayType ? new CollectionNode(levelId,
            object.getUniqueId(), object) : new ObjectNode(levelId, object.getUniqueId(), object));

        if (references.containsKey(referenceNode)) {
          Reference existingReference = references.get(referenceNode);
          existingReference.getAttributes().add(attributes);
        } else {
          references.put(referenceNode, new Reference(attributes));
        }
      }
    }
    return references;
  }

  @Override
  public int getLevelId() {
    return levelId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CollectionNode))
      return false;
    return ((CollectionNode) o).uniqueID == this.uniqueID;
  }

  @Override
  public int hashCode() {
    return (int) uniqueID;
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
    for (IVariable variable : object.getVariables()) {
      IJavaVariable javaVariable = (IJavaVariable) variable;
      ReferenceAttributes attributes = ReferenceAttributes.createAttributes(javaVariable);
      String valueRepresentation = variable.getValue().toString();
      primitiveValuesMap.put(attributes, valueRepresentation);
    }
    return primitiveValuesMap;
  }

  public Map<ReferenceAttributes, String> extractPrimitiveValues2() {
    Map<ReferenceAttributes, String> primitiveValuesMap = new LinkedHashMap<>();
    try {
      IVariable[] variables;
      if(object.getJavaType() instanceof IJavaArrayType)
        variables = object.getVariables();
      else
        variables = Utils.evaluateExpression(NodeManager.jsf, object).getVariables();
      
      for (IVariable variable : variables) {
        IJavaVariable javaVariable = (IJavaVariable) variable;
        ReferenceAttributes attributes = ReferenceAttributes.createAttributes(javaVariable);
        String valueRepresentation = variable.getValue().toString();
        primitiveValuesMap.put(attributes, valueRepresentation);
      }
    } catch (DebugException e) {
      return Collections.emptyMap();
    }
    return primitiveValuesMap;
  }

  @Override
  public Type getDeclaredType() {
    try {
      return Type.createType(object.getJavaType());
    } catch (DebugException e) {
      return Type.INVALID_TYPE;
    }
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public Map<INode, Reference> expandPartially(int[] collectionIndices) {
    try {
      Map<INode, Reference> references = new HashMap<>();
      IVariable[] variables;
      if(object.getJavaType() instanceof IJavaArrayType)
        variables = object.getVariables();
      else
        variables = Utils.evaluateExpression(NodeManager.jsf, object).getVariables();
      
      for (int i : collectionIndices) {
        IJavaVariable variable = (IJavaVariable) variables[i];
        IJavaValue value = (IJavaValue) variable.getValue();
        if (!(value instanceof IJavaPrimitiveValue || value.isNull())) {
          ReferenceAttributes attributes = ReferenceAttributes.createAttributes((IJavaVariable) variable);
          IJavaObject object = (IJavaObject) value;
          
          IJavaType javaType = object.getJavaType();
          INode referenceNode = null;
          if (javaType instanceof IJavaArrayType)
            referenceNode = new CollectionNode(levelId, object.getUniqueId(), object);
          else if (javaType instanceof IJavaClassType) {
            boolean found = false;
            for (IJavaInterfaceType it : ((IJavaClassType) javaType).getAllInterfaces()) {
              if (it.getName().equals("java.util.Collection")) {
                found = true;
                break;
              }
            }
            referenceNode = found ? 
            new CollectionNode(levelId, object.getUniqueId(), object)
            : new ObjectNode(levelId, object.getUniqueId(), object);
          } else
            referenceNode = new ObjectNode(levelId, object.getUniqueId(), object);
          
          if (references.containsKey(referenceNode)) {
            Reference existingReference = references.get(referenceNode);
            existingReference.getAttributes().add(attributes);
          } else {
            references.put(referenceNode, new Reference(attributes));
          }
        }
      }
      return references;
    } catch (DebugException e) {
    }
    return Collections.emptyMap();
  }
}