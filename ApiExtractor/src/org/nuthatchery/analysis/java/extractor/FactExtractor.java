package org.nuthatchery.analysis.java.extractor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.Printer;

public class FactExtractor extends ClassVisitor {

	private static PrintWriter output;


	protected String context;
	protected final Stack<String> currentClass = new Stack<>();
	protected final Stack<String> currentMethod = new Stack<>();
	protected final Stack<String> currentSource = new Stack<>();
	protected final List<String> localInfo = new ArrayList<>();
	protected final List<String> stackInfo = new ArrayList<>();

	protected int currentLine = -1;

	protected Set<String> seen = new HashSet<>();

	public FactExtractor(String context) {
		super(Opcodes.ASM6);
		this.context = context;
	}

	private String getClassName() {
		return currentClass.peek();
	}

	private String getMemberName(String name, String desc) {
		return String.format("%s::%s%s", currentClass.peek(), name, desc);
	}

	private String getMemberName(String owner, String name, String desc) {
		return String.format("%s::%s%s", owner, name, desc);
	}

	private String getMethodName() {
		return currentMethod.peek();
	}

	private String indent() {
		StringBuilder b = new StringBuilder();
		indent(b);
		return b.toString();
	}

	private void indent(StringBuilder b) {
		int depth = currentClass.size() + currentMethod.size();
		for (int i = 0; i < depth; i++)
			b.append("  ");
	}

	private void put(String s) {
		if (!seen.contains(s)) {
			System.err.flush();
			System.out.println(indent() + s);
			if (output != null)
				output.println(s);
			System.out.flush();
			seen.add(s);
		}
	}

	private void put(String relation, String obj) {
		put(String.format("%s(\"%s\",\"%s\").", relation, context, obj));
	}

	private void put(String relation, String obj, String tgt) {
		put(String.format("%s(\"%s\",\"%s\",\"%s\").", relation, context, obj, tgt));
	}

	private void put(String relation, String obj, String tgt, String mod) {
		put(String.format("%s(\"%s\",\"%s\",\"%s\",\"%s\").", relation, context, obj, tgt, mod));
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		JavaUtil.logln("\n" + indent() + "class " + name + " {");
		// logf("\n\n" + indent() + "visit(version=%d, access=%d,
		// name=%s, signature=%s, superName=%s, interfaces=%s)%n", version,
		// access, name, signature, superName, Arrays.toString(interfaces));
		currentClass.push(name);
		put(JavaFacts.CLASS, name);
		put(JavaFacts.SIGNATURE, name, name);
		if (superName != null)
			put(JavaFacts.EXTENDS, name, superName);
		if (interfaces != null)
			for (String s : interfaces)
				put(JavaFacts.IMPLEMENTS, name, s);
		if ((access & Opcodes.ACC_PUBLIC) != 0)
			put(JavaFacts.ACCESS, name, JavaFacts.PUBLIC);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		JavaUtil.logf(indent() + "visitAnnotation(desc=%s, visible=%b)%n", desc, visible);
		return null;
	}

	public void visitAttribute(Attribute attr) {
		JavaUtil.logf(indent() + "visitAttribue(attr=%s)%n", attr);
	}

