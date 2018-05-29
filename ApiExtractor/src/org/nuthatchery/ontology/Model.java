package org.nuthatchery.ontology;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.simple.Types;
import org.nuthatchery.ontology.standard.RdfVocabulary;

public class Model {
	private final RDF factory;
	private final String prefix;
	private final IRI name;
	private Graph graph;
	private final Dataset dataset;

	public Model(Dataset dataset, String name, String prefix, RDF factory) {
		super();
		if (!(prefix.endsWith("#") || prefix.endsWith("/") || prefix.endsWith(":") || prefix.isEmpty())) {
			throw new IllegalArgumentException("Prefix should end with #, / or :");
		}
		this.dataset = dataset;
		this.graph = null;
		this.name = factory.createIRI(name);
		this.prefix = prefix;
		this.factory = factory;
	}

	public Model(Dataset ds, String prefix, RDF factory) {
		this(ds, prefix.substring(0, prefix.length() - 1), prefix, factory);
	}

	public Model(String prefix, RDF factory) {
		this(null, prefix.substring(0, prefix.length() - 1), prefix, factory);
	}

	public Model(Graph graph, String prefix, RDF factory) {
		this(null, prefix.substring(0, prefix.length() - 1), prefix, factory);
		this.graph = graph;
	}

	/**
	 * Create an IRI for a node
	 *
	 * @param shortName
	 *            Local name of the node, to be combined with the model's prefix
	 * @return An IRI
	 */
	public IRI node(String shortName) {
		return factory.createIRI(prefix + shortName);
	}

	/**
	 * Create an IRI
	 *
	 * @param fullName
	 *            Full name of the IRI
	 * @return An IRI
	 */
	public IRI iri(String fullName) {
		return factory.createIRI(fullName);
	}

	/**
	 * Create a blank node
	 *
	 * @return A blank node, distinct from any other blank node
	 */
	public BlankNode blank() {
		return factory.createBlankNode();
	}

	/**
	 * Create a list builder
	 *
	 * @return A list builder
	 */
	public ListBuilder list() {
		return new ListBuilder(this);
	}

	/**
	 * Create a named blank node
	 *
	 * @param s
	 *            Node name
	 * @return A named blank node, equal to other blank nodes with the same name in
	 *         the same model
	 */
	public BlankNode blank(String s) {
		return factory.createBlankNode(s);
	}

	/**
	 * Create a string literal
	 *
	 * @param s
	 *            The string
	 * @return A literal
	 */
	public Literal literal(String s) {
		return factory.createLiteral(s);
	}

	/**
	 * Create a language-specific string literal
	 *
	 * @param s
	 *            The string
	 * @param lang
	 *            The language
	 * @return A literal
	 */
	public Literal literal(String s, String lang) {
		return factory.createLiteral(s, lang);
	}

	/**
	 * Create a typed literal
	 *
	 * @param s
	 *            Lexical representation of the literal
	 * @param type
	 *            IRI of the type
	 * @return A literal
	 */
	public Literal literal(String s, IRI type) {
		return factory.createLiteral(s, type);
	}

	/**
	 * Create a boolean
	 *
	 * @param b
	 *            True or false
	 * @return A literal
	 */
	public Literal literal(boolean b) {
		return factory.createLiteral(String.valueOf(b), Types.XSD_BOOLEAN);
	}

	/**
	 * Create an integer literal
	 *
	 * @param l
	 *            An integer
	 * @return A literal
	 */
	public Literal literal(long l) {
		return factory.createLiteral(String.valueOf(l), Types.XSD_INTEGER);
	}

	/**
	 * Create an integer literal
	 *
	 * @param i
	 *            An integer
	 * @return A literal
	 */
	public Literal literal(BigInteger i) {
		return factory.createLiteral(i.toString(), Types.XSD_INTEGER);
	}

	/**
	 * Create a decimal literal
	 *
	 * @param d
	 *            A decimal
	 * @return A literal
	 */
	public Literal literal(BigDecimal d) {
		return factory.createLiteral(d.toString(), Types.XSD_DECIMAL);
	}

	/**
	 * Create a floating point literal
	 *
	 * @param d
	 *            A double-precision floating point value
	 * @return A literal
	 */
	public Literal literal(double d) {
		return factory.createLiteral(String.valueOf(d), Types.XSD_DOUBLE);
	}

	/**
	 * Create a literal.
	 *
	 * The literal's type is automatically determined from the object, according to
	 * {@link #getType(Object)}.
	 *
	 * @param obj
	 *            An object
	 * @return A literal
	 */
	public Literal literal(Object obj) {
		return factory.createLiteral(String.valueOf(obj), getType(obj));
	}

	/**
	 * Create a new model, with the given prefix.
	 * <p>
	 * The name will be the prefix minus the trailing separator. The model will use
	 * the same RDF factory, so blank identifiers created with
	 * {@link #blank(String)} in the new model will be {@link #equals(Object)} to
	 * blanks in this model.
	 * <p>
	 * If this model is based on a dataset, the new model will share the dataset,
	 * otherwise a new graph will be created.
	 *
	 * @param prefix
	 *            IRI prefix, should end in <code>#</code>, <code>/</code> or
	 *            <code>:</code>.
	 * @return A new model
	 */
	public Model model(String prefix) {
		return new Model(dataset, prefix, factory);
	}

