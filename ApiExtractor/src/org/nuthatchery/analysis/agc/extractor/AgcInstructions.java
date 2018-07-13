package org.nuthatchery.analysis.agc.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.nuthatchery.ontology.basic.CommonVocabulary;


public class AgcInstructions {
	public static final String AGC = "https://model.nuthatchery.org/agc/block2/";
	public static final OntModel AGC_MODEL = ModelFactory.createOntologyModel();
	public static final Resource K = AGC_MODEL.createResource(AGC + "K");
	public static final Resource KC = AGC_MODEL.createResource(AGC + "KC");
	public static final Resource ADDR_K10 = AGC_MODEL.createResource(AGC + "address/10");
	public static final Resource ADDR_K12 = AGC_MODEL.createResource(AGC + "address/12");
	public static final Resource ADDR_K12_FIXED = AGC_MODEL.createResource(AGC + "address/12/fixed");
	public static final Resource ADDR_KC = AGC_MODEL.createResource(AGC + "address/9/io");
	public static final Resource MEM_ANY = AGC_MODEL.createResource(AGC + "memAny");
	public static final Resource MEM_FIXED = AGC_MODEL.createResource(AGC + "memFixed");
	public static final Resource MEM_ERASABLE = AGC_MODEL.createResource(AGC + "memErasable");
	public static final Property P_PARAM_NAME = AGC_MODEL.createProperty(AGC + "paramName");
	public static final Property P_PARAM_TYPE = AGC_MODEL.createProperty(AGC + "paramType");
	public static final Property P_MEM_AREA = AGC_MODEL.createProperty(AGC + "memArea");
	public static final Property P_OPERAND = AGC_MODEL.createProperty(AGC + "operand");
	public static final Property P_CALL = AGC_MODEL.createProperty(AGC + "call");
	public static final OntClass C_INSTRUCTION = AGC_MODEL.createClass(AGC + "instruction");
	public static final OntClass C_BASIC = AGC_MODEL.createClass(AGC + "basicInsn");
	public static final OntClass C_EXTRA = AGC_MODEL.createClass(AGC + "extraInsn");
	public static final Property P_OPCODE_BASE = AGC_MODEL.createProperty(AGC + "opCode");
	public static final Property P_MOD1 = AGC_MODEL.createProperty(AGC + "mod1");
	public static final Property P_MOD2 = AGC_MODEL.createProperty(AGC + "mod2");
	public static final Property P_CALL_PSEUDO = AGC_MODEL.createProperty(AGC + "pseudoCall");
	public static final Property P_LABEL = AGC_MODEL.createProperty(AGC + "label");
	public static final Property P_CODE = AGC_MODEL.createProperty(AGC + "code");
	public static final Property P_COMMENT = AGC_MODEL.createProperty(AGC + "comment");

