package org.nuthatchery.ontology.uri;

import org.apache.jena.rdf.model.Resource;

public class IRIUtil {

	public static Resource addFragment(Resource base, String frag) {
		if (!base.isURIResource())
			throw new IllegalArgumentException("Must be IRI");
		String iriBase = base.getURI();
		if (iriBase.endsWith("#"))
			return base.getModel().createResource(iriBase + frag);
		else
			return base.getModel().createResource(iriBase + "#" + frag);
	}

	public static Resource addPath(Resource base, String path) {
		if (!base.isURIResource())
			throw new IllegalArgumentException("Must be IRI");
		String iriBase = base.getURI();
		if (iriBase.endsWith("/") || iriBase.endsWith(":"))
			return base.getModel().createResource(iriBase + path);
		else
			return base.getModel().createResource(iriBase + "/" + path);
	}

}
