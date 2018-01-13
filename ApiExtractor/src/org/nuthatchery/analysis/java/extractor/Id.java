package org.nuthatchery.analysis.java.extractor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Spliterator;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public abstract class Id {
	public static final Map<Object, Id> cache = new WeakHashMap<>();
	public static final Id STRINGS = new UriId("literal://string/");
	public static final Id TEXT = new UriId("literal://text/");
	public static final Id CHARACTERS = new UriId("literal://character/");
	public static final Id NUMBERS = new UriId("literal://number/");
	public static final Id INTEGERS = new UriId(NUMBERS, "/integer/");
	public static final Id ORDINALS = new UriId(NUMBERS, "/ordinal/");
	public static final Id RATIONALS = new UriId(NUMBERS, "/rational/");
	public static final Id DECIMALS = new UriId(NUMBERS, "/decimal/");
	public static final Id FLOATS = new UriId(NUMBERS, "/float/");

	public static Id id(String name) {
		Id id = cache.get(name);
		if(id == null) {
			id = new UriId(name);
			cache.put(name, id);
		}
		return id;
	}

	public static Id id(Id parent, String name) {
		return new UriId(parent, name);
	}

	public static Id integer(long i) {
		return new NumberId<Long>(INTEGERS, i);
	}

	public static Id integer(BigInteger i) {
		return new NumberId<BigInteger>(INTEGERS, i);
	}

	public static Id decimal(BigDecimal i) {
		return new NumberId<BigDecimal>(DECIMALS, i);
	}

	public static Id floating(double i) {
		return new NumberId<Double>(FLOATS, i);
	}

	public static Id string(String s) {
		return new StringId(s);
	}

	public static Id text(String s, String language) {
		return new StringId(TEXT.resolve(language), s);
	}

	public static Id character(char c) {
		return new CharId(String.valueOf(c));
	}

	public static Id character(int codePoint) {
		return new CharId(String.valueOf(Character.toChars(codePoint)));
	}

	public static Id character(String c) {
		if (c.codePointCount(0, c.length()) != 1) {
			throw new IllegalArgumentException("Argument should contain a single Unicode codepoint: " + c);
		}
		return null;
	}

	public boolean isAdaptableTo(Class<?> type) {
		return type == String.class || type == URI.class || type.isAssignableFrom(getClass());
	}

	@SuppressWarnings("unchecked")
	public <T> T as(Class<T> type) {
		Object result = null;
		if (type.isAssignableFrom(getClass()))
			result = this;
		else if (type == String.class)
			result = toString();
		else if (type == URI.class)
			result = getURI();
		return (T) result;
	}

	public URI getURI() {
		return getModelId().getURI().resolve(getPathName());
	}

	public abstract Id getModelId();

	public abstract Id resolve(String path);

	protected abstract String getPathName();

	public String getRDF() {
		URI uri = getURI();
		if (uri.getScheme().equals("literal")) {
			switch (uri.getAuthority()) {
			case "string":
				return JavaUtil.quote(uri.getPath().substring(1));	
			case "text": {
				String[] rawPath = uri.getRawPath().split("/");
				if(rawPath.length < 1)
					throw new IllegalStateException();
				String lang = UriEncoding.percentDecode(rawPath[0]);
				rawPath = Arrays.copyOfRange(rawPath, 1, rawPath.length);
				return JavaUtil.quote(String.join("/", rawPath))+"@"+lang;
			}
			case "number": {
				String[] rawPath = uri.getRawPath().split("/");
				if(rawPath.length < 1)
					throw new IllegalStateException();
				String type = UriEncoding.percentDecode(rawPath[0]);
				rawPath = Arrays.copyOfRange(rawPath, 1, rawPath.length);
				return JavaUtil.quote(String.join("/", rawPath))+"^^"+type;
			}
			case "character":
				return JavaUtil.quote(uri.getPath());	
			default:
			}
		}
		return "<" + getURI().toString() + ">"; // TODO: whatever
	}

	public String getJSON() {
		return getURI().toString(); // TODO: quote it!
	}

	public static abstract class AbstractId extends Id {
		private final Id modelId;

		public AbstractId(Id parent) {
			modelId = parent;
		}

		public Id getModelId() {
			return modelId;
		}

	}

	private static class UriId extends AbstractId {
		private final URI uri;

		public UriId(URI uri) {
			this(null, uri);
		}

		public UriId(Id parent, URI uri) {
			super(parent);
			if (parent != null)
				this.uri = parent.getURI().resolve(uri);
			else
				this.uri = uri;
		}

		private static URI createUri(String uri) {
			try {
				return new URI(uri);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(uri, e);
			}
		}
		public UriId(Id parent, String uri) {
			this(parent, createUri(uri));
		}

		public UriId(String uri) {
			this(null, createUri(uri));
		}

		public Id resolve(String path) {
			try {
				if (path.startsWith("/"))
					return new UriId(getModelId(), new URI(path));
				else if(path.startsWith("?"))
					return new UriId(this, new URI(uri.toString() + path));
				else
					return new UriId(this, getURIFolder().resolve(new URI(path)));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(path, e);
			}
		}

		@Override
		protected String getPathName() {
			return uri.getPath();
		}

		@Override
		public Id getModelId() {
			return this;
		}

		public URI getURI() {
			return uri;
		}
		
		private URI getURIFolder() {
			if(uri.isOpaque())
				return uri;
			String s = uri.toString();
			if(s.endsWith("/"))
				return uri;
			else
				return createUri(s + "/");
		}
	}

	private static class StringId extends UriId {
		private String value;

		public StringId(String value) {
			super(STRINGS, UriEncoding.percentEncode(value));
			this.value = value;
		}

		public StringId(Id parent, String value) {
			super(parent, UriEncoding.percentEncode(value));
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		public <T> T as(Class<T> type) {
			if (type == String.class)
				return (T) value;
			else
				return super.as(type);
		}
	}

	private static class CharId extends UriId {
		private String value;

		public CharId(String value) {
			super(STRINGS, UriEncoding.percentEncode(value));
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		public <T> T as(Class<T> type) {
			if (type == String.class)
				return (T) value;
			else if (type == Character.class && value.length() == 1)
				return (T) Character.valueOf(value.charAt(0));
			else if (type == Integer.class)
				return (T) Integer.valueOf(value.codePointAt(0));
			else
				return super.as(type);
		}
	}

	private static class NumberId<T extends Number> extends UriId {

		private T value;

		public NumberId(Id parent, T val) {
			super(parent, val.toString());
			this.value = val;
		}

		public String toString() {
			return value.toString();
		}

		public String getJSON() {
			return value.toString();
		}

		public boolean isAdaptableTo(Class<?> otherType) {
			return super.isAdaptableTo(otherType) || otherType.isAssignableFrom(value.getClass());
		}

		@SuppressWarnings("unchecked")
		public <U> U as(Class<U> otherType) {
			U result = super.as(otherType);
			if (result == null) {
				if (otherType.isAssignableFrom(value.getClass())) {
					result = (U) value;
				} else if (Number.class.isAssignableFrom(otherType)) {
					try {
						Method method = otherType.getMethod("valueOf", String.class);
						if (Modifier.isStatic(method.getModifiers())) {
							Object o = method.invoke(null, value.toString());
							if (otherType.isInstance(o))
								result = (U) o;
						}
					} catch (Exception e) {
						// fine if this fails – just means it's not compatible
					}
				}
			}
			return null;
		}
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static class DecOrFracId extends AbstractId {
		private final BigDecimal numerator;
		private final BigDecimal denominator;

		public DecOrFracId(Id parent, long value) {
			this(parent, BigInteger.valueOf(value), BigInteger.ONE);
		}

		public DecOrFracId(Id parent, long num, long denom) {
			this(parent, BigInteger.valueOf(num), BigInteger.valueOf(denom));
		}

		public DecOrFracId(Id parent, BigInteger num, BigInteger denom) {
			super(parent);

			if (denom.compareTo(BigInteger.ZERO) < 0) {
				num = num.negate();
				denom = denom.negate();
			}

			BigInteger gcd = num.gcd(denom);
			if (gcd != BigInteger.ZERO && gcd != BigInteger.ONE) {
				num = num.divide(gcd);
				denom = denom.divide(gcd);
			} else if (denom == BigInteger.ZERO && num != BigInteger.ZERO) {
				num = num.divide(num.gcd(num));
			}

			this.numerator = new BigDecimal(num).stripTrailingZeros();
			this.denominator = new BigDecimal(denom).stripTrailingZeros();

		}

		public DecOrFracId(Id parent, BigDecimal value) {
			super(parent);
			value = value.stripTrailingZeros();
			this.numerator = value;
			this.denominator = BigDecimal.ONE;
		}

		@Override
		protected String getPathName() {
			return numerator.toString();
		}

		public boolean isAdaptableTo(Class<?> type) {
			return super.isAdaptableTo(type) || as(type) != null;
		}

		@SuppressWarnings("unchecked")
		public <T> T as(Class<T> type) {
			try {
				if (type == String.class)
					return (T) toString();
				else if (type == URI.class)
					return (T) getURI();
				else if (type.isAssignableFrom(getClass()))
					return (T) this;
				else if (denominator != BigDecimal.ZERO) {
					BigDecimal tmp = numerator.divide(denominator, BigDecimal.ROUND_HALF_EVEN);
					if (type.isAssignableFrom(BigDecimal.class))
						return (T) tmp;
					else if (type == BigInteger.class)
						return (T) tmp.toBigInteger();
					else if (type == Integer.class)
						return (T) Integer.valueOf(tmp.intValueExact());
					else if (type == Long.class)
						return (T) Long.valueOf(tmp.longValueExact());
					else if (type == Short.class)
						return (T) Short.valueOf(tmp.shortValueExact());
					else if (type == Byte.class)
						return (T) Byte.valueOf(tmp.byteValueExact());
					else if (type == Double.class)
						return (T) Double.valueOf(tmp.doubleValue());
					else if (type == Float.class)
						return (T) Float.valueOf(tmp.floatValue());
				} else if (type == Double.class) {
					return (T) Double.valueOf((numerator.doubleValue() / denominator.doubleValue()));
				} else if (type == Float.class) {
					return (T) Double.valueOf((numerator.floatValue() / denominator.floatValue()));
				}
			} catch (ArithmeticException e) {
			}
			return null;
		}

		@Override
		public Id resolve(String path) {
throw new UnsupportedOperationException();		}
	}

}
