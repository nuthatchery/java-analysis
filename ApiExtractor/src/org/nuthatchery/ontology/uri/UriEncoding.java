package org.nuthatchery.ontology.uri;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class UriEncoding {
	/**
	 * Character is not in the sets {@link #RESERVED} or {@link #UNRESERVED}.
	 *
	 * (Normally, these characters should be escaped in URIs)
	 */
	public static final byte OTHER = 0x00;
	/**
	 * Reserved characters function as URI delimiters
	 */
	public static final byte RESERVED = 0x01;
	/**
	 * Unreserved characters can be used unquoted in URIs
	 */
	public static final byte UNRESERVED = 0x02;

	private static final byte[] URI_CHARS = new byte[128];

	/**
	 * Extra characters allowed in an opaque scheme-specific part, in addition to
	 * {@link #URI_UNRESERVED} and encoded characters.
	 */
	public static final String URI_EXTRA_CHARS_OPAQUE = "?;:@&=+$,";
	/**
	 * Extra characters allowed in paths, in addition to {@link #URI_UNRESERVED} and
	 * encoded characters.
	 */
	public static final String URI_EXTRA_CHARS_PATH = ":@&=+$,";
	public static final String URI_GEN_DELIMS = ":/?#[]@";
	public static final String URI_SUB_DELIMS = "!$&'()*+,;=";
	public static final String URI_EXTRA_CHARS_PCHAR = URI_SUB_DELIMS + ":@";
	public static final String URI_EXTRA_CHARS_FRAGMENT = URI_EXTRA_CHARS_PCHAR + "/?";
	public static final String URI_EXTRA_CHARS_QUERY = URI_EXTRA_CHARS_PCHAR + "/?";
	/**
	 * Extra characters allowed in a reg_name authority part, in addition to
	 * {@link #URI_UNRESERVED} and encoded characters.
	 */
	public static final String URI_EXTRA_CHARS_REGNAME = ";:@&=+$,";
	/**
	 * Extra characters allowed in the userinfo part, in addition to
	 * {@link #URI_UNRESERVED} and encoded characters.
	 */
	public static final String URI_EXTRA_CHARS_USERINFO = ";:&=+$,";
	public static final String URI_SCHEME_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+-.";
	public static final String URI_UNRESERVED = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._~";
	static {
		for (int i = 0; i < URI_UNRESERVED.length(); i++) {
			URI_CHARS[URI_UNRESERVED.charAt(i)] = UNRESERVED;
		}
		for (int i = 0; i < URI_GEN_DELIMS.length(); i++) {
			URI_CHARS[URI_GEN_DELIMS.charAt(i)] = RESERVED;
		}
		for (int i = 0; i < URI_SUB_DELIMS.length(); i++) {
			URI_CHARS[URI_SUB_DELIMS.charAt(i)] = RESERVED;
		}
	}

	public static String[] checkCharacters(int first, int last) {
		String allowed = "";
		String disallowed = "";
		int firstOk = -1;
		int lastOk = -1;
		int firstBad = -1;
		int lastBad = -1;
		String prefixOk = "(int c) -> ", prefixBad = "(int c) -> ";
		for (int i = first; i <= last; i++) {
			String s = String.valueOf(Character.toChars(i));
			try {
				new URI("http://example.com/" + s);
				if (firstOk == -1) {
					firstOk = i;
				}
				if (firstBad != -1) {
					disallowed += encodeCheck(prefixBad, firstBad, lastBad);
					firstBad = -1;
					prefixBad = "||";
				}
				lastOk = i;
			} catch (Exception e) {
				if (firstOk != -1) {
					allowed += encodeCheck(prefixOk, firstOk, lastOk);
					prefixOk = "||";
				}
				if (firstBad == -1) {
					firstBad = i;
				}
				lastBad = i;
				// System.out.printf("* \\u%04X ws=%b, is in %s:
				// %s%n",i,Character.isWhitespace(i),
				// Character.UnicodeBlock.of(i), Character.getName(i));
				firstOk = -1;
			}
		}
		if (firstOk != -1) {
			allowed += encodeCheck(prefixOk, firstOk, lastOk);
		}
		if (firstBad != -1) {
			disallowed += encodeCheck(prefixBad, firstBad, lastBad);
		}
		return new String[] { allowed, disallowed };
	}

	private static String encodeCheck(String prefix, int first, int last) {
		if (first == last)
			return String.format("  %s c == 0x%04X  // %s%n", prefix, first, Character.getName(first));
		else
			return String.format("  %s (c >= 0x%04X && c <= 0x%04X)  // %s – %s%n", prefix, first, last,
					Character.getName(first), Character.getName(last));

	}

	public static void main(String[] args) {
		System.out.println("Characters allowed in Java URI paths:");

		String[] ss = checkCharacters(0, 0x10ffff);

		System.out.println("allowed = " + ss[0]);
		System.out.println("disallowed = " + ss[1]);
	}

	/**
	 * Percent-decodes a string.
	 *
	 * <p>
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as well
	 * as all non-unreserved characters with the UTF-8 representation of the
	 * character as a sequence of %xx bytes.
	 *
	 * <p>
	 * This method also handles <em>Internationalized Resource Identifiers</em>
	 * (IRIs), where non-ASCII characters can appear unencoded.
	 *
	 * <p>
	 * Percent encoding is specified in
	 * <a href="https://tools.ietf.org/html/rfc3986#page-12">RFC 3986</a> and
	 * <a href="https://tools.ietf.org/html/rfc3986#page-6">RFC 3987</a>.
	 *
	 * <p>
	 * Note that this decoder will fail if the encoded octets are not valid UTF-8
	 *
	 * @param s
	 *            A %-encoded string
	 * @return A decoded version of the string
	 * @throws IllegalArgumentException
	 *             if percent-encoded octets do not form a valid UTF-8 encoded
	 *             string
	 * @see #percentEncodingProperty(IString)
	 */
	public static String percentDecode(String s) {
		byte[] inBytes = s.getBytes(StandardCharsets.UTF_8);
		ByteBuffer outBytes = ByteBuffer.allocate(inBytes.length);
		for (int i = 0; i < inBytes.length; i++) {
			int c = inBytes[i];
			if (c == '%') {
				// System.out.println(Integer.valueOf(s.substring(i + 1, i + 3),
				// 16).byteValue());
				outBytes.put(Integer.valueOf(s.substring(i + 1, i + 3), 16).byteValue());
				i += 2;
			} else {
				outBytes.put((byte) c);
			}
		}
		outBytes.limit(outBytes.position());
		outBytes.rewind();
		try {
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
			return decoder.decode(outBytes).toString();
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Error decoding UTF-8 string: ", e);
		}
	}

	/**
	 * Percent-encodes a string.
	 *
	 * <p>
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as well
	 * as all non-unreserved characters with the UTF-8 representation of the
	 * character as a sequence of %xx bytes.
	 *
	 * <p>
	 * Percent encoding is specified in
	 * <a href="https://tools.ietf.org/html/rfc3986#page-12">RFC 3986</a>:
	 *
	 * <p>
	 * This method will also handle multi-word characters correctly (i.e., Unicode
	 * characters beyond 65535). Example:
	 *
	 * <li>percentEncode("føø") = "f%C3%B8%C3%B8"
	 * <li>percentEncode("\ud801\udc00") = "%F0%90%90%80"
	 *
	 * @param s
	 *            A string
	 * @return A %-encoded version of the string
	 * @see UriEncoding#percentEncodingProperty(String)
	 */
	public static String percentEncode(String s) {
		return percentEncode(s, null);
	}

	/**
	 * Percent-encodes a string.
	 *
	 * <p>
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as well
	 * as all non-unreserved characters with the UTF-8 representation of the
	 * character as a sequence of %xx bytes.
	 *
	 * <p>
	 * Percent encoding is specified in
	 * <a href="https://tools.ietf.org/html/rfc3986#page-12">RFC 3986</a>:
	 *
	 * <p>
	 * This method will also handle multi-word characters correctly (i.e., Unicode
	 * characters beyond 65535). Example:
	 *
	 * <li>percentEncode("føø") = "f%C3%B8%C3%B8"
	 * <li>percentEncode("\ud801\udc00") = "%F0%90%90%80"
	 *
	 * @param s
	 *            A string
	 * @param preserve
	 *            Null, or a string of characters that should be kept unencoded
	 *            (e.g., if the input is already (partly) in URI/IRI format, and you
	 *            don't want slashes to be encoded). This should be used carefully:
	 *            if the string is already encoded, some sequences might end up
	 *            double-encoded.
	 * @return A %-encoded version of the string
	 * @see UriEncoding#percentEncodingProperty(String)
	 */
	public static String percentEncode(String s, String preserve) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		StringBuilder result = new StringBuilder(s.length());
		for (byte b : bytes) {
			if (b >= 0) { // aka 0 >= b < 128
				byte spec = URI_CHARS[b];
				switch (spec) {
				case OTHER:
					break;
				case RESERVED:
					if (preserve == null || preserve.indexOf(b) == -1) {
						break;
					}
					// fall through
				case UNRESERVED: // can always appear unquoted
					result.append((char) b);
					continue;
				}
			}
			result.append("%");
			result.append(String.format("%02X", b));
		}
		return result.toString();
	}

	/**
	 * Percent-encodes a string according to IRI rules.
	 *
	 * <p>
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as well
	 * as all non-unreserved characters with the UTF-8 representation of the
	 * character as a sequence of %xx bytes.
	 *
	 * <p>
	 * This method uses the
	 * <a href="https://tools.ietf.org/html/rfc3987#page-6">Internationalized
	 * Resource Identifiers (IRIs, RFC3987)</a> syntax, where characters outside the
	 * ASCII range can appear unencoded. The encoding is otherwise the same as for
	 * URIs / RFC3986.
	 *
	 * <p>
	 * This method will handle multi-word characters correctly (i.e., Unicode
	 * characters beyond 65535). Example:
	 *
	 * <li>percentEncodeIri("føø") = "føø"
	 * <li>percentEncodeIri("\ud801\udc00") = "\ud801\udc00"
	 *
	 * @param s
	 *            A string
	 * @param preserve
	 *            Null, or a string of characters that should be kept unencoded
	 *            (e.g., if the input is already (partly) in URI/IRI format, and you
	 *            don't want slashes to be encoded). This should be used carefully:
	 *            if the string is already encoded, some sequences might end up
	 *            double-encoded.
	 * @param javaEncoding
	 *            If true, also encode characters that would be disallowed by Java's
	 *            URI class
	 *
	 * @return A %-encoded version of the string
	 * @see UriEncoding#percentEncodingPropertyIri(String)
	 */
	public static String percentEncodeIri(String s, String preserve, boolean javaEncoding) {
		StringBuilder result = new StringBuilder(s.length());
		s.codePoints().forEachOrdered((int c) -> {
			if (c < 0x80) {
				switch (URI_CHARS[c]) {
				case RESERVED:
					if (preserve != null && preserve.indexOf(c) != -1) {
						result.appendCodePoint(c);
						break;
					}
					// fall through
				case OTHER:
					result.append(String.format("%%%02X", c));
					break;
				case UNRESERVED:
					result.appendCodePoint(c);
					break;
				}
			} else if (
			// these are the disallowed characters according to RFC3987:
			c < 0xA0 //
					|| (c > 0xd7ff && c < 0xf900) //
					|| (c > 0xfdcf && c < 0xfdf0) //
					|| (c > 0xffef && c < 0x10000) //
					|| ((c & 0xffff) > 0xfffd) //
			// these seem to be disallowed by Java's URI class
			// (see #checkCharacters() below)
					|| (javaEncoding && (//
			c == 0x00A0 // NO-BREAK SPACE
					|| c == 0x1680 // OGHAM SPACE MARK
					|| c == 0x180E // MONGOLIAN VOWEL SEPARATOR
					|| (c >= 0x2000 && c <= 0x200A) // EN QUAD – HAIR SPACE
					|| (c >= 0x2028 && c <= 0x2029) // LINE SEPARATOR –
													// PARAGRAPH SEPARATOR
					|| c == 0x202F // NARROW NO-BREAK SPACE
					|| c == 0x205F // MEDIUM MATHEMATICAL SPACE
					|| c == 0x3000 // IDEOGRAPHIC SPACE

			))) {
				ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(Character.toChars(c)));
				for (byte b : bb.array()) {
					result.append(String.format("%%%02X", b));
				}
			} else {
				result.appendCodePoint(c);
			}
		});
		return result.toString();
	}

	public static boolean percentEncodingProperty(String s) {
		return UriEncoding.percentDecode(percentEncode(s)).equals(s);
	}

	public static boolean percentEncodingPropertyIri(String s) {
		return UriEncoding.percentDecode(percentEncodeIri(s, null, false)).equals(s);
	}
}
