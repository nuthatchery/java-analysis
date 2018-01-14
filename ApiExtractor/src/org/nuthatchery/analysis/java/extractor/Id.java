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

public interface Id {
	static final Id TRUE = new LiteralId(true);
	static final Id FALSE = new LiteralId(false);
	static final Id ZERO = new LiteralId(0);
	static final Id ONE = new LiteralId(1);
	static final Id TYPE_BOOLEAN = new RootId("xsd", "boolean");
	static final Id TYPE_INTEGER= new RootId("xsd", "integer");
	static final Id TYPE_DECIMAL = new RootId("xsd", "decimal");
	static final Id TYPE_FLOAT = new RootId("xsd", "float");
	static final Id TYPE_DOUBLE = new RootId("xsd", "double");
	static final Id TYPE_STRING = new RootId("xsd", "string");

	public static Id string(String s) { return new LiteralId(s);}
	public static Id literal(Object s) { return new LiteralId(s);}
	public static Id id(String scheme, String auth, String...path) {
		return new RootId(scheme, auth).addPathSegments(path);
	}
	public static Id id(Id root, String...path) {
		return root.addPathSegments(path);
	}
	
	List<String> getPath();

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

	default Id addPathSegments(String... pathSegments) {
		return new PathId(this, pathSegments);
	}
	default Id addSubPath(String subPath) {
		return new PathId(this, subPath.split("/"));
	}
	default Id addSubPath(List<String> subPath) {
		return new PathId(this, subPath);
	}

	default List<Id> getParams() {
		throw new UnsupportedOperationException("does not support parameters");
	}

	default Map<Id, Id> getParamMap() {
		throw new UnsupportedOperationException("does not support parameters");
	}

	default Id getParam(Id paramKey) {
		throw new UnsupportedOperationException("does not support parameters");
	}

	default Id addParam(Id paramKey) {
		return new ParamId(this, paramKey);
	}

	default Id addParam(Id paramKey, Id paramValue) {
		return new ParamId(this, paramKey, paramValue);
	}

	default String getFragment() {
		throw new UnsupportedOperationException("does not support parameters");
	}

	Id getParent();

	String toString();

	String toUriString();

	String toRdfString();

	boolean isRoot();

	boolean isContainer();

	default boolean hasFragment() {
		return false;
	}

	public static Id root(String scheme, String authority) {
		return new RootId(scheme, authority);
	}

