package org.nuthatchery.analysis.java.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.nuthatchery.ontology.Id;
import org.nuthatchery.ontology.IdFactory;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.AnalyzerAdapter;

class MethodFactExtractor extends AnalyzerAdapter {
	// private ClassFactExtractor parent;
	private final Id currentMethodId;
	private ILogger log;
	/**
	 *
	 */
	private final ClassFactExtractor parent;

	public MethodFactExtractor(ClassFactExtractor parent, Id methodId, int access, String name, String desc,
			ILogger log) {
		super(Opcodes.ASM6, ClassFactExtractor.className, access, name, desc, null);
		this.parent = parent;
		this.currentMethodId = methodId;
		this.log = log;
		// this.parent = parent;
	}

	/**
	 * Get an argument from the stack, with 0 referring to the top of the stack
	 *
	 * @param argNum
	 * @return The argument, or null if not found
	 */
	protected Object stackFind(int argNum) {
		if (stack == null) {
			return null;
		}

		int i = stack.size() - 1;
		for (; i >= 0; i--, argNum--) {
			while (stack.get(i) == Opcodes.TOP) { // top half of a two-word
				// piece of data
				i--;
			}
			if (argNum == 0) {
				return stack.get(i);
			}
		}
		return null;
	}

	/**
	 * Get an argument from the stack, with 0 referring to the top of the stack
	 *
	 * @param argNum
	 * @return The argument, or null if not found
	 */
	protected Object stackGetType(int argNum) {
		Object o = stackFind(argNum);
		if (o instanceof Label) {
			return uninitializedTypes.get(o);
		} else {
			return o;
		}
	}

	/**
	 * Get an argument from the stack, with 0 referring to the top of the stack
	 *
	 * @param argNum
	 * @return The argument, or null if not found
	 */
	protected Object stackGetType(int argNum, String dflt) {
		Object o = stackFind(argNum);
		if (o instanceof Label) {
			return uninitializedTypes.get(o);
		} else if (o == null) {
			return dflt;
		} else {
			return o;
		}
	}

	/**
	 * Return true if the stack has at least the given number of arguments
	 *
	 * @param num
	 * @return
	 */
	protected boolean stackHasArgument(int num) {
		return stackFind(num) != null;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return super.visitAnnotationDefault();
	}

	@Override
	public void visitAttribute(Attribute attr) {
		log.logf("visitAttribue(attr=%s)%n", attr);
		super.visitAttribute(attr);
	}

	@Override
	public void visitCode() {
		log.log("{");
		parent.currentLine = -1;
		super.visitCode();
	}

