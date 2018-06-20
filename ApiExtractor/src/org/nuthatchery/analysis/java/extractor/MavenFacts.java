package org.nuthatchery.analysis.java.extractor;

import org.apache.commons.rdf.api.IRI;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;

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

}
