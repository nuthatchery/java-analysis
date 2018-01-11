package org.nuthatchery.analysis.java.extractor;

import org.objectweb.asm.Type;

public class JavaUtil {

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
		System.out.flush();
		System.err.printf(s, args);
		System.err.flush();
	}

	public static void logln(String s) {
		System.out.flush();
		System.err.println(s);
		System.err.flush();
	}

}