	@Override
	public void visitEnd() {
		parent.currentLine = -1;
		log.log("} // end of " + currentMethodId);
		super.visitEnd();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// logln(indent() + "" + Printer.OPCODES[opcode] +
		// " " +
		// owner + "." + name + " [" + desc + "]");
		log.log("# access field: " + JavaUtil.decodeDescriptor(owner, name, desc));
		parent.put(JavaFacts.USES_TYPE, currentMethodId, JavaFacts.Types.object(owner));
		switch (opcode) {
		case Opcodes.GETSTATIC:
			parent.put(JavaFacts.READS, currentMethodId, parent.getMemberId(owner, name, desc), JavaFacts.ACCESS_STATIC);
			break;
		case Opcodes.PUTSTATIC:
			parent.put(JavaFacts.WRITES, currentMethodId, parent.getMemberId(owner, name, desc), JavaFacts.ACCESS_STATIC);
			break;
		case Opcodes.GETFIELD:
			parent.put(JavaFacts.READS, currentMethodId, parent.getMemberId(owner, name, desc), JavaFacts.ACCESS_FIELD);
			break;
		case Opcodes.PUTFIELD:
			parent.put(JavaFacts.WRITES, currentMethodId, parent.getMemberId(owner, name, desc), JavaFacts.ACCESS_FIELD);
			break;
		default:
			throw new RuntimeException();
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		super.visitIincInsn(var, increment);
	}

	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		parent.put(JavaFacts.CALLS, currentMethodId, parent.getMemberName(name, desc), JavaFacts.ACCESS_DYNAMIC);
		log.log("INVOKEDYNAMIC " + name + " [" + desc + "]" + " " + bsm + " " + Arrays.toString(bsmArgs));
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		switch (opcode) {
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPLE:
			// compare two ints
			break;
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFGT:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFLE:
			// compare a single int with zero
			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE: {
			// compare two references
			Id methodId = currentMethodId;
			if (parent.currentLine > 0) {
				methodId = methodId.setParam(IdFactory.literal("line"), IdFactory.literal(parent.currentLine));
			}
			Id type1 = JavaUtil.frameTypeToId(stackGetType(0, "java/lang/Object"));
			Id type2 = JavaUtil.frameTypeToId(stackGetType(1, "java/lang/Object"));
			log.logf("* ref equals on type %s with %s", type1, type2);
			parent.put(JavaFacts.USES_REF_EQUALS, methodId, type1);
			parent.put(JavaFacts.USES_REF_EQUALS, methodId, type2);
			break;
		}
		case Opcodes.IFNONNULL:
		case Opcodes.IFNULL: {
			// compare a single reference with null
			Id methodId = currentMethodId;
			if (parent.currentLine > 0) {
				methodId = methodId.setParam(IdFactory.literal("line"), IdFactory.literal(parent.currentLine));
			}

			Id type = JavaUtil.frameTypeToId(stackGetType(0, "java/lang/Object"));
			parent.put(JavaFacts.USES_REF_NULLCHECK, methodId, type);
			break;
		}
		case Opcodes.GOTO:
			// jump to an instruction within the same method
			break;
		case Opcodes.JSR:
			// jump to subroutine within same method
			parent.put(JavaFacts.USES_JSR, currentMethodId);
			return;
			// break;
		}
		super.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLabel(Label label) {
		log.log(label + ":");
		super.visitLabel(label);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		log.log(parent.currentSource.peek() + ":" + line + "\tlabel=" + start);
		parent.currentLine = line;
		super.visitLineNumber(line, start);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		log.logf("visitLocalVariable(name=%s, desc=%s, signature=%s, start=%s, end=%s, index=%d)%n",
				JavaUtil.decodeDescriptor(name, desc), desc, signature, start, end, index);
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
			int[] index, String desc, boolean visible) {
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		// logln(indent() + "" + Printer.OPCODES[opcode] +
		// " " +
		// owner + "." + name + " [" + desc + "]");
		Id classId = JavaFacts.Types.object(owner);
		Id methodId = JavaFacts.method(classId, name, desc);
		// NOTE: Example of finding the types of actual arguments and formal
		// parameters
		Type objType = Type.getObjectType(owner);
		if (opcode == Opcodes.INVOKESTATIC) {
			objType = null;
		}
		Type methType = Type.getType(desc);
		Type[] argumentTypes = methType.getArgumentTypes();
		if (stack != null) {
			List<String> stackTypes = stack.stream().map(JavaUtil::frameTypeToString).collect(Collectors.toList());
			int ars = methType.getArgumentsAndReturnSizes();
			int argSize = (ars >>> 2) - (objType == null ? 1 : 0);
			int retSize = ars & 3;
			log.log("# INVOKE METHOD: " + JavaUtil.decodeDescriptor(owner, name, desc));
			log.logf("  Environment: argSize=%d, retSize=%d", argSize, retSize);
			log.log("   * Stack: " + stackTypes + " <- top of stack");
			log.log("   * Locals: " + locals.stream().map(JavaUtil::frameTypeToString).collect(Collectors.toList()));
			log.log("   * Uninitialized: " + uninitializedTypes);
			List<String> paramTypes = Arrays.stream(argumentTypes).map((Type t) -> JavaUtil.typeToString(t))
					.collect(Collectors.toList());
			String retType = JavaUtil.typeToString(methType.getReturnType());
			List<String> actuals = stackTypes.subList(stackTypes.size() - argSize, stackTypes.size());
			log.logf("  Method is %s%s -> %s, stack arguments are %s",
					objType != null ? objType.getClassName() + "." : "static ", paramTypes, retType, actuals);
			actuals.clear();
			if (retSize == 1) {
				stackTypes.add(retType);
			} else if (retSize == 2) {
				stackTypes.add("TOP");
				stackTypes.add(retType);
			}
			if (!retType.equals("void")) {
				stackTypes.add(retType);
			}
			log.logf("  Stack after return will be: %s <- top of stack", stackTypes);
		}

		for (int i = 0; i < argumentTypes.length; i++) {
			Object type = stackGetType(i);
			if (type != null) {
				parent.put(IdFactory.literal("ACTUAL_ARGUMENT_TYPE" + i), JavaUtil.frameTypeToId(type));
			}
		}

		Id modifier;
		switch (opcode) {
		case Opcodes.INVOKEVIRTUAL:
			modifier = JavaFacts.ACCESS_VIRTUAL; //vanlig dynamic dispatch 
			break;
		case Opcodes.INVOKESPECIAL:
			modifier = JavaFacts.ACCESS_SPECIAL;
			break;
		case Opcodes.INVOKESTATIC:
			modifier = JavaFacts.ACCESS_STATIC;
			break;
		case Opcodes.INVOKEINTERFACE:
			modifier = JavaFacts.ACCESS_INTERFACE;
			break;
		default:
			throw new RuntimeException();
		}
		parent.put(JavaFacts.CALLS, currentMethodId, methodId, modifier);
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		log.logf("visitMultiANewArrayInsn(desc=%s, dims=%d)%n", desc, dims);
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitParameter(String name, int access) {
		log.logf("visitParameter(name=%s, access=%d)%n", name, access);
		Id paramId = currentMethodId.addPath(name);
		parent.put(JavaFacts.PARAMETER, currentMethodId, paramId);
		parent.addFieldAccessFlags(access, paramId);

		super.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		// logln(indent() + "" + Printer.OPCODES[opcode] +
		// " " +
		// type);
		parent.put(JavaFacts.USES_TYPE, currentMethodId, JavaFacts.Types.object(type));
		switch (opcode) {
		case Opcodes.NEW:
			parent.put(JavaFacts.CREATES, currentMethodId, JavaFacts.Types.object(type));
			break;
		case Opcodes.NEWARRAY:
			parent.put(JavaFacts.CREATES, currentMethodId,
					JavaFacts.Types.array(1/* TODO: correct dim */, JavaFacts.Types.object(type)));
			break;
		case Opcodes.ANEWARRAY:
			parent.put(JavaFacts.CREATES, currentMethodId,
					JavaFacts.Types.array(1/* TODO: correct dim */, JavaFacts.Types.object(type)));
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

	@Override
	public void visitVarInsn(int opcode, int var) {
		log.logf("visitVarInsn(opcode=%s, var=%d)%n", opcode, var);
		if (opcode != Opcodes.RET) {
			super.visitVarInsn(opcode, var);
		}
	}
}