package org.nuthatchery.analysis.java.extractor;

import java.util.List;

import org.objectweb.asm.Type;

public abstract class JavaFacts {
	public static final Id JAVA_FACTS = IdFactory.id(IdFactory.root("http", "//nuthatchery.org"), "javaFacts");
	public static final Id CLASS = IdFactory.id(JAVA_FACTS, "class");
	public static final Id EXTENDS = IdFactory.id(JAVA_FACTS, "extends");
	public static final Id IMPLEMENTS = IdFactory.id(JAVA_FACTS, "implements");
	public static final Id PUBLIC = IdFactory.id(JAVA_FACTS, "public");
	public static final Id ACCESS = IdFactory.id(JAVA_FACTS, "access");
	public static final Id THROWS = IdFactory.id(JAVA_FACTS, "throws");
	public static final Id METHOD = IdFactory.id(JAVA_FACTS, "method");
	public static final Id GENERIC = IdFactory.id(JAVA_FACTS, "generic");
	public static final Id CALLS = IdFactory.id(JAVA_FACTS, "calls");
	public static final Id VIRTUAL = IdFactory.id(JAVA_FACTS, "virtual");
	public static final Id SPECIAL = IdFactory.id(JAVA_FACTS, "special");
	public static final Id STATIC = IdFactory.id(JAVA_FACTS, "static");
	public static final Id INTERFACE = IdFactory.id(JAVA_FACTS, "interface");
	public static final Id DYNAMIC = IdFactory.id(JAVA_FACTS, "dynamic");
	public static final Id CREATES = IdFactory.id(JAVA_FACTS, "creates");
	public static final Id USES_TYPE = IdFactory.id(JAVA_FACTS, "usesType");
	public static final Id USES_REF_NULLCHECK = IdFactory.id(JAVA_FACTS, "usesRefNullCheck");
	public static final Id USES_REF_EQUALS = IdFactory.id(JAVA_FACTS, "usesRefEquals");
	public static final Id USES_OBJ_EQUALS = IdFactory.id(JAVA_FACTS, "usesObjEquals");
	public static final Id USES_JSR = IdFactory.id(JAVA_FACTS, "usesJsr");
	public static final Id FIELD = IdFactory.id(JAVA_FACTS, "field");
	public static final Id READS = IdFactory.id(JAVA_FACTS, "reads");
	public static final Id WRITES = IdFactory.id(JAVA_FACTS, "writes");
	public static final Id SIGNATURE = IdFactory.id(JAVA_FACTS, "signature");
	public static final Id CONSTRUCTOR = IdFactory.id(JAVA_FACTS, "constructor");
	public static final Id CONSTRUCTS = IdFactory.id(JAVA_FACTS, "constructs");
	public static final Id DECLARES_THROW = IdFactory.id(JAVA_FACTS, "declaresThrow");
	public static final Id SOURCE = IdFactory.id(JAVA_FACTS, "source");
	public static final Id DEBUG = IdFactory.id(JAVA_FACTS, "debug");
	public static final Id INITIAL_VALUE = IdFactory.id(JAVA_FACTS, "initialValue");
	public static final Id JAVA_METHODS = IdFactory.root("java", "//members");
	public static final Id JAVA_TYPES = IdFactory.root("java", "//types");

	public static class Types {
		public static final Id VOID = JAVA_TYPES.addPath("void");
		public static final Id BOOLEAN = JAVA_TYPES.addPath("boolean");
		public static final Id CHAR = IdFactory.id(IdFactory.root("java", "//types"), "char");
		public static final Id BYTE = JAVA_TYPES.addPath("byte");
		public static final Id SHORT = JAVA_TYPES.addPath("short");
		public static final Id INT = JAVA_TYPES.addPath("int");
		public static final Id FLOAT = JAVA_TYPES.addPath("float");
		public static final Id LONG = JAVA_TYPES.addPath("long");
		public static final Id UNINITIALIZED_THIS = JAVA_TYPES.addPath("new-obj");
		public static final Id TOP = JAVA_TYPES.addPath("top-half");
		public static final Id DOUBLE = JAVA_TYPES.addPath("double");
		private static final Id ARRAY = JAVA_TYPES.addPath("array");

		public static Id array(int dim, Id type) {
			return ARRAY.addPath(Integer.toString(dim)).addPath(type.getPath());
		}

		public static Id object(String string) {
			return OBJECT.addPath(string.split("/"));
		}

		private static final Id OBJECT = JAVA_TYPES.addPath("object");
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
		return JAVA_METHODS.addPath(owner.getPath()).addPath(sb.toString().split("/"));
		// + UriEncoding.percentEncodeIri(methodName,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true)
		// + UriEncoding.percentEncodeIri(methodDesc,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true));
	}
}
