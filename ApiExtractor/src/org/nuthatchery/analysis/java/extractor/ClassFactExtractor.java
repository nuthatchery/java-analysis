package org.nuthatchery.analysis.java.extractor;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.basic.Predicates;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class ClassFactExtractor extends ClassVisitor {
	public static String className; // TODO
	protected String context;
	protected final Stack<IRI> currentClass = new Stack<>();

	protected int currentLine = -1;

	protected final Stack<String> currentSource = new Stack<>();

	private final Model model;

	private final ILogger log;
	private IRI memberId;
	protected Set<String> seen = new HashSet<>();

	public ClassFactExtractor(Model fw, ILogger logger) {
		super(Opcodes.ASM6);
		this.model = fw;
		this.log = logger.indent(this::indentLevel);
	}

	protected IRI getClassId() {
		return currentClass.peek();
	}

	BlankNodeOrIRI getMemberId(String owner, String name, String desc) {
		return JavaFacts.method(model, JavaFacts.Types.object(model, owner), name, desc);
	}

	BlankNodeOrIRI getMemberName(String name, String desc) {
		return JavaFacts.method(model, getClassId(), name, desc);
	}

	protected Integer indentLevel() {
		return currentClass.size();
	}
	/*
	protected void put(IRI relation, BlankNodeOrIRI obj) {
		model.add(obj, relation, model.literal(true));
	}*/

	protected void put(BlankNodeOrIRI subj, IRI pred, RDFTerm obj) {
		model.add(subj, pred, obj);
	}

	/*
	 * protected void put(IRI relation, BlankNodeOrIRI obj, RDFTerm tgt,
	 * BlankNodeOrIRI field) { fw.put(obj, relation, tgt, field); }
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		log.log("\n" + "class " + name + " {");
		log.logf("\n\n" + "visit(version=%d, access=%d, name=%s, signature=%s, superName=%s, interfaces=%s)%n", version,
				access, name, signature, superName, interfaces);
		int major = version & 0xffff;
		int minor = (version >>> 16) & 0xffff;

		className = name;
		IRI id = JavaFacts.Types.object(model, name);
		currentClass.push(id);
		put(id, JavaFacts.P_CLASS_FILE_VERSION, model.literal(major));
		if (minor != 0) {
			put(id, JavaFacts.P_CLASS_FILE_MINOR, model.literal(minor));
		}
		put(id, Predicates.IS_A, JavaFacts.C_CLASS);
		if (superName != null) {
			put(id, JavaFacts.EXTENDS, JavaFacts.Types.object(model, superName));
		}
		if (interfaces != null) {
			for (String s : interfaces) {
				put(id, JavaFacts.IMPLEMENTS, JavaFacts.Types.object(model, s));
			}
		}
		addClassAccessFlags(access, id);

	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		log.logf("visitAnnotation(desc=%s, visible=%b)%n", desc, visible);
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		log.logf("visitAttribue(attr=%s)%n", attr);
	}

	@Override
	public void visitEnd() {
		BlankNodeOrIRI s = getClassId();
		currentClass.pop();
		log.log("} // end of " + s + "\n");
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// logf("visitField(access=%s, name=%s, desc=%s,
		// signature=%s, value=%s)%n", Printer.OPCODES[access],
		// name, desc, signature, value);
		log.log("\n" + "field " + JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(model, getClassId(), name, desc);
		put(memberId, Predicates.IS_A, JavaFacts.C_FIELD);
		if (value != null) {
			put(memberId, JavaFacts.INITIAL_VALUE, model.literal(value));
		}
		addFieldAccessFlags(access, memberId);

		memberId = null;
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		log.logf("visitOuterClass(name=%s, outerName=%s, innerName=%s, access=%s)%n", name, outerName, innerName,
				access);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// logf("visitMethod(access=%s, name=%s,
		// desc=%s,
		// signature=%s, exceptions=%s)%n",
		// Printer.OPCODES[access], name, desc, signature,
		// Arrays.toString(exceptions));
		System.out.flush();
		System.err.flush();
		log.log("\n" + "method " + JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(model, getClassId(), name, desc);
		if (name.equals("<init>")) {
			put(memberId, Predicates.IS_A, JavaFacts.C_CONSTRUCTOR);
			put(memberId, JavaFacts.CONSTRUCTS, getClassId());
		} else {
			put(memberId, Predicates.IS_A, JavaFacts.C_METHOD);
		}
		if (signature != null) {
			put(memberId, JavaFacts.GENERIC, model.literal(signature));
		}
		if (exceptions != null) {
			for (String s : exceptions) {
				put(memberId, JavaFacts.DECLARES_THROW, JavaFacts.Types.object(model, s));
			}
		}
		addMethodAccessFlags(access, memberId);
		return new MethodFactExtractor(this, memberId, access, name, desc, log);
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		log.logf("visitModule(name=%s, access=%d, version=%d)%n", name, access, version);
		return null;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		log.logf("visitOuterClass(owner=%s, name=%s, desc=%s)%n", owner, name, desc);
	}

	@Override
	public void visitSource(String source, String debug) {
		if (source != null) {
			put(getClassId(), JavaFacts.P_SOURCE_FILE, model.literal(source));
			currentSource.push(source);
		} else {
			currentSource.push("");
		}
		if (debug != null) {
			put(getClassId(), JavaFacts.DEBUG, model.literal(source));
			// logf("visitSource(source=%s, debug=%s)%n",
			// source,
			// debug);
		}
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.logf("visitTypeAnnotation(typeRed=%d, typePath=%s, desc=%s, visible=%b)%n", typeRef, typePath.toString(),
				desc, visible);
		return null;
	}

	public void addAccessFlags(int access, BlankNodeOrIRI id) {
		if ((access & Opcodes.ACC_PUBLIC) != 0) {
			put(id, JavaFacts.ACCESS, JavaFacts.Flags.PUBLIC);
		} else if ((access & Opcodes.ACC_PRIVATE) != 0) {
			put(id, JavaFacts.ACCESS, JavaFacts.Flags.PRIVATE);
		} else if ((access & Opcodes.ACC_PROTECTED) != 0) {
			put(id, JavaFacts.ACCESS, JavaFacts.Flags.PROTECTED);
		} else {
			put(id, JavaFacts.ACCESS, JavaFacts.Flags.PACKAGE);
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

	public void addClassAccessFlags(int access, BlankNodeOrIRI id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_MODULE) != 0) { // class
			putFlag(id, JavaFacts.Flags.MODULE);
		}
	}

	public void addMethodAccessFlags(int access, BlankNodeOrIRI id) {
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

	/**
	 * Also for parameters
	 *
	 * @param access
	 * @param id
	 */
	public void addFieldAccessFlags(int access, BlankNodeOrIRI id) {
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

	public void addModuleAccessFlags(int access, BlankNodeOrIRI id) {
		addAccessFlags(access, id);
		if ((access & Opcodes.ACC_STATIC_PHASE) != 0) { // 0x40, module
			putFlag(id, JavaFacts.Flags.STATIC_PHASE);
		}
		if ((access & Opcodes.ACC_MANDATED) != 0) { // parameter, module
			putFlag(id, JavaFacts.Flags.MANDATED);
		}
	}

	public void putFlag(BlankNodeOrIRI id, IRI flag) {
		put(id, JavaFacts.P_HAS_FLAG, flag);
	}

	public Model getModel() {
		return model;
	}
}
