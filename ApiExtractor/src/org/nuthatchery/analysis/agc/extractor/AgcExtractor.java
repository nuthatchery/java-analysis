package org.nuthatchery.analysis.agc.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.nuthatchery.analysis.agc.extractor.AgcInstructions.Instruction;
import org.nuthatchery.analysis.agc.extractor.AgcInstructions.InstructionUse;
import org.nuthatchery.analysis.java.extractor.ExtractApi;
import org.nuthatchery.analysis.java.extractor.JavaUtil.ILogger;
import org.nuthatchery.analysis.java.extractor.ListBuilder;
import org.nuthatchery.ontology.basic.CommonVocabulary;


public class AgcExtractor {
	public static final List<String> registers = Arrays.asList("A", "L", "Q", "EB", "FB", "Z", "BB", "=0", //
			"ARUPT", "LRUPT", "QRUPT", "SAMPTIME1", "SAMPTIME2", "ZRUPT", //
			"BBRUPT", "BRUPT", //
			"CYR", "SR", "CYL", "EDOP", "TIME2", "TIME1", "TIME3", "TIME4", //
			"TIME5", "TIME6", "CDUX", "CDUY", "CDUZ", "OPTY", "OPTX", //
			"PIPAX", "PIPAY", "PIPAZ", //
			"Q-RHCCTR", "P-RHCCTR", "R-RHCCTR", //
			"INLINK", "RNRAD", "GYROCTR", //
			"CDUXCMD", "CDUYCMD", "CDUZCMD", //
			"OPTYCMD", "OPTXCMD", "THRUST", "LEMONM", "OUTLINK", "ALTM");
	private Set<String> labelsUsed = new HashSet<>();
	private Set<String> labelsDefined = new HashSet<>();
	private final BufferedReader reader;
	private final ILogger log;
	private final Model model;
	private int lineNum = 1;
	private int lineInPage = 1;
	private int pageNum = 1;
	private int column = 1, startColumn = -1;
	private String fileName = null;
	private String currentLine;
	private String instr = null;
	private String operand = null;
	private String mod1 = null;
	private String mod2 = null;
	private String label = null;
	private String falseLabel = null;
	private boolean header = true;
	private String headerText = "";
	/**
	 * 0 = BOL, 1 =
	 */
	private int mode = 0;
	private Resource node;
	private ListBuilder list;
	private String dir;
	private boolean comment;

	public AgcExtractor(Model model, ILogger logger, String dir, String fileName) throws FileNotFoundException {
		this.dir = dir;
		File file = new File(dir, fileName);
		this.fileName = fileName;
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		this.log = logger;
		this.model = model;
	}

	public void extract() throws IOException {
		StringBuilder b = new StringBuilder(80);
		lineNum = 1;
		list = new ListBuilder(model);
		header = true;
		headerText = "";
		while ((currentLine = reader.readLine()) != null) {
			column = 1;
			if (currentLine.startsWith("$")) {
				String file = currentLine.substring(1, currentLine.length());
				file = file.split(" |\t")[0];
				AgcExtractor subEx = new AgcExtractor(model, log, dir, file);
				subEx.pageNum = pageNum;
				subEx.lineInPage = lineInPage;
				subEx.labelsUsed = labelsUsed;
				subEx.labelsDefined = labelsDefined;
				subEx.extract();
				pageNum = subEx.pageNum;
				lineInPage = subEx.lineInPage;
				Resource blank = model.createResource();
				model.add(blank, AgcInstructions.P_CODE, model.createResource(file));
				list.add(blank);
			} else {
				visitLineStart();
				currentLine.chars().forEachOrdered((i) -> {
					if (comment) {
						b.appendCodePoint(i);
						column++;
					} else if (i == '\t' || i == ' ' || i == '#') {
						if (b.length() > 0) {
							visitToken(b.toString(), startColumn);
							b.delete(0, b.length());
						}
						column++;
						if (i == '\t') {
							column = ((column + 8) / 8) * 8 + 1;
							header = false;
						} else if (i == '#') {
							b.appendCodePoint(i);
							comment = true;
						} else {
							header = false;
						}
					} else {
						if (b.length() == 0) {
							startColumn = column;
						}
						b.appendCodePoint(i);
						header = false;
						++column;
					}
				});
				if (b.length() > 0) {
					if (comment) {
						visitComment(b.toString(), startColumn);
					} else {
						visitToken(b.toString(), startColumn);
					}
					b.delete(0, b.length());
				}
				visitLineEnd();
			}
			lineNum++;
			lineInPage++;
		}

		Resource code = list.build();
		model.add(model.createResource(fileName), AgcInstructions.P_CODE, code);
		model.add(model.createResource(fileName), AgcInstructions.P_COMMENT, model.createTypedLiteral(headerText));

	}

