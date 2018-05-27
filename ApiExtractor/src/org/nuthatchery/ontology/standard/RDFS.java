package org.nuthatchery.ontology.standard;

import org.nuthatchery.ontology.Id;
import org.nuthatchery.ontology.IdFactory;

public class RDFS {
	public static final Id RDFS_PREFIX = IdFactory
			.namespace(RDF.W3_PREFIX.addPath("2000").addPath("01").addPath("rdf-schema").setFragment(""), "rdfs");

	/**
	 * Object of property P must be of class C
	 */
	public static final Id RDFS_DOMAIN = RDFS_PREFIX.setFragment("domain");
	/**
	 * Subject of property P must be of class C
	 */
	public static final Id RDFS_RANGE = RDFS_PREFIX.setFragment("range");
	/**
	 * Class of all resources
	 */
	public static final Id RDFS_RESOURCE = RDFS_PREFIX.setFragment("Resource");
	/**
	 * Class of all literal values
	 */
	public static final Id RDFS_LITERAL = RDFS_PREFIX.setFragment("Literal");
	/**
	 * Class of all datatypes
	 */
	public static final Id RDFS_DATATYPE = RDFS_PREFIX.setFragment("Datatype");
	/**
	 * Class of all classes
	 */
	public static final Id RDFS_CLASS = RDFS_PREFIX.setFragment("Class");
	/**
	 * Class C1 is subclass of C2
	 */
	public static final Id RDFS_SUBCLASS_OF = RDFS_PREFIX.setFragment("subClassOf");
	/**
	 * Property P1 is a subclass of P2
	 */
	public static final Id RDFS_SUBPROPERTY_OF = RDFS_PREFIX.setFragment("subPropertyOf");
	/**
	 * Resource R1 is member of container R2
	 */
	public static final Id RDFS_MEMBER = RDFS_PREFIX.setFragment("member");
	/**
	 * Class of containers
	 */
	public static final Id RDFS_CONTAINER = RDFS_PREFIX.setFragment("Container");
	/**
	 * Class of :_1, :_2, ... container membership
	 */
	public static final Id RDFS_CONTAINER_MEMBERSHIP_PROPERTY = RDFS_PREFIX.setFragment("ContainerMembershipProperty");
	/**
	 * Resource R has comment L
	 */
	public static final Id RDFS_COMMENT = RDFS_PREFIX.setFragment("comment");
	/**
	 * Resource R1 has more information in R2
	 */
	public static final Id RDFS_SEE_ALSO = RDFS_PREFIX.setFragment("seeAlso");
	/**
	 * Resources R1 is defined by R2
	 */
	public static final Id RDFS_IS_DEFINED_BY = RDFS_PREFIX.setFragment("isDefinedBy");
	/**
	 * Resource R has label L
	 */
	public static final Id RDFS_LABEL = RDFS_PREFIX.setFragment("label");
}