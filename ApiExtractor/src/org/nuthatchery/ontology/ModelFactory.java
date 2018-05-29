package org.nuthatchery.ontology;

import java.util.function.Supplier;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.nuthatchery.ontology.standard.RdfVocabulary;

public class ModelFactory {
	private static ModelFactory instance;
	private final Supplier<RDF> constructor;
	private RdfVocabulary rdfVocabulary;

	private ModelFactory(Supplier<RDF> constructor) {
		this.constructor = constructor;
	}

	public static synchronized ModelFactory getInstance() {
		if (instance == null) {
			instance = new ModelFactory(() -> new SimpleRDF());
		}
		return instance;
	}

	public static synchronized void setFactory(Supplier<RDF> constructor) {
		instance = new ModelFactory(constructor);
	}

	public RDF createRDF() {
		return constructor.get();
	}

	public Model createModel(String prefix) {
		return new Model(prefix, createRDF());
	}

	public Model createModel(String name, String prefix) {
		return new Model(null, name, prefix, createRDF());
	}

	public Model createModel(Dataset ds, String prefix) {
		return new Model(ds, prefix, createRDF());
	}

	public Model createModel(Dataset ds, String name, String prefix) {
		return new Model(ds, name, prefix, createRDF());
	}


	public Model createModel(Graph graph, String prefix) {
		return new Model(graph, prefix, createRDF());
	}
	public RdfVocabulary rdfVocabulary() {
		if (rdfVocabulary == null) {
			rdfVocabulary = new RdfVocabulary(createRDF());
		}
		return rdfVocabulary;
	}

}
