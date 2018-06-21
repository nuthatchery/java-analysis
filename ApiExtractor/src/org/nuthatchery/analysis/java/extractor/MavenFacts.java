package org.nuthatchery.analysis.java.extractor;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.simple.Types;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;
import org.nuthatchery.ontology.standard.RdfVocabulary;

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
	public static final String mavenPrefix = "http://model.nuthatchery.org/maven/";
	public static final String mavenProjectPrefix = mavenPrefix + "project/";
	public static final Model mavenProjectModel = //
			ModelFactory.getInstance().createModel(mavenProjectPrefix);
	public static final IRI ARTIFACT_ID = mavenProjectModel.node("artifactID");
	public static final IRI GROUP_ID = mavenProjectModel.node("groupID");
	public static final IRI VERSION = mavenProjectModel.node("version");
	public static final IRI C_PROJECT = mavenProjectModel.node("");

	static {
		mavenProjectModel.add(C_PROJECT, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDFS_CLASS);
		isProperty(mavenProjectModel, ARTIFACT_ID, C_PROJECT, Types.XSD_STRING);
		isProperty(mavenProjectModel, GROUP_ID, C_PROJECT, Types.XSD_STRING);
		isProperty(mavenProjectModel, VERSION, C_PROJECT, Types.XSD_STRING);
	}

	private static void isProperty(Model m, IRI prop, BlankNodeOrIRI sub, RDFTerm obj, RDFTerm... more) {
		m.add(prop, RdfVocabulary.RDF_TYPE, RdfVocabulary.RDF_PROPERTY);
		if (sub != null) {
			m.add(prop, RdfVocabulary.RDFS_RANGE, sub);
		}
		if (obj != null) {
			m.add(prop, RdfVocabulary.RDFS_DOMAIN, obj);
		}
		for (int i = 0; i < more.length; i += 2) {
			m.add(prop, (IRI) more[i], more[i + 1]);
		}
	}

}
