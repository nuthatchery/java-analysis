package org.nuthatchery.analysis.java.extractor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface Id {
	Stream<String> getPath();

	default Id removeParam(Id paramKey) {
		throw new UnsupportedOperationException("does not support parameters");
	}

	default boolean hasParam(Id paramKey) {
		return false;
	}

	default boolean hasParams() {
		return false;
	}

	String getScheme();

	String getAuthority();

	Id addPath(String segment);

	Id addPath(Stream<String> segments);

	Id getParam(Id paramKey);

	Id setParam(Id paramKey);

	Id setParam(Id paramKey, Id paramValue);

	String getFragment();

	Id setFragment(String s);

	Id removeFragment();

	Id getParent();

	String toString();

	String toUriString();

	String toRdfString();

	boolean isRoot();

	boolean isContainer();

	boolean hasFragment();

	boolean isOpaque();

	default Id addPath(String[] split) {
		Id id = this;
		for(String s : split)
			id = id.addPath(s);
		return id;
	}

	Id resolve(String rawSchemeSpecificPart);
}
