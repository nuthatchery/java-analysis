package org.nuthatchery.analysis.java.extractor;

import java.util.HashMap;

//import org.apache.commons.rdf.api.BlankNode;
//import org.apache.commons.rdf.api.BlankNodeOrIRI;
//import org.apache.commons.rdf.api.IRI;
//import org.apache.commons.rdf.api.RDFTerm;
//import org.nuthatchery.ontology.Model;
//import org.nuthatchery.ontology.ModelFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.nuthatchery.ontology.uri.IRIUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;

/**
 * Java-spesifikt, diktet opp selv
 *
 * @author anna, anya
 *
 */
public abstract class JavaFacts {
	public static final String J = "https://model.nuthatchery.org/java/";
	public static final String JN = "java://";
	public static final String JF = J; // + "flags/";
	// public static final String JM = J + "methods/";
	public static final String JVM = "https://model.nuthatchery.org/jvm/";
	public static final String JT = J; // + "types/";
	// funnet på selv, bruker for å enkode ting fra bytecode
	public static final OntModel jvmModel = ModelFactory.createOntologyModel();
	public static final OntModel javaModel = ModelFactory.createOntologyModel();
	public static final OntModel javaFlagsModel = javaModel; // ModelFactory.createOntologyModel();
	public static final OntModel javaMethodsModel = javaModel; // ModelFactory.createOntologyModel();
	public static final OntModel javaTypesModel = javaModel; // ModelFactory.createOntologyModel();
	public static final Property hasAccess = javaModel.createProperty(J + "hasAccess");
	public static final OntClass C_CLASS = javaModel.createClass(J + "class");
	public static final OntClass C_INTERFACE = javaModel.createClass(J + "interface");
	public static final OntClass C_ENUM = javaModel.createClass(J + "enum");
	public static final OntClass C_CONSTRUCTOR = javaModel.createClass(J + "constructor");
	public static final Property CONSTRUCTS = javaModel.createProperty(J + "constructs");
	public static final Property CREATES = javaModel.createProperty(J + "creates");
	public static final Property DEBUG = javaModel.createProperty(J + "debug");
	public static final Property DECLARES_THROW = javaModel.createProperty(J + "declaresThrow");
	public static final Property P_EXTENDS = javaModel.createProperty(J + "extends");
	public static final Property GENERIC = javaModel.createProperty(J + "generic");
	public static final Property P_SUBTYPE_OF = javaModel.createProperty(J + "implements");

	public static final Property INITIAL_VALUE = javaModel.createProperty(J + "initialValue");
	public static final Property ACCESS_FIELD = javaModel.createProperty(J + "field");
	public static final Property ACCESS_DYNAMIC = javaModel.createProperty(J + "dynamic");
	public static final Property ACCESS_SPECIAL = javaModel.createProperty(J + "special");
	public static final Property ACCESS_STATIC = javaModel.createProperty(J + "static");
	public static final Property ACCESS_INTERFACE = javaModel.createProperty(J + "interface");

	public static final Property ACCESS_VIRTUAL = javaModel.createProperty(J + "virtual");

	public static final OntClass C_MEMBER = javaModel.createClass(J + "member");
	public static final OntClass C_METHOD = javaModel.createClass(J + "method");
	public static final OntClass C_FIELD = javaModel.createClass(J + "field");
	public static final Property P_HAS_FLAG = javaModel.createProperty(J + "hasFlag");
	public static final Property READS = javaModel.createProperty(J + "reads");
	public static final Property SIGNATURE = javaModel.createProperty(J + "signature");
	public static final Property P_SOURCE_FILE = javaModel.createProperty(J + "sourceFile");
	public static final Property P_THROWS = javaModel.createProperty(J + "throws");
	/*
	 * public static final IRI USES_JSR = javaModel.createResource(J + "usesJsr");
	 * public static final IRI USES_OBJ_EQUALS = javaModel.createResource(J +
	 * "usesObjEquals"); public static final IRI USES_REF_EQUALS =
	 * javaModel.createResource(J + "usesRefEquals"); public static final IRI
	 * USES_REF_NULLCHECK = javaModel.createResource(J + "usesRefNullCheck"); public
	 * static final IRI USES_TYPE = javaModel.createResource(J + "usesType");
	 */
	public static final Property WRITES = javaModel.createProperty(J + "writes");
	public static final Property P_CLASS_FILE_VERSION = javaModel.createProperty(J + "classFileVersion");
	public static final Property P_CLASS_FILE_MINOR = javaModel.createProperty(J + "classFileMinorVersion");
	public static final Property PARAMETER = javaModel.createProperty(J + "parameter");

