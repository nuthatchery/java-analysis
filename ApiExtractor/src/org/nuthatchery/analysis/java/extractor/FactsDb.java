package org.nuthatchery.analysis.java.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class FactsDb {
	public static abstract class AbstractFactsWriter implements IFactsWriter {
		protected String context;

		public AbstractFactsWriter(String context) {
			this.context = context;
		}

		@Override
		public String getContext() {
			return context;
		}

		@Override
		public void setContext(String context) {
			this.context = context;
		}
	}

	public static abstract class AbstractTextFactsWriter extends AbstractFactsWriter {

		protected final String fileName;
		private PrintWriter output;
		protected final List<String> todo = new ArrayList<>();
		protected final boolean zipIt = true;

		public AbstractTextFactsWriter(String context, String fileName) {
			super(context);
			this.fileName = fileName;
		}

		@Override
		public boolean checkpoint() {
			try {
				if (output == null) {
					output = getWriter(".log");
				}
				todo.stream().forEachOrdered((String line) -> {
					output.println(line);
				});
				todo.clear();
				output.flush();
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		protected PrintWriter getWriter(String s) throws IOException {
			if (zipIt) {
				return new PrintWriter(new GZIPOutputStream(new FileOutputStream(fileName + s + ".gz"), true));
			} else {
				return new PrintWriter(new FileOutputStream(fileName + s));
			}
		}

		protected PrintWriter open() throws IOException {
			if (output != null) {
				output.close();
			}
			return getWriter(fileName);
		}

		@Override
		public void precheck() throws IOException {
			try (PrintWriter tmp = getWriter(".log")) {
			}
		}

		protected void saved() {
			try {
				Files.deleteIfExists(new File(fileName + ".log").toPath());
			} catch (IOException e) {
			}
		}

		protected void store() throws IOException {
			if (output == null) {
				output = getWriter(".log");
			}
			todo.stream().forEachOrdered((String line) -> {
				output.println(line);
			});
			todo.clear();

			output.close();
			File file = new File(fileName + (zipIt ? ".log.gz" : ".log"));
			file.renameTo(new File(fileName + (zipIt ? ".gz" : "")));

		}
	}

	public static interface IFactsWriter {
		/**
		 * Temporarily save the current database.
		 *
		 * @return
		 */
		boolean checkpoint();

		public String getContext();

		/**
		 * Try to open the output, if any.
		 *
		 * Will throw an exception if this fails.
		 *
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		void precheck() throws FileNotFoundException, IOException;

		/**
		 * Add a fact.
		 *
		 * @param from
		 *            The subject of the relation
		 * @param label
		 *            The relation's label
		 */
		public void put(Id from, Id label);

		/**
		 * Add a fact.
		 *
		 * @param from
		 *            The subject of the relation
		 * @param label
		 *            The relation's label
		 * @param to
		 *            The object of the relation
		 */
		public void put(Id from, Id label, Id to);

		/**
		 * Add a fact.
		 *
		 * @param from
		 *            The subject of the relation
		 * @param label
		 *            The relation's label
		 * @param to
		 *            The object of the relation
		 * @param modifier
		 *            An extra modifier to the relation
		 */
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

		/**
		 * Save contents of database.
		 *
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		void save() throws FileNotFoundException, IOException;

		/**
		 * Switch to a new context.
		 *
		 * @param context
		 */
		public void setContext(String context);
	}

	public static class NTripleFactsWriter extends AbstractTextFactsWriter {
		// private final Set<String> facts;
		private final Set<String> namespaces = new HashSet<>();

		public NTripleFactsWriter(String fileName, String context) {
			this(fileName, context, new HashSet<>());
		}

		public NTripleFactsWriter(String fileName, String context, Set<String> facts) {
			super(context, fileName);
			// this.facts = facts;
		}

		@Override
		public void put(Id obj, Id relation) {
			ns(obj);
			ns(relation);

			put(String.format("%s %s true .", obj.toRdfString(), relation.toRdfString()));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt) {
			ns(obj);
			ns(relation);
			ns(tgt);
			put(String.format("%s %s %s .", obj.toRdfString(), relation.toRdfString(), tgt.toRdfString()));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt, Id mod) {
			ns(obj);
			ns(relation);
			ns(mod);
			relation = relation.setParam(mod);

			put(String.format("%s %s %s .", obj.toRdfString(), relation.toRdfString(), tgt.toRdfString()));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt, Id mod1, Id mod2) {
			ns(obj);
			ns(relation);
			ns(tgt);
			ns(mod1);
			ns(mod2);
			relation = relation.setParam(mod1);
			relation = relation.setParam(mod2);

			put(String.format("%s %s %s .", obj.toRdfString(), relation.toRdfString(), tgt.toRdfString()));
		}
		private void ns(Id id) {
			String ns = id.getNamespace();
			if(ns != null && !namespaces.contains(ns)) {
				namespaces.add(ns);
				put(String.format("@prefix %s: <%s> .", ns, IdFactory.getNamespace(ns).toFullUriString()));
			}
		}
		private void put(String factString) {
			// if (facts.add(factString))
			todo.add(factString);
			if (todo.size() > 1000) {
				checkpoint();
			}
		}

		@Override
		public void save() throws IOException {
			super.store();
			/*
			 * try (PrintWriter writer = open()) { facts.stream().sorted().forEach((String
			 * l) -> { writer.println(l); }); writer.close(); saved(); }
			 */
		}
	}

	public static class PrologFactsWriter extends AbstractTextFactsWriter {
		private final Set<String> facts;

		public PrologFactsWriter(String fileName, String context) {
			this(fileName, context, new HashSet<>());
		}

		public PrologFactsWriter(String fileName, String context, Set<String> facts) {
			super(context, fileName);
			this.facts = facts;
		}

		@Override
		public void put(Id obj, Id relation) {
			put(String.format("%s(\"%s\",\"%s\").", relation.toRdfString(), context, obj));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\").", relation.toRdfString(), context, obj, tgt));
		}

		@Override
		public void put(Id obj, Id relation, Id tgt, Id mod) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\",\"%s\").", relation.toRdfString(), context, obj, tgt, mod));
		}

		@Override
		public void put(Id from, Id label, Id to, Id modifier1, Id modifier2) {
			put(String.format("%s(\"%s\",\"%s\",\"%s\",\"%s\", \"%s\").", label.toRdfString(), context, from, to,
					modifier1, modifier2));
		}

		private void put(String factString) {
			if (facts.add(factString)) {
				todo.add(factString);
			}
		}

		@Override
		public void save() throws IOException {
			try (PrintWriter writer = open()) {
				facts.stream().sorted().forEach((String l) -> {
					writer.println(l);
				});
				writer.close();
				saved();
			}
		}

		@Override
		public void setContext(String context) {
			this.context = context;

		}
	}

	public static IFactsWriter nTripleFactsWriter(String fileName, String context) {
		return new NTripleFactsWriter(fileName, context);
	}

	public static IFactsWriter prologFactsWriter(String fileName, String context) {
		return new PrologFactsWriter(fileName, context);
	}
}
