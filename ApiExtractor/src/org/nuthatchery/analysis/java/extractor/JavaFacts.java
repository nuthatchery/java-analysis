package org.nuthatchery.analysis.java.extractor;

import java.util.HashMap;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.nuthatchery.ontology.standard.RdfVocabulary;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;

/**
 * Java-spesifikt, diktet opp selv
 *
 * @author anna, anya
 *
 */
public abstract class JavaFacts {
	/**
	 * Copied from JVM specs: Flags
	 *
	 * @author anna, anya
	 */
	public static final class Flags {
		public static final IRI INTERFACE = javaModel.node("interface");
		public static final IRI FINAL = javaFlagsModel.node("final");
		public static final IRI SUPER = javaFlagsModel.node("super");
		public static final IRI MODULE = javaFlagsModel.node("module");
		public static final IRI ABSTRACT = javaFlagsModel.node("abstract");
		public static final IRI SYNTHETIC = javaFlagsModel.node("synthetic");
		public static final IRI ANNOTATION = javaFlagsModel.node("annotation");
		public static final IRI ENUM = javaFlagsModel.node("enum");
		public static final IRI NATIVE = javaFlagsModel.node("native");
		public static final IRI PRIVATE = javaFlagsModel.node("private");
		public static final IRI PROTECTED = javaFlagsModel.node("protected");
		public static final IRI VOLATILE = javaFlagsModel.node("volatile");
		public static final IRI TRANSIENT = javaFlagsModel.node("transient");
		public static final IRI SYNCHRONIZED = javaFlagsModel.node("synchronized");
		public static final IRI BRIDGE = javaFlagsModel.node("bridge");
		public static final IRI VARARGS = javaFlagsModel.node("varArgs");
		public static final IRI STRICT = javaFlagsModel.node("strict");
		public static final IRI MANDATED = javaFlagsModel.node("mandated");
		public static final IRI OPEN = javaFlagsModel.node("open");
		public static final IRI TRANSITIVE = javaFlagsModel.node("transitive");
		public static final IRI STATIC_PHASE = javaFlagsModel.node("staticPhase");
		public static final IRI PUBLIC = javaFlagsModel.node("public");
		public static final IRI STATIC = javaFlagsModel.node("static");
		public static final IRI DEPRECATED = javaFlagsModel.node("deprecated");
		public static final IRI PACKAGE = javaFlagsModel.node("package");
	}

	/**
	 * JVM types
	 *
	 * @author anna, anya
	 *
	 */
	public static class Types {
		public static final IRI ARRAY_REF = javaTypesModel.node("array-ref");
		public static final IRI ARRAY_DIM = javaTypesModel.node("array-dim");
		public static final IRI ARRAY_ELEMENT_TYPE = javaTypesModel.node("array-element-type");
		public static final IRI REFERENCE_TYPE = javaTypesModel.node("ref");
		public static final IRI JAVA_TYPE = javaTypesModel.node("type");
		public static final IRI OBJECT_REF_TYPE = javaTypesModel.node("object-ref-type");
		public static final IRI PRIMITIVE_TYPE = javaTypesModel.node("primitive");
		public static final IRI BOOLEAN = javaTypesModel.node("boolean");
		public static final IRI BYTE = javaTypesModel.node("byte");
		public static final IRI CHAR = javaTypesModel.node("char");
		public static final IRI DOUBLE = javaTypesModel.node("double");
		public static final IRI FLOAT = javaTypesModel.node("float");
		public static final IRI INT = javaTypesModel.node("int");
		public static final IRI LONG = javaTypesModel.node("long");
		public static final IRI SHORT = javaTypesModel.node("short");
		public static final IRI TOP = javaTypesModel.node("top-half");
		public static final IRI UNINITIALIZED_THIS = javaTypesModel.node("new-obj");
		public static final IRI VOID = javaTypesModel.node("void");

