package org.nuthatchery.analysis.java.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.Model.ListBuilder;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.nuthatchery.ontology.standard.RdfVocabulary;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.AnalyzerAdapter;

class MethodFactExtractor extends AnalyzerAdapter {
	private class VarUsage {
		int lastLabel;
		int varNum;
		BlankNodeOrIRI varId;
	}

	private static int count = 0;
	private final IRI currentMethodId;
	private ListBuilder insnList;
	private BlankNodeOrIRI currentInsn;
	private ILogger log;
	private final Model model;
	private final List<VarUsage> localVarUse = new ArrayList<>();
	private final List<Label> labels = new ArrayList<>();
	/**
	 *
	 */
	private final ClassFactExtractor parent;
	private int startLine;

	public MethodFactExtractor(ClassFactExtractor parent, IRI methodId, int access, String name, String desc,
			ILogger log) {
		super(Opcodes.ASM6, parent.className, access, name, desc, null);
		this.parent = parent;
		this.currentMethodId = methodId;
		this.log = log;
		this.model = parent.getModel();
		parent.currentLine = -1;
	}

	protected void putInstruction(int opcode, Object... args) {
		if (args.length % 2 == 1)
			throw new IllegalArgumentException();

		insnList.add(currentInsn);
		model.add(currentInsn, JavaFacts.P_CALL, JavaFacts.opcode(opcode));
		for (int i = 0; i < args.length; i += 2) {
			if (args[i] instanceof IRI) {
				IRI pred = (IRI) args[i];
				RDFTerm obj;
				if (args[i + 1] instanceof RDFTerm) {
					obj = (RDFTerm) args[i + 1];
				} else {
					obj = model.literal(args[i + 1]);
				}
				model.add(currentInsn, pred, obj);
			} else
				throw new IllegalArgumentException();
		}
		currentInsn = model.blank(String.valueOf(count++));
	}