	public void visitEnd() {
		String s = getClassName();
		currentClass.pop();
		JavaUtil.logln(indent() + "} // end of " + s + "\n");
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// logf(indent() + "visitField(access=%s, name=%s, desc=%s,
		// signature=%s, value=%s)%n", Printer.OPCODES[access],
		// name, desc, signature, value);
		JavaUtil.logln("\n" + indent() + "field " + JavaUtil.decodeDescriptor(getClassName(), name, desc));
		String fullName = getMemberName(name, desc);
		currentMethod.push(fullName);
		put(JavaFacts.FIELD, fullName);
		put(JavaFacts.SIGNATURE, fullName, JavaUtil.decodeDescriptor(getClassName(), name, desc));
		if (value != null) {
			put(JavaFacts.INITIAL_VALUE, fullName, value.toString());
		}
		currentMethod.pop();
		return null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		JavaUtil.logf(indent() + "visitOuterClass(name=%s, outerName=%s, innerName=%s, access=%s)%n", name, outerName, innerName,
				Printer.OPCODES[access]);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// logf(indent() + "visitMethod(access=%s, name=%s,
		// desc=%s,
		// signature=%s, exceptions=%s)%n",
		// Printer.OPCODES[access], name, desc, signature,
		// Arrays.toString(exceptions));
		System.out.flush();
		System.err.flush();
		JavaUtil.logln("\n" + indent() + "method " + JavaUtil.decodeDescriptor(getClassName(), name, desc));
		String fullName = getMemberName(name, desc);
		currentMethod.push(fullName);
		put(JavaFacts.METHOD, fullName);
		if (name.equals("<init>")) {
			put(JavaFacts.CONSTRUCTOR, fullName);
			put(JavaFacts.CONSTRUCTS, fullName, getClassName());
		}
		put(JavaFacts.SIGNATURE, fullName, JavaUtil.decodeDescriptor(getClassName(), name, desc));
		if (signature != null)
			put(JavaFacts.GENERIC, fullName, signature);
		if (exceptions != null)
			for (String s : exceptions)
				put(JavaFacts.DECLARES_THROW, fullName, s);
		if ((access & Opcodes.ACC_PUBLIC) != 0)
			put(JavaFacts.PUBLIC, fullName); // or put(JF.ACCESS, fullName, PUBLIC);
		return new MethodVisitor(Opcodes.ASM6) {

			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				return super.visitAnnotation(desc, visible);
			}

			public AnnotationVisitor visitAnnotationDefault() {
				return super.visitAnnotationDefault();
			}

			public void visitAttribute(Attribute attr) {
				JavaUtil.logf(indent() + "visitAttribue(attr=%s)%n", attr);
				super.visitAttribute(attr);
			}

			public void visitCode() {
				JavaUtil.logln(indent() + "{");
				localInfo.clear();
				stackInfo.clear();
				super.visitCode();
			}

			@Override
			public void visitEnd() {
				String s = getMethodName();
				currentMethod.pop();
				currentLine = -1;
				JavaUtil.logln(indent() + "} // end of " + s);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				// logln(indent() + "" + Printer.OPCODES[opcode] +
				// " " +
				// owner + "." + name + " [" + desc + "]");
				JavaUtil.logln(indent() + "# access field: " + JavaUtil.decodeDescriptor(owner, name, desc));
				put(JavaFacts.USES_TYPE, getMethodName(), owner);
				switch (opcode) {
				case Opcodes.GETSTATIC:
					put(JavaFacts.READS, getMethodName(), getMemberName(owner, name, desc), JavaFacts.STATIC);
					break;
				case Opcodes.PUTSTATIC:
					put(JavaFacts.WRITES, getMethodName(), getMemberName(owner, name, desc), JavaFacts.STATIC);
					break;
				case Opcodes.GETFIELD:
					put(JavaFacts.READS, getMethodName(), getMemberName(owner, name, desc), JavaFacts.FIELD);
					break;
				case Opcodes.PUTFIELD:
					put(JavaFacts.WRITES, getMethodName(), getMemberName(owner, name, desc), JavaFacts.FIELD);
					break;
				default:
					throw new RuntimeException();
				}
				super.visitFieldInsn(opcode, owner, fullName, desc);
			}

			public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
				JavaUtil.logf(indent() + "visitFrame(type=%d, nLocal=%d, local=%s, nStack=%d, stack=%s)%n", type, nLocal,
						Arrays.toString(local), nStack, Arrays.toString(stack));
				if (type == Opcodes.F_NEW) {
					loadFrame(localInfo, nLocal, local);
					loadFrame(stackInfo, nStack, stack);
				}
				super.visitFrame(type, nLocal, local, nStack, stack);
			}

			public void visitIincInsn(int var, int increment) {
				super.visitIincInsn(var, increment);
			}

			public void visitInsn(int opcode) {
				super.visitInsn(opcode);
			}

