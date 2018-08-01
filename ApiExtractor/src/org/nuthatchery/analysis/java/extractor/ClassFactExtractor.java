package org.nuthatchery.analysis.java.extractor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class ClassFactExtractor extends ClassVisitor {
	protected String className;
	protected String context;

	private final Stack<Resource> classIdStack = new Stack<>();
	private final Stack<Resource> classJnStack = new Stack<>();

	protected int currentLine = -1;

	protected final Stack<String> sourceStack = new Stack<>();

	private final Model model;
	private final ILogger log;
	private Resource memberId;

	protected Set<String> seen = new HashSet<>();
	protected final String prefix;
	private Resource memberJn;

	public ClassFactExtractor(Model fw, String prefix, ILogger logger) {
		super(Opcodes.ASM6);
		this.model = fw;
		this.log = logger.indent(this::indentLevel);
		this.prefix = prefix;
	}

	public void addAccessFlags(int access, Resource id) {
		if ((access & Opcodes.ACC_PUBLIC) != 0) {
			id.addProperty(JavaFacts.hasAccess, JavaFacts.Flags.PUBLIC);
		} else if ((access & Opcodes.ACC_PRIVATE) != 0) {
			id.addProperty(JavaFacts.hasAccess, JavaFacts.Flags.PRIVATE);
		} else if ((access & Opcodes.ACC_PROTECTED) != 0) {
			id.addProperty(JavaFacts.hasAccess, JavaFacts.Flags.PROTECTED);
		} else {
			id.addProperty(JavaFacts.hasAccess, JavaFacts.Flags.PACKAGE);
		}
		if ((access & Opcodes.ACC_STATIC) != 0) {
			putFlag(id, JavaFacts.Flags.STATIC);
		}
		if ((access & Opcodes.ACC_FINAL) != 0) {
			putFlag(id, JavaFacts.Flags.FINAL);
		}
		if ((access & Opcodes.ACC_NATIVE) != 0) {
			putFlag(id, JavaFacts.Flags.NATIVE);
		}
		if ((access & Opcodes.ACC_INTERFACE) != 0) {
			putFlag(id, JavaFacts.Flags.INTERFACE);
		}
		if ((access & Opcodes.ACC_ABSTRACT) != 0) {
			putFlag(id, JavaFacts.Flags.ABSTRACT);
		}
		if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
			putFlag(id, JavaFacts.Flags.SYNTHETIC);
		}
		if ((access & Opcodes.ACC_ANNOTATION) != 0) {
			putFlag(id, JavaFacts.Flags.ANNOTATION);
		}
		if ((access & Opcodes.ACC_ENUM) != 0) {
			putFlag(id, JavaFacts.Flags.ENUM);
		}
		if ((access & Opcodes.ACC_DEPRECATED) != 0) {
			putFlag(id, JavaFacts.Flags.DEPRECATED);
		}
	}

	public void addClassAccessFlags(int access, Resource id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_MODULE) != 0) { // class
			putFlag(id, JavaFacts.Flags.MODULE);
		}
	}

	/**
	 * Also for parameters
	 *
	 * @param access
	 * @param id
	 */
	public void addFieldAccessFlags(int access, Resource id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_VOLATILE) != 0) { // 0x40, field
			putFlag(id, JavaFacts.Flags.VOLATILE);
		}
		if ((access & Opcodes.ACC_TRANSIENT) != 0) { // 0x80, field
			putFlag(id, JavaFacts.Flags.TRANSIENT);
		}
		if ((access & Opcodes.ACC_MANDATED) != 0) { // parameter, module
			putFlag(id, JavaFacts.Flags.MANDATED);
		}
	}

	public void addMethodAccessFlags(int access, Resource id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) { // method
			putFlag(id, JavaFacts.Flags.SUPER);
		}
		if ((access & Opcodes.ACC_BRIDGE) != 0) { // 0x40, method
			putFlag(id, JavaFacts.Flags.BRIDGE);
		}
		if ((access & Opcodes.ACC_VARARGS) != 0) { // 0x80, method
			putFlag(id, JavaFacts.Flags.VARARGS);
		}
	}

	public void addModuleAccessFlags(int access, Resource id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_STATIC_PHASE) != 0) { // 0x40, module
			putFlag(id, JavaFacts.Flags.STATIC_PHASE);
		}
		if ((access & Opcodes.ACC_MANDATED) != 0) { // parameter, module
			putFlag(id, JavaFacts.Flags.MANDATED);
		}
	}

	private Resource getClassId() {
		return classIdStack.peek();
	}

	private Resource getClassJn() {
		return classJnStack.peek();
	}

	protected Resource getMemberId(String owner, String name, String desc) {
		return JavaFacts.method(model, JavaFacts.Types.object(model, JavaFacts.JN, owner), name, desc);
	}


	public Model getModel() {
		return model;
	}

	protected Integer indentLevel() {
		return classIdStack.size();
	}

	public void putFlag(Resource id, Resource flag) {
		id.addProperty(JavaFacts.P_HAS_FLAG, flag);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		log.log("\n" + "class " + name + " {");
		log.logf("\n\n" + "visit(version=%d, access=%d, name=%s, signature=%s, superName=%s, interfaces=%s)%n", version,
				access, name, signature, superName, Arrays.toString(interfaces));
		int major = version & 0xffff;
		int minor = (version >>> 16) & 0xffff;

		className = name.replace('/', '.');
		Resource id = JavaFacts.Types.object(model, model.getNsPrefixURI(""), className);
		Resource javaName = JavaFacts.Types.object(model, JavaFacts.JN, className);
		javaName.addProperty(RDFS.isDefinedBy, id);
		classIdStack.push(id);
		classJnStack.push(javaName);
		if (!className.contains("$")) {
			id.addProperty(CommonVocabulary.P_NAME, model.createTypedLiteral(className));
		}
		id.addProperty(CommonVocabulary.P_IDNAME, model.createTypedLiteral(className));
		id.addProperty(RDF.type, CommonVocabulary.C_DEF);
		id.addProperty(JavaFacts.P_CLASS_FILE_VERSION, model.createTypedLiteral(major));
		if (minor != 0) {
			id.addProperty(JavaFacts.P_CLASS_FILE_MINOR, model.createTypedLiteral(minor));
		}
		if ((access & Opcodes.ACC_INTERFACE) != 0) {
			id.addProperty(CommonVocabulary.P_DEFINES, JavaFacts.C_INTERFACE);
		} else if ((access & Opcodes.ACC_ENUM) != 0) {
			id.addProperty(CommonVocabulary.P_DEFINES, JavaFacts.C_ENUM);
		} else {
			id.addProperty(CommonVocabulary.P_DEFINES, JavaFacts.C_CLASS);
		}
		if (superName != null) {
			id.addProperty(JavaFacts.P_EXTENDS, JavaFacts.Types.object(model, JavaFacts.JN, superName));
		}
		if (interfaces != null) {
			for (String s : interfaces) {
				id.addProperty(JavaFacts.P_SUBTYPE_OF, JavaFacts.Types.object(model, JavaFacts.JN, s));
			}
		}
		addClassAccessFlags(access, id);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// log.warnf("unimplemented: ClassFactExtractor.visitAnnotation(desc=%s,
		// visible=%b)%n", desc, visible);
		Resource anno = model.createResource();
		model.add(getClassId(), JavaFacts.P_ANNOTATION, anno);
		model.add(anno, JavaFacts.P_TYPE, JavaFacts.Types.object(model, JavaFacts.JN, desc));
		// TODO: also traverse the annotation
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitAttribute(Attribute attr) {
		log.warnf("unimplemented: ClassFactExtractor.visitAttribue(attr=%s)%n", attr);
		super.visitAttribute(attr);
	}

	@Override
	public void visitEnd() {
		Resource s = getClassId();
		classIdStack.pop();
		classJnStack.pop();
		log.log("} // end of " + s + "\n");
		super.visitEnd();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// logf("visitField(access=%s, name=%s, desc=%s,
		// signature=%s, value=%s)%n", Printer.OPCODES[access],
		// name, desc, signature, value);
		log.log("\n" + "field " + JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(model, getClassId(), name, desc);
		memberJn = JavaFacts.method(model, getClassJn(), name, desc);
		String descName = className + "." + name + ":" + desc;
		model.add(memberJn, RDFS.isDefinedBy, memberId);
		model.add(memberId, CommonVocabulary.P_NAME, model.createLiteral(name));
		model.add(memberId, CommonVocabulary.P_IDNAME, model.createLiteral(descName));
		model.add(memberId, RDF.type, CommonVocabulary.C_DEF);
		model.add(memberId, JavaFacts.P_TYPE, JavaUtil.typeToId(model, JavaFacts.JN, Type.getType(desc)));
		model.add(memberId, JavaFacts.P_MEMBER_OF, getClassId());
		if (value != null) {
			model.add(memberId, CommonVocabulary.P_DEFINES, JavaFacts.C_FIELD);
			model.add(memberId, JavaFacts.INITIAL_VALUE, model.createTypedLiteral(value));
		} else {
			model.add(memberId, CommonVocabulary.P_DECLARES, JavaFacts.C_FIELD);
		}

		addFieldAccessFlags(access, memberId);

		memberId = null;
		super.visitField(access, name, descName, signature, value);
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		log.logf("visitInnerClass(name=%s, outerName=%s, innerName=%s, access=%s)%n", name, outerName, innerName,
				access);

		// id.addProperty(CommonVocabulary.P_IDNAME, model.createTypedLiteral(className));

		if (outerName != null) {
			Resource inner = JavaFacts.Types.object(model, JavaFacts.JN, name);
			Resource outer = JavaFacts.Types.object(model, JavaFacts.JN, outerName);
			model.add(inner, JavaFacts.P_MEMBER_OF, outer);
			if (innerName != null) {
				model.add(inner, CommonVocabulary.P_NAME, model.createLiteral(innerName));
			}
		}
		super.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		log.logf("visitMethod(access=%s, name=%s, desc=%s, signature=%s, exceptions=%s)%n", "", name, desc, signature,
				Arrays.toString(exceptions));
		System.out.flush();
		System.err.flush();

		log.log("\n" + "method " + className + "." + name + desc);// JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(model, getClassId(), name, desc);
		memberJn = JavaFacts.method(model, getClassJn(), name, desc);
		String descName = className + "." + name + ":" + desc;
		model.add(memberJn, RDFS.isDefinedBy, memberId);
		model.add(memberId, CommonVocabulary.P_NAME, model.createLiteral(name));
		model.add(memberId, CommonVocabulary.P_IDNAME, model.createLiteral(descName));
		model.add(memberId, RDF.type, CommonVocabulary.C_DEF);
		model.add(memberId, JavaFacts.P_RETURN_TYPE,
				JavaUtil.typeToId(model, JavaFacts.JN, Type.getType(desc).getReturnType()));
		model.add(memberId, JavaFacts.P_MEMBER_OF, getClassId());
		if (name.equals("<init>")) {
			model.add(memberId, CommonVocabulary.P_DEFINES, JavaFacts.C_FIELD);
			model.add(memberId, CommonVocabulary.P_DEFINES, JavaFacts.C_CONSTRUCTOR);
			model.add(memberId, JavaFacts.CONSTRUCTS, getClassId());
		} else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
			model.add(memberId, CommonVocabulary.P_DECLARES, JavaFacts.C_METHOD);
		} else {
			model.add(memberId, CommonVocabulary.P_DEFINES, JavaFacts.C_METHOD);
		}
		if (signature != null) {
			model.add(memberId, JavaFacts.GENERIC, model.createLiteral(signature));
		}
		if (exceptions != null) {
			for (String s : exceptions) {
				model.add(memberId, JavaFacts.DECLARES_THROW, JavaFacts.Types.object(model, JavaFacts.JN, s));
			}
		}
		addMethodAccessFlags(access, memberId);
		return new MethodFactExtractor(this, memberId, access, name, desc, log);
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		log.warnf("unimplemented: ClassFactExtractor.visitModule(name=%s, access=%d, version=%s)%n", name, access,
				version);
		super.visitModule(name, access, version);
		return null;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		log.logf("visitOuterClass(owner=%s, name=%s, desc=%s)%n", owner, name, desc);
		Resource ownerId = JavaFacts.Types.object(model, JavaFacts.JN, owner);
		if (name != null && desc != null) {
			Resource methodId = JavaFacts.method(model, ownerId, name, desc);
			model.add(getClassId(), JavaFacts.P_MEMBER_OF, methodId);
		} else {
			model.add(getClassId(), JavaFacts.P_MEMBER_OF, ownerId);
		}
		super.visitOuterClass(owner, name, desc);
	}

	@Override
	public void visitSource(String source, String debug) {
		if (source != null) {
			model.add(getClassId(), JavaFacts.P_SOURCE_FILE, model.createLiteral(source));
			sourceStack.push(source);
		} else {
			sourceStack.push("");
		}
		if (debug != null) {
			model.add(getClassId(), JavaFacts.DEBUG, model.createLiteral(source));
			// logf("visitSource(source=%s, debug=%s)%n",
			// source,
			// debug);
		}
		super.visitSource(source, debug);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.warnf(
				"unimplemented: ClassFactExtractor.visitTypeAnnotation(typeRed=%d, typePath=%s, desc=%s, visible=%b)%n",
				typeRef,
				typePath.toString(), desc, visible);
		super.visitTypeAnnotation(typeRef, typePath, desc, visible);
		return null;
	}
}
