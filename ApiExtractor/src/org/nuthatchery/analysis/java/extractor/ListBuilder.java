package org.nuthatchery.analysis.java.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;

public class ListBuilder {
	private final List<RDFNode> list = new ArrayList<>();
	private final Model model;

	public ListBuilder(Model model) {
		this.model = model;
	}

	public void add(RDFNode node) {
		list.add(node);
	}

	public RDFList build() {
		return model.createList(list.iterator());
	}
}