	public static final Property P_CODE = javaModel.createProperty(J + "code");
	public static final Property P_NEXT = javaModel.createProperty(J + "next");
	public static final Property P_NEXT_IF_TRUE = javaModel.createProperty(J + "nextIfTrue");
	public static final Property P_NEXT_IF_FALSE = javaModel.createProperty(J + "nextIfFalse");
	public static final Resource R_END = javaModel.createResource(J + "end");
	public static final Property insn = javaModel.createProperty(J + "insn");
	public static final Property P_LINE = CommonVocabulary.P_LINE_NUMBER;
	public static final Property P_SRC_START = javaModel.createProperty(J + "srcStart");
	public static final Property P_SRC_END = javaModel.createProperty(J + "srcEnd");
	public static final OntClass C_INSTRUCTION = javaModel.createClass(J + "JvmInstruction");
	public static final OntClass C_JVM_INSN = javaModel.createClass(J + "JvmInsn");
	public static final Property P_ANNOTATION = javaModel.createProperty(J + "annotation");
	public static final Property P_OPERAND_INT = javaModel.createProperty(J + "intOperand");
	public static final Property P_OPERAND = javaModel.createProperty(J + "operand");
	public static final Property P_OPERAND_CONSTANT = javaModel.createProperty(J + "constantOperand");
	public static final Property P_OPERAND_MEMBER = javaModel.createProperty(J + "memberOperand");
	public static final Property P_OPERAND_TYPE = javaModel.createProperty(J + "typeOperand");
	public static final Property P_OPERAND_VAR = javaModel.createProperty(J + "varOperand");
	public static final Property P_OPERAND_LABEL = javaModel.createProperty(J + "labelOperand");
	public static final Property P_OPERAND_LIST = javaModel.createProperty(J + "listOperand");
	public static final Property P_TRY_CATCH_BLOCK = javaModel.createProperty(J + "tryCatchBlock");
	public static final Property P_TYPE = javaModel.createProperty(J + "type");
	public static final Property P_RETURN_TYPE = javaModel.createProperty(J + "rType");
	public static final Property P_PARAMETERS = javaModel.createProperty(J + "params");
	public static final Property P_MAX_STACK = javaModel.createProperty(J + "maxStack");
	public static final Property P_MAX_LOCALS = javaModel.createProperty(J + "maxLocals");
	public static final Property P_MEMBER_OF = javaModel.createProperty(J + "memberOf");
	public static final Property P_PART_OF = javaModel.createProperty(J + "partOf");

	static {
		javaModel.add(C_CLASS, RDFS.subClassOf, Types.REFERENCE_TYPE);
		javaModel.add(C_ENUM, RDFS.subClassOf, Types.REFERENCE_TYPE);
		javaModel.add(C_INTERFACE, RDFS.subClassOf, Types.REFERENCE_TYPE);

		isProperty(javaModel, P_CODE, C_METHOD, C_INSTRUCTION);
		isProperty(javaModel, P_NEXT, C_INSTRUCTION, C_INSTRUCTION);
		isProperty(javaModel, P_NEXT_IF_TRUE, C_INSTRUCTION, C_INSTRUCTION, //
				RDFS.subPropertyOf, P_NEXT);
		isProperty(javaModel, P_NEXT_IF_FALSE, C_INSTRUCTION, C_INSTRUCTION, //
				RDFS.subPropertyOf, P_NEXT);
		javaModel.add(R_END, RDF.type, C_INSTRUCTION);
		isProperty(javaModel, P_OPERAND, C_INSTRUCTION, null);
		isProperty(javaModel, P_OPERAND_CONSTANT, C_INSTRUCTION, RDFS.Resource, //
				RDFS.subPropertyOf, P_OPERAND);
		isProperty(javaModel, P_OPERAND_INT, C_INSTRUCTION, XSD.xint, //
				RDFS.subPropertyOf, P_OPERAND);
		isProperty(javaModel, P_OPERAND_MEMBER, C_INSTRUCTION, C_MEMBER, //
				RDFS.subPropertyOf, P_OPERAND);
		isProperty(javaModel, P_OPERAND_TYPE, C_INSTRUCTION, Types.JAVA_TYPE, //
				RDFS.subPropertyOf, P_OPERAND);
		isProperty(javaModel, P_OPERAND_VAR, C_INSTRUCTION, XSD.xint, //
				RDFS.subPropertyOf, P_OPERAND);
		isProperty(javaModel, insn, C_INSTRUCTION, C_JVM_INSN);
		isProperty(javaModel, P_LINE, C_INSTRUCTION, XSD.xint);
		javaModel.add(C_INSTRUCTION, RDF.type, RDFS.Class);
		javaModel.add(C_JVM_INSN, RDF.type, RDFS.Class);
	}

