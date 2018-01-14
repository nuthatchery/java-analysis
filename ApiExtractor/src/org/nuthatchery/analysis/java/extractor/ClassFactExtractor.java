package org.nuthatchery.analysis.java.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.nuthatchery.analysis.java.extractor.FactsDb.IFactsWriter;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.Printer;

public class ClassFactExtractor extends ClassVisitor {
	protected String context;
	protected final Stack<Id> currentClass = new Stack<>();
	protected final Stack<String> currentSource = new Stack<>();

	protected int currentLine = -1;

	protected Set<String> seen = new HashSet<>();

	private final IFactsWriter fw;

	private final ILogger log;
	private Id memberId;
	public static String className; // TODO

	public ClassFactExtractor(IFactsWriter fw, ILogger logger) {
		super(Opcodes.ASM6);
		this.fw = fw;
		this.log = logger.indent(this::indentLevel);
	}

	protected Integer indentLevel() {
		return currentClass.size();
	}
	
	protected Id getClassId() {
		return currentClass.peek();
	}

	Id getMemberName(String name, String desc) {
		return JavaFacts.method(getClassId(), name, desc); 
	}

	Id getMemberId(String owner, String name, String desc) {
		return JavaFacts.method(JavaFacts.Types.object(owner), name, desc); 
	}


	protected void put(Id relation, Id obj) {
		fw.put(obj, relation);
	}

	protected void put(Id relation, Id obj, Id tgt) {
		fw.put(obj, relation, tgt);
	}

	protected void put(Id relation, Id obj, Id tgt, Id field) {
		fw.put(obj, relation, tgt, field);
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		log.log("\n" + "class " + name + " {");
		// logf("\n\n" + "visit(version=%d, access=%d,
		// name=%s, signature=%s, superName=%s, interfaces=%s)%n", version,
		// access, name, signature, superName, Arrays.toString(interfaces));
		className = name;
		Id id = JavaFacts.Types.object(name);
		currentClass.push(id);
		put(JavaFacts.CLASS, id);
		if (superName != null)
			put(JavaFacts.EXTENDS, id, JavaFacts.Types.object(superName));
		if (interfaces != null)
			for (String s : interfaces)
				put(JavaFacts.IMPLEMENTS, id, JavaFacts.Types.object(s));
		if ((access & Opcodes.ACC_PUBLIC) != 0)
			put(JavaFacts.ACCESS, id, JavaFacts.PUBLIC);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		log.logf("visitAnnotation(desc=%s, visible=%b)%n", desc, visible);
		return null;
	}

	public void visitAttribute(Attribute attr) {
		log.logf("visitAttribue(attr=%s)%n", attr);
	}

	public void visitEnd() {
		Id s = getClassId();
		currentClass.pop();
		log.log("} // end of " + s + "\n");
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// logf("visitField(access=%s, name=%s, desc=%s,
		// signature=%s, value=%s)%n", Printer.OPCODES[access],
		// name, desc, signature, value);
		log.log("\n" + "field " + JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(getClassId(), name, desc);
		put(JavaFacts.FIELD, memberId);
		if (value != null) {
			put(JavaFacts.INITIAL_VALUE, memberId, IdFactory.literal(value));
		}
		memberId = null;
		return null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		log.logf("visitOuterClass(name=%s, outerName=%s, innerName=%s, access=%s)%n", name, outerName,
				innerName, access);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// logf("visitMethod(access=%s, name=%s,
		// desc=%s,
		// signature=%s, exceptions=%s)%n",
		// Printer.OPCODES[access], name, desc, signature,
		// Arrays.toString(exceptions));
		System.out.flush();
		System.err.flush();
		log.log("\n" + "method " + JavaUtil.decodeDescriptor(className, name, desc));
		memberId = JavaFacts.method(getClassId(), name, desc);
		put(JavaFacts.METHOD, memberId);
		if (name.equals("<init>")) {
			put(JavaFacts.CONSTRUCTOR, memberId);
			put(JavaFacts.CONSTRUCTS, memberId, getClassId());
		}
		if (signature != null)
			put(JavaFacts.GENERIC, memberId, IdFactory.literal(signature));
		if (exceptions != null)
			for (String s : exceptions)
				put(JavaFacts.DECLARES_THROW, memberId, JavaFacts.Types.object(s));
		if ((access & Opcodes.ACC_PUBLIC) != 0)
			put(JavaFacts.PUBLIC, memberId); // or put(JF.ACCESS, fullName,
												// PUBLIC);
		return new MethodFactExtractor(this, memberId, access, name, desc, log);
	}


	public ModuleVisitor visitModule(String name, int access, String version) {
		log.logf("visitModule(name=%s, access=%d, version=%d)%n", name, access, version);
		return null;
	}

	public void visitOuterClass(String owner, String name, String desc) {
		log.logf("visitOuterClass(owner=%s, name=%s, desc=%s)%n", owner, name, desc);
	}

	public void visitSource(String source, String debug) {
		if (source != null) {
			put(JavaFacts.SOURCE, getClassId(), IdFactory.literal(source));
			currentSource.push(source);
		} else {
			currentSource.push("");
		}
		if (debug != null)
			put(JavaFacts.DEBUG, getClassId(), IdFactory.literal(source));
		// logf("visitSource(source=%s, debug=%s)%n",
		// source,
		// debug);
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.logf("visitTypeAnnotation(typeRed=%d, typePath=%s, desc=%s, visible=%b)%n", typeRef,
				typePath.toString(), desc, visible);
		return null;
	}


}
