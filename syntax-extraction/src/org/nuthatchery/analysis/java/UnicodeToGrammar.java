package org.nuthatchery.analysis.java;

import java.util.Arrays;
import java.util.function.IntPredicate;

public class UnicodeToGrammar {

	public static String[] checkCharacters(int first, int last, IntPredicate p) {
		String allowed = "";
		String disallowed = "";
		int firstOk = -1;
		int lastOk = -1;
		int firstBad = -1;
		int lastBad = -1;
		String prefixOk = "", prefixBad = "";
		for (int i = first; i <= last; i++) {
			String s = String.valueOf(Character.toChars(i));
			try {
				// TEST URIS: new URI("http://example.com/" + s);
				if (!p.test(i)) {
					throw new Exception();
				}
				if (firstOk == -1) {
					firstOk = i;
				}
				if (firstBad != -1) {
					disallowed += encodeCheck(prefixBad, firstBad, lastBad);
					firstBad = -1;
					prefixBad = " ";
				}
				lastOk = i;
			} catch (Exception e) {
				if (firstOk != -1) {
					allowed += encodeCheck(prefixOk, firstOk, lastOk);
					prefixOk = " ";
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

	private static String uEncode(int codePoint) {
		if(codePoint < 128) {
			return String.format("\\a%02X", codePoint);
		}
		else if(codePoint < 65536) {
			return String.format("\\u%04X", codePoint);
		}
		else {
			return String.format("\\U%06X", codePoint);
		}
	}
	private static String encodeCheck(String prefix, int first, int last) {
		if (first == last || last == -1) {
			String comment = "// " + Character.getName(first);
			return String.format("%s%s", prefix, uEncode(first));
		} else {
			String comment = "// " + Character.getName(first) + " – " + Character.getName(last);
			return String.format("%s%s-%s", prefix, uEncode(first), uEncode(last));
		}

	}

	static class CharTest {
		IntPredicate pred;
		String name;

		public CharTest(IntPredicate pred, String name) {
			super();
			this.pred = pred;
			this.name = name;
		}

	}
	public static void main(String[] args) {
		System.out.println("Characters allowed in Java URI paths:");

		for (CharTest ct : Arrays.asList(//
				new CharTest(Character::isLetter, "Letter"), //
				new CharTest(Character::isAlphabetic, "Alphabetic"), //
				new CharTest(Character::isIdeographic, "Ideographic"), //
				new CharTest(Character::isUpperCase, "UpperCase"), //
				new CharTest(Character::isLowerCase, "LowerCase"), //
				new CharTest(Character::isDigit, "Digit"), //
				new CharTest(Character::isLetterOrDigit, "LetterOrDigit"), //
				new CharTest(Character::isWhitespace, "Whitespace"), //
				new CharTest(Character::isLetterOrDigit, "LetterOrDigit"), //
				new CharTest(Character::isJavaIdentifierStart, "JavaIdentifierStart"), //
				new CharTest(Character::isJavaIdentifierPart, "JavaIdentifierPart"), //
				new CharTest(Character::isUnicodeIdentifierStart, "UnicodeIdentifierStart"), //
				new CharTest(Character::isUnicodeIdentifierPart, "UnicodeIdentifierPart") //
				)) {
			String[] ss = checkCharacters(0, 0x10ffff, ct.pred);

			System.out.println("lexical "  + ct.name + " = [" + ss[0] + "];");
			//System.out.println("lexical Not"  + ct.name + " = " + ss[1]);
			//System.out.printf("%-24s: %3d ranges%n", ct.name, ss[0].split("\n").length);
		}
	}
}