	private void visitComment(String s, int col) {
		PatternMatcher matcher = PatternMatcher.matcher() //
				.matchCase("^### FILE=\"([^\"]*)\"", (mr) -> {
					System.out.println("FILE: " + mr.group(1));
				}) //
				.matchCase("^## Page ([0-9]+)", (mr) -> {
					pageNum = Integer.parseInt(mr.group(1));
					lineInPage = 1;
				}) //
				.matchCase("^#+ ?(.*)", (mr) -> {
					if (header) {
						headerText += mr.group(1) + "\n";
					} else {
						model.add(node, AgcInstructions.P_COMMENT, model.createTypedLiteral(mr.group(1)));
					}
				})
				;

		if (!matcher.match(s))
			throw new IllegalArgumentException("Strange comment: " + currentLine);
	}

	public static void match(Pattern pat, String s, Consumer<MatchResult> f) {
		Matcher matcher = pat.matcher(s);
		if (matcher.matches()) {
			f.accept(matcher.toMatchResult());
		}
	}

	private void visitLineEnd() {
		list.add(node);
		// model.stream(node, null, null).forEachOrdered((triple) -> {
		// System.out.printf(" %s --%s-> %s%n", triple.getSubject(),
		// triple.getPredicate(), triple.getObject());
		// });
		System.out.println("---");
	}

	private void visitLineStart() {
		mode = 0;
		instr = null;
		operand = null;
		mod1 = null;
		mod2 = null;
		label = null;
		falseLabel = null;
		comment = false;
		node = model.createResource("p" + pageNum + "l" + lineInPage);

	}

	private void visitToken(String string, int col) {
		switch (mode++) {
		case 0:
			if (col == 1) { // it's a label
				label = string;
				labelsDefined.add(string);
				model.add(node, AgcInstructions.P_LABEL, model.createResource(label));
			} else if (col < 9) { // it's a false label
				falseLabel = string;
			} else {
				visitToken(string, col);
			}
			break;
		case 1: // it's an instruction
			instr = string;
			Instruction iDef = AgcInstructions.opCodeMap.get(string);
			if (iDef != null) {
				model.add(node, AgcInstructions.P_CALL, iDef.node);
			} else {
				model.add(node, AgcInstructions.P_CALL_PSEUDO, model.createTypedLiteral(instr));
			}

			break;
		case 2: // the operand
			if (registers.contains(string)) {
				model.add(node, AgcInstructions.P_OPERAND, AgcInstructions.AGC_MODEL.createResource("reg" + string));
			} else if (instr.equals("COUNT") || instr.equals("COUNT*")) {
				model.add(node, AgcInstructions.P_OPERAND, model.createTypedLiteral(string));
			} else {
				PatternMatcher pm = PatternMatcher.matcher()//
						.matchCase("^[-+]?[0-7]+$", (mr) -> {
							model.add(node, AgcInstructions.P_OPERAND, model.createTypedLiteral(string));
						}).matchCase("^[-+]?[0-9]+D$", (mr) -> {
							model.add(node, AgcInstructions.P_OPERAND, model.createTypedLiteral(string));
						}).matchCase("^[-+]?[0-9]*\\.[0-9]*", (mr) -> {
							model.add(node, AgcInstructions.P_OPERAND, model.createTypedLiteral(string));
						});
				if (!pm.match(string)) {
					model.add(node, AgcInstructions.P_OPERAND, model.createResource(string));
					labelsUsed.add(string);
					operand = string;
				}
			}

			break;
		case 3:
			mod1 = string;
			model.add(node, AgcInstructions.P_MOD1, model.createTypedLiteral(mod1));
			break;
		case 4:
			mod2 = string;
			model.add(node, AgcInstructions.P_MOD2, model.createTypedLiteral(mod2));
			break;
		default:
			System.err.println("ERROR: extra token '" + string + "' at line " + lineNum + " column " + col);
		}
	}

	public static void main(String[] args) {
		Dataset dataset = DatasetFactory.create();

		try {

			Model model = ModelFactory.createDefaultModel();
			dataset.addNamedModel("Comanche055/", model);
			AgcExtractor ex = new AgcExtractor(model, null,
					"/home/anya/git/virtualagc/Comanche055/", "MAIN.agc");
			ex.extract();
			for (String s : ex.labelsUsed) {
				if (!ex.labelsDefined.contains(s)) {
					System.out.println("Undefined label: " + s);
				}
			}
			try (OutputStream output = new FileOutputStream("/tmp/dataProg.trig")) {
				dataset.addNamedModel(AgcInstructions.AGC, AgcInstructions.AGC_MODEL);
				dataset.getDefaultModel().setNsPrefix("b2", AgcInstructions.AGC);
				dataset.getDefaultModel().setNsPrefix("nh", CommonVocabulary.NS);
				dataset.getDefaultModel().setNsPrefix("rdf", RDF.uri);
				dataset.getDefaultModel().setNsPrefix("rdfs", RDFS.uri);
				dataset.getDefaultModel().setNsPrefix("xsd", XSD.NS);
				RDFDataMgr.write(output, dataset, Lang.TRIG);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