	/**
	 * Get an argument from the stack, with 0 referring to the top of the stack
	 *
	 * @param argNum
	 * @return The argument, or null if not found
	 */
	protected Object stackFind(int argNum) {
		if (stack == null)
			return null;

		int i = stack.size() - 1;
		for (; i >= 0; i--, argNum--) {
			while (stack.get(i) == Opcodes.TOP) { // top half of a two-word
				// piece of data
				i--;
			}
			if (argNum == 0)
				return stack.get(i);
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
		if (o instanceof Label)
			return uninitializedTypes.get(o);
		else
			return o;
	}

	/**
	 * Get an argument from the stack, with 0 referring to the top of the stack
	 *
	 * @param argNum
	 * @return The argument, or null if not found
	 */
	protected Object stackGetType(int argNum, String dflt) {
		Object o = stackFind(argNum);
		if (o instanceof Label)
			return uninitializedTypes.get(o);
		else if (o == null)
			return dflt;
		else
			return o;
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
		log.warnf("unimplemented: MethodFactExtractor.visitAnnotation(desc=%s,visible=%b)%n", desc, visible);
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		log.warnf("unimplemented: MethodFactExtractor.visitAnnotationDefault()%n");
		return super.visitAnnotationDefault();
	}

	@Override
	public void visitAttribute(Attribute attr) {
		log.warnf("unimplemented: visitAttribue(attr=%s)%n", attr);
		super.visitAttribute(attr);
	}

	@Override
	public void visitCode() {
		log.log("{");
		parent.currentLine = -1;
		currentInsn = model.blank();
		insnList = model.list();
		super.visitCode();
	}

	@Override
	public void visitEnd() {
		if (startLine != -1 && parent.currentLine != -1) {
			BlankNode start = model.blank();
			model.add(start, JavaFacts.P_LINE, model.literal(startLine));
			BlankNode end = model.blank();
			model.add(end, JavaFacts.P_LINE, model.literal(parent.currentLine));
			model.add(currentMethodId, JavaFacts.P_SRC_START, start);
			model.add(currentMethodId, JavaFacts.P_SRC_END, end);
		}
		parent.currentLine = -1;
		log.log("} // end of " + currentMethodId);
		if (insnList != null) {
			model.add(currentMethodId, JavaFacts.P_CODE, insnList.build());
		}
		insnList = null;
		super.visitEnd();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		log.log("# access field: " + JavaUtil.decodeDescriptor(owner, name, desc));
		BlankNodeOrIRI memberId = parent.getMemberId(owner, name, desc);
		putInstruction(opcode, JavaFacts.P_OPERAND_MEMBER, memberId);
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		putInstruction(Opcodes.IINC, JavaFacts.P_OPERAND_VAR, var, JavaFacts.P_OPERAND_INT, increment);
		super.visitIincInsn(var, increment);
	}

	@Override
	public void visitInsn(int opcode) {
		putInstruction(opcode);
		super.visitInsn(opcode);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.warnf(
				"unimplemented: MethodFactExtractor.visitInsnAnnotation(typeRef=%d, typePath=%s, desc=%s, visible=%b)%n", //
				typeRef, typePath.toString(), desc, visible);
		return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		putInstruction(opcode, JavaFacts.P_OPERAND_INT, operand);
		super.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		log.log("INVOKEDYNAMIC " + name + " [" + desc + "]" + " " + bsm + " " + Arrays.toString(bsmArgs));
		putInstruction(Opcodes.INVOKEDYNAMIC, JavaFacts.P_OPERAND_MEMBER, parent.getMemberName(name, desc));
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		BlankNode target = model.blank(label.toString());
		putInstruction(opcode, JavaFacts.P_OPERAND_LABEL, target);
		if (opcode == Opcodes.JSR)
			return;
		super.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLabel(Label label) {
		log.log(label + ":");
		currentInsn = model.blank(label.toString());
		labels.add(label);
		super.visitLabel(label);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		RDFTerm operand;
		if (cst instanceof Type) {
			operand = JavaUtil.typeToId(model, (Type) cst);
		} else {
			operand = model.literal(cst);
		}
		putInstruction(Opcodes.LDC, JavaFacts.P_OPERAND_CONSTANT, operand);
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		log.log(parent.currentSource.peek() + ":" + line + "\tlabel=" + start);
		if (parent.currentLine == -1) {
			startLine = line;
		}
		parent.currentLine = line;
		currentInsn = model.blank(start.toString());
		model.add(currentInsn, JavaFacts.P_LINE, model.literal(line));
		super.visitLineNumber(line, start);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		log.logf("visitLocalVariable(name=%s, desc=%s, signature=%s, start=%s, end=%s, index=%d)%n",
				JavaUtil.decodeDescriptor(name, desc), desc, signature, start, end, index);
		int startIndex = labels.indexOf(start);
		int endIndex = labels.indexOf(end);
		if (startIndex < 0 || endIndex < 0)
			throw new IllegalStateException("wrong labels!");
		for (VarUsage vu : localVarUse) {
			if (vu.varNum == index && startIndex <= vu.lastLabel && endIndex > vu.lastLabel) {
				model.add(vu.varId, CommonVocabulary.P_NAME, model.literal(name));
				model.add(vu.varId, JavaFacts.P_TYPE, //
						JavaUtil.typeToId(model, Type.getType(desc)));
				model.add(vu.varId, CommonVocabulary.P_IDNAME, //
						model.literal(name + ":" + desc + (signature == null ? "" : signature)));
			}
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
			int[] index, String desc, boolean visible) {
		log.warnf(
				"unimplemented: MethodFactExtractor.visitInsnAnnotation(typeRef=%d, typePath=%s, start=%s, end=%s, desc=%s, visible=%b)%n", //
				typeRef, typePath.toString(), Arrays.toString(start), Arrays.toString(end), desc, visible);
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		ListBuilder list = model.list();
		for (int i = 0; i < keys.length; i++) {
			BlankNode node = model.blank();
			model.add(node, JavaFacts.P_OPERAND_INT, model.literal(keys[i]));
			model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(labels[i].toString()));
			list.add(node);
		}
		putInstruction(Opcodes.LOOKUPSWITCH, JavaFacts.P_OPERAND_LIST, list.build());

		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		log.logf("visitMaxs(maxStack=%d, maxLocals=%d)%n", //
				maxStack, maxLocals);
		model.add(currentMethodId, JavaFacts.P_MAX_STACK, model.literal(maxStack));
		model.add(currentMethodId, JavaFacts.P_MAX_LOCALS, model.literal(maxLocals));
		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		IRI classId = JavaFacts.Types.object(model, owner);
		IRI methodId = JavaFacts.method(model, classId, name, desc);
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
			log.log("# INVOKE: " + owner + "." + name + ":" + desc);
			log.log("# INVOKE METHOD: " + JavaUtil.decodeDescriptor(owner, name, desc));
			log.logf("  Environment: argSize=%d, retSize=%d", argSize, retSize);
			log.log("   * Stack: " + stackTypes + " <- top of stack");
			log.log("   * Locals: "
					+ super.locals.stream().map(JavaUtil::frameTypeToString).collect(Collectors.toList()));
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
				// model.add(model.literal("ACTUAL_ARGUMENT_TYPE" + i),
				// JavaUtil.frameTypeToId(type));
			}
		}

		putInstruction(opcode, JavaFacts.P_OPERAND_MEMBER, methodId);
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		log.logf("visitMultiANewArrayInsn(desc=%s, dims=%d)%n", desc, dims);
		putInstruction(Opcodes.MULTIANEWARRAY, JavaFacts.P_OPERAND_TYPE, //
				JavaFacts.Types.array(model, dims, //
						JavaFacts.Types.object(model, desc)));
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitParameter(String name, int access) {
		log.logf("visitParameter(name=%s, access=%d)%n", name, access);
		BlankNodeOrIRI paramId = model.node(currentMethodId, name);
		model.add(currentMethodId, JavaFacts.PARAMETER, paramId);
		parent.addFieldAccessFlags(access, paramId);

		super.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		log.warnf(
				"unimplemented: MethodFactExtractor.visitParameterAnnotation(typeRef=%d, typePath=%s, desc=%s, visible=%b)%n", //
				parameter, desc, visible);
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		ListBuilder list = model.list();

		BlankNode node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_INT, model.literal(min));
		list.add(node);

		node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_INT, model.literal(max));
		list.add(node);