	public static class RootId implements Id {
		protected static final Pattern SCHEME_PAT = Pattern.compile("[A-Za-z][A-Za-z0-9+.\\-]*");
		protected static final Pattern AUTHORITY_PAT = Pattern
				.compile("(//)?([A-Za-z0-9~_.\\-]|%[0-9A-Fa-f][0-9A-Fa-f]|[!$\\&'()*+,;=])+(:[0-9]*)?");

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((authority == null) ? 0 : authority.hashCode());
			result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof RootId)) {
				return false;
			}
			RootId other = (RootId) obj;
			if (authority == null) {
				if (other.authority != null) {
					return false;
				}
			} else if (!authority.equals(other.authority)) {
				return false;
			}
			if (scheme == null) {
				if (other.scheme != null) {
					return false;
				}
			} else if (!scheme.equals(other.scheme)) {
				return false;
			}
			return true;
		}

		private final String scheme;
		private final String authority;

		public RootId(String scheme, String authority) {
			if (!SCHEME_PAT.matcher(scheme).matches())
				throw new IllegalArgumentException(scheme);
			if (!AUTHORITY_PAT.matcher(authority).matches())
				throw new IllegalArgumentException(authority);
			this.scheme = scheme.toLowerCase();
			this.authority = authority.toLowerCase();
		}

		@Override
		public List<String> getPath() {
			return Collections.emptyList();
		}

		@Override
		public String getScheme() {
			return scheme;
		}

		@Override
		public String getAuthority() {
			return authority;
		}

		@Override
		public Id getParent() {
			return null;
		}

		@Override
		public String toUriString() {
			return scheme + ":" + authority;
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}

		@Override
		public String toString() {
			return toUriString();
		}

		@Override
		public boolean isRoot() {
			return true;
		}

		@Override
		public boolean isContainer() {
			return true;
		}

	}

	public static class PathId implements Id {
		// protected static final Pattern SEGMENT_PAT = Pattern.compile(
		// "([A-Za-z0-9~_.\\-]|[\u00a1-\u167f\u1681-\u180d\u180f-\u1fff\u200b-\u2027\u202a-\u202e\u2030-\u205e\2060-\2fff\u3001-\ufffd]|%[0-9A-Fa-f][0-9A-Fa-f]|[!$\\&'()*+,;=:@])*");

		private final List<String> path;
		private final Id parent;

		public PathId(Id parent, String... pathSegments) {
			this(parent, Arrays.asList(pathSegments));
		}

		public PathId(Id parent, List<String> pathSegments) {
			if (parent == null || !parent.isContainer())
				throw new IllegalArgumentException();

			this.parent = parent;
			List<String> path = new ArrayList<>(parent.getPath().size() + pathSegments.size());
			path.addAll(parent.getPath());
			boolean first = true;
			for (String p : pathSegments) {
				if (first && p.equals("/")) {
					path.clear();
				} else if (p.equals(".")) {
				} else if (p.equals("..")) {
					if (!path.isEmpty())
						path.remove(path.size() - 1);
				} else {
					path.add(UriEncoding.percentEncodeIri(p, "", true));
				}
			}
			this.path = Collections.unmodifiableList(path);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof PathId)) {
				return false;
			}
			PathId other = (PathId) obj;
			if (parent == null) {
				if (other.parent != null) {
					return false;
				}
			} else if (!parent.equals(other.parent)) {
				return false;
			}
			if (path == null) {
				if (other.path != null) {
					return false;
				}
			} else if (!path.equals(other.path)) {
				return false;
			}
			return true;
		}

		@Override
		public List<String> getPath() {
			return path;
		}

		@Override
		public String getScheme() {
			return parent.getScheme();
		}

		@Override
		public String getAuthority() {
			return parent.getAuthority();
		}

		@Override
		public Id getParent() {
			return parent;
		}

		@Override
		public String toUriString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getScheme());
			sb.append(":");
			sb.append(getAuthority());
			for (String p : path) {
				sb.append("/");
				sb.append(p);
			}
			return sb.toString();
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}

		@Override
		public String toString() {
			return toUriString();
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public boolean isContainer() {
			return true;
		}
	}

	public static class ParamId implements Id {
		private final Id parent;
		private final List<Id> params;
		private final Map<Id, Id> paramMap;

		public ParamId(Id parent, Id key) {
			this(parent, key, Id.TRUE, new ArrayList<>(), new HashMap<>());
		}

		public ParamId(Id parent, Id key, Id value) {
			this(parent, key, value, new ArrayList<>(), new HashMap<>());
		}

		private ParamId(Id parent, Id key, Id value, List<Id> params, Map<Id, Id> paramMap) {
			if (parent == null || !parent.isContainer())
				throw new IllegalArgumentException();
			this.parent = parent;

			if (key != null) {
				params.add(key);
				paramMap.put(key, value != null ? value : Id.TRUE);
			}
			this.params = Collections.unmodifiableList(params);
			this.paramMap = Collections.unmodifiableMap(paramMap);
		}

		@Override
		public List<String> getPath() {
			return parent.getPath();
		}

		@Override
		public String getScheme() {
			return parent.getScheme();
		}

		@Override
		public String getAuthority() {
			return parent.getAuthority();
		}

		@Override
		public Id getParent() {
			return parent;
		}

		@Override
		public String toUriString() {
			StringBuilder sb = new StringBuilder();
			sb.append(parent.toUriString());
			sb.append("?");
			for (Id key : params) {
				sb.append(UriEncoding.percentEncodeIri(key.toUriString(), UriEncoding.URI_EXTRA_CHARS_QUERY, true));
				if (paramMap.get(key) != Id.TRUE) {
					sb.append("=");
					sb.append(UriEncoding.percentEncodeIri(paramMap.get(key).toUriString(),
							UriEncoding.URI_EXTRA_CHARS_QUERY, true));
				}
			}
			return sb.toString();
		}

		@Override
		public String toRdfString() {
			return "<" + toUriString() + ">";
		}

		@Override
		public String toString() {
			return toUriString();
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public boolean isContainer() {
			return false;
		}

		@Override
		public List<Id> getParams() {
			return params;
		}

		@Override
		public Map<Id, Id> getParamMap() {
			return paramMap;
		}

		@Override
		public Id getParam(Id paramKey) {
			return paramMap.get(paramKey);
		}

		@Override
		public boolean hasParam(Id paramKey) {
			return paramMap.containsKey(paramKey);
		}

		@Override
		public String getFragment() {
			return null;
		}

		@Override
		public boolean hasParams() {
			return true;
		}

		@Override
		public boolean hasFragment() {
			return false;
		}

		@Override
		public Id addParam(Id paramKey) {
			return new ParamId(parent, paramKey, Id.TRUE, new ArrayList<>(params), new HashMap<>(paramMap));
		}

		@Override
		public Id removeParam(Id paramKey) {
			ArrayList<Id> pl = new ArrayList<>(params);
			HashMap<Id, Id> pm = new HashMap<>(paramMap);
			pl.remove(paramKey);
			pm.remove(paramKey);
			return new ParamId(parent, null, null, pl, pm);
		}

		@Override
		public Id addParam(Id paramKey, Id paramValue) {
			return new ParamId(parent, paramKey, paramValue, new ArrayList<>(params), new HashMap<>(paramMap));
		}

		public Id addPathSegments(String... pathSegments) {
			return new ParamId(parent.addPathSegments(pathSegments), null, null, params, paramMap); 
		}

	}


	public static class LiteralId implements Id {

		private final Id type;
		private final String stringRep;
		private final String uriString;
		
		public LiteralId(Object obj) {
			String uriString  = null;
			if(obj instanceof Boolean) {
				this.type = Id.TYPE_BOOLEAN;
				this.stringRep = obj.toString();				
			}
			else if(obj instanceof Integer || obj instanceof Short || obj instanceof Byte || obj instanceof Long || obj instanceof BigInteger) {
				this.type = Id.TYPE_INTEGER;
				this.stringRep = obj.toString();				
			}
			else if(obj instanceof Double) {
				this.type = Id.TYPE_DOUBLE;
				this.stringRep = obj.toString();				
			}
			else if(obj instanceof Float) {
				this.type = Id.TYPE_FLOAT;
				this.stringRep = obj.toString();				
			}
			else if(obj instanceof BigDecimal) {
				this.type = Id.TYPE_DECIMAL;
				this.stringRep = obj.toString();				
			}
			else if(obj instanceof String) {
				this.type = Id.TYPE_STRING;
				this.stringRep = JavaUtil.quote(obj.toString());		
				uriString = UriEncoding.percentEncodeIri(obj.toString(), UriEncoding.URI_EXTRA_CHARS_PATH, true);
			}
			else {
				throw new IllegalArgumentException("type not found for: " + obj);
			}
			if(uriString == null)
				this.uriString = UriEncoding.percentEncodeIri(obj.toString(), UriEncoding.URI_EXTRA_CHARS_PATH, true);
			else
				this.uriString = uriString;
		}


		@Override
		public List<String> getPath() {
			return Collections.emptyList();
		}

		@Override
		public String getScheme() {
			return type.getScheme();
		}

		@Override
		public String getAuthority() {
			return type.getAuthority();
		}

		@Override
		public Id getParent() {
			return type;
		}

		@Override
		public String toUriString() {
			return "values://" + type.toString() + "/" + uriString;
		}

		@Override
		public String toRdfString() {
			return stringRep + "^^" + type.toUriString();
		}

		@Override
		public String toString() {
			return stringRep;
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public boolean isContainer() {
			return false;
		}

	}

}
