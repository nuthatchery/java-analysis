package com.example.graal_getting_started;

import java.io.File;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import fr.lirmm.graphik.graal.store.triplestore.rdf4j.RDF4jStore;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.rdf.RDFParser;
import fr.lirmm.graphik.graal.io.rdf.RDFWriter;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.DefaultKnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class App
{
	public static void main(String[] args) throws Exception {
		try(DefaultKnowledgeBase df = new DefaultKnowledgeBase(new RDF4jStore(new SailRepository(new MemoryStore())), new RDFParser(new File("data.n3"), RDFFormat.TURTLE))) {
			RDFWriter writer = new RDFWriter(System.out, RDFFormat.TURTLE);
			System.out.println(df.getFacts().toString().replaceAll(" ", "\n"));

			SparqlConjunctiveQueryParser sp = new SparqlConjunctiveQueryParser(
					"PREFIX jf: <http://nuthatchery.org/javaFacts/> "
							+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
							+ "SELECT ?x ?y "
							+ "WHERE "
							+ "{ "
							+ "?x <jf:calls?jf:virtual> ?y "
							+ "}"
					);
			/*made transitive by adding star: modeling chapter, pg 59
		SparqlConjunctiveQueryParser sp = new SparqlConjunctiveQueryParser(
				"PREFIX jf: <http://nuthatchery.org/javaFacts/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "SELECT ?x ?y "
				+ "WHERE "
				+ "{ "
				+ "?x <jf:calls?jf:virtual>* ?y "
				+ "}"
				);
			 */
			ConjunctiveQuery query = sp.getConjunctiveQuery();
			CloseableIterator<Substitution> results = df.query(query);
			if (results.hasNext()) {
				do {
					System.out.println(results.next());
				} while (results.hasNext());
			} else {
				System.out.println("No answers.\n");
			}

			//		df.addQuery(cq);
			//		System.out.println(df.query(cq));
			//		df.homomorphism(cq);
			//		df.addQuery()


			//		DlgpWriter dwriter = new DlgpWriter();
			//		 ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- "
			//			      + " <Professor>(X),                                         "
			//			      + " worksFor(X, <http://www.Department0.University0.edu>),  "
			//			      + " name(X, Y1),                                            "
			//			      + " emailAddress(X, Y2).");
			//		 dwriter.write(query);
			//		 dwriter.write("\n= Answers =\n");



			// 2 - Add a fact
			//		kbb.add(DlgpParser.parseAtom("human(socrate)."));
			// 3 - Generate the KB
			//		KnowledgeBase kb = kbb.build();
			// 4 - Create a DLGP writer to print data
			//		DlgpWriter writer = new DlgpWriter();
			//		// 5 - Parse a query from a Java String
			//		ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- mortal(X).");
			//		// 6 - Query the KB
			//		CloseableIterator resultIterator = kb.query(query);
			//		// 7 - Iterate and print results
			//		writer.write("\n= Answers =\n");
			//		if (resultIterator.hasNext()) {
			//			do {
			//				writer.write(resultIterator.next());
			//				writer.write("\n");
			//			} while (resultIterator.hasNext());
			//		} else {
			//			writer.write("No answers.\n");
			//		}
			//		// 8 - Close resources
			//		kb.close();
			//		writer.close();
		}
	}
}
