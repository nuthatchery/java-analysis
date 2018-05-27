package org.nuthatchery.ontology.standard;

import org.nuthatchery.ontology.Id;
import org.nuthatchery.ontology.IdFactory;

public class XSD {
	public static final Id XSD_PREFIX = IdFactory.namespace(RDF.W3_PREFIX.addPath("2001").addPath("XMLSchema").setFragment(""),
			"xsd");
	public static final Id TYPE_BOOLEAN = XSD_PREFIX.setFragment("boolean");
	public static final Id TYPE_DECIMAL = XSD_PREFIX.setFragment("decimal");

	public static final Id TYPE_DOUBLE = XSD_PREFIX.setFragment("double");

	public static final Id TYPE_FLOAT = XSD_PREFIX.setFragment("float");

	public static final Id TYPE_INTEGER = XSD_PREFIX.setFragment("integer");

	public static final Id TYPE_STRING = XSD_PREFIX.setFragment("string");

}