	private static void isProperty(OntModel m, Property prop, Resource sub, RDFNode obj, RDFNode... more) {
		m.add(prop, RDF.type, RDF.Property);
		if (sub != null) {
			m.add(prop, RDFS.range, sub);
		}
		if (obj != null) {
			m.add(prop, RDFS.domain, obj);
		}
		for (int i = 0; i < more.length; i += 2) {
			m.add(prop, (Property) more[i], more[i + 1]);
		}
	}

	public static Resource method(Model m, Resource owner, String memberName, String memberDesc) {
		memberName = memberName.replaceAll("[<>]", "--");
		// return m.node(owner, memberName + "/" + memberDesc);
		Type type = Type.getType(memberDesc);
		StringBuilder sb = new StringBuilder();
		sb.append(memberName); // .re.replaceAll("[<>]", "-"));
		if (type.getSort() == Type.METHOD) {
			sb.append("-");
			String sep = "";
			for (Type t : type.getArgumentTypes()) {
				sb.append(sep);
				while (t.getSort() == Type.ARRAY) {
					sb.append("A");
					sb.append(t.getDimensions());
					t = t.getElementType();
				}
				if (t.getSort() == Type.OBJECT) {
					String name = "O" + t.getClassName();
					name = name.replaceAll("^O(java\\.lang\\.|java\\.util\\.)", "");
					sb.append(name);
				} else {
					sb.append(t.getClassName());
				}
				sep = "-";
			}
			sb.append("-");
			type = type.getReturnType();
		}

		if (!memberName.equals("$init")) {
			sb.append("-");
			while (type.getSort() == Type.ARRAY) {
				sb.append("A");
				sb.append(type.getDimensions());
				type = type.getElementType();
			}
			if (type.getSort() == Type.OBJECT) {
				String name = "O" + type.getClassName();
				name = name.replaceAll("^O(java\\.lang\\.|java\\.util\\.)", "");
				sb.append(name);
			} else {
				sb.append(type.getClassName());
			}
		}
		return IRIUtil.addPath(owner, sb.toString());
		// + UriEncoding.percentEncodeIri(methodName,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true)
		// + UriEncoding.percentEncodeIri(methodDesc,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true));
	}

	public static Resource opcode(int opcode) {
		return jvmModel.createResource(JVM + Printer.OPCODES[opcode].toLowerCase());
	}

	/**
	 * Copied from JVM specs: Flags
	 *
	 * @author anna, anya
	 */
	public static final class Flags {
		public static final Resource INTERFACE = javaModel.createResource(J + "interface");
		public static final Resource FINAL = javaFlagsModel.createResource(JF + "final");
		public static final Resource SUPER = javaFlagsModel.createResource(JF + "super");
		public static final Resource MODULE = javaFlagsModel.createResource(JF + "module");
		public static final Resource ABSTRACT = javaFlagsModel.createResource(JF + "abstract");
		public static final Resource SYNTHETIC = javaFlagsModel.createResource(JF + "synthetic");
		public static final Resource ANNOTATION = javaFlagsModel.createResource(JF + "annotation");
		public static final Resource ENUM = javaFlagsModel.createResource(JF + "enum");
		public static final Resource NATIVE = javaFlagsModel.createResource(JF + "native");
		public static final Resource PRIVATE = javaFlagsModel.createResource(JF + "private");
		public static final Resource PROTECTED = javaFlagsModel.createResource(JF + "protected");
		public static final Resource VOLATILE = javaFlagsModel.createResource(JF + "volatile");
		public static final Resource TRANSIENT = javaFlagsModel.createResource(JF + "transient");
		public static final Resource SYNCHRONIZED = javaFlagsModel.createResource(JF + "synchronized");
		public static final Resource BRIDGE = javaFlagsModel.createResource(JF + "bridge");
		public static final Resource VARARGS = javaFlagsModel.createResource(JF + "varArgs");
		public static final Resource STRICT = javaFlagsModel.createResource(JF + "strict");
		public static final Resource MANDATED = javaFlagsModel.createResource(JF + "mandated");
		public static final Resource OPEN = javaFlagsModel.createResource(JF + "open");
		public static final Resource TRANSITIVE = javaFlagsModel.createResource(JF + "transitive");
		public static final Resource STATIC_PHASE = javaFlagsModel.createResource(JF + "staticPhase");
		public static final Resource PUBLIC = javaFlagsModel.createResource(JF + "public");
		public static final Resource STATIC = javaFlagsModel.createResource(JF + "static");
		public static final Resource DEPRECATED = javaFlagsModel.createResource(JF + "deprecated");
		public static final Resource PACKAGE = javaFlagsModel.createResource(JF + "package");
	}