	public static final Map<String, Instruction> opCodeMap = new HashMap<>();
	public static final List<Instruction> instructions = new ArrayList<>();
	static {
		// insnDesc("Mnemonic", "Octal", "Extracode", "Address Constraint", "Brief
		// Description");
		insnDesc("TC K", "00000 + K", false, ADDR_K12, "Call subroutine `K`.");
		insnDesc("TCR K", "00000 + K", false, ADDR_K12, "Call subroutine `K`.").sameAs("TC", "K");
		insnDesc("XXALQ", "00000", false, null, "").sameAs("TC", 0);
		insnDesc("XLQ", "00001", false, null, "").sameAs("TC", 1);
		insnDesc("RETURN", "00002", false, null, "").sameAs("TC", 2);
		insnDesc("RELINT", "00003", false, null, "Enable interrupts.").sameAs("TC", 3);
		insnDesc("INHINT", "00004", false, null, "Disable interrupts.").sameAs(
				"TC",
				4);
		insnDesc("EXTEND", "00006", false, null,
				"Interpret the *next* instruction as Extracode.").sameAs("TC", 6);
		insnDesc("CCS K", "10000 + K", false, ADDR_K10, "Count, compare, and skip.");
		insnDesc("TCF K", "10000 + K", false, ADDR_K12_FIXED,
				"Jump to address `K`.");
		insnDesc("NOOP", "10001 + Z", false, null, MEM_FIXED,
				"No-op.");
		insnDesc("DAS K", "20001 + K", false, ADDR_K10, "Double-precision integer addition.");
		insnDesc("DDOUBL", "20001", false, null, "Double the `(A,L)` register pair.").sameAs("DAS", 0);
		insnDesc("LXCH K", "22000 + K", false, ADDR_K10, "Exchange contents of `L` and `K`.");
		insnDesc("ZL", "22007", false, null, "").sameAs("LXCH", 6);
		insnDesc("INCR K", "24000 + K", false, ADDR_K10, "Increment value stored at address `K`.");
		insnDesc("ADS K", "26000 + K", false, ADDR_K10, "Add contents of `A` to value at address `K`.");
		insnDesc("CA K", "30000 + K", false, ADDR_K12, "Copy `K` to `A`.");
		insnDesc("CAF K", "30000 + K", false, ADDR_K12, MEM_FIXED, "Copy `K` to `A`.")
		.sameAs("CA", "K");
		insnDesc("CAE K", "30000 + K", false, ADDR_K12, MEM_ERASABLE, "Copy `K` to `A`.")
		.sameAs("CA", "K");
		insnDesc("NOOP", "30000", false, null, MEM_ERASABLE, "No-op.").sameAs("CA", 0);
		insnDesc("CS K", "40000 + K", false, ADDR_K12, "Copy -`K` to `A`.");
		insnDesc("COM", "40000", false, null, "").sameAs("CS", 0);
		insnDesc("INDEX K", "50000 + K", false, ADDR_K10,
				"Indexes the *next* instruction.  (I.e., adds the contents of `K` to the next instruction before executing it, but without altering the memory location at which the next instruction is stored.)");
		insnDesc("RESUME", "50017", false, null,
				"Resume interrupted program.  (The contents of `ZRUPT` are loaded into the program counter, and the contents of `BRUPT` are executed as if they were the instruction actually stored at the new program counter.)")
		.sameAs("INDEX", 017);
		insnDesc("DXCH K", "52001 + K", false, ADDR_K10,
				"Double-precision exchange of the contents of `K`,`K+1` with `A,L`.");
		insnDesc("DTCF", "52005", false, null, "").sameAs("DXCH", 4);
		insnDesc("DTCB", "52006", false, null, "").sameAs("DXCH", 5);
		insnDesc("TS K", "54000 + K", false, ADDR_K10, "Copy the contents of `A` to location `K`.");
		insnDesc("OVSK", "54000", false, null, "").sameAs("TS", 0);
		insnDesc("TCAA", "54005", false, null, "").sameAs("TS", 5);
		insnDesc("XCH K", "56000 + K", false, ADDR_K10, "Exchange the contents of `A` and location `K`.");
		insnDesc("AD K", "60000 + K", false, ADDR_K12, "Add the contents of location `K` to `A`.");
		insnDesc("DOUBLE", "60000", false, null, "Doubles the `A` register.").sameAs("AD", 0);
		insnDesc("MASK K", "70000 + K", false, ADDR_K12, "Bitwise boolean AND the contents of `K` to `A`.");
		insnDesc("READ KC", "00000 + KC", true, ADDR_KC, "Read i/o channel `KC` to `A`.");
		insnDesc("WRITE KC", "01000 + KC", true, ADDR_KC, "Write `A` to i/o channel `KC`.");
		insnDesc("RAND KC", "02000 + KC", true, ADDR_KC,
				"Read i/o channel `KC` and bitwise boolean AND it to `A`.");
		insnDesc("WAND KC", "03000 + KC", true, ADDR_KC,
				"Read i/o channel `KC` and bitwise boolean AND it to `A`, then write it to i/o channel `KC`.");
		insnDesc("ROR KC", "04000 + KC", true, ADDR_KC,
				"Read i/o channel `KC` and bitwise boolean OR it to `A`.");
		insnDesc("WOR KC", "05000 + KC", true, ADDR_KC,
				"Read i/o channel `KC` and bitwise boolean OR it to `A`, then write it to i/o channel `KC`.");
		insnDesc("RXOR KC", "06000 + KC", true, ADDR_KC,
				"Read i/o channel `KC` and bitwise boolean exclusive-OR it to `A`.");
		insnDesc("EDRUPT KC", "07000 + KC", true, ADDR_KC,
				"(For machine checkout only.  I'm not sure what it's supposed to do.)");
		insnDesc("DV K", "10000 + K", true, ADDR_K10,
				"Divide the double-precision integer value in `(A,L)` by the contents of `K`, putting the quotient in `A` and the remainder in `L`.");
		insnDesc("BZF K", "10000 + K", true, ADDR_K12_FIXED, "");
		insnDesc("MSU K", "20000 + K", true, ADDR_K10, "");
		insnDesc("QXCH K", "22000 + K", true, ADDR_K10, "");
		insnDesc("ZQ", "22007", true, null, "").sameAs("QXCH", 00007);
		insnDesc("AUG K", "24000 + K", true, ADDR_K10, "");
		insnDesc("DIM K", "26000 + K", true, ADDR_K10, "");
		insnDesc("DCA K", "30001 + K", true, ADDR_K12, "");
		insnDesc("DCS K", "40001 + K", true, ADDR_K12, "");
		insnDesc("DCOM", "40001", true, null, "").sameAs("DCS", 00000);
		insnDesc("INDEX K", "50000 + K", true, ADDR_K12, "");
		insnDesc("SU K", "60000 + K", true, ADDR_K10, "");
		insnDesc("BZMF K", "60000 + K", true, ADDR_K12_FIXED, "");
		insnDesc("MP K", "70000 + K", true, ADDR_K12, "");
		insnDesc("SQUARE", "70000", true, null, "").sameAs("MP", 00000);


		instructions.addAll(opCodeMap.values());
		Collections.sort(instructions);

	}

