package org.nuthatchery.ontology.uri;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

public class IRIUtil {
	private final RDF rdf;

	public IRIUtil(RDF rdf) {
		this.rdf = rdf;
	}

	public IRI addFragment(IRI base, String frag) {
		String iriBase = base.getIRIString();
		if (iriBase.endsWith("#")) {
			return rdf.createIRI(iriBase + frag);
		} else {
			return rdf.createIRI(iriBase + "#" + frag);
		}
	}

	public IRI addPath(IRI base, String path) {
		String iriBase = base.getIRIString();
		if (iriBase.endsWith("/") || iriBase.endsWith(":")) {
			return rdf.createIRI(iriBase + path);
		} else {
			return rdf.createIRI(iriBase + "/" + path);
		}
	}

}