	/**
	 * JVM types
	 *
	 * @author anna, anya
	 *
	 */
	public static final class Types {
		public static final Resource ARRAY_REF = javaTypesModel.createResource(JT + "array-ref");
		public static final Property ARRAY_DIM = javaTypesModel.createProperty(JT + "array-dim");
		public static final Property ARRAY_ELEMENT_TYPE = javaTypesModel.createProperty(JT + "array-element-type");
		public static final Resource REFERENCE_TYPE = javaTypesModel.createResource(JT + "ref");
		public static final Resource JAVA_TYPE = javaTypesModel.createResource(JT + "type");
		public static final Resource OBJECT_REF_TYPE = javaTypesModel.createResource(JT + "object-ref-type");
		public static final Resource PRIMITIVE_TYPE = javaTypesModel.createResource(JT + "primitive");
		public static final Resource BOOLEAN = javaTypesModel.createResource(JT + "boolean");
		public static final Resource BYTE = javaTypesModel.createResource(JT + "byte");
		public static final Resource CHAR = javaTypesModel.createResource(JT + "char");
		public static final Resource DOUBLE = javaTypesModel.createResource(JT + "double");
		public static final Resource FLOAT = javaTypesModel.createResource(JT + "float");
		public static final Resource INT = javaTypesModel.createResource(JT + "int");
		public static final Resource LONG = javaTypesModel.createResource(JT + "long");
		public static final Resource SHORT = javaTypesModel.createResource(JT + "short");
		public static final Resource TOP = javaTypesModel.createResource(JT + "top-half");
		public static final Resource UNINITIALIZED_THIS = javaTypesModel.createResource(JT + "new-obj");
		public static final Resource VOID = javaTypesModel.createResource(JT + "void");

		static {
			javaTypesModel.add(JAVA_TYPE, RDFS.subClassOf, CommonVocabulary.C_TYPE); // all JAVA_TYPEs
			// are subclasses in
			// C-like languages
			javaTypesModel.add(PRIMITIVE_TYPE, RDFS.subClassOf, JAVA_TYPE);
			javaTypesModel.add(BOOLEAN, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(BYTE, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(SHORT, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(INT, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(LONG, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(FLOAT, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(DOUBLE, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(CHAR, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(TOP, RDFS.subClassOf, PRIMITIVE_TYPE); // TOP of stack when top element
			// takes up two spots on the
			// stack; real time is on
			// element top-1
			javaTypesModel.add(VOID, RDFS.subClassOf, PRIMITIVE_TYPE);
			javaTypesModel.add(REFERENCE_TYPE, RDFS.subClassOf, JAVA_TYPE);
			javaTypesModel.add(UNINITIALIZED_THIS, RDFS.subClassOf, REFERENCE_TYPE);
		}

		public static Resource array(Model m, int dim, Resource type) {
			Resource t = m.createResource()//
					.addProperty(RDF.type, ARRAY_REF)//
					.addProperty(ARRAY_DIM, m.createTypedLiteral(dim))//
					.addProperty(ARRAY_ELEMENT_TYPE, type);
			return t;
		}

		public static Resource object(Model m, String prefix, String typeName) {
			Resource t = m.createResource(prefix + typeName)//
					.addProperty(RDF.type, REFERENCE_TYPE);
			return t;
		}
	}

	// public static HashMap<String, String> URIsimplifier = new HashMap<>();

}
