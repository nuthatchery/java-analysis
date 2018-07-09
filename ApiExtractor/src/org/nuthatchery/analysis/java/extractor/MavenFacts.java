package org.nuthatchery.analysis.java.extractor;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.*;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * A class for MavenFacts For now this is just to test parsing the Maven POM
 * file. This class may be moved into its own file, or it may be deleted and the
 * facts will directly represented as RDF properties or it may be deleted and we
 * will use the Maven.Model properties instead
 *
 * @author anna
 *
 */
public abstract class MavenFacts {
	public static final String M = "http://model.nuthatchery.org/maven/";
	public static final String MP = M + "project/";
	public static final OntModel mavenModel = //
			ModelFactory.createOntologyModel();
	public static final Property artifactId = mavenModel.createProperty(M + "artifactId");
	public static final Property groupId = mavenModel.createProperty(M + "groupId");
	public static final Property version = mavenModel.createProperty(M + "version");
	public static final Property dependsOn = mavenModel.createProperty(M + "dependsOn");
	public static final OntClass MavenProject = mavenModel.createClass(M + "project");
	public static final OntClass MavenCoordinate = mavenModel.createClass(M + "MavenCoordinate");
	public static final Property hasCoord = mavenModel.createProperty(M + "hasCoordinate");;

	static {
		mavenModel.add(MavenProject, RDF.type, RDFS.Class);
		isProperty(mavenModel, artifactId, MavenProject, XSD.xstring);
		isProperty(mavenModel, groupId, MavenProject, XSD.xstring);
		isProperty(mavenModel, version, MavenProject, XSD.xstring);
	}

	/**
	 * State that something is a property
	 *
	 * TODO: extract to utility class and make public
	 *
	 * @param m
	 *            A model
	 * @param prop
	 *            IRI of the property
	 * @param sub
	 *            An RDF class for the possible subjects, this will be the
	 *            rdfs:range of the property; can be null for no range
	 * @param obj
	 *            An RDF class for the possible objects, this will be the
	 *            rdfs:domain of the property; can be null for no range
	 * @param more
	 *            Pairs of additional terms that add extra information about the
	 *            property. E.g, with more = [subPropertyOf, foo], we'll also get
	 *            (prop, subPropertyOf foo)
	 */
	private static void isProperty(Model m, Property prop, Resource sub, RDFNode obj, RDFNode... more) {
		m.add(prop, RDF.type, RDF.Property);
		if (sub != null) {
			m.add(prop, RDFS.range, sub);
		}
		if (obj != null) {
			m.add(prop, RDFS.domain, obj);
		}
		for (int i = 0; i < more.length; i += 2) {
			m.add(prop, (Property) more[i], more[i + 1]);
		}
	}

}
