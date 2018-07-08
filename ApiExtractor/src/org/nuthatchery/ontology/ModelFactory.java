package org.nuthatchery.ontology;


import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

public class ModelFactory {
	private static ModelFactory instance;

	public static synchronized ModelFactory getInstance() {
		if (instance == null) {
			instance = new ModelFactory();
		}
		return instance;
	}

	private ModelFactory() {
	}

	public NModel createModel(Dataset ds, String prefix) {
		return new NModel(ds, prefix);
	}

	public NModel createModel(Dataset ds, String name, String prefix) {
		return new NModel(ds, name, prefix);
	}

	public NModel createModel(Model model, String prefix) {
		return new NModel(model, prefix);
	}

	public NModel createModel(String prefix) {
		return new NModel(prefix);
	}

	public NModel createModel(String name, String prefix) {
		return new NModel(null, name, prefix);
	}
}