		node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(dflt.toString()));
		list.add(node);

		for (Label label : labels) {
			node = model.blank();
			model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(label.toString()));
			list.add(node);
		}
		putInstruction(Opcodes.TABLESWITCH, JavaFacts.P_OPERAND_LIST, list.build());

		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.warnf(
				"unimplemented: MethodFactExtractor.visitTryCatchAnnotation(typeRef=%d, typePath=%s, desc=%s, visible=%b)%n", //
				typeRef, typePath.toString(), desc, visible);
		return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		ListBuilder list = model.list();

		BlankNode node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(start.toString()));
		list.add(node);

		node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(end.toString()));
		list.add(node);

		node = model.blank();
		model.add(node, JavaFacts.P_OPERAND_LABEL, model.blank(handler.toString()));
		model.add(node, JavaFacts.P_OPERAND_TYPE, JavaFacts.Types.object(model, type));
		list.add(node);

		model.add(currentMethodId, JavaFacts.P_TRY_CATCH_BLOCK, list.build());

		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		log.warnf(
				"unimplemented: MethodFactExtractor.visitTypeAnnotation(typeRef=%d, typePath=%s, desc=%s, visible=%b)%n", //
				typeRef, typePath.toString(), desc, visible);
		// TODO
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		putInstruction(opcode, JavaFacts.P_OPERAND_TYPE, JavaFacts.Types.object(model, type));
		super.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		VarUsage vu = new VarUsage();
		vu.lastLabel = labels.size() - 1;
		vu.varNum = var;
		vu.varId = model.blank();
		model.add(vu.varId, RdfVocabulary.RDF_VALUE, model.literal(var));
		if (super.locals != null && super.locals.size() > var) {
			model.add(vu.varId, JavaFacts.P_TYPE, JavaUtil.frameTypeToId(model, super.locals.get(var)));
		}
		localVarUse.add(vu);
		log.logf("visitVarInsn(opcode=%s, var=%d)%n", opcode, var);

		putInstruction(opcode, JavaFacts.P_OPERAND_VAR, vu.varId);
		if (opcode != Opcodes.RET) {
			super.visitVarInsn(opcode, var);
		}
	}
}