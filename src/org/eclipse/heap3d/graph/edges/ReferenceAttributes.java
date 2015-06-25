package org.eclipse.heap3d.graph.edges;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class ReferenceAttributes {
  public final String name;
  public final Type type;
  
  public boolean isLocal;
  public boolean isArgument;
  public boolean isArrayElement;
  public boolean isStatic;
  public boolean isFinal;
  public boolean isPrivate;
  public boolean isPublic;
  public boolean isProtected;
  public boolean isPackage;

  public ReferenceAttributes(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
  }

  public static ReferenceAttributes createAttributes(IJavaVariable variable) throws DebugException {
    ReferenceAttributes attribute = new ReferenceAttributes(variable.getName(), 
        Type.createType(variable.getJavaType()));
    
    attribute.isLocal = variable.isLocal();
    attribute.isFinal = variable.isFinal();
    attribute.isPrivate = variable.isPrivate();
    attribute.isPublic = variable.isPublic();
    attribute.isProtected = variable.isProtected();
    attribute.isPackage = variable.isPackagePrivate();
    attribute.isStatic = variable.isStatic();

    return attribute;
  }
}