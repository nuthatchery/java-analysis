package org.nuthatchery.analysis.java.extractor;

public abstract class JavaFacts  {
	public static final Id JAVA_FACTS = Id.id("http://nuthatchery.org/javaFacts/");
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
	public static final Id JAVA_METHODS = Id.id("java://methods/");
	public static final Id JAVA_TYPES = Id.id("java://types/classes");
	public static class Types {
		public static final Id VOID = Id.id("java://types/void");
		public static final Id BOOLEAN = Id.id("java://types/boolean");
		public static final Id CHAR = Id.id("java://types/char");
		public static final Id BYTE = Id.id("java://types/byte");
		public static final Id SHORT = Id.id("java://types/short");
		public static final Id INT = Id.id("java://types/int");
		public static final Id FLOAT = Id.id("java://types/float");
		public static final Id LONG = Id.id("java://types/long");
		public static final Id UNINITIALIZED_THIS= Id.id("java://types/new-obj");
		public static final Id TOP= Id.id("java://types/top-half");
		public static final Id DOUBLE = Id.id("java://types/double");
		private static final Id ARRAY = Id.id("java://types/array/");
		public static Id array(int dim, Id type) {
			return ARRAY.resolve("" + dim + "/" + type.getPathName());
		}
		public static Id object(String string) {
			return OBJECT.resolve(string);
		}
		private static final Id OBJECT = Id.id("java://types/object/");
	}
}
