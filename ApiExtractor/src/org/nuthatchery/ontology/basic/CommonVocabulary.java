package org.nuthatchery.ontology.basic;

import org.apache.commons.rdf.api.IRI;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;
import org.nuthatchery.ontology.standard.RdfVocabulary;

public class CommonVocabulary {
	public static final String PREFIX = "http://model.nuthatchery.org/common/";
	private static CommonVocabulary instance;
	private final Model vocab;
	private RdfVocabulary rdfVocab;

	public static CommonVocabulary getInstance() {
		if(instance == null) {
			instance = new CommonVocabulary(ModelFactory.getInstance());
		}
		return instance;
	}

	public CommonVocabulary(ModelFactory fac) {
		vocab = fac.createModel(PREFIX);
		rdfVocab = fac.rdfVocabulary();
		NAMED = vocab.node("named");
		vocab.add(NAMED, rdfVocab.RDF_TYPE, rdfVocab.RDFS_CLASS);
		TYPE = vocab.node("type");
		vocab.add(TYPE, rdfVocab.RDF_TYPE, rdfVocab.RDFS_CLASS);
		vocab.add(TYPE, rdfVocab.RDFS_SUBCLASS_OF, NAMED);
		OP = vocab.node("op");
		vocab.add(OP, rdfVocab.RDF_TYPE, rdfVocab.RDFS_CLASS);
		vocab.add(OP, rdfVocab.RDFS_SUBCLASS_OF, NAMED);
		LINE_NUMBER = vocab.node("lineNumber");

	}

	public final IRI NAMED;
	public final IRI TYPE;
	public final IRI OP;
	public final IRI LINE_NUMBER;
}
