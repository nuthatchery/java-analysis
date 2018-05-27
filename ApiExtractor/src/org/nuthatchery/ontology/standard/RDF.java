package org.nuthatchery.ontology.standard;

import org.nuthatchery.ontology.Id;
import org.nuthatchery.ontology.IdFactory;

public class RDF {
	public static final Id RDF_PREFIX = IdFactory
			.namespace(RDF.W3_PREFIX.addPath("1999").addPath("02").addPath("22-rdf-syntax-ns").setFragment(""), "rdf");
	/**
	 * Resource R is an instance of C
	 */
	public static final Id RDF_TYPE = RDF_PREFIX.setFragment("type");
	/**
	 * Class of properties
	 */
	public static final Id RDF_PROPERTY = RDF_PREFIX.setFragment("Property");
	/**
	 * Class of XML literals
	 */
	public static final Id RDF_XMLLITERAL = RDF_PREFIX.setFragment("XMLLiteral");
	/**
	 * The empty list instance
	 */
	public static final Id RDF_NIL = RDF_PREFIX.setFragment("nil");
	/**
	 * Class of list containers
	 */
	public static final Id RDF_LIST = RDF_PREFIX.setFragment("List");
	/**
	 * Class of RDF statements
	 */
	public static final Id RDF_STATEMENT = RDF_PREFIX.setFragment("Statement");
	/**
	 * R is subject of S
	 */
	public static final Id RDF_SUBJECT = RDF_PREFIX.setFragment("subject");
	/**
	 * R is predicate of S
	 */
	public static final Id RDF_PREDICATE = RDF_PREFIX.setFragment("predicate");
	/**
	 * R is object of S
	 */
	public static final Id RDF_OBJECT = RDF_PREFIX.setFragment("object");
	/**
	 * R is first element of list L
	 */
	public static final Id RDF_FIRST = RDF_PREFIX.setFragment("first");
	/**
	 * L1 is rest of list L2
	 */
	public static final Id RDF_REST = RDF_PREFIX.setFragment("rest");
	/**
	 * Class of ordered containers
	 */
	public static final Id RDF_SEQ = RDF_PREFIX.setFragment("Seq");
	/**
	 * Class of unordered containers
	 */
	public static final Id RDF_BAG = RDF_PREFIX.setFragment("Bag");
	/**
	 * Class of containers of alternatives
	 */
	public static final Id RDF_ALT = RDF_PREFIX.setFragment("Alt");
	/**
	 * Primary of main value of a property
	 */
	public static final Id RDF_VALUE = RDF_PREFIX.setFragment("value");

	/**
	 * Container indices
	 *
	 * @param index The index
	 * @return The IRI <code>rdf:_</code>index
	 * @requires index >= 1
	 */
	public static Id index(int index) {
		if(index < 1) {
			throw new IllegalArgumentException(String.valueOf(index));
		}
		return RDF_PREFIX.setFragment(String.format("_%d", index));
	}

	public static final Id LITERAL_FALSE = IdFactory.literal(false);
	public static final Id LITERAL_ONE = IdFactory.literal(1);
	public static final Id LITERAL_TRUE = IdFactory.literal(true);
	public static final Id LITERAL_ZERO = IdFactory.literal(0);
	public static final Id W3_PREFIX = IdFactory.root("http", "//www.w3.org");
}