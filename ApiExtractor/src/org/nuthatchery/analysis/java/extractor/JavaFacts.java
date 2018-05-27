package org.nuthatchery.analysis.java.extractor;

import org.objectweb.asm.Type;

public abstract class JavaFacts {
	public static class Types {
		private static final Id ARRAY = JAVA_TYPES.addPath("array");
		public static final Id BOOLEAN = JAVA_TYPES.addPath("boolean");
		public static final Id BYTE = JAVA_TYPES.addPath("byte");
		public static final Id CHAR = IdFactory.id(IdFactory.root("java", "//types"), "char");
		public static final Id DOUBLE = JAVA_TYPES.addPath("double");
		public static final Id FLOAT = JAVA_TYPES.addPath("float");
		public static final Id INT = JAVA_TYPES.addPath("int");
		public static final Id LONG = JAVA_TYPES.addPath("long");
		private static final Id OBJECT = JAVA_TYPES.addPath("object");
		public static final Id SHORT = JAVA_TYPES.addPath("short");
		public static final Id TOP = JAVA_TYPES.addPath("top-half");
		public static final Id UNINITIALIZED_THIS = JAVA_TYPES.addPath("new-obj");

		public static final Id VOID = JAVA_TYPES.addPath("void");

		public static Id array(int dim, Id type) {
			return ARRAY.addPath(Integer.toString(dim)).addPath(type.getPath());
		}

		public static Id object(String string) {
			return OBJECT.addPath(string.split("/"));
		}
	}

	public static final Id JAVA_FACTS = IdFactory
			.namespace(IdFactory.root("http", "//nuthatchery.org").addPath("javaFacts"), "jf");
	public static final Id ACCESS = JAVA_FACTS.addPath("access");
	public static final Id JAVA_FLAGS = JAVA_FACTS.addPath("flags");
	public static final Id CALLS = JAVA_FACTS.addPath("calls");
	public static final Id CLASS = JAVA_FACTS.addPath("class");
	public static final Id CONSTRUCTOR = JAVA_FACTS.addPath("constructor");
	public static final Id CONSTRUCTS = JAVA_FACTS.addPath("constructs");
	public static final Id CREATES = JAVA_FACTS.addPath("creates");
	public static final Id DEBUG = JAVA_FACTS.addPath("debug");
	public static final Id DECLARES_THROW = JAVA_FACTS.addPath("declaresThrow");
	public static final Id EXTENDS = JAVA_FACTS.addPath("extends");
	public static final Id GENERIC = JAVA_FACTS.addPath("generic");
	public static final Id IMPLEMENTS = JAVA_FACTS.addPath("implements");
	public static final Id INITIAL_VALUE = JAVA_FACTS.addPath("initialValue");

	public static final Id ACCESS_FIELD = JAVA_FACTS.addPath("field");
	public static final Id ACCESS_DYNAMIC = JAVA_FACTS.addPath("dynamic");
	public static final Id ACCESS_SPECIAL = JAVA_FACTS.addPath("special");
	public static final Id ACCESS_STATIC = JAVA_FACTS.addPath("static");
	public static final Id ACCESS_INTERFACE = JAVA_FACTS.addPath("interface");
	public static final Id ACCESS_VIRTUAL = JAVA_FACTS.addPath("virtual");

	/**
	 * Copied from JVM specs: Flags
	 * @author anna, anya
	 */
	public static final class Flags {
		public static final Id INTERFACE = JAVA_FACTS.addPath("interface");
		public static final Id FINAL = JAVA_FLAGS.addPath("final");
		public static final Id SUPER = JAVA_FLAGS.addPath("super");
		public static final Id MODULE = JAVA_FLAGS.addPath("module");
		public static final Id ABSTRACT = JAVA_FLAGS.addPath("abstract");
		public static final Id SYNTHETIC = JAVA_FLAGS.addPath("synthetic");
		public static final Id ANNOTATION = JAVA_FLAGS.addPath("annotation");
		public static final Id ENUM = JAVA_FLAGS.addPath("enum");
		public static final Id NATIVE = JAVA_FLAGS.addPath("native");
		public static final Id PRIVATE = JAVA_FLAGS.addPath("private");
		public static final Id PROTECTED = JAVA_FLAGS.addPath("protected");
		public static final Id VOLATILE = JAVA_FLAGS.addPath("volatile");
		public static final Id TRANSIENT = JAVA_FLAGS.addPath("transient");
		public static final Id SYNCHRONIZED = JAVA_FLAGS.addPath("synchronized");
		public static final Id BRIDGE = JAVA_FLAGS.addPath("bridge");
		public static final Id VARARGS = JAVA_FLAGS.addPath("varArgs");
		public static final Id STRICT = JAVA_FLAGS.addPath("strict");
		public static final Id MANDATED = JAVA_FLAGS.addPath("mandated");
		public static final Id OPEN = JAVA_FLAGS.addPath("open");
		public static final Id TRANSITIVE = JAVA_FLAGS.addPath("transitive");
		public static final Id STATIC_PHASE = JAVA_FLAGS.addPath("staticPhase");
		public static final Id PUBLIC = JAVA_FLAGS.addPath("public");
		public static final Id STATIC = JAVA_FLAGS.addPath("static");
		public static final Id DEPRECATED = JAVA_FLAGS.addPath("deprecated");
		public static final Id PACKAGE = JAVA_FLAGS.addPath("package");
	}

	public static final Id JAVA_METHODS = IdFactory.root("java", "members");
	public static final Id JAVA_TYPES = IdFactory.root("java", "types");
	public static final Id METHOD = JAVA_FACTS.addPath("method");
	public static final Id READS = JAVA_FACTS.addPath("reads");
	public static final Id SIGNATURE = JAVA_FACTS.addPath("signature");
	public static final Id SOURCE = JAVA_FACTS.addPath("source");
	public static final Id THROWS = JAVA_FACTS.addPath("throws");
	public static final Id USES_JSR = JAVA_FACTS.addPath("usesJsr");
	public static final Id USES_OBJ_EQUALS = JAVA_FACTS.addPath("usesObjEquals");
	public static final Id USES_REF_EQUALS = JAVA_FACTS.addPath("usesRefEquals");
	public static final Id USES_REF_NULLCHECK = JAVA_FACTS.addPath("usesRefNullCheck");
	public static final Id USES_TYPE = JAVA_FACTS.addPath("usesType");

	public static final Id WRITES = JAVA_FACTS.addPath("writes");
	public static final Id CLASS_FILE_VERSION = JAVA_FACTS.addPath("classFileVersion");
	public static final Id CLASS_FILE_MINOR = JAVA_FACTS.addPath("classFileMinorVersion");
	public static final Id PARAMETER = JAVA_FACTS.addPath("parameter");

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
		} else {
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
