package org.nuthatchery.ontology.basic;

import org.apache.commons.rdf.api.IRI;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;
import org.nuthatchery.ontology.standard.RdfVocabulary;

public class CommonVocabulary {
	public static final String PREFIX = "http://model.nuthatchery.org/common/";
	private static final Model vocab;

	public static final IRI P_NAME;
	public static final IRI P_IDNAME;
	/**
	 * Should have NAME, IDNAME.
	 */
	public static final IRI C_DEF;
	public static final IRI P_DEFINES;
	public static final IRI P_DECLARES;

	public static final IRI C_NAMED;
	public static final IRI C_TYPE;
	public static final IRI C_OP;
	public static final IRI P_LINE_NUMBER;
	static {
		ModelFactory fac = ModelFactory.getInstance();
		vocab = fac.createModel(PREFIX);
		P_NAME = vocab.node("name");
		P_IDNAME = vocab.node("idName");
		C_DEF = vocab.node("def");
		P_DEFINES = vocab.node("defines");
		P_DECLARES = vocab.node("declares");

		vocab.add(P_DEFINES, RdfVocabulary.RDFS_SUBPROPERTY_OF, P_DECLARES);
		C_NAMED = vocab.node("named");
		vocab.add(C_NAMED, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
		C_TYPE = vocab.node("type");
		vocab.add(C_TYPE, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
		vocab.add(C_TYPE, RdfVocabulary.RDFS_SUBCLASS_OF, C_NAMED);
		C_OP = vocab.node("op");
		vocab.add(C_OP, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
		vocab.add(C_OP, RdfVocabulary.RDFS_SUBCLASS_OF, C_NAMED);
		P_LINE_NUMBER = vocab.node("lineNumber");

	}

	public static final Model getModel() {
		return vocab;
	}
}
