package org.nuthatchery.analysis.java.extractor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class FactsDb {
	public static IFactsWriter prologFactsWriter(String fileName, String context) {
		return new PrologFactsWriter(fileName, context);
	}

	public static IFactsWriter nTripleFactsWriter(String fileName, String context) {
		return new NTripleFactsWriter(fileName, context);
	}

	public static abstract class AbstractFactsWriter implements IFactsWriter {
		protected final String context;

		public AbstractFactsWriter(String context) {
			this.context = context;
		}

		public String getContext() {
			return context;
		}
	}

	public static class NTripleFactsWriter extends AbstractFactsWriter {
		private final String fileName;
		private final Set<String> facts;

		public NTripleFactsWriter(String fileName, String context) {
			this(fileName, context, new HashSet<>());
		}

		public NTripleFactsWriter(String fileName, String context, Set<String> facts) {
			super(context);
			this.fileName = fileName;
			this.facts = facts;
		}

		public void put(Id obj, Id relation) {
			put(String.format("%s %s true .", obj.getRDF(), relation.getRDF()));
		}

		private void put(String factString) {
			facts.add(factString);
		}

		public void put(Id obj, Id relation, Id tgt) {
			put(String.format("%s %s %s .", obj.getRDF(), relation.getRDF(), tgt.getRDF()));
		}

		public void put(Id obj, Id relation, Id tgt, Id mod) {
			URI relUri = relation.getURI();
			URI modUri = mod.getURI();
			
			modUri = relUri.resolve("").relativize(modUri);
			relUri = URI.create(relUri.toString() + "?" + modUri);
			
			put(String.format("%s <%s> %s .", obj.getRDF(), relUri, tgt.getRDF()));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt, Id mod1, Id mod2) {
			URI relUri = relation.getURI();
			URI mod1Uri = mod1.getURI();
			URI mod2Uri = mod2.getURI();
			
			mod1Uri = relUri.resolve("").relativize(mod1Uri);
			mod2Uri = relUri.resolve("").relativize(mod2Uri);
			relUri = URI.create(relUri.toString() + "?" + mod1Uri + "&" + mod2Uri);
			
			put(String.format("%s <%s> %s .", obj.getRDF(), relUri, tgt.getRDF()));
		}

		@Override
		public IFactsWriter newContext(String context) {
			return new PrologFactsWriter(fileName, context, facts);
		}

		@Override
		public void save() throws FileNotFoundException {
			try (PrintWriter writer = new PrintWriter("/tmp/data.pl")) {
				facts.stream().sorted().forEach((String l) -> {
					writer.println(l);
				});
			}
		}
	}

	public static class PrologFactsWriter extends AbstractFactsWriter {
		private final String fileName;
		private final Set<String> facts;

		public PrologFactsWriter(String fileName, String context) {
			this(fileName, context, new HashSet<>());
		}

		public PrologFactsWriter(String fileName, String context, Set<String> facts) {
			super(context);
			this.fileName = fileName;
			this.facts = facts;
		}

		public void put(Id obj, Id relation) {
			put(String.format("%s(\"%s\",\"%s\").", relation.getRDF(), context, obj));
		}

		private void put(String factString) {
			facts.add(factString);
		}

		public void put(Id obj, Id relation, Id tgt) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\").", relation.getRDF(), context, obj, tgt));
		}

		public void put(Id obj, Id relation, Id tgt, Id mod) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\",\"%s\").", relation.getRDF(), context, obj, tgt, mod));
		}

		@Override
		public void put(Id from, Id label, Id to, Id modifier1, Id modifier2) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\",\"%s\", \"%s\").", label.getRDF(), context, from, to, modifier1,
					modifier2));
		}

		@Override
		public IFactsWriter newContext(String context) {
			return new PrologFactsWriter(fileName, context, facts);
		}

		@Override
		public void save() throws FileNotFoundException {
			try (PrintWriter writer = new PrintWriter("/tmp/data.pl")) {
				facts.stream().sorted().forEach((String l) -> {
					writer.println(l);
				});
			}
		}
	}

	public static interface IFactsWriter {
		public IFactsWriter newContext(String context);

		void save() throws FileNotFoundException;

		public void put(Id from, Id label);

		public void put(Id from, Id label, Id to);

		public void put(Id from, Id label, Id to, Id modifier);

		/**
		 * Add a fact.
		 * 
		 * @param from
		 *            The subject of the relation
		 * @param label
		 *            The relation's label
		 * @param to
		 *            The object of the relation
		 * @param modifier1
		 *            An extra modifier to the relation
		 * @param modifier2
		 *            An extra modifier to the relation
		 */
		public void put(Id from, Id label, Id to, Id modifier1, Id modifier2);

		public String getContext();
	}
}