		static {
			javaTypesModel.add(JAVA_TYPE, RdfVocabulary.RDFS_SUBCLASS_OF, CommonVocabulary.C_TYPE); // all JAVA_TYPEs
			// are subclasses in
			// C-like languages
			javaTypesModel.add(PRIMITIVE_TYPE, RdfVocabulary.RDFS_SUBCLASS_OF, JAVA_TYPE);
			javaTypesModel.add(BOOLEAN, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(BYTE, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(SHORT, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(INT, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(LONG, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(FLOAT, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(DOUBLE, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(CHAR, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(TOP, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE); // TOP of stack when top element
			// takes up two spots on the
			// stack; real time is on
			// element top-1
			javaTypesModel.add(VOID, RdfVocabulary.RDFS_SUBCLASS_OF, PRIMITIVE_TYPE);
			javaTypesModel.add(REFERENCE_TYPE, RdfVocabulary.RDFS_SUBCLASS_OF, JAVA_TYPE);
			javaTypesModel.add(UNINITIALIZED_THIS, RdfVocabulary.RDFS_SUBCLASS_OF, REFERENCE_TYPE);
		}

		public static BlankNodeOrIRI array(Model m, int dim, BlankNodeOrIRI type) {
			BlankNode t = javaTypesModel.blank();
			m.add(t, RdfVocabulary.RDF_TYPE, ARRAY_REF);
			m.add(t, ARRAY_DIM, m.literal(dim));
			m.add(t, ARRAY_ELEMENT_TYPE, type);
			return t;
		}

		public static IRI object(Model m, String typeName) {
			IRI t = m.node(typeName);
			m.add(t, RdfVocabulary.RDF_TYPE, REFERENCE_TYPE);
			return t;
		}
	}

	public static final String javaPrefix = "http://model.nuthatchery.org/java/";
	public static final String javaFlagsPrefix = javaPrefix + "flags/";
	public static final String javaMethodsPrefix = javaPrefix + "methods/";
	public static final String javaTypesPrefix = javaPrefix + "types/";
	public static final Model javaModel = //
			ModelFactory.getInstance().createModel(javaPrefix);
	public static final Model javaFlagsModel = //
			ModelFactory.getInstance().createModel(javaFlagsPrefix);

	public static final Model javaMethodsModel = //
			ModelFactory.getInstance().createModel(javaMethodsPrefix);

	// funnet på selv, bruker for å enkode ting fra bytecode

	public static final Model javaTypesModel = //
			ModelFactory.getInstance().createModel(javaTypesPrefix);
	public static final IRI ACCESS = javaModel.node("access");
	public static final IRI CALLS = javaModel.node("calls");
	public static final IRI C_CLASS = javaModel.node("class");
	public static final IRI C_INTERFACE = javaModel.node("interface");
	public static final IRI C_ENUM = javaModel.node("enum");
	public static final IRI C_CONSTRUCTOR = javaModel.node("constructor");
	public static final IRI CONSTRUCTS = javaModel.node("constructs");
	public static final IRI CREATES = javaModel.node("creates");
	public static final IRI DEBUG = javaModel.node("debug");
	public static final IRI DECLARES_THROW = javaModel.node("declaresThrow");
	public static final IRI P_EXTENDS = javaModel.node("extends");
	public static final IRI GENERIC = javaModel.node("generic");
	public static final IRI P_SUBTYPE_OF = javaModel.node("implements");

	public static final IRI INITIAL_VALUE = javaModel.node("initialValue");
	public static final IRI ACCESS_FIELD = javaModel.node("field");
	public static final IRI ACCESS_DYNAMIC = javaModel.node("dynamic");
	public static final IRI ACCESS_SPECIAL = javaModel.node("special");
	public static final IRI ACCESS_STATIC = javaModel.node("static");
	public static final IRI ACCESS_INTERFACE = javaModel.node("interface");

	public static final IRI ACCESS_VIRTUAL = javaModel.node("virtual");

	public static final IRI C_MEMBER = javaModel.node("member");
	public static final IRI C_METHOD = javaModel.node("method");
	public static final IRI C_FIELD = javaModel.node("field");
	public static final IRI P_HAS_FLAG = javaModel.node("hasFlag");
	public static final IRI READS = javaModel.node("reads");
	public static final IRI SIGNATURE = javaModel.node("signature");
	public static final IRI P_SOURCE_FILE = javaModel.node("sourceFile");
	public static final IRI P_THROWS = javaModel.node("throws");
	/*
	 * public static final IRI USES_JSR = javaModel.node("usesJsr"); public static
	 * final IRI USES_OBJ_EQUALS = javaModel.node("usesObjEquals"); public static
	 * final IRI USES_REF_EQUALS = javaModel.node("usesRefEquals"); public static
	 * final IRI USES_REF_NULLCHECK = javaModel.node("usesRefNullCheck"); public
	 * static final IRI USES_TYPE = javaModel.node("usesType");
	 */
	public static final IRI WRITES = javaModel.node("writes");
	public static final IRI P_CLASS_FILE_VERSION = javaModel.node("classFileVersion");
	public static final IRI P_CLASS_FILE_MINOR = javaModel.node("classFileMinorVersion");
	public static final IRI PARAMETER = javaModel.node("parameter");

	public static final IRI P_CODE = javaModel.node("code");
	public static final IRI P_NEXT = javaModel.node("next");
	public static final IRI P_NEXT_IF_TRUE = javaModel.node("nextIfTrue");
	public static final IRI P_NEXT_IF_FALSE = javaModel.node("nextIfFalse");
	public static final IRI R_END = javaModel.node("end");
	public static final IRI P_CALL = javaModel.node("call");
	public static final IRI P_LINE = CommonVocabulary.P_LINE_NUMBER;
	public static final IRI P_SRC_START = javaModel.node("srcStart");
	public static final IRI P_SRC_END = javaModel.node("srcEnd");
	public static final IRI C_INSTRUCTION = javaModel.node("jvmInstruction");
	public static final IRI C_JVM_INSN = javaModel.node("jvmInsn");
	public static final IRI P_OPERAND_INT = javaModel.node("intOperand");
	public static final IRI P_OPERAND = javaModel.node("operand");
	public static final IRI P_OPERAND_CONSTANT = javaModel.node("constantOperand");
	public static final IRI P_OPERAND_MEMBER = javaModel.node("memberOperand");
	public static final IRI P_OPERAND_TYPE = javaModel.node("typeOperand");
	public static final IRI P_OPERAND_VAR = javaModel.node("varOperand");
	public static final IRI P_OPERAND_LABEL = javaModel.node("labelOperand");
	public static final IRI P_OPERAND_LIST = javaModel.node("listOperand");
	public static final IRI P_TRY_CATCH_BLOCK = javaModel.node("tryCatchBlock");
	public static final IRI P_TYPE = javaModel.node("type");
	public static final IRI P_RETURN_TYPE = javaModel.node("rType");
	public static final IRI P_PARAMETERS = javaModel.node("params");
	public static final IRI P_MAX_STACK = javaModel.node("maxStack");
	public static final IRI P_MAX_LOCALS = javaModel.node("maxLocals");
	public static final IRI P_MEMBER_OF = javaModel.node("memberOf");

	static {
		javaModel.add(C_CLASS, RdfVocabulary.RDFS_SUBCLASS_OF, Types.REFERENCE_TYPE);
		javaModel.add(C_ENUM, RdfVocabulary.RDFS_SUBCLASS_OF, Types.REFERENCE_TYPE);
		javaModel.add(C_INTERFACE, RdfVocabulary.RDFS_SUBCLASS_OF, Types.REFERENCE_TYPE);

		isProperty(javaModel, P_CODE, C_METHOD, C_INSTRUCTION);
		isProperty(javaModel, P_NEXT, C_INSTRUCTION, C_INSTRUCTION);
		isProperty(javaModel, P_NEXT_IF_TRUE, C_INSTRUCTION, C_INSTRUCTION, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_NEXT);
		isProperty(javaModel, P_NEXT_IF_FALSE, C_INSTRUCTION, C_INSTRUCTION, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_NEXT);
		javaModel.add(R_END, RdfVocabulary.RDF_TYPE, C_INSTRUCTION);
		isProperty(javaModel, P_OPERAND, C_INSTRUCTION, null);
		isProperty(javaModel, P_OPERAND_CONSTANT, C_INSTRUCTION, RdfVocabulary.RDFS_RESOURCE, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_OPERAND);
		isProperty(javaModel, P_OPERAND_INT, C_INSTRUCTION, org.apache.commons.rdf.simple.Types.XSD_INTEGER, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_OPERAND);
		isProperty(javaModel, P_OPERAND_MEMBER, C_INSTRUCTION, C_MEMBER, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_OPERAND);
		isProperty(javaModel, P_OPERAND_TYPE, C_INSTRUCTION, Types.JAVA_TYPE, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_OPERAND);
		isProperty(javaModel, P_OPERAND_VAR, C_INSTRUCTION, org.apache.commons.rdf.simple.Types.XSD_INTEGER, //
				RdfVocabulary.RDFS_SUBPROPERTY_OF, P_OPERAND);
		isProperty(javaModel, P_CALL, C_INSTRUCTION, C_JVM_INSN);
		isProperty(javaModel, P_LINE, C_INSTRUCTION, org.apache.commons.rdf.simple.Types.XSD_INTEGER);
		javaModel.add(C_INSTRUCTION, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
		javaModel.add(C_JVM_INSN, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
	}

	private static void isProperty(Model m, IRI prop, BlankNodeOrIRI sub, RDFTerm obj, RDFTerm... more) {
		m.add(prop, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDF_PROPERTY);
		if (sub != null) {
			m.add(prop, RdfVocabulary.RDFS_RANGE, sub);
		}
		if (obj != null) {
			m.add(prop, RdfVocabulary.RDFS_DOMAIN, obj);
		}
		for (int i = 0; i < more.length; i += 2) {
			m.add(prop, (IRI) more[i], more[i + 1]);
		}
	}

	public static IRI method(Model m, IRI owner, String memberName, String memberDesc) {
		memberName = memberName.replaceAll("[<>]", "\\$");
		// return m.node(owner, memberName + "/" + memberDesc);
		Type type = Type.getType(memberDesc);
		StringBuilder sb = new StringBuilder();
		sb.append(memberName); // .re.replaceAll("[<>]", "-"));
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

		if (!memberName.equals("$init")) {
			sb.append(":");
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
		}
		return m.node(owner, sb.toString());
		// + UriEncoding.percentEncodeIri(methodName,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true)
		// + UriEncoding.percentEncodeIri(methodDesc,
		// JavaUtil.JAVA_EXTRA_URI_PATH_CHARS, true));
	}

	public static IRI opcode(int opcode) {
		return javaModel.node(Printer.OPCODES[opcode].toLowerCase());
	}

	// public static HashMap<String, String> URIsimplifier = new HashMap<>();

}