			public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
				return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
			}

			public void visitIntInsn(int opcode, int operand) {
				super.visitIntInsn(opcode, operand);
			}

			@Override
			public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
				put(JavaFacts.CALLS, getMethodName(), getMemberName(name, desc), JavaFacts.DYNAMIC);
				JavaUtil.logln(indent() + "INVOKEDYNAMIC " + name + " [" + desc + "]" + " " + bsm + " "
						+ Arrays.toString(bsmArgs));
				super.visitInvokeDynamicInsn(fullName, desc, bsm, bsmArgs);
			}

			public void visitJumpInsn(int opcode, Label label) {
				super.visitJumpInsn(opcode, label);
			}

			public void visitLabel(Label label) {
				JavaUtil.logln(indent() + label + ":");
				super.visitLabel(label);
			}

			public void visitLdcInsn(Object cst) {
				super.visitLdcInsn(cst);
			}

			public void visitLineNumber(int line, Label start) {
				JavaUtil.logln(indent() + currentSource.peek() + ":" + line + "\tlabel=" + start);
				currentLine = line;
				super.visitLineNumber(line, start);
			}

			public void visitLocalVariable(String name, String desc, String signature, Label start, Label end,
					int index) {
				JavaUtil.logf(indent() + "visitLocalVariable(name=%s, desc=%s, signature=%s, start=%s, end=%s, index=%d)%n",
						JavaUtil.decodeDescriptor(name, desc), desc, signature, start, end, index);
				super.visitLocalVariable(fullName, desc, signature, start, end, index);
			}

			public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
					Label[] end, int[] index, String desc, boolean visible) {
				return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
			}

			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
				super.visitLookupSwitchInsn(dflt, keys, labels);
			}

			public void visitMaxs(int maxStack, int maxLocals) {
				super.visitMaxs(maxStack, maxLocals);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				// logln(indent() + "" + Printer.OPCODES[opcode] +
				// " " +
				// owner + "." + name + " [" + desc + "]");
				JavaUtil.logln(indent() + "# call method: " + JavaUtil.decodeDescriptor(owner, name, desc));
				JavaUtil.logln(indent() + "  Stack: " + stackInfo);
				String modifier;
				switch (opcode) {
				case Opcodes.INVOKEVIRTUAL:
					modifier = JavaFacts.VIRTUAL;
					break;
				case Opcodes.INVOKESPECIAL:
					modifier = JavaFacts.SPECIAL;
					break;
				case Opcodes.INVOKESTATIC:
					modifier = JavaFacts.STATIC;
					break;
				case Opcodes.INVOKEINTERFACE:
					modifier = JavaFacts.INTERFACE;
					break;
				default:
					throw new RuntimeException();
				}
				put(JavaFacts.CALLS, getMethodName(), getMemberName(owner, name, desc), modifier);
				super.visitMethodInsn(opcode, owner, fullName, desc, itf);
			}

			public void visitMultiANewArrayInsn(String desc, int dims) {
				JavaUtil.logf(indent() + "visitMultiANewArrayInsn(desc=%s, dims=%d)%n", desc, dims);
				super.visitMultiANewArrayInsn(desc, dims);
			}

			public void visitParameter(String name, int access) {
				JavaUtil.logf(indent() + "visitParameter(name=%s, access=%d)%n", name, access);
				super.visitParameter(fullName, access);
			}

			public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
				return super.visitParameterAnnotation(parameter, desc, visible);
			}

			public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
				super.visitTableSwitchInsn(min, max, dflt, labels);
			}

			public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc,
					boolean visible) {
				return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
			}

			public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				super.visitTryCatchBlock(start, end, handler, type);
			}

			public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
				return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				// logln(indent() + "" + Printer.OPCODES[opcode] +
				// " " +
				// type);
				put(JavaFacts.USES_TYPE, getMethodName(), type);
				switch (opcode) {
				case Opcodes.NEW:
					put(JavaFacts.CREATES, getMethodName(), type);
					break;
				case Opcodes.NEWARRAY:
					put(JavaFacts.CREATES, getMethodName(), "[" + type);
					break;
				case Opcodes.ANEWARRAY:
					put(JavaFacts.CREATES, getMethodName(), "[" + type);
					break;
				case Opcodes.CHECKCAST:
					break;
				case Opcodes.INSTANCEOF:
					break;
				default:
					throw new RuntimeException();
				}
				super.visitTypeInsn(opcode, type);
			}

			public void visitVarInsn(int opcode, int var) {
				super.visitVarInsn(opcode, var);
			}
		};
	}

	protected void loadFrame(List<String> frame, int nLocal, Object[] local) {
		for (int i = 0; i < nLocal; i++) {
			Object o = local[i];
			if (o == Opcodes.TOP)
				frame.add("/top");
			else if (o == Opcodes.INTEGER)
				frame.add("/int");
			else if (o == Opcodes.FLOAT)
				frame.add("/float");
			else if (o == Opcodes.DOUBLE)
				frame.add("/double");
			else if (o == Opcodes.LONG)
				frame.add("/long");
			else if (o == Opcodes.NULL)
				frame.add("/null");
			else if (o == Opcodes.UNINITIALIZED_THIS)
				frame.add("/uthis");
			else
				frame.add(o.toString());
		}
	}

	public ModuleVisitor visitModule(String name, int access, String version) {
		JavaUtil.logf(indent() + "visitModule(name=%s, access=%d, version=%d)%n", name, access, version);
		return null;
	}

	public void visitOuterClass(String owner, String name, String desc) {
		JavaUtil.logf(indent() + "visitOuterClass(owner=%s, name=%s, desc=%s)%n", owner, name, desc);
	}

	public void visitSource(String source, String debug) {
		if (source != null) {
			put(JavaFacts.SOURCE, getClassName(), source);
			currentSource.push(source);
		} else {
			currentSource.push("");
		}
		if (debug != null)
			put(JavaFacts.DEBUG, getClassName(), source);
		// logf(indent() + "visitSource(source=%s, debug=%s)%n",
		// source,
		// debug);
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		JavaUtil.logf(indent() + "visitTypeAnnotation(typeRed=%d, typePath=%s, desc=%s, visible=%b)%n", typeRef,
				typePath.toString(), desc, visible);
		return null;
	}
}
