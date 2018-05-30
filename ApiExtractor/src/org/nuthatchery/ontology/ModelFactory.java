package org.nuthatchery.ontology;

import java.util.function.Supplier;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class ModelFactory {
	private static ModelFactory instance;

	public static synchronized ModelFactory getInstance() {
		if (instance == null) {
			instance = new ModelFactory(() -> new SimpleRDF());
		}
		return instance;
	}

	public static synchronized void setFactory(Supplier<RDF> constructor) {
		instance = new ModelFactory(constructor);
	}

	private final Supplier<RDF> constructor;

	private ModelFactory(Supplier<RDF> constructor) {
		this.constructor = constructor;
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

	public Model createModel(String prefix) {
		return new Model(prefix, createRDF());
	}

	public Model createModel(String name, String prefix) {
		return new Model(null, name, prefix, createRDF());
	}

	public RDF createRDF() {
		return constructor.get();
	}
}
