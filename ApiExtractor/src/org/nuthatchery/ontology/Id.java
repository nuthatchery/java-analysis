package org.nuthatchery.ontology;

import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;

public interface Id {
	Id addPath(Stream<String> segments);

	Id addPath(String segment);

	default Id addPath(String... split) {
		Id id = this;
		for (String s : split) {
			id = id.addPath(s);
		}
		return id;
	}

	String getAuthority();

	String getFragment();

	Id getParam(Id paramKey);

	Id getParent();

	Stream<String> getPath();

	String getScheme();

	boolean hasFragment();

	default boolean hasParam(Id paramKey) {
		return false;
	}

	default boolean hasParams() {
		return false;
	}

	boolean isContainer();

	boolean isOpaque();

	boolean isRoot();

	Id removeFragment();

	default Id removeParam(Id paramKey) {
		throw new UnsupportedOperationException("does not support parameters");
	}

	Id resolve(String rawSchemeSpecificPart);

	Id setFragment(String s);

	Id setParam(Id paramKey);

	Id setParam(Id paramKey, Id paramValue);

	String toRdfString();

	@Override
	String toString();

	String toUriString();

	String getNamespace();

	String toFullUriString();

	RDFTerm asRDFTerm();

	IRI asIRI();
	BlankNodeOrIRI asBlankNodeOrIRI();
	Literal asLiteral();
}
