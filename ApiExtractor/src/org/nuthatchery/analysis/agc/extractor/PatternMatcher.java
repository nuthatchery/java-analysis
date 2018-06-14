package org.nuthatchery.analysis.agc.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcher {
	private Map<String, Pattern> patterns = new HashMap<>();
	private List<MatchCase> cases = new ArrayList<>();
	public static PatternMatcher matcher() {
		return new PatternMatcher();
	}

	public PatternMatcher matchCase(String pattern, Consumer<MatchResult> f) {
		Pattern p = patterns.get(pattern);
		if (p == null) {
			p = Pattern.compile(pattern);
			patterns.put(pattern, p);
		}
		MatchCase mc = new MatchCase();
		mc.pat = p;
		mc.f = f;
		cases.add(mc);
		return this;
	}

	public boolean match(String s) {
		for (MatchCase mc : cases) {
			Matcher matcher = mc.pat.matcher(s);
			if (matcher.matches()) {
				mc.f.accept(matcher.toMatchResult());
				return true;
			}
		}
		return false;
	}

	static class MatchCase {
		Pattern pat;
		Consumer<MatchResult> f;
	}
}
