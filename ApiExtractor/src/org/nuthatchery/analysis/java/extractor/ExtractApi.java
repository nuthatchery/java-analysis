package org.nuthatchery.analysis.java.extractor;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.nuthatchery.analysis.java.extractor.FactsDb.IFactsWriter;
import org.objectweb.asm.ClassReader;

public class ExtractApi {
	private static final List<String> DEFAULT_CLASSES = Arrays.asList("../../../../../ImmutablePosition.class",
			"../../../../../MutablePosition.class", "../../../../../Test.class", "../../../../../A.class", "../../../../../App.class");
	//"/home/anya/.m2/repository/com/lowagie/itext/2.1.5/itext-2.1.5.jar");

	public static String fill(String s, int size, String ellipsis, boolean flushRight) {
		if (s.length() > size) {
			if (flushRight) {
				return ellipsis + s.substring(s.length() - (size - ellipsis.length()), s.length());
			} else {
				return s.substring(0, s.length() - (size - ellipsis.length())) + ellipsis;
			}
		}
		if (flushRight) {
			for (; s.length() < size; s = " " + s) {
				;
			}
		} else {
			for (; s.length() < size; s = s + " ") {
				;
			}
		}
		return s;

	}

	public static void main(String[] args) throws IOException {
		boolean openAsResource = args.length == 0;
		int logLevel = args.length == 0 ? 10 : 0;
		List<String> arguments = new ArrayList<>(openAsResource ? DEFAULT_CLASSES : Arrays.asList(args));
		List<String> files = new ArrayList<>();
		FileSystem fs = FileSystems.getDefault();
		int nClasses = 0;
		int nJars = 0;
		if (openAsResource) {
			files.addAll(arguments);
		} else {
			for (int i = 0; i < arguments.size(); i++) {
				Path path = fs.getPath(arguments.get(i));
				String str = path.toString();
				if (Files.isRegularFile(path)) {
					if (str.endsWith(".class")) {
						files.add(path.toString());
						nClasses++;
					} else if (str.endsWith(".jar")) {
						files.add(path.toString());
						nJars++;
					}
				} else if (Files.isDirectory(path)) {
					System.out.println("Adding files from " + path);
					Files.list(path).forEach((Path p) -> {
						arguments.add(p.toString());
					});
				}
			}
			System.out.println("" + nClasses + " class files and " + nJars + " jars found.");
		}
		IFactsWriter fw = FactsDb.nTripleFactsWriter("/tmp/data.n3", "C");
		fw.precheck();
		System.out.println(JavaFacts.Types.BOOLEAN);
		ClassFactExtractor ea = new ClassFactExtractor(fw, JavaUtil.logger(logLevel));
		ClassReader cr;
		int i = 0;
		int n = files.size();
		Console console = System.console();
		String prc = "Processing:  ";
		String chk = "(checkpoint) ";
		String msg = prc;
		for (String file : files) {
			IdFactory.push();
			if (console != null) {
				console.printf("[%02d%%] %s%s\r", (i * 100) / n, msg, fill(file, 60, "…", true));
			}
			if (openAsResource && file.endsWith(".class")) {
				cr = new ClassReader(ExtractApi.class.getResourceAsStream(file));
				cr.accept(ea, ClassReader.EXPAND_FRAMES);
			} else if (file.endsWith(".class")) {
				try (InputStream stream = new FileInputStream(file)) {
					cr = new ClassReader(stream);
					cr.accept(ea, ClassReader.EXPAND_FRAMES);
				}
			} else if (file.endsWith(".jar")) {
				try (JarFile jarFile = new JarFile(file)) {
					int nEntries = jarFile.size();
					int j = 0;
					for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
						JarEntry nextElement = entries.nextElement();
						if (nextElement.getName().endsWith(".class")) {
							try (InputStream stream = jarFile.getInputStream(nextElement)) {

								if (console != null) {
									console.printf("[%2d%%] JAR: %2d%% %s\r", (i * 100) / n, (j * 100) / nEntries,
											fill(file, 60, "…", true));
								}
								cr = new ClassReader(stream);
								cr.accept(ea, ClassReader.EXPAND_FRAMES);
							}
						} else if (console != null) {
							console.printf("[%2d%%] JAR: %2d%% %s\r", (i * 100) / n, (j * 100) / nEntries,
									fill("", 60, "…", true));
						}
						j++;
					}
				}
			}
			if (i++ % 10 == 0) {
				if (fw.checkpoint()) {
					msg = chk;
				}
			} else if (i % 10 == 5) {
				msg = prc;
			}
			IdFactory.pop();
		}
		System.out.println();
		fw.save();
	}

}