	private static Instruction insnDesc(String name, String code, boolean extra, Resource arg, String desc) {
		return insnDesc(name, code, extra, arg, MEM_ANY, desc);
	}

	private static Instruction insnDesc(String name, String code, boolean extra, Resource arg, Resource mem,
			String desc) {
		String[] split = name.split(" ");
		if (split.length < 1 || split.length > 2)
			throw new IllegalArgumentException(name);
		name = split[0];
		String a = split.length == 2 ? split[1] : null;
		split = code.split(" *\\+ *");
		int opCode = Integer.parseInt(split[0], 8);
		if (split.length == 1 && a == null) {
			// ok
		} else if (split.length == 2 && Objects.equals(a, split[1])) {
			// ok
		} else if (split.length == 2 && Objects.equals("Z", split[1])) {
			// ok
		} else
			throw new IllegalArgumentException(name);
		if (a != null && arg == null)
			throw new IllegalArgumentException();
		Instruction op = new Instruction(name, a, arg, opCode, extra, mem, desc);
		opCodeMap.put(name, op);
		return op;
	}


	static class Instruction implements Comparable<Instruction> {
		public final String name;
		public final String paramName;
		public final Resource paramType;
		public final int opCode;
		public final boolean extraCode;
		public final Resource memArea;
		public final String desc;
		public Instruction sameAs;
		public RDFNode sameAsArg;
		Resource node;

		public Instruction(String name, String paramName, Resource arg, int opCode, boolean extraCode, Resource mem,
				String desc) {
			super();
			this.name = name;
			this.paramName = paramName;
			this.paramType = arg;
			this.opCode = opCode;
			this.extraCode = extraCode;
			this.memArea = mem;
			this.desc = desc;
			this.node = AGC_MODEL.createResource(AGC + name);
			AGC_MODEL.add(node, CommonVocabulary.P_NAME, AGC_MODEL.createTypedLiteral(name));
			if (extraCode) {
				AGC_MODEL.add(node, RDF.type, C_BASIC);
			} else {
				AGC_MODEL.add(node, RDF.type, C_EXTRA);
			}
			if (paramName != null) {
				AGC_MODEL.add(node, P_PARAM_NAME, AGC_MODEL.createTypedLiteral(paramName));
			}
			if (paramType != null) {
				AGC_MODEL.add(node, P_PARAM_TYPE, paramType);
			}
			AGC_MODEL.add(node, P_OPCODE_BASE, AGC_MODEL.createTypedLiteral(opCode));
			AGC_MODEL.add(node, P_MEM_AREA, mem);
			AGC_MODEL.add(node, CommonVocabulary.P_SHORT_DESC, AGC_MODEL.createTypedLiteral(desc));
		}

		@Override
		public String toString() {
			String op = name;
			String offset = "";
			if (paramName != null) {
				op += " " + paramName;
				offset += " + " + paramName;
			}
			return String.format("%-10s %s%05o%-5s   %s %s", op, extraCode ? "*" : " ", opCode, offset,
					sameAs != null ? sameAs.name : "",
							sameAsArg != null ? sameAsArg.toString() : "");
		}

		private Instruction sameAs() {
			Resource blank = AGC_MODEL.createResource();
			AGC_MODEL.add(node, P_CALL, blank);
			AGC_MODEL.add(blank, P_CALL, sameAs.node);
			if (sameAsArg != null) {
				AGC_MODEL.add(blank, P_OPERAND, sameAsArg);
			}
			return this;
		}

		public Instruction sameAs(String name, int arg) {
			sameAs = opCodeMap.get(name);
			sameAsArg = AGC_MODEL.createTypedLiteral(arg);
			return sameAs();
		}

		public Instruction sameAs(String name, String arg) {
			sameAs = opCodeMap.get(name);
			sameAsArg = AGC_MODEL.createTypedLiteral(arg);
			return sameAs();
		}

		public Instruction sameAs(String name) {
			sameAs = opCodeMap.get(name);
			sameAsArg = null;
			return sameAs();
		}

		@Override
		public int compareTo(Instruction o) {
			int r = Integer.compare(opCode, o.opCode);
			if (r == 0) {
				r = Boolean.compare(extraCode, o.extraCode);
			}
			if (r == 0) {
				r = Boolean.compare(sameAs != null, o.sameAs != null);
			}
			if (r == 0 && sameAsArg != null && o.sameAsArg != null) {
				r = sameAsArg.toString().compareTo(o.sameAsArg.toString());
			}
			return r;
		}
	}

	static class InstructionUse {
		public final Instruction opcode;
		public final Resource id;
		public String operand = null;
		public String mod1 = null;
		public String mod2 = null;

		public InstructionUse(Instruction opcode, Resource id) {
			super();
			this.opcode = opcode;
			this.id = id;
		}

	}

}
