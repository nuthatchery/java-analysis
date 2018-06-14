package org.nuthatchery.ontology.standard;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.simple.Types;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;

public class RdfVocabulary {
	public static final String RDFS_PREFIX = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String XSD_PREFIX = "http://www.w3.org/2001/XMLSchema#";
	private static final Model rdfModel;
	private static final Model rdfsModel;

	/**
	 * Resource R is an instance of C
	 */
	public static final IRI RDF_TYPE;
	/**
	 * Class of properties
	 */
	public static final IRI RDF_PROPERTY;
	/**
	 * Class of XML literals
	 */
	public static final IRI RDF_XMLLITERAL;
	/**
	 * The empty list instance
	 */
	public static final IRI RDF_NIL;
	/**
	 * Class of list containers
	 */
	public static final IRI RDF_LIST;
	/**
	 * Class of RDF statements
	 */
	public static final IRI RDF_STATEMENT;
	/**
	 * R is subject of S
	 */
	public static final IRI RDF_SUBJECT;
	/**
	 * R is predicate of S
	 */
	public static final IRI RDF_PREDICATE;
	/**
	 * R is object of S
	 */
	public static final IRI RDF_OBJECT;
	/**
	 * R is first element of list L
	 */
	public static final IRI RDF_FIRST;
	/**
	 * L1 is rest of list L2
	 */
	public static final IRI RDF_REST;
	/**
	 * Class of ordered containers
	 */
	public static final IRI RDF_SEQ;
	/**
	 * Class of unordered containers
	 */
	public static final IRI RDF_BAG;
	/**
	 * Class of containers of alternatives
	 */
	public static final IRI RDF_ALT;
	/**
	 * Primary of main value of a property
	 */
	public static final IRI RDF_VALUE;

	public static final Literal LITERAL_FALSE;

	public static final Literal LITERAL_ONE;
	public static final Literal LITERAL_TRUE;
	public static final Literal LITERAL_ZERO;
	/**
	 * Object of property P must be of class C
	 */
	public static final IRI RDFS_DOMAIN;

	/**
	 * Subject of property P must be of class C
	 */
	public static final IRI RDFS_RANGE;
	/**
	 * Class of all resources
	 */
	public static final IRI RDFS_RESOURCE;
	/**
	 * Class of all literal values
	 */
	public static final IRI RDFS_LITERAL;
	/**
	 * Class of all datatypes
	 */
	public static final IRI RDFS_DATATYPE;
	/**
	 * Class of all classes
	 */
	public static final IRI RDFS_CLASS;
	/**
	 * Class C1 is subclass of C2
	 */
	public static final IRI RDFS_SUBCLASS_OF;
	/**
	 * Property P1 is a subclass of P2
	 */
	public static final IRI RDFS_SUBPROPERTY_OF;
	/**
	 * Resource R1 is member of container R2
	 */
	public static final IRI RDFS_MEMBER;
	/**
	 * Class of containers
	 */
	public static final IRI RDFS_CONTAINER;
	/**
	 * Class of :_1, :_2, ... container membership
	 */
	public static final IRI RDFS_CONTAINER_MEMBERSHIP_PROPERTY;
	/**
	 * Resource R has comment L
	 */
	public static final IRI RDFS_COMMENT;
	/**
	 * Resource R1 has more information in R2
	 */
	public static final IRI RDFS_SEE_ALSO;
	/**
	 * Resources R1 is defined by R2
	 */
	public static final IRI RDFS_IS_DEFINED_BY;
	/**
	 * Resource R has label L
	 */
	public static final IRI RDFS_LABEL;
	public static final IRI TYPE_BOOLEAN = Types.XSD_BOOLEAN;
	public static final IRI TYPE_DECIMAL = Types.XSD_DECIMAL;
	public static final IRI TYPE_DOUBLE = Types.XSD_DOUBLE;

	public static final IRI TYPE_FLOAT = Types.XSD_FLOAT;

	public static final IRI TYPE_INTEGER = Types.XSD_INTEGER;

	public static final IRI TYPE_STRING = Types.XSD_STRING;

	static {
		rdfModel = ModelFactory.getInstance().createModel(RDF_PREFIX);
		rdfsModel = ModelFactory.getInstance().createModel(RDFS_PREFIX);
		// xsdModel = new Model("http://www.w3.org/2001/XMLSchema#", factory);
		RDF_TYPE = rdfModel.node("type");
		RDF_PROPERTY = rdfModel.node("Property");
		RDF_XMLLITERAL = rdfModel.node("XMLLiteral");
		RDF_NIL = rdfModel.node("nil");
		RDF_LIST = rdfModel.node("List");
		RDF_STATEMENT = rdfModel.node("Statement");
		RDF_SUBJECT = rdfModel.node("subject");
		RDF_PREDICATE = rdfModel.node("predicate");
		RDF_OBJECT = rdfModel.node("object");
		RDF_FIRST = rdfModel.node("first");
		RDF_REST = rdfModel.node("rest");
		RDF_SEQ = rdfModel.node("Seq");
		RDF_BAG = rdfModel.node("Bag");
		RDF_ALT = rdfModel.node("Alt");
		RDF_VALUE = rdfModel.node("value");
		LITERAL_FALSE = rdfModel.literal(false);
		LITERAL_ONE = rdfModel.literal(1);
		LITERAL_TRUE = rdfModel.literal(true);
		LITERAL_ZERO = rdfModel.literal(0);

		RDFS_DOMAIN = rdfsModel.node("domain");
		RDFS_RANGE = rdfsModel.node("range");
		RDFS_RESOURCE = rdfsModel.node("Resource");
		RDFS_LITERAL = rdfsModel.node("Literal");
		RDFS_DATATYPE = rdfsModel.node("Datatype");
		RDFS_CLASS = rdfsModel.node("Class");
		RDFS_SUBCLASS_OF = rdfsModel.node("subClassOf");
		RDFS_SUBPROPERTY_OF = rdfsModel.node("subPropertyOf");
		RDFS_MEMBER = rdfsModel.node("member");
		RDFS_CONTAINER = rdfsModel.node("Container");
		RDFS_CONTAINER_MEMBERSHIP_PROPERTY = rdfsModel.node("ContainerMembershipProperty");
		RDFS_COMMENT = rdfsModel.node("comment");
		RDFS_SEE_ALSO = rdfsModel.node("seeAlso");
		RDFS_IS_DEFINED_BY = rdfsModel.node("isDefinedBy");
		RDFS_LABEL = rdfsModel.node("label");
		/*
		 * TYPE_BOOLEAN = xsdModel.node("boolean"); TYPE_DECIMAL =
		 * xsdModel.node("decimal"); TYPE_DOUBLE = xsdModel.node("double"); TYPE_FLOAT =
		 * xsdModel.node("float"); TYPE_INTEGER = xsdModel.node("integer"); TYPE_STRING
		 * = xsdModel.node("string");
		 */
	}

	public static Model getRdfModel() {
		return rdfModel;
	}

	public static Model getRdfsModel() {
		return rdfsModel;
	}

	/**
	 * Container indices
	 *
	 * @param index
	 *            The index
	 * @return The IRI <code>rdf:_</code>index
	 * @requires index >= 1
	 */
	public IRI index(int index) {
		if (index < 1)
			throw new IllegalArgumentException(String.valueOf(index));
		return rdfModel.node(String.format("_%d", index));
	}
}