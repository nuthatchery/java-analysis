package org.nuthatchery.analysis.java.extractor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class IdFactory {

	private static class AuthRootId extends HashedId {
		protected static final Pattern AUTHORITY_PAT = Pattern
				.compile("^(//|)(([a-z0-9~_.\\-]|%[0-9a-f][0-9a-f]|[!$\\&'()*+,;=])+)(:[0-9]*|)$");
		protected static final Pattern SCHEME_PAT = Pattern.compile("^[A-Za-z][A-Za-z0-9+.\\-]*$");

		private final String authority;
		private final String hierPrefix;
		private final String scheme;

		public AuthRootId(String scheme, String authority, String hierPrefix) {
			super(null);
			if (!SCHEME_PAT.matcher(scheme).matches()) {
				throw new IllegalArgumentException(scheme);
			}
			this.scheme = scheme;
			this.authority = authority;
			this.hierPrefix = hierPrefix;
		}

		@Override
		public String computeUriString(boolean full) {
			return scheme + ":" + hierPrefix + authority;
		}

		@Override
		public String getAuthority() {
			return authority;
		}

		@Override
		public String getScheme() {
			return scheme;
		}

		@Override
		public boolean isContainer() {
			return true;
		}

		@Override
		public boolean isOpaque() {
			return authority == null;
		}

		@Override
		public boolean isRoot() {
			return true;
		}

		@Override
		public Id resolve(String part) {
			Id id = this;
			for (String p : part.split("/")) {
				id = id.addPath(UriEncoding.percentDecode(p));
			}
			return id;
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}

		@Override
		protected String uriSep() {
			if (authority != null) {
				return "/";
			} else {
				return "";
			}
		}
	}

	private static class FragmentBaseId extends HashedId {
		public FragmentBaseId(Id parent) {
			super(parent);
			if (parent == null || parent instanceof FragmentBaseId || parent instanceof FragmentId) {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Id addPath(String segment) {
			return parent.addPath(segment).setFragment("");
		}

		@Override
		protected String computeUriString(boolean full) {
			return parent.toUriString(full) + "#";
		}

		@Override
		public Id removeFragment() {
			return parent;
		}

		@Override
		public Id removeParam(Id key) {
			return parent.removeParam(key).setFragment("");
		}

		@Override
		public Id resolve(String part) {
			return setFragment(UriEncoding.percentDecode(part));
		}

		@Override
		public Id setFragment(String f) {
			if (f == null) {
				return removeFragment();
			} else if (f.equals("")) {
				return this;
			} else {
				return super.setFragment(f);
			}
		}

		@Override
		public Id setParam(Id key, Id val) {
			return parent.setParam(key, val).setFragment("");
		}

		@Override
		public String toRdfString() {
			return toUriString();
		}
	}

	private static class FragmentId extends HashedId {

		private final String fragment;

		public FragmentId(Id parent, String fragment) {
			super(parent);
			if (fragment == null || !(parent instanceof FragmentBaseId)) {
				throw new IllegalArgumentException();
			}
			this.fragment = fragment;
		}

		@Override
		public Id addPath(String segment) {
			return parent.addPath(segment).setFragment(fragment);
		}

		@Override
		protected String computeUriString(boolean full) {
			return parent.toUriString(full)
					+ UriEncoding.percentEncodeIri(fragment, UriEncoding.URI_EXTRA_CHARS_FRAGMENT, true);

		}

		@Override
		public Id removeFragment() {
			return parent.removeFragment();
		}

		@Override
		public Id removeParam(Id key) {
			return parent.removeParam(key).setFragment(fragment);
		}

		@Override
		public Id resolve(String part) {
			return setFragment(UriEncoding.percentDecode(part));
		}

		@Override
		public Id setFragment(String f) {
			if (f == null) {
				return removeFragment();
			} else if (fragment.equals(f)) {
				return this;
			} else {
				return parent.setFragment(f);
			}
		}

		@Override
		public Id setParam(Id key, Id val) {
			return parent.setParam(key, val).setFragment(fragment);
		}

		@Override
		public String toRdfString() {
			return toUriString();
		}
	}

	private static abstract class HashedId implements Id {
		protected static Map<String, Id> cache = new HashMap<>();
		protected static Stack<Map<String, Id>> caches = new Stack<>();
		private static final String CURRENT = ".".intern();
		protected static final Map<String, Id> namespaceIds = new HashMap<>();
		private static final String PARENT = "..".intern();
		private static final String ROOT = "/".intern();

		protected static Id addParam(Id parent, Id key, Id value) {
			String query = System.identityHashCode(parent) + "?" + System.identityHashCode(key) + "="
					+ System.identityHashCode(value);
			Id id = cache.get(query);
			if (id == null) {
				id = new ParamId(parent, key, value);
				cache.put(query, id);
			}
			return id;
		}

		public static Id auth(String scheme, String authority) {
			scheme = scheme.toLowerCase().intern();
			authority = authority.toLowerCase();
			Matcher match = AuthRootId.AUTHORITY_PAT.matcher(authority);

			if (!match.matches()) {
				throw new IllegalArgumentException(authority);
			}
			String slashes = match.group(1);
			String body = match.group(2);
			String port = match.group(4);
			if (UriSchemes.hasPort(scheme, port)) {
				port = "";
			}
			if (slashes.equals("")) {
				slashes = UriSchemes.hierPartPrefix(scheme);
			}
			String hierPrefix = slashes.intern();
			authority = (body + port).intern();

			String query = System.identityHashCode(scheme) + ":" + hierPrefix + System.identityHashCode(authority);
			Id id = cache.get(query);
			if (id == null) {
				id = new AuthRootId(scheme, authority, hierPrefix);
				cache.put(query, id);
			}
			return id;
		}

		public static Id fromUri(URI uri) {
			uri = uri.normalize();
			String scheme = uri.getScheme().toLowerCase();

			if (scheme.equals("values") && uri.getRawAuthority() != null && uri.getRawPath() != null) {
				if (uri.getRawPath().length() > 0) {
					return LiteralId.fromUri(uri.getAuthority(), uri.getRawPath());
				}
			}
			if (uri.isOpaque()) {
				String fragment = uri.getFragment();
				String schemeSpecific = uri.getRawSchemeSpecificPart();
				Id id = namespaceIds.get(scheme);
				if (id == null) {
					id = opaque(scheme);
				}
				if (schemeSpecific != null) {
					id = id.resolve(schemeSpecific);
				}
				if (fragment != null) {
					id = id.setFragment(fragment);
				}
				return id;
			} else {
				String auth = uri.getAuthority();
				Id id = auth(scheme, auth);
				String path = uri.getRawPath();
				if (path != null) {
					for (String p : path.split("/")) {
						p = UriEncoding.percentDecode(p);
						id = id.addPath(p);
					}
				}
				String query = uri.getRawQuery();
				if (query != null) {
					for (String q : query.split("&")) {
						String[] p = q.split("=", 2);
						if (p.length == 1) {
							id = id.setParam(fromUriString(p[0]));
						} else if (p.length == 2) {
							id = id.setParam(fromUriString(p[0]), fromUriString(p[1]));
						}
					}
				}
				String fragment = uri.getFragment();
				if (fragment != null) {
					id = id.setFragment(fragment);
				}
				return id;
			}
		}

		public static Id fromUriString(String uri) {
			try {
				return fromUri(new URI(uri));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}

		public static Id literal(Object obj) {
			Id type = LiteralId.getType(obj);
			String query = System.identityHashCode(type) + "[" + obj.toString() + "]";
			Id id = cache.get(query);
			if (id == null) {
				id = new LiteralId(obj);
				cache.put(query, id);
			}
			return id;
		}

		public static Id namespace(Id id, String namespace) {
			if (namespaceIds.containsKey(namespace)) {
				if (namespaceIds.get(namespace) != id) {
					throw new IllegalArgumentException(
							"Namespace " + namespace + " already defined as " + namespaceIds.get(namespace));
				}
				return id;
			}
			namespaceIds.put(namespace, id);
			if (id instanceof HashedId) {
				HashedId hid = (HashedId) id;
				if (hid.abbrv == null) {
					hid.abbrv = namespace + ":";
					System.err.println("Define namespace: " + namespace + " -> " + id);
				}
			}
			return id;
		}

		public static Id opaque(String scheme) {
			scheme = scheme.toLowerCase().intern();
			String query = System.identityHashCode(scheme) + ":";
			Id id = cache.get(query);
			if (id == null) {
				id = new AuthRootId(scheme, null, "");
				cache.put(query, id);
			}
			return id;
		}

		public static void pop() {
			if (!caches.isEmpty()) {
				cache = caches.pop();
			}
		}

		public static void push() {
			WeakHashMap<String, Id> map = new WeakHashMap<>(cache);
			caches.push(cache);
			cache = map;
		}

		@Override
		public String getNamespace() {
			if (abbrv != null) {
				return abbrv.substring(0, abbrv.length() - 1);
			} else if (parent != null) {
				return parent.getNamespace();
			} else {
				return null;
			}
		}

		private String abbrv = null;

		protected HashedId parent;

		protected String uriString;

		public HashedId(Id parent) {
			this.parent = (HashedId) parent;
		}

		@Override
		public Id addPath(Stream<String> segments) {
			Id id = this;
			for (Iterator<String> iterator = segments.iterator(); iterator.hasNext();) {
				String s = iterator.next();
				id = id.addPath(s);
			}
			return id;
		}

		@Override
		public Id addPath(String segment) {
			if (segment.equals(CURRENT) || segment.equals("")) {
				return this;
			} else if (segment.equals(PARENT)) {
				if (parent instanceof PathId) {
					return parent;
				} else {
					return this;
				}
			} else if (segment.equals(ROOT)) {
				if (parent instanceof PathId) {
					return parent.addPath(segment);
				} else {
					return parent;
				}
			}

			String query = System.identityHashCode(this) + "." + segment;
			Id id = cache.get(query);
			if (id == null) {
				id = new PathId(this, segment);
				cache.put(query, id);
			}
			return id;
		}

		protected abstract String computeUriString(boolean full);

		@Override
		public boolean equals(Object o) {
			if (o instanceof HashedId) {
				return this == o;
			} else {
				return false;
			}
		}

		@Override
		public String getAuthority() {
			return getParent().getAuthority();
		}

		@Override
		public String getFragment() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Id getParam(Id id) {
			return null;
		}

		@Override
		public Id getParent() {
			return parent;
		}

		@Override
		public Stream<String> getPath() {
			return Stream.empty();
		}

		@Override
		public String getScheme() {
			return getParent().getScheme();
		}

		@Override
		public boolean hasFragment() {
			return false;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean hasParam(Id id) {
			return false;
		}

		@Override
		public boolean isContainer() {
			return false;
		}

		@Override
		public boolean isOpaque() {
			if (parent == null) {
				return false;
			} else {
				return parent.isOpaque();
			}
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public Id removeFragment() {
			return this;
		}

		@Override
		public Id setFragment(String fragment) {
			Id base;
			if (this instanceof FragmentBaseId) {
				base = this;
			} else {
				String query = System.identityHashCode(this) + "#";
				base = cache.get(query);
				if (base == null) {
					base = new FragmentBaseId(this);
					cache.put(query, base);
				}
			}
			if (fragment.equals("")) {
				return base;
			} else {
				fragment = fragment.intern();
				String query = System.identityHashCode(base) + "#" + System.identityHashCode(fragment);
				Id id = cache.get(query);
				if (id == null) {
					id = new FragmentId(base, fragment);
					cache.put(query, id);
				}
				return id;
			}
		}

		@Override
		public Id setParam(Id key) {
			return setParam(key, IdFactory.TRUE);
		}

		@Override
		public Id setParam(Id key, Id value) {
			return addParam(this, key, value);
		}

		@Override
		public String toString() {
			return toUriString();
		}
		@Override
		public final String toFullUriString() {
			return computeUriString(true);
		}

		protected String toUriString(boolean full) {
			if(full) {
				return computeUriString(full);
			} else {
				return toUriString();
			}
		}

		@Override
		@SuppressWarnings("unused")
		public final String toUriString() {
			if (abbrv != null) {
				return abbrv;
			}
			if (uriString == null) {
				uriString = computeUriString(false);
				if (false) {
					try {
						URI uri = new URI(uriString);
						Id id = HashedId.fromUri(uri);
						if (this != id) {
							System.err.println(ClassFactExtractor.className);
							assert this == id : "Whoops: uri=" + uriString + ", this=" + this + " != id=" + id;
						}
					} catch (URISyntaxException e) {
						System.err.println("Failed: " + uriString);
						e.printStackTrace();
					}
				}
			}
			return uriString;
		}

		protected String uriSep() {
			if (abbrv != null) {
				return "";
			} else {
				return "/";
			}
		}
	}

	private static class LiteralId extends HashedId {

		public static Id fromUri(String auth, String path) {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			path = UriEncoding.percentDecode(path);

			switch (auth) {
			case "boolean":
				return HashedId.literal(Boolean.valueOf(path));
			case "integer":
				return HashedId.literal(Long.valueOf(path));
			case "double":
				return HashedId.literal(Double.valueOf(path));
			case "float":
				return HashedId.literal(Float.valueOf(path));
			case "decimal":
				return HashedId.literal(Double.valueOf(path));
			case "string":
				return HashedId.literal(path);
			default:
				throw new IllegalArgumentException("type not found for: " + auth + path);
			}
		}

		public static Id getType(Object obj) {
			if (obj instanceof Boolean) {
				return IdFactory.TYPE_BOOLEAN;
			} else if (obj instanceof Integer || obj instanceof Short || obj instanceof Byte || obj instanceof Long
					|| obj instanceof BigInteger) {
				return IdFactory.TYPE_INTEGER;
			} else if (obj instanceof Double) {
				return IdFactory.TYPE_DOUBLE;
			} else if (obj instanceof Float) {
				return IdFactory.TYPE_FLOAT;
			} else if (obj instanceof BigDecimal) {
				return IdFactory.TYPE_DECIMAL;
			} else if (obj instanceof String) {
				return IdFactory.TYPE_STRING;
			} else {
				return null;
			}
		}

		private final String stringRep;

		private final Id type;

		private final String uriPath;

		public LiteralId(Object obj) {
			super(null);
			String uriPath = null;
			this.type = getType(obj);
			String stringRep = obj.toString();

			if (type == IdFactory.TYPE_BOOLEAN) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_BOOLEAN;
			} else if (type == IdFactory.TYPE_INTEGER) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_INTEGER;
			} else if (type == IdFactory.TYPE_DOUBLE) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_DOUBLE;
			} else if (type == IdFactory.TYPE_FLOAT) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_FLOAT;
			} else if (type == IdFactory.TYPE_DECIMAL) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_DECIMAL;
			} else if (type == IdFactory.TYPE_STRING) {
				this.parent = (HashedId) IdFactory.ROOT_VALUES_STRING;
				stringRep = JavaUtil.quote(obj.toString());
				uriPath = UriEncoding.percentEncodeIri(obj.toString(), UriEncoding.URI_EXTRA_CHARS_PATH, true);
			} else {
				throw new IllegalArgumentException("type not found for: " + obj);
			}
			this.stringRep = stringRep;
			if (uriPath == null) {
				this.uriPath = UriEncoding.percentEncodeIri(obj.toString(), UriEncoding.URI_EXTRA_CHARS_PATH, true);
			} else {
				this.uriPath = uriPath;
			}
		}

		@Override
		public Id addPath(String segment) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected String computeUriString(boolean full) {
			return parent.toUriString(full) + "/" + uriPath;
		}

		@Override
		public Stream<String> getPath() {
			return Stream.of(uriPath);
		}

		@Override
		public Id resolve(String rawSchemeSpecificPart) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Id setFragment(String fragment) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toRdfString() {
			// if(stringRep.startsWith("\"")) {
			// return stringRep + "^^" + type.toUriString();
			// } else {
			return stringRep;
			// }
		}

		@Override
		public String toString() {
			return stringRep;
		}
	}

	private static class ParamId extends HashedId {
		private final HashedId paramKey;
		private final HashedId paramVal;

		public ParamId(Id parent, Id key, Id value) {
			super(parent);
			if (parent == null) {
				throw new IllegalArgumentException();
			}
			if (!(parent.isContainer() || parent.hasParams())) {
				throw new IllegalArgumentException();
			}

			this.paramKey = (HashedId) key;
			this.paramVal = (HashedId) value;
		}

		@Override
		public Id addPath(String segment) {
			return HashedId.addParam(parent.addPath(segment), paramKey, paramVal);
		}

		private Id changeParam(Id key, Id value) {
			if (paramKey == key) {
				if (paramVal == value) {
					return this;
				} else {
					return HashedId.addParam(parent, key, value);
				}
			} else if (parent instanceof ParamId) {
				return ((ParamId) parent).changeParam(key, value);
			} else {
				return null;
			}
		}

		@Override
		public String computeUriString(boolean full) {
			String base = parent.toUriString(full);
			if (parent.hasParams()) {
				base += "&";
			} else {
				base += "?";
			}

			base += UriEncoding.percentEncodeIri(paramKey.computeUriString(full), UriEncoding.URI_EXTRA_CHARS_QUERY, true);

			if (paramVal != IdFactory.TRUE) {
				base += "=";
				base += UriEncoding.percentEncodeIri(paramVal.computeUriString(full), UriEncoding.URI_EXTRA_CHARS_QUERY, true);
			}
			return base;
		}

		@Override
		public Id getParam(Id paramKey) {
			if (this.paramKey.equals(paramKey)) {
				return paramVal;
			}
			return parent.getParam(paramKey);
		}

		@Override
		public Stream<String> getPath() {
			return parent.getPath();
		}

		@Override
		public boolean hasParam(Id paramKey) {
			return getParam(paramKey) != null;
		}

		@Override
		public boolean hasParams() {
			return true;
		}

		@Override
		public Id removeParam(Id key) {
			if (paramKey.equals(key)) {
				return parent;
			} else {
				return HashedId.addParam(parent.removeParam(key), paramKey, paramVal);
			}
		}

		@Override
		public Id resolve(String part) {
			Id id = this;
			for (String q : part.split("&")) {
				String p[] = q.split("=", 2);
				if (p.length == 1) {
					id = id.setParam(HashedId.fromUriString(UriEncoding.percentDecode(p[0])));
				} else if (p.length == 2) {
					id = id.setParam(HashedId.fromUriString(UriEncoding.percentDecode(p[0])),
							HashedId.fromUriString(UriEncoding.percentDecode(p[1])));
				}
			}
			return id;
		}

		@Override
		public Id setParam(Id key, Id value) {
			Id id = changeParam(key, value);
			if (id == null) {
				return HashedId.addParam(this, key, value);
			} else {
				return id;
			}
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}

	}

	private static class PathId extends HashedId {
		// protected static final Pattern SEGMENT_PAT = Pattern.compile(
		// "([A-Za-z0-9~_.\\-]|[\u00a1-\u167f\u1681-\u180d\u180f-\u1fff\u200b-\u2027\u202a-\u202e\u2030-\u205e\2060-\2fff\u3001-\ufffd]|%[0-9A-Fa-f][0-9A-Fa-f]|[!$\\&'()*+,;=:@])*");

		private final String segment;

		public PathId(Id parent, String segment) {
			super(parent);

			if (parent == null || !parent.isContainer()) {
				throw new IllegalArgumentException();
			}

			this.segment = segment;// UriEncoding.percentEncodeIri(segment,
			// UriEncoding.URI_EXTRA_CHARS_PATH, true);
		}

		@Override
		public String computeUriString(boolean full) {
			return parent.toUriString(full) + parent.uriSep()
			+ UriEncoding.percentEncodeIri(segment, UriEncoding.URI_EXTRA_CHARS_PATH + "()", true);
		}

		@Override
		public Stream<String> getPath() {
			return getPathBuilder().build();
		}

		public Builder<String> getPathBuilder() {
			Builder<String> builder = parent instanceof PathId ? ((PathId) parent).getPathBuilder() : Stream.builder();
			return builder.add(segment);
		}

		@Override
		public boolean isContainer() {
			return true;
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public Id resolve(String part) {
			Id id = this;
			for (String p : part.split("/")) {
				id = id.addPath(UriEncoding.percentDecode(p));
			}
			return id;
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}
	}

	static final Id FALSE = new LiteralId(false);
	static final Id ONE = new LiteralId(1);
	static final Id ROOT_W3 = HashedId.auth("http", "//www.w3.org");
	static final Id ROOT_RDF = HashedId
			.namespace(ROOT_W3.addPath("1999").addPath("02").addPath("22-rdf-syntax-ns").setFragment(""), "rdf");
	static final Id ROOT_RDFS = HashedId
			.namespace(ROOT_W3.addPath("2000").addPath("01").addPath("rdf-schema").setFragment(""), "rdfs");
	static final Id ROOT_VALUES_BOOLEAN = HashedId.auth("values", "//boolean");
	static final Id ROOT_VALUES_DECIMAL = HashedId.auth("values", "//decimal");
	static final Id ROOT_VALUES_DOUBLE = HashedId.auth("values", "//double");
	static final Id ROOT_VALUES_FLOAT = HashedId.auth("values", "//float");
	static final Id ROOT_VALUES_INTEGER = HashedId.auth("values", "//integer");
	static final Id ROOT_VALUES_STRING = HashedId.auth("values", "//string");
	static final Id ROOT_XSD = HashedId.namespace(ROOT_W3.addPath("2001").addPath("XMLSchema").setFragment(""), "xsd");
	static final Id TRUE = new LiteralId(true);
	static final Id TYPE_BOOLEAN = ROOT_XSD.setFragment("boolean");
	static final Id TYPE_DECIMAL = ROOT_XSD.setFragment("decimal");

	static final Id TYPE_DOUBLE = ROOT_XSD.setFragment("double");

	static final Id TYPE_FLOAT = ROOT_XSD.setFragment("float");

	static final Id TYPE_INTEGER = ROOT_XSD.setFragment("integer");

	static final Id TYPE_STRING = ROOT_XSD.setFragment("string");

	static final Id ZERO = new LiteralId(0);

	public static Id id(Id root, String... path) {
		for (String p : path) {
			root = root.addPath(p);
		}
		return root;
	}

	public static Id literal(Object s) {
		return HashedId.literal(s);
	}

	public static void pop() {
		HashedId.pop();
	}

	public static void push() {
		HashedId.push();
	}

	public static Id root(String scheme, String authority) {
		return HashedId.auth(scheme, authority);
	}

	public static Id namespace(Id id, String namespace) {
		return HashedId.namespace(id, namespace);
	}

	public static Id getNamespace(String ns) {
		return HashedId.namespaceIds.get(ns);
	}
}
