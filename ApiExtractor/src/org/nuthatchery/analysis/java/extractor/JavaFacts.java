package org.nuthatchery.analysis.java.extractor;

import java.util.List;

import org.objectweb.asm.Type;

public abstract class JavaFacts {
	public static final Id JAVA_FACTS = Id.id("http", "//nuthatchery.org", "javaFacts");
	public static final Id CLASS = Id.id(JAVA_FACTS, "class");
	public static final Id EXTENDS = Id.id(JAVA_FACTS, "extends");
	public static final Id IMPLEMENTS = Id.id(JAVA_FACTS, "implements");
	public static final Id PUBLIC = Id.id(JAVA_FACTS, "public");
	public static final Id ACCESS = Id.id(JAVA_FACTS, "access");
	public static final Id THROWS = Id.id(JAVA_FACTS, "throws");
	public static final Id METHOD = Id.id(JAVA_FACTS, "method");
	public static final Id GENERIC = Id.id(JAVA_FACTS, "generic");
	public static final Id CALLS = Id.id(JAVA_FACTS, "calls");
	public static final Id VIRTUAL = Id.id(JAVA_FACTS, "virtual");
	public static final Id SPECIAL = Id.id(JAVA_FACTS, "special");
	public static final Id STATIC = Id.id(JAVA_FACTS, "static");
	public static final Id INTERFACE = Id.id(JAVA_FACTS, "interface");
	public static final Id DYNAMIC = Id.id(JAVA_FACTS, "dynamic");
	public static final Id CREATES = Id.id(JAVA_FACTS, "creates");
	public static final Id USES_TYPE = Id.id(JAVA_FACTS, "usesType");
	public static final Id USES_REF_NULLCHECK = Id.id(JAVA_FACTS, "usesRefNullCheck");
	public static final Id USES_REF_EQUALS = Id.id(JAVA_FACTS, "usesRefEquals");
	public static final Id USES_OBJ_EQUALS = Id.id(JAVA_FACTS, "usesObjEquals");
	public static final Id USES_JSR = Id.id(JAVA_FACTS, "usesJsr");
	public static final Id FIELD = Id.id(JAVA_FACTS, "field");
	public static final Id READS = Id.id(JAVA_FACTS, "reads");
	public static final Id WRITES = Id.id(JAVA_FACTS, "writes");
	public static final Id SIGNATURE = Id.id(JAVA_FACTS, "signature");
	public static final Id CONSTRUCTOR = Id.id(JAVA_FACTS, "constructor");
	public static final Id CONSTRUCTS = Id.id(JAVA_FACTS, "constructs");
	public static final Id DECLARES_THROW = Id.id(JAVA_FACTS, "declaresThrow");
	public static final Id SOURCE = Id.id(JAVA_FACTS, "source");
	public static final Id DEBUG = Id.id(JAVA_FACTS, "debug");
	public static final Id INITIAL_VALUE = Id.id(JAVA_FACTS, "initialValue");
	public static final Id JAVA_METHODS = Id.id("java", "//members");
	public static final Id JAVA_TYPES = Id.id("java", "//types");

	public static class Types {
		public static final Id VOID = JAVA_TYPES.addSubPath("void");
		public static final Id BOOLEAN = JAVA_TYPES.addSubPath("boolean");
		public static final Id CHAR = Id.id("java", "//types", "char");
		public static final Id BYTE = JAVA_TYPES.addSubPath("byte");
		public static final Id SHORT = JAVA_TYPES.addSubPath("short");
		public static final Id INT = JAVA_TYPES.addSubPath("int");
		public static final Id FLOAT = JAVA_TYPES.addSubPath("float");
		public static final Id LONG = JAVA_TYPES.addSubPath("long");
		public static final Id UNINITIALIZED_THIS = JAVA_TYPES.addSubPath("new-obj");
		public static final Id TOP = JAVA_TYPES.addSubPath("top-half");
		public static final Id DOUBLE = JAVA_TYPES.addSubPath("double");
		private static final Id ARRAY = JAVA_TYPES.addSubPath("array/");

		public static Id array(int dim, Id type) {
			return ARRAY.addSubPath(Integer.toString(dim)).addSubPath(type.getPath());
		}

		public static Id object(String string) {
			return OBJECT.addSubPath(string);
		}

		private static final Id OBJECT = JAVA_TYPES.addSubPath("object/");
	}

	public static Id method(Id owner, String memberName, String memberDesc) {
		Type type = Type.getType(memberDesc);
		StringBuilder sb = new StringBuilder();
		sb.append(memberName.replaceAll("[<>]", "-"));
		if (type.getSort() == Type.METHOD) {
			sb.append("(");
			String sep = "";
			for (Type t : type.getArgumentTypes()) {
				sb.append(sep);
				while (t.getSort() == Type.ARRAY) {
					sb.append("A");
					sb.append(t.getDimensions());
					t = t.getElementType();
				}
				if (t.getSort() == Type.OBJECT) {
					sb.append(t.getClassName());
				} else {
					sb.append(t.getClassName());
				}
				sep = ",";
			}
			sb.append(")");
			type = type.getReturnType();
		}
		else {
			sb.append("~");
		}
		while (type.getSort() == Type.ARRAY) {
			sb.append("A");
			sb.append(type.getDimensions());
			type = type.getElementType();
		}
		if (type.getSort() == Type.OBJECT) {
			sb.append(type.getClassName());
		} else {
			sb.append(type.getClassName());
		}
		return JAVA_METHODS.addSubPath(owner.getPath()).addPathSegments(sb.toString());
		// + UriEncoding.percentEncodeIri(methodName,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true)
		// + UriEncoding.percentEncodeIri(methodDesc,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true));
	}
}
