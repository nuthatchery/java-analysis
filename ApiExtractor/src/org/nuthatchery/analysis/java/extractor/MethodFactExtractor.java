package org.nuthatchery.analysis.java.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
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
	private final IRI currentMethodId;
	private BlankNodeOrIRI currentInsn;
	private IRI insnChain;
	private BlankNodeOrIRI lastInsn;
	private ILogger log;
	private static int count = 0;
	/**
	 *
	 */
	private final ClassFactExtractor parent;

	public MethodFactExtractor(ClassFactExtractor parent, IRI methodId, int access, String name, String desc,
			ILogger log) {
		super(Opcodes.ASM6, ClassFactExtractor.className, access, name, desc, null);
		this.parent = parent;
		this.currentMethodId = methodId;
		this.log = log;
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
		nextInstruction();
		lastInsn = currentMethodId;
		insnChain = JavaFacts.P_CODE;
		super.visitCode();
	}

	/**
	 * Move to next instruction.
	 *
	 * @return Identifier of previous instruction
	 */
	protected void nextInstruction() {
		lastInsn = currentInsn;
		currentInsn = parent.getModel().blank(String.valueOf(count++));
	}

	@Override
	public void visitEnd() {
		parent.currentLine = -1;
		log.log("} // end of " + currentMethodId);
		if (lastInsn != null && insnChain != null) {
			parent.getModel().add(lastInsn, insnChain, JavaFacts.R_END);
		}

		super.visitEnd();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// logln(indent() + "" + Printer.OPCODES[opcode] +
		// " " +
		// owner + "." + name + " [" + desc + "]");
		log.log("# access field: " + JavaUtil.decodeDescriptor(owner, name, desc));
		BlankNodeOrIRI memberId = parent.getMemberId(owner, name, desc);
		putInstruction(opcode, JavaFacts.P_FIELD, memberId);
		nextInstruction();
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		putInstruction(Opcodes.IINC, JavaFacts.P_VAR, var, JavaFacts.P_OPERAND, increment);
		nextInstruction();
		super.visitIincInsn(var, increment);
	}

	protected void putInstruction(int opcode, Object... args) {
		if (args.length % 2 == 1) {
			throw new IllegalArgumentException();
		}
		System.out.println("(" + lastInsn + "," + insnChain + "," + currentInsn + ")");
		if (lastInsn != null && insnChain != null) {
			parent.getModel().add(lastInsn, insnChain, currentInsn);
		}
		insnChain = JavaFacts.P_NEXT;
		parent.put(currentInsn, JavaFacts.P_CALL, JavaFacts.opcode(opcode));
		for (int i = 0; i < args.length; i += 2) {
			if (args[i] instanceof IRI) {
				IRI pred = (IRI) args[i];
				RDFTerm obj;
				if (args[i + 1] instanceof RDFTerm) {
					obj = (RDFTerm) args[i + 1];
				} else {
					obj = parent.getModel().literal(args[i + 1]);
				}
				parent.put(currentInsn, pred, obj);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public void visitInsn(int opcode) {
		putInstruction(opcode);
		nextInstruction();
		super.visitInsn(opcode);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		putInstruction(opcode, JavaFacts.P_OPERAND, operand);
		nextInstruction();
		super.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		log.log("INVOKEDYNAMIC " + name + " [" + desc + "]" + " " + bsm + " " + Arrays.toString(bsmArgs));
		putInstruction(Opcodes.INVOKEDYNAMIC, JavaFacts.P_OPERAND, parent.getMemberName(name, desc));
		nextInstruction();
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		BlankNode target = parent.getModel().blank(label.toString());
		putInstruction(opcode);
		if (opcode == Opcodes.GOTO) {
			parent.getModel().add(currentInsn, JavaFacts.P_NEXT, target);
			insnChain = null;
		} else if (opcode == Opcodes.JSR) {
			throw new UnsupportedOperationException();
		} else {
			parent.getModel().add(currentInsn, JavaFacts.P_NEXT_IF_TRUE, target);
			insnChain = JavaFacts.P_NEXT_IF_FALSE;
		}
		nextInstruction();
		super.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLabel(Label label) {
		log.log(label + ":");
		currentInsn = parent.getModel().blank(label.toString());
		super.visitLabel(label);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		putInstruction(Opcodes.LDC, JavaFacts.P_OPERAND, cst);
		nextInstruction();
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		log.log(parent.currentSource.peek() + ":" + line + "\tlabel=" + start);
		parent.currentLine = line;
		currentInsn = parent.getModel().blank(start.toString());
		parent.getModel().add(currentInsn, JavaFacts.P_LINE, parent.getModel().literal(line));
		super.visitLineNumber(line, start);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		log.logf("visitLocalVariable(name=%s, desc=%s, signature=%s, start=%s, end=%s, index=%d)%n",
				JavaUtil.decodeDescriptor(name, desc), desc, signature, start, end, index);
		// TODO
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
			int[] index, String desc, boolean visible) {
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		// TODO
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
		IRI classId = JavaFacts.Types.object(parent.getModel(), owner);
		IRI methodId = JavaFacts.method(parent.getModel(), classId, name, desc);
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
				// TODO
				// parent.put(parent.getModel().literal("ACTUAL_ARGUMENT_TYPE" + i),
				// JavaUtil.frameTypeToId(type));
			}
		}

		putInstruction(opcode, JavaFacts.P_OPERAND, methodId);
		nextInstruction();
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		log.logf("visitMultiANewArrayInsn(desc=%s, dims=%d)%n", desc, dims);
		putInstruction(Opcodes.MULTIANEWARRAY, JavaFacts.P_TYPE, //
				JavaFacts.Types.array(parent.getModel(), dims, //
						JavaFacts.Types.object(parent.getModel(), desc)));
		nextInstruction();
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitParameter(String name, int access) {
		log.logf("visitParameter(name=%s, access=%d)%n", name, access);
		BlankNodeOrIRI paramId = parent.getModel().node(currentMethodId, name);
		parent.put(currentMethodId, JavaFacts.PARAMETER, paramId);
		parent.addFieldAccessFlags(access, paramId);

		super.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		// TODO
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		// TODO
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		putInstruction(opcode, JavaFacts.P_OPERAND, JavaFacts.Types.object(parent.getModel(), type));
		nextInstruction();
		super.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		log.logf("visitVarInsn(opcode=%s, var=%d)%n", opcode, var);
		if (opcode == Opcodes.RET) {
			putInstruction(opcode);
			parent.getModel().add(currentInsn, JavaFacts.P_NEXT, JavaFacts.R_END);
			insnChain = null;
			nextInstruction();
		} else {
			putInstruction(opcode, JavaFacts.P_VAR, var);
			nextInstruction();
			super.visitVarInsn(opcode, var);
		}
	}
}