	/**
	 * Create a new model, with the given prefix.
	 * <p>
	 * The model will use the same RDF factory, so blank identifiers created with
	 * {@link #blank(String)} in the new model will be {@link #equals(Object)} to
	 * blanks in this model.
	 * <p>
	 * If this model is based on a dataset, the new model will share the dataset,
	 * otherwise a new graph will be created.
	 *
	 * @param prefix
	 *            IRI prefix, should end in <code>#</code>, <code>/</code> or
	 *            <code>:</code>.
	 * @return A new model
	 */
	public Model model(String name, String prefix) {
		return new Model(dataset, name, prefix, factory);
	}

	/**
	 * Create a new model, with the given name and prefix.
	 * <p>
	 * The name will be the prefix minus the trailing separator. The model will use
	 * the same RDF factory, so blank identifiers created with
	 * {@link #blank(String)} in the new model will be {@link #equals(Object)} to
	 * blanks in this model.
	 * <p>
	 * If this model is based on a dataset, the new model will share the dataset,
	 * otherwise a new graph will be created.
	 *
	 * @param prefix
	 *            IRI prefix
	 * @param factory
	 *            An RDF factory
	 * @return A new model
	 */
	public Model model(String prefix, RDF factory) {
		return new Model(prefix, factory);
	}

	/**
	 * @return Name of this model
	 */
	public IRI getName() {
		return name;
	}

	/**
	 * @return Dataset of this model, or null if the model is based on graph.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @return Graph of this model; this may be a subgraph of {@link #getDataset()}
	 */
	public Graph getGraph() {
		if (dataset != null) {
			return dataset.getGraph(name).get();
		} else {
			ensureGraph();
			return graph;
		}
	}

	private void ensureGraph() {
		if (graph == null) {
			graph = factory.createGraph();
		}
	}

	/**
	 * @return True if this model has an underlying dataset and
	 *         {@link #getDataset()} returns non-null.
	 */
	public boolean hasDataset() {
		return dataset != null;
	}

	@SuppressWarnings("unchecked")
	public <T extends RDF> T getFactory(Class<T> factoryClazz) {
		if (factoryClazz.isInstance(factory)) {
			return (T) factory;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public RDF getFactory() {
		return factory;
	}

	public void add(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
		if (dataset != null) {
			dataset.add(name, subject, predicate, object);
		} else {
			ensureGraph();
			graph.add(subject, predicate, object);
		}
	}

	public boolean contains(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
		if (dataset != null) {
			return dataset.contains(Optional.of(name), subject, predicate, object);
		} else if (graph != null) {
			return graph.contains(subject, predicate, object);
		} else {
			return false;
		}
	}

	public Stream<? extends Triple> stream() {
		return getGraph().stream();
	}

	public Stream<? extends Triple> stream(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
		return getGraph().stream(subject, predicate, object);
	}

	public IRI node(IRI node, String subPath) {
		return factory.createIRI(node.getIRIString() + "/" + subPath);
	}

	/**
	 * Determine the type of an object.
	 *
	 * <ul>
	 * <li>{@link Types.XSD_BOOLEAN} for Boolean
	 * <li>{@link Types.XSD_BOOLEAN} for Boolean
	 * <li>{@link Types.XSD_INTEGER} for Integer, Short, Byte, Long, BigInteger
	 * <li>{@link Types.XSD_DOUBLE} for Double
	 * <li>{@link Types.XSD_FLOAT} for Float
	 * <li>{@link Types.XSD_DECIMAL} for BigDecimal
	 * <li>{@link Types.XSD_STRING} for String
	 * <li><code>null</code> otherwise
	 * </ul>
	 *
	 * @param obj
	 * @return XSD type of <code>obj</code>
	 */
	public static IRI getType(Object obj) {
		if (obj instanceof Boolean) {
			return Types.XSD_BOOLEAN;
		} else if (obj instanceof Integer || obj instanceof Short || obj instanceof Byte || obj instanceof Long
				|| obj instanceof BigInteger) {
			return Types.XSD_INTEGER;
		} else if (obj instanceof Double) {
			return Types.XSD_DOUBLE;
		} else if (obj instanceof Float) {
			return Types.XSD_FLOAT;
		} else if (obj instanceof BigDecimal) {
			return Types.XSD_DECIMAL;
		} else if (obj instanceof String) {
			return Types.XSD_STRING;
		} else {
			System.err
			.println("Mode.getType(): can't find type of " + obj.getClass().toString() + " " + obj.toString());
			return null;
		}
	}

	public static class ListBuilder implements AutoCloseable {
		private static final IRI NIL = RdfVocabulary.getInstance().RDF_NIL;
		private static final IRI FIRST = RdfVocabulary.getInstance().RDF_FIRST;
		private static final IRI REST = RdfVocabulary.getInstance().RDF_REST;
		private static final IRI LIST = RdfVocabulary.getInstance().RDF_LIST;
		private static final IRI TYPE = RdfVocabulary.getInstance().RDF_TYPE;
		private BlankNodeOrIRI head = NIL;
		private BlankNodeOrIRI current = null;
		private final Model model;

		public ListBuilder(Model m) {
			this.model = m;
		}

		public void add(RDFTerm t) {
			BlankNode node = model.blank();
			//			model.add(node, TYPE, LIST);
			model.add(node, FIRST, t);
			if(current == null) {
				head = node;
			} else {
				model.add(current, REST, node);
			}
			current = node;
		}
		public BlankNodeOrIRI build() {
			if(current != null) {
				model.add(current, REST, NIL);
			}
			return head;
		}

		@Override
		public void close() {
			build();
		}
	}
}
