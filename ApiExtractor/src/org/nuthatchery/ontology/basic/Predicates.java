package org.nuthatchery.ontology.basic;

import org.apache.commons.rdf.api.IRI;
import org.nuthatchery.ontology.standard.RdfVocabulary;

public class Predicates {
	public static final RdfVocabulary vocab = RdfVocabulary.getInstance();
	public static final IRI IS_A = vocab.RDF_TYPE;

}
