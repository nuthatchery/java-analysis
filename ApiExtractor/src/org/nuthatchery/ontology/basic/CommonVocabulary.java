package org.nuthatchery.ontology.basic;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
public class CommonVocabulary {
	public static final String NS = "http://model.nuthatchery.org/common/";
	private static final OntModel vocab = ModelFactory.createOntologyModel();

	public static final Property P_NAME;
	public static final Property P_IDNAME;
	/**
	 * Should have NAME, IDNAME.
	 */
	public static final OntClass C_DEF;
	public static final Property P_DEFINES;
	public static final Property P_DECLARES;

	public static final Property C_NAMED;
	public static final Property C_TYPE;
	public static final Property C_OP;
	public static final Property P_LINE_NUMBER;
	public static final Property P_SHORT_DESC;
	static {
		P_NAME = vocab.createProperty(NS + "name");
		P_IDNAME = vocab.createProperty(NS + "idName");
		C_DEF = vocab.createClass(NS + "def");
		P_DEFINES = vocab.createProperty(NS + "defines");
		P_DECLARES = vocab.createProperty(NS + "declares");
		P_SHORT_DESC = vocab.createProperty(NS + "shortDesc");

		vocab.add(P_DEFINES, RDFS.subPropertyOf, P_DECLARES);
		C_NAMED = vocab.createProperty(NS + "named");
		vocab.add(C_NAMED, RDF.type, RDFS.Class);
		C_TYPE = vocab.createProperty(NS + "type");
		vocab.add(C_TYPE, RDF.type, RDFS.Class);
		vocab.add(C_TYPE, RDFS.subClassOf, C_NAMED);
		C_OP = vocab.createProperty(NS + "op");
		vocab.add(C_OP, RDF.type, RDFS.Class);
		vocab.add(C_OP, RDFS.subClassOf, C_NAMED);
		P_LINE_NUMBER = vocab.createProperty(NS + "lineNumber");

	}
}
