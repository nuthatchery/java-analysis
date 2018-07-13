package org.nuthatchery.analysis.java.extractor;

import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JavaUtil {
	public interface ILogger {
		public ILogger indent(Supplier<Integer> indentation);

		public void log(String s);

		public void logf(String s, Object... args);

		public void warn(String s);

		public void warnf(String s, Object... args);
	}

	public static class NullLogger implements ILogger {

		@Override
		public ILogger indent(Supplier<Integer> indentation) {
			return this;
		}

		@Override
		public void log(String s) {
			// do nothing
		}

		@Override
		public void logf(String s, Object... args) {
			// do nothing
		}

		@Override
		public void warn(String s) {
			stdLogger.warn(s);
		}

		@Override
		public void warnf(String s, Object... args) {
			stdLogger.warnf(s, args);
		}
	}

	public static class StdoutLogger implements ILogger {
		private Supplier<Integer> indentLevel;

		public StdoutLogger() {
			this(null);
		}

		public StdoutLogger(Supplier<Integer> indentation) {
			indentLevel = indentation;
		}

		@Override
		public ILogger indent(Supplier<Integer> indentation) {
			if (indentLevel == null)
				return new StdoutLogger(indentation);
			else
				return new StdoutLogger(() -> indentLevel.get() + indentation.get());
		}

		@Override
		public void log(String msg) {
			StringBuilder b = new StringBuilder();
			int i = 0;
			if (indentLevel != null) {
				i = indentLevel.get();
			}
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
		public void logf(String s, Object... args) {
			log(String.format(s, args));
		}

		@Override
		public void warn(String s) {
			log(s);
		}

		@Override
		public void warnf(String s, Object... args) {
			logf(s, args);
		}
	}

	public static final String JAVA_EXTRA_URI_PATH_CHARS = "/()'$";

	public static ILogger nullLogger = new NullLogger();

	public static ILogger stdLogger = new StdoutLogger();

	/**
	 * @param desc
	 *            The descriptor
	 * @return Readable presentation of the descriptor
	 */
	public static String decodeDescriptor(String desc) {
		return decodeDescriptor(null, desc);
	}

	/**
	 * @param name
	 *            Name of the entity this is the descriptor for
	 * @param desc
	 *            The descriptor
	 * @return Readable presentation of the descriptor
	 */
	public static String decodeDescriptor(String name, String desc) {
		StringBuilder b = new StringBuilder();
		JavaUtil.decodeDescriptor(name, Type.getType(desc), b);
		return b.toString();
	}

	/**
	 * @param owner
	 *            Name of the entity (i.e., class) that owns <code>name</code>
	 * @param name
	 *            Name of the entity this is the descriptor for
	 * @param desc
	 *            The descriptor
	 * @return Readable presentation of the descriptor
	 */
	public static String decodeDescriptor(String owner, String name, String desc) {
		StringBuilder b = new StringBuilder();

		JavaUtil.decodeDescriptor(owner + "." + name, Type.getType(desc), b);
		return b.toString();
	}

	private static void decodeDescriptor(String name, Type type, StringBuilder b) {
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
			for (int i = type.getDimensions(); i > 0; i--) {
				b.append("[]");
			}
		} else {
			b.append(type.getClassName());
			if (name != null) {
				b.append(" ");
				b.append(name);
			}
		}
	}

	public static Resource frameTypeToId(Model m, String prefix, Object o) {
		if (o == Opcodes.TOP)
			return JavaFacts.Types.TOP;
		else if (o == Opcodes.INTEGER)
			return JavaFacts.Types.INT;
		else if (o == Opcodes.FLOAT)
			return JavaFacts.Types.FLOAT;
		else if (o == Opcodes.DOUBLE)
			return JavaFacts.Types.DOUBLE;
		else if (o == Opcodes.LONG)
			return JavaFacts.Types.LONG;
		else if (o == Opcodes.NULL)
			return JavaFacts.Types.VOID;
		else if (o == Opcodes.UNINITIALIZED_THIS)
			return JavaFacts.Types.UNINITIALIZED_THIS;
		else if (o instanceof String)
			return typeToId(m, prefix, Type.getObjectType((String) o));
		throw new IllegalArgumentException("" + o);
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

	public static ILogger logger(int logLevel) {
		if (logLevel == 0)
			return nullLogger;
		else
			return stdLogger;
	}

	public static String quote(String s) {
		return "\"" + s.replaceAll("([\\\"\'])", "\\\\$1").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r") + "\"";
	}

	public static Resource typeToId(Model m, String prefix, Type t) {
		switch (t.getSort()) {
		case Type.VOID:
			return JavaFacts.Types.VOID;
		case Type.BOOLEAN:
			return JavaFacts.Types.BOOLEAN;
		case Type.CHAR:
			return JavaFacts.Types.CHAR;
		case Type.BYTE:
			return JavaFacts.Types.BYTE;
		case Type.SHORT:
			return JavaFacts.Types.SHORT;
		case Type.INT:
			return JavaFacts.Types.INT;
		case Type.FLOAT:
			return JavaFacts.Types.FLOAT;
		case Type.LONG:
			return JavaFacts.Types.LONG;
		case Type.DOUBLE:
			return JavaFacts.Types.DOUBLE;
		case Type.ARRAY:
			return JavaFacts.Types.array(m, t.getDimensions(), typeToId(m, prefix, t.getElementType()));
		case Type.OBJECT:
			return JavaFacts.Types.object(m, prefix, t.getInternalName());
		default:
			throw new IllegalArgumentException("" + t + t.getSort());
		}
	}

	public static String typeToString(Type t) {
		if (t.getSort() == Type.OBJECT)
			return t.getInternalName();
		else if (t.getSort() == Type.ARRAY)
			return t.getInternalName();
		else
			return t.getClassName();
	}

	public static String unquote(String s) {
		if (s.length() > 1
				&& ((s.startsWith("\"") && s.endsWith(("\"")) || (s.startsWith("\'") && s.endsWith("\'"))))) {
			s = s.substring(1, s.length() - 1);
		} else
			throw new IllegalArgumentException();
		return s.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r").replaceAll("\\\\(.)", "$1");
	}
}
