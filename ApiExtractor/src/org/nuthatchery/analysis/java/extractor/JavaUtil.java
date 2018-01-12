package org.nuthatchery.analysis.java.extractor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JavaUtil {
	public static ILogger stdLogger = new StdoutLogger();

	public static String decodeDescriptor(String name, String desc) {
		StringBuilder b = new StringBuilder();
		JavaUtil.decodeDescriptor(name, Type.getType(desc), b);
		return b.toString();
	}

	public static String decodeDescriptor(String desc) {
		return decodeDescriptor(null, desc);
	}

	public static String decodeDescriptor(String owner, String name, String desc) {
		StringBuilder b = new StringBuilder();

		JavaUtil.decodeDescriptor(owner + "::" + name, Type.getType(desc), b);
		return b.toString();
	}

	public static void decodeDescriptor(String name, Type type, StringBuilder b) {
		if (type.getSort() == Type.METHOD) {
			decodeDescriptor(null, type.getReturnType(), b);
			if (name != null) {
				b.append(" ");
				b.append(name);
			}
			b.append("(");
			String sep = "";
			for (Type t : type.getArgumentTypes()) {
				b.append(sep);
				decodeDescriptor(null, t, b);
				sep = ", ";
			}
			b.append(")");
		} else if (type.getSort() == Type.ARRAY) {
			decodeDescriptor(null, type.getElementType(), b);
			for (int i = type.getDimensions(); i > 0; i--)
				b.append("[]");
		} else {
			b.append(type.getClassName());
			if (name != null) {
				b.append(" ");
				b.append(name);
			}
		}
	}

	public static void logf(String s, Object... args) {
		stdLogger.logf(s, args);
	}

	public static void logln(String s) {
		stdLogger.log(s);
	}

	public static class StdoutLogger implements ILogger {
		private Supplier<Integer> indentLevel;

		public StdoutLogger() {
			this(null);
		}

		public StdoutLogger(Supplier<Integer> indentation) {
			indentLevel = indentation;
		}

		public void logf(String s, Object... args) {
			log(String.format(s, args));
		}

		public void log(String msg) {
			StringBuilder b = new StringBuilder();
			int i = 0;
			if (indentLevel != null)
				i = indentLevel.get();
			while (i-- > 0) {
				b.append("    ");
			}
			String[] lines = msg.split("\n");
			System.out.flush();
			for (String s : lines) {
				System.err.append(b);
				System.err.println(s);
			}
			System.err.flush();
		}

		@Override
		public ILogger indent(Supplier<Integer> indentation) {
			if (indentLevel == null)
				return new StdoutLogger(indentation);
			else
				return new StdoutLogger(() -> indentLevel.get() + indentation.get());
		}
	}

	public interface ILogger {
		public void logf(String s, Object... args);

		public void log(String s);

		public ILogger indent(Supplier<Integer> indentation);
	}

	/**
	 * Percent-decodes a string.
	 * 
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as
	 * well as all non-unreserved characters with the UTF-8 representation of
	 * the character as a sequence of %xx bytes.
	 * 
	 * Percent encoding is specified in
	 * <a href="https://tools.ietf.org/html/rfc3986#page-12">RFC 3986</a>:
	 * 
	 * @param s
	 *            A %-encoded string
	 * @return A decoded version of the string
	 * @throws IllegalArgumentException
	 *             if the string contains non-encoded unicode characters past
	 *             code point 255.
	 * @see #percentEncodingProperty(IString)
	 */
	public static String percentDecode(String s) {
		ByteBuffer bytes = ByteBuffer.allocate(s.length());
		for (int i = 0; i < s.length(); i++) {
			int c = s.charAt(i);
			if (c == '%') {
				System.out.println(Integer.valueOf(s.substring(i + 1, i + 3), 16).byteValue());
				bytes.put(Integer.valueOf(s.substring(i + 1, i + 3), 16).byteValue());
				i += 2;
			} else if (c < 256) {
				bytes.put((byte) c);
			} else {
				throw new IllegalArgumentException("Unencoded character in string: " + c);
			}
		}
		bytes.limit(bytes.position());
		bytes.rewind();
		return StandardCharsets.UTF_8.decode(bytes).toString();
	}

	public static boolean percentEncodingProperty(String s) {
		return percentDecode(percentEncode(s)).equals(s);
	}

	/**
	 * Percent-encodes a string.
	 * 
	 * Percent encoding (aka URL-encoding) replaces all reserved characters as
	 * well as all non-unreserved characters with the UTF-8 representation of
	 * the character as a sequence of %xx bytes.
	 * 
	 * Percent encoding is specified in
	 * <a href="https://tools.ietf.org/html/rfc3986#page-12">RFC 3986</a>:
	 * 
	 * This method will also handle multi-word characters correctly (i.e.,
	 * Unicode characters beyond 65535). Example:
	 * 
	 * <li>percentEncode("føø") = "f%C3%B8%C3%B8"
	 * <li>percentEncode("\ud801\udc00") = "%F0%90%90%80"
	 * 
	 * @param s
	 *            A string
	 * @return A %-encoded version of the string
	 * @see #percentEncodingProperty(String)
	 */
	public static String percentEncode(String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		StringBuilder result = new StringBuilder(s.length());
		for (byte b : bytes) {
			switch (b) {
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '-':
			case '_':
			case '~':
			case '.':
				result.append((char) b);
				break;
			default:
				result.append("%");
				result.append(String.format("%02X", b));
			}
		}
		return result.toString();
	}

	public static String quote(String s) {
		return "\"" + s.replaceAll("([\\\"\'])", "\\\\$1").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r") + "\"";
	}

	public static String unquote(String s) {
		if (s.length() > 1 && ((s.startsWith("\"") && s.endsWith(("\"")) || (s.startsWith("\'") && s.endsWith("\'")))))
			s = s.substring(1, s.length() - 1);
		else
			throw new IllegalArgumentException();
		return s.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r").replaceAll("\\\\(.)", "$1");
	}

	public static String frameTypeToString(Object o) {
		if (o == Opcodes.TOP)
			return "TOP";
		else if (o == Opcodes.INTEGER)
			return "int";
		else if (o == Opcodes.FLOAT)
			return "float";
		else if (o == Opcodes.DOUBLE)
			return "double";
		else if (o == Opcodes.LONG)
			return "long";
		else if (o == Opcodes.NULL)
			return "null";
		else if (o == Opcodes.UNINITIALIZED_THIS)
			return "uinit_this";
		else
			return o.toString();
	
	}
	public static Id frameTypeToId(Object t) {
		return Id.string(frameTypeToString(t)); // TODO: decide on encoding of types as Ids
	}

	public static String typeToString(Type t) {
		if(t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT)
			return t.getInternalName();
		else
			return t.getClassName();
	}
	public static Id typeToId(Type t) {
		return Id.string(typeToString(t)); // TODO: decide on encoding of types as Ids
	}
}
