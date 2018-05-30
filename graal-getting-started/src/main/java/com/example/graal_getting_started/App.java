package com.example.graal_getting_started;

import java.io.File;
import java.util.Scanner;

import org.eclipse.rdf4j.rio.RDFFormat;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.api.core.mapper.Mapper;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.common.rdf4j.RDFTypeAtomMapper;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.mapper.AbstractMapper;
import fr.lirmm.graphik.graal.core.mapper.MapperAtomConverter;
import fr.lirmm.graphik.graal.io.rdf.RDFParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;
import fr.lirmm.graphik.util.stream.converter.ConverterIterator;

public class App {
	public static void main(String[] args) throws Exception {
		KBBuilder kbb = new KBBuilder();
		kbb.setStore(new DefaultInMemoryGraphStore()); // RDF4jStore(new SailRepository(new MemoryStore())));
		RDFParser rdfParser = new RDFParser(new File("/tmp/data.trig"), RDFFormat.TRIG);
		kbb.addAtoms(rdfParser, new AbstractMapper() {
			@Override
			public Atom map(Atom arg0) {
				System.err.println(arg0);
				return RDFTypeAtomMapper.instance().map(arg0);
			}

			@Override
			public Atom unmap(Atom arg0) {
				return RDFTypeAtomMapper.instance().unmap(arg0);
			}

			@Override
			public Predicate map(Predicate arg0) {
				return arg0;
			}

			@Override
			public Predicate unmap(Predicate arg0) {
				return arg0;
			}
		});
		/*
		 * while(rdfParser.hasNext()) { Atom atom = (Atom) rdfParser.next();
		 * kbb.add(atom); } rdfParser.close();
		 */

		kbb.addAll(new DlgpParser(""//
				+ "@prefix j: <http://model.nuthatchery.org/java/> " //
				+ "@prefix jvm: <http://model.nuthatchery.org/java/>\n" //
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				// + "@una\n"
				//+ "j:next(X,Y) :- j:nextIfTrue(X,Y)." + "j:next(X,Y) :- j:nextIfFalse(X,Y)."
				+ "hasInsn(M,I) :- j:code(M,L), listHasInsn(L,I)." //
				+ "listHasInsn(L,I) :- rdf:first(L,I)." //
				+ "listHasInsn(L,I) :- rdf:rest(L,L2), listHasInsn(L2,I)." //
				// + "executes(M,J) :- hasInsn(M,I), j:call(I,J)."
				+ "isInvoke(I) :- j:call(I,jvm:invokevirtual)." + "isInvoke(I) :- j:call(I,jvm:invokestatic)."
				+ "isInvoke(I) :- j:call(I,jvm:invokeinterface)." + "isInvoke(I) :- j:call(I,jvm:invokespecial)."
				+ "invokes(Method,Insn,Target) :- hasInsn(Method,Insn), isInvoke(Insn), j:memberOperand(Insn,Target)."
				+ "invokesT(Method,Target) :- invokes(Method,X,Target)."
				+ "invokesT(Method,Target) :- invokes(Method,X,Y), invokes(Y,Target)."
				+ "isRecursive(M) :- invokesT(M,M)."
				// + "hasMemberOperand(M,X) :- hasInsn(M,I), j:memberOperand(I,X)."
				// + "executesA(M,J,O) :- executes(M,I,J), j:memberOperand(I,O)."
				));
		KnowledgeBase kb = kbb.build();
		System.out.println(kb.getFacts().toString().replaceAll(" ", "\n"));
		for (String name : kb.getRuleNames()) {
			System.out.println(kb.getRule(name));
		}

		query(kb, "?(A,X,B) :- invokes(A,I,B), j:call(I,X).");
		query(kb, "?(M) :- isRecursive(M).");

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("?");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.equals("quit") || line.equals("exit")) {
					System.exit(0);
				}
				query(kb, "?" + line);
				System.out.print("?");
			}
		}
		// SparqlConjunctiveQueryParser sp = new SparqlConjunctiveQueryParser(
		// "PREFIX j: <http://model.nuthatchery.org/java/> "
		// + "PREFIX jType: <http://model.nuthatchery.org/java/types> "
		// + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
		// + "SELECT ?x ?y "
		// + "WHERE "
		// + "{ "
		// + "?x j:code / j:next ?z ."
		// + "?z j:call ?y ."
		// // + "?x <jf:calls?jf:virtual> ?y "
		// + "}"
		// );
		/*
		 * made transitive by adding star: modeling chapter, pg 59
		 * SparqlConjunctiveQueryParser sp = new SparqlConjunctiveQueryParser(
		 * "PREFIX jf: <http://nuthatchery.org/javaFacts/> " +
		 * "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x ?y " +
		 * "WHERE " + "{ " + "?x <jf:calls?jf:virtual>* ?y " + "}" );
		 */
		// ConjunctiveQuery query = sp.getConjunctiveQuery();

		// df.addQuery(cq);
		// System.out.println(df.query(cq));
		// df.homomorphism(cq);
		// df.addQuery()

		// DlgpWriter dwriter = new DlgpWriter();
		// ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- "
		// + " <Professor>(X), "
		// + " worksFor(X, <http://www.Department0.University0.edu>), "
		// + " name(X, Y1), "
		// + " emailAddress(X, Y2).");
		// dwriter.write(query);
		// dwriter.write("\n= Answers =\n");

		// 2 - Add a fact
		// kbb.add(DlgpParser.parseAtom("human(socrate)."));
		// 3 - Generate the KB
		// KnowledgeBase kb = kbb.build();
		// 4 - Create a DLGP writer to print data
		// DlgpWriter writer = new DlgpWriter();
		// // 5 - Parse a query from a Java String
		// ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- mortal(X).");
		// // 6 - Query the KB
		// CloseableIterator resultIterator = kb.query(query);
		// // 7 - Iterate and print results
		// writer.write("\n= Answers =\n");
		// if (resultIterator.hasNext()) {
		// do {
		// writer.write(resultIterator.next());
		// writer.write("\n");
		// } while (resultIterator.hasNext());
		// } else {
		// writer.write("No answers.\n");
		// }
		// // 8 - Close resources
		// kb.close();
		// writer.close();
	}

	public static void query(KnowledgeBase kb, String q) {
		try {
			ConjunctiveQuery query = DlgpParser.parseQuery(//
					"@prefix j: <http://model.nuthatchery.org/java/> \n"//
					+ "@prefix jvm: <http://model.nuthatchery.org/java/>\n" //
					+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" //
					+ q);
			CloseableIterator<Substitution> results = kb.query(query);
			System.out.println(q);
			int i = 1;
			if (results.hasNext()) {
				do {
					Substitution next = results.next();
					System.out.print(i++ + ": ");
					for (Variable t : next.getTerms()) {
						System.out.println("\t" + t + " = " + next.createImageOf(t));
					}
				} while (results.hasNext());
			} else {
				System.out.println("No answers.\n");
			}
		} catch (IteratorException | KnowledgeBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
