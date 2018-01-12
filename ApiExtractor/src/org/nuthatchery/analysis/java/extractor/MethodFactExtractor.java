package org.nuthatchery.analysis.java.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.AnalyzerAdapter;

class MethodFactExtractor extends AnalyzerAdapter {
	/**
	 * 
	 */
	private final ClassFactExtractor parent;
	// private ClassFactExtractor parent;
	private String fullName;
	private ILogger log;

	public MethodFactExtractor(ClassFactExtractor parent, String fullName, int access, String name, String desc,
			ILogger log) {
		super(Opcodes.ASM6, parent.getClassName(), access, name, desc, null);
		this.parent = parent;
		this.fullName = fullName;
		this.log = log;
		// this.parent = parent;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(desc, visible);
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return super.visitAnnotationDefault();
	}

	public void visitAttribute(Attribute attr) {
		log.logf("visitAttribue(attr=%s)%n", attr);
		super.visitAttribute(attr);
	}

	public void visitCode() {
		log.log("{");
		parent.localInfo.clear();
		parent.stackInfo.clear();
		super.visitCode();
	}

	@Override
	public void visitEnd() {
		String s = parent.getMethodName();
		parent.currentMethod.pop();
		parent.currentLine = -1;
		log.log("} // end of " + s);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// logln(indent() + "" + Printer.OPCODES[opcode] +
		// " " +
		// owner + "." + name + " [" + desc + "]");
		log.log("# access field: " + JavaUtil.decodeDescriptor(owner, name, desc));
		parent.put(JavaFacts.USES_TYPE, parent.getMethodName(), owner);
		switch (opcode) {
		case Opcodes.GETSTATIC:
			parent.put(JavaFacts.READS, parent.getMethodName(), parent.getMemberName(owner, name, desc),
					JavaFacts.STATIC);
			break;
		case Opcodes.PUTSTATIC:
			parent.put(JavaFacts.WRITES, parent.getMethodName(), parent.getMemberName(owner, name, desc),
					JavaFacts.STATIC);
			break;
		case Opcodes.GETFIELD:
			parent.put(JavaFacts.READS, parent.getMethodName(), parent.getMemberName(owner, name, desc),
					JavaFacts.FIELD);
			break;
		case Opcodes.PUTFIELD:
			parent.put(JavaFacts.WRITES, parent.getMethodName(), parent.getMemberName(owner, name, desc),
					JavaFacts.FIELD);
			break;
		default:
			throw new RuntimeException();
		}
		super.visitFieldInsn(opcode, owner, fullName, desc);
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
		parent.put(JavaFacts.CALLS, parent.getMethodName(), parent.getMemberName(name, desc), JavaFacts.DYNAMIC);
		log.log("INVOKEDYNAMIC " + name + " [" + desc + "]" + " " + bsm + " " + Arrays.toString(bsmArgs));
		super.visitInvokeDynamicInsn(fullName, desc, bsm, bsmArgs);
	}

	public void visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
	}

	public void visitLabel(Label label) {
		log.log(label + ":");
		super.visitLabel(label);
	}

	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
	}

	public void visitLineNumber(int line, Label start) {
		log.log(parent.currentSource.peek() + ":" + line + "\tlabel=" + start);
		parent.currentLine = line;
		super.visitLineNumber(line, start);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		log.logf("visitLocalVariable(name=%s, desc=%s, signature=%s, start=%s, end=%s, index=%d)%n",
				JavaUtil.decodeDescriptor(name, desc), desc, signature, start, end, index);
		super.visitLocalVariable(fullName, desc, signature, start, end, index);
	}

	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
			int[] index, String desc, boolean visible) {
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
		
		// NOTE: Example of finding the types of actual arguments and formal parameters
		Type objType = Type.getObjectType(owner);
		int objParam = 1;
		if(opcode == Opcodes.INVOKESTATIC) {
			objType = null;
			objParam = 0;
		}
			
		List<String> stackTypes = stack.stream().map(JavaUtil::frameTypeToString).collect(Collectors.toList());
		log.log("# INVOKE METHOD: " + JavaUtil.decodeDescriptor(owner, name, desc));
		log.log("  Environment:");
		log.log("   * Stack: " +  stackTypes + " <- top of stack");
		log.log("   * Locals: "
				+ locals.stream().map(JavaUtil::frameTypeToString).collect(Collectors.toList()));
		log.log("   * Uninitialized: " + uninitializedTypes);
		Type methType = Type.getType(desc);
		List<String> paramTypes = Arrays.stream(methType.getArgumentTypes()).map((Type t) -> JavaUtil.typeToString(t)).collect(Collectors.toList());
		String retType = JavaUtil.typeToString(methType.getReturnType());
		List<String> actuals = stackTypes.subList(stackTypes.size()-paramTypes.size()-objParam, stackTypes.size());
		log.logf("  Method is %s%s -> %s, stack arguments are %s", objType != null ? objType.getClassName() + "." : "static ", paramTypes, retType, actuals);
		actuals.clear();
		if(!retType.equals("void"))
			stackTypes.add(retType);
		log.logf("  Stack after return will be: %s <- top of stack", stackTypes);

		
		int[] i = {0};
		Arrays.stream(methType.getArgumentTypes()).map((Type t) -> JavaUtil.typeToId(t)).forEachOrdered((Id id) -> {
			parent.put(Id.string("ACTUAL_ARGUMENT_TYPE" + i[0]++), Id.string(fullName), id);
		});
		
		Id modifier;
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
		parent.put(JavaFacts.CALLS, parent.getMethodName(), parent.getMemberName(owner, name, desc), modifier);
		super.visitMethodInsn(opcode, owner, fullName, desc, itf);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		log.logf("visitMultiANewArrayInsn(desc=%s, dims=%d)%n", desc, dims);
		super.visitMultiANewArrayInsn(desc, dims);
	}

	public void visitParameter(String name, int access) {
		log.logf("visitParameter(name=%s, access=%d)%n", name, access);
		super.visitParameter(fullName, access);
	}

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
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
		parent.put(JavaFacts.USES_TYPE, parent.getMethodName(), type);
		switch (opcode) {
		case Opcodes.NEW:
			parent.put(JavaFacts.CREATES, parent.getMethodName(), type);
			break;
		case Opcodes.NEWARRAY:
			parent.put(JavaFacts.CREATES, parent.getMethodName(), "[" + type);
			break;
		case Opcodes.ANEWARRAY:
			parent.put(JavaFacts.CREATES, parent.getMethodName(), "[" + type);
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

}