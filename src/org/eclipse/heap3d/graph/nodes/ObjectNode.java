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
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;
import org.imperial.eclipse3dheap.reference.Type;

public class ObjectNode implements INode {

	private final long uniqueID;
	private int levelId;
	private IJavaObject object;

	public ObjectNode(int levelId, long uniqueID, IJavaObject object) {
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
			if(((IJavaVariable) variable).isStatic())
				continue;
			IJavaValue value = (IJavaValue) variable.getValue();
			if (!(value instanceof IJavaPrimitiveValue || value.isNull())) {
				ReferenceAttributes attributes = ReferenceAttributes
						.createAttributes((IJavaVariable) variable);
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
	}

	@Override
	public int getLevelId() {
		return levelId;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ObjectNode))
			return false;
		return ((ObjectNode) o).uniqueID == this.uniqueID;
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
    }
    catch(DebugException e) {}
    return Collections.emptyMap();
  }
  
  private Map<ReferenceAttributes, String> extractPrimitiveValues() throws DebugException {
    Map<ReferenceAttributes, String> primitiveValuesMap = new LinkedHashMap<>();
    for(IVariable variable : object.getVariables()) {
      IJavaVariable javaVariable = (IJavaVariable) variable;
      ReferenceAttributes attributes = ReferenceAttributes.createAttributes(javaVariable);
      String valueRepresentation = variable.getValue().toString();
      primitiveValuesMap.put(attributes, valueRepresentation);
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
    return false;
  }

  @Override
  public Map<INode, Reference> expandPartially(int[] collectionIndices) {
    return Collections.emptyMap();
  }
  
  @Override
  public String toString() {
	  return object.toString();
  }
}