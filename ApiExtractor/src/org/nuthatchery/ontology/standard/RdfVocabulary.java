package org.nuthatchery.ontology.standard;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.Types;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;

public class RdfVocabulary {
	public static final String RDFS_PREFIX = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final Model rdfModel;
	private final Model rdfsModel;
	private static RdfVocabulary instance;

	public static RdfVocabulary getInstance() {
		if(instance == null) {
			instance = ModelFactory.getInstance().rdfVocabulary();
		}
		return instance;
	}

	public RdfVocabulary(RDF factory) {
		synchronized (this.getClass()) {
			if (instance == null) {
				instance = this;
			}
		}

		rdfModel = new Model(RDF_PREFIX, factory);
		rdfsModel = new Model(RDFS_PREFIX, factory);
		//xsdModel = new Model("http://www.w3.org/2001/XMLSchema#", factory);
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

	/**
	 * Resource R is an instance of C
	 */
	public final IRI RDF_TYPE;
	/**
	 * Class of properties
	 */
	public final IRI RDF_PROPERTY;
	/**
	 * Class of XML literals
	 */
	public final IRI RDF_XMLLITERAL;
	/**
	 * The empty list instance
	 */
	public final IRI RDF_NIL;
	/**
	 * Class of list containers
	 */
	public final IRI RDF_LIST;
	/**
	 * Class of RDF statements
	 */
	public final IRI RDF_STATEMENT;
	/**
	 * R is subject of S
	 */
	public final IRI RDF_SUBJECT;
	/**
	 * R is predicate of S
	 */
	public final IRI RDF_PREDICATE;
	/**
	 * R is object of S
	 */
	public final IRI RDF_OBJECT;
	/**
	 * R is first element of list L
	 */
	public final IRI RDF_FIRST;
	/**
	 * L1 is rest of list L2
	 */
	public final IRI RDF_REST;
	/**
	 * Class of ordered containers
	 */
	public final IRI RDF_SEQ;
	/**
	 * Class of unordered containers
	 */
	public final IRI RDF_BAG;
	/**
	 * Class of containers of alternatives
	 */
	public final IRI RDF_ALT;
	/**
	 * Primary of main value of a property
	 */
	public final IRI RDF_VALUE;

	/**
	 * Container indices
	 *
	 * @param index
	 *            The index
	 * @return The IRI <code>rdf:_</code>index
	 * @requires index >= 1
	 */
	public IRI index(int index) {
		if (index < 1) {
			throw new IllegalArgumentException(String.valueOf(index));
		}
		return rdfModel.node(String.format("_%d", index));
	}

	public final Literal LITERAL_FALSE;
	public final Literal LITERAL_ONE;
	public final Literal LITERAL_TRUE;
	public final Literal LITERAL_ZERO;

	/**
	 * Object of property P must be of class C
	 */
	public final IRI RDFS_DOMAIN;
	/**
	 * Subject of property P must be of class C
	 */
	public final IRI RDFS_RANGE;
	/**
	 * Class of all resources
	 */
	public final IRI RDFS_RESOURCE;
	/**
	 * Class of all literal values
	 */
	public final IRI RDFS_LITERAL;
	/**
	 * Class of all datatypes
	 */
	public final IRI RDFS_DATATYPE;
	/**
	 * Class of all classes
	 */
	public final IRI RDFS_CLASS;
	/**
	 * Class C1 is subclass of C2
	 */
	public final IRI RDFS_SUBCLASS_OF;
	/**
	 * Property P1 is a subclass of P2
	 */
	public final IRI RDFS_SUBPROPERTY_OF;
	/**
	 * Resource R1 is member of container R2
	 */
	public final IRI RDFS_MEMBER;
	/**
	 * Class of containers
	 */
	public final IRI RDFS_CONTAINER;
	/**
	 * Class of :_1, :_2, ... container membership
	 */
	public final IRI RDFS_CONTAINER_MEMBERSHIP_PROPERTY;
	/**
	 * Resource R has comment L
	 */
	public final IRI RDFS_COMMENT;
	/**
	 * Resource R1 has more information in R2
	 */
	public final IRI RDFS_SEE_ALSO;
	/**
	 * Resources R1 is defined by R2
	 */
	public final IRI RDFS_IS_DEFINED_BY;
	/**
	 * Resource R has label L
	 */
	public final IRI RDFS_LABEL;
	public final IRI TYPE_BOOLEAN = Types.XSD_BOOLEAN;
	public final IRI TYPE_DECIMAL = Types.XSD_DECIMAL;

	public final IRI TYPE_DOUBLE = Types.XSD_DOUBLE;

	public final IRI TYPE_FLOAT = Types.XSD_FLOAT;

	public final IRI TYPE_INTEGER = Types.XSD_INTEGER;

	public final IRI TYPE_STRING = Types.XSD_STRING;
}