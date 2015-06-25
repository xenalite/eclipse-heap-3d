package org.eclipse.heap3d.graph.edges;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;

public class Type {

	public enum TypeKind {
		INTERFACE, ARRAY, REFERENCE, PRIMITIVE, STRING;
	}

	public static final Type INVALID_TYPE = new Type("! INVALID !",
			TypeKind.REFERENCE);

	private String fullyQualifiedTypeName;
	private TypeKind typeKind;

	public Type(String name, TypeKind typeKind) {
		this.fullyQualifiedTypeName = name;
		this.typeKind = typeKind;
	}

	public String getFullName() {
		return fullyQualifiedTypeName;
	}

	public String getShortName() {
		String[] packageName = fullyQualifiedTypeName.split("\\.");
		return packageName[packageName.length - 1];
	}

	public TypeKind getTypeKind() {
		return typeKind;
	}

	public static Type createType(IJavaType javaType) throws DebugException {
		TypeKind kind;
		if (javaType instanceof IJavaInterfaceType)
			kind = TypeKind.INTERFACE;
		else if (javaType instanceof IJavaArrayType)
			kind = TypeKind.ARRAY;
		else if (javaType instanceof IJavaClassType) {
			switch(javaType.getName()) {
				case "java.lang.String" : kind = TypeKind.STRING; break;
				default : kind = TypeKind.REFERENCE; break;
			}
		}
		else if (javaType instanceof IJavaReferenceType)
			kind = TypeKind.REFERENCE;
		else
			kind = TypeKind.PRIMITIVE;

		return new Type(javaType.getName(), kind);
	}
}
