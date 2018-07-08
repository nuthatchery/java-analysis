package org.nuthatchery.ontology;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class NModel {
	public static class ListBuilder implements AutoCloseable {
		private Resource head = RDF.nil;
		private Resource current = null;
		private final NModel model;

		public ListBuilder(NModel m) {
			this.model = m;
		}

		public void add(RDFNode t) {
			Resource node = model.blank();
			model.add(node, RDF.first, t);
			if (current == null) {
				head = node;
			} else {
				model.add(current, RDF.rest, node);
			}
			current = node;
		}

		public Resource build() {
			if (current != null) {
				model.add(current, RDF.rest, RDF.nil);
			}
			return head;
		}

		@Override
		public void close() {
			build();
		}
	}

	/**
	 * Determine the type of an object.
	 *
	 * <ul>
	 * <li>{@link XSDDatatype.XSDBOOLEAN} for Boolean
	 * <li>{@link XSDDatatype.XSDBOOLEAN} for Boolean
	 * <li>{@link XSDDatatype.XSDINTEGER} for Integer, Short, Byte, Long, BigInteger
	 * <li>{@link XSDDatatype.XSDDOUBLE} for Double
	 * <li>{@link XSDDatatype.XSDFLOAT} for Float
	 * <li>{@link XSDDatatype.XSDDECIMAL} for BigDecimal
	 * <li>{@link XSDDatatype.XSDSTRING} for String
	 * <li><code>null</code> otherwise
	 * </ul>
	 *
	 * @param obj
	 * @return XSD type of <code>obj</code>
	 */
	public static XSDDatatype getType(Object obj) {
		if (obj instanceof Boolean)
			return XSDDatatype.XSDboolean;
		else if (obj instanceof Integer || obj instanceof Short || obj instanceof Byte || obj instanceof Long
				|| obj instanceof BigInteger)
			return XSDDatatype.XSDint;
		else if (obj instanceof Double)
			return XSDDatatype.XSDdouble;
		else if (obj instanceof Float)
			return XSDDatatype.XSDfloat;
		else if (obj instanceof BigDecimal)
			return XSDDatatype.XSDdecimal;
		else if (obj instanceof String)
			return XSDDatatype.XSDstring;
		else {
			System.err
			.println("Mode.getType(): can't find type of " + obj.getClass().toString() + " " + obj.toString());
			return null;
		}
	}

	private final String prefix;
	private final Resource name;

	private Model model;

	private final Dataset dataset;

	public NModel(Dataset ds, String prefix) {
		this(ds, prefix.substring(0, prefix.length() - 1), prefix);
	}

	public NModel(Dataset dataset, String name, String prefix) {
		super();
		if (!(prefix.endsWith("#") || prefix.endsWith("/") || prefix.endsWith(":") || prefix.isEmpty()))
			throw new IllegalArgumentException("Prefix should end with #, / or :");
		this.dataset = dataset;
		this.model = ModelFactory.createDefaultModel();
		this.name = model.createResource(name);
		if (dataset != null) {
			dataset.addNamedModel(name, model);
		}
		this.prefix = prefix;
	}

	public NModel(Model model, String prefix) {
		this(null, prefix.substring(0, prefix.length() - 1), prefix);
		this.model = model;
	}

	public NModel(String prefix) {
		this(null, prefix.substring(0, prefix.length() - 1), prefix);
	}

	public void add(Resource subject, Property predicate, RDFNode object) {
		model.add(subject, predicate, object);
	}

	/**
	 * Create a blank node
	 *
	 * @return A blank node, distinct from any other blank node
	 */
	public Resource blank() {
		return model.createResource();
	}

	/**
	 * Create a named blank node
	 *
	 * @param s
	 *            Node name
	 * @return A named blank node, equal to other blank nodes with the same name in
	 *         the same model
	 */
	public Resource blank(String s) {
		return model.createResource(AnonId.create(s));
	}

	public boolean contains(Resource subject, Property predicate, RDFNode object) {
		if (model != null)
			return model.contains(subject, predicate, object);
		else
			return false;
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
	public Model getGraph() {

		return model;
	}

	/**
	 * @return Name of this model
	 */
	public Resource getName() {
		return name;
	}

	/**
	 * @return True if this model has an underlying dataset and
	 *         {@link #getDataset()} returns non-null.
	 */
	public boolean hasDataset() {
		return dataset != null;
	}

	/**
	 * Create an IRI
	 *
	 * @param fullName
	 *            Full name of the IRI
	 * @return An IRI
	 */
	public Resource iri(String fullName) {
		return model.createResource(fullName);
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
	 * Create a decimal literal
	 *
	 * @param d
	 *            A decimal
	 * @return A literal
	 */
	public Literal literal(BigDecimal d) {
		return model.createTypedLiteral(d.toString(), XSDDatatype.XSDdecimal);
	}

	/**
	 * Create an integer literal
	 *
	 * @param i
	 *            An integer
	 * @return A literal
	 */
	public Literal literal(BigInteger i) {
		return model.createTypedLiteral(i.toString(), XSDDatatype.XSDint);
	}

	/**
	 * Create a boolean
	 *
	 * @param b
	 *            True or false
	 * @return A literal
	 */
	public Literal literal(boolean b) {
		return model.createTypedLiteral(String.valueOf(b), XSDDatatype.XSDboolean);
	}

	/**
	 * Create a floating point literal
	 *
	 * @param d
	 *            A double-precision floating point value
	 * @return A literal
	 */
	public Literal literal(double d) {
		return model.createTypedLiteral(String.valueOf(d), XSDDatatype.XSDdouble);
	}

	/**
	 * Create an integer literal
	 *
	 * @param l
	 *            An integer
	 * @return A literal
	 */
	public Literal literal(long l) {
		return model.createTypedLiteral(String.valueOf(l), XSDDatatype.XSDint);
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
		return model.createTypedLiteral(String.valueOf(obj), getType(obj));
	}

	/**
	 * Create a string literal
	 *
	 * @param s
	 *            The string
	 * @return A literal
	 */
	public Literal literal(String s) {
		return model.createTypedLiteral(s, XSDDatatype.XSDstring);
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
	public Literal literal(String s, XSDDatatype type) {
		return model.createTypedLiteral(s, type);
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
		return model.createTypedLiteral(s, lang);
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
	public NModel model(String prefix) {
		return new NModel(dataset, prefix);
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
	public NModel model(String name, String prefix) {
		return new NModel(dataset, name, prefix);
	}

	public Resource node(Resource node, String subPath) {
		return model.createResource(node.getURI() + "/" + subPath);
	}

	/**
	 * Create an IRI for a node
	 *
	 * @param shortName
	 *            Local name of the node, to be combined with the model's prefix
	 * @return An IRI
	 */
	public Resource node(String shortName) {
		return model.createResource(prefix + shortName);
	}
}
