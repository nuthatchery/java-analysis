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
	
}