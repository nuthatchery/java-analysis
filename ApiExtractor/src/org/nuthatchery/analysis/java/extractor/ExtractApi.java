package org.nuthatchery.analysis.java.extractor;

import net.rootdev.jenajung.JenaJungJFrame;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.nuthatchery.ontology.Model;
import org.nuthatchery.ontology.ModelFactory;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.nuthatchery.ontology.standard.RdfVocabulary;
import org.objectweb.asm.ClassReader;

public class ExtractApi {
	private static final String DB_PREFIX = "http://db.nuthatchery.org/java/";
	private static final List<String> DEFAULT_CLASSES = Arrays.asList("-m", "demo", "../../../../../Test.class",
			"../../../../../Test$1.class", "../../../../../Test$Bar.class", "../../../../../IMutablePosition.class");
	private static int logLevel;
	private static boolean demoMode = false;
	// "/home/anya/.m2/repository/com/lowagie/itext/2.1.5/itext-2.1.5.jar");
	private static JenaRDF jenaRDF;
	private static Model defaultModel;

	private static void addModelName(IRI s) {
		defaultModel.add(s, RdfVocabulary.RDFS_IS_DEFINED_BY, s);
	}

	private static void extractModel(Dataset dataset, Model mainModel, String modelName, String arg)
			throws IOException, FileNotFoundException {
		List<String> files = findFiles(arg);
		Console console = System.console();
		int i = 0;
		int n = files.size();

		if (modelName == null) {
			modelName = "";
		}
		Model model = mainModel.model(DB_PREFIX + modelName, "this:"); // DB_PREFIX + modelName + "/");
		for (String file : files) {
			// System.out.println("Processing: " + file);
			try {
				if (dataset.supportsTransactions()) {
					// System.out.println("Begin write transaction");
					dataset.begin(ReadWrite.WRITE);
				}
				if (console != null) {
					console.printf("[%02d%%] %3s:     %s\r", (i * 100) / n, "CLS", fill(file, 70, "…", true));
				}
				if (demoMode && file.endsWith(".class")) {
					addModelName(model.getName());
					ClassFactExtractor ea = new ClassFactExtractor(model, JavaUtil.logger(logLevel));
					ClassReader cr = new ClassReader(ExtractApi.class.getResourceAsStream(file));
					cr.accept(ea, ClassReader.EXPAND_FRAMES);
				} else if (file.endsWith(".class")) {
					addModelName(model.getName());
					try (InputStream stream = new FileInputStream(file)) {
						ClassFactExtractor ea = new ClassFactExtractor(model, JavaUtil.logger(logLevel));
						ClassReader cr = new ClassReader(stream);
						cr.accept(ea, ClassReader.EXPAND_FRAMES);
					}
				} else if (file.endsWith(".jar")) {
					try (JarFile jarFile = new JarFile(file)) {
						String name = file;
						if (name.contains("/")) {
							name = name.substring(name.lastIndexOf("/") + 1, name.length());
						}
						Model m = model.model(DB_PREFIX + name, "this:");
						addModelName(m.getName());
						ClassFactExtractor ea = new ClassFactExtractor(m, JavaUtil.logger(logLevel));
						int nEntries = jarFile.size();
						int j = 0;
						for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
							JarEntry nextElement = entries.nextElement();
							// System.out.println("Processing JarEntry " + nextElement.getName());
							if (nextElement.getName().endsWith(".class")) {
								try (InputStream stream = jarFile.getInputStream(nextElement)) {

									if (console != null) {
										console.printf("[%2d%%] %3s: %2d%% %s\r", (i * 100) / n, "JAR",
												(j * 100) / nEntries,
												fill(file, 70, "…", true));
									}
									ClassReader cr = new ClassReader(stream);
									cr.accept(ea, ClassReader.EXPAND_FRAMES);
								}
							} else if (nextElement.getName().endsWith("pom.xml")) {
								// TODO extract to class
								System.out.println("found POM.XML, trying to parse");
								org.apache.maven.model.Model result = null;
								try (InputStream stream = jarFile.getInputStream(nextElement)) {
									try {
										MavenXpp3Reader reader = new MavenXpp3Reader();
										result = reader.read(stream);
									} catch (XmlPullParserException e) {
										System.out.println("Failed parsing pom.xml");
									}
									String groupId = result.getGroupId();
									if (groupId == null) {
										groupId = result.getParent().getGroupId();
									}
									String artifactId = result.getArtifactId();
									if (artifactId == null) {
										artifactId = result.getParent().getArtifactId();
									}
									String version = result.getVersion();
									if (version == null) {
										version = result.getParent().getVersion();
									}
									// TODO extract to POMFactExtractor or something like that
									IRI mvn_coord = model
											.node("Maven-coordinate:" + groupId + ":" + artifactId + ":" + version);
									m.add(m.getName(), RdfVocabulary.RDF_TYPE, MavenFacts.C_PROJECT);
									m.add(m.getName(), MavenFacts.PROJECT_OBJECT, mvn_coord);
									m.add(mvn_coord, MavenFacts.GROUP_ID, model.literal(groupId));
									m.add(mvn_coord, MavenFacts.ARTIFACT_ID, model.literal(artifactId));
									m.add(mvn_coord, MavenFacts.VERSION, model.literal(version));

									System.out.println(result.getDependencies());
									for (Dependency d : result.getDependencies()) {
										// TODO intention is to bind dependency property directly to the other node
										String maven_coordinate = d.getGroupId() + ":" + d.getArtifactId() + ":"
												+ d.getVersion();
										m.add(mvn_coord, MavenFacts.DEPENDS_ON, model.node(maven_coordinate));
										System.out.println(m.getName() + " depends on " + maven_coordinate);
									}

									{// TODO build methods into handlers into maven graph

									}

								}
							} else if (console != null) {
								console.printf("[%2d%%] %3s: %2d%% %s\r", (i * 100) / n, "JAR", (j * 100) / nEntries,
										fill("", 70, "…", true));
							}
							j++;
						}
					}
				}
				if (dataset.supportsTransactions()) {
					dataset.commit();
					// System.out.println("commit transaction");
					dataset.end();
					// System.out.println("end transaction");
				}
			} catch (RuntimeException e) {
				if (dataset.supportsTransactionAbort()) {
					dataset.abort();
					// System.out.println("abort transaction");
				}
				if (dataset.supportsTransactions()) {
					dataset.end();
					// System.out.println("end transaction");
				}
				throw e;
			}
		}
		if (console != null) {
			console.printf("[%02d%%] ALL:     %s\r", (i * 100) / n, fill("done", 80, "…", false));
			console.printf("\n");
		}
	}

	public static String fill(String s, int size, String ellipsis, boolean flushRight) {
		if (s.length() > size) {
			if (flushRight)
				return ellipsis + s.substring(s.length() - (size - ellipsis.length()), s.length());
			else
				return s.substring(0, s.length() - (size - ellipsis.length())) + ellipsis;
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

	private static List<String> findFiles(String loc) throws IOException {
		List<String> files = new ArrayList<>();
		if (demoMode) {
			files.add(loc);
			return files;
		}
		FileSystem fs = FileSystems.getDefault();
		int nClasses = 0;
		int nJars = 0;
		List<String> todo = new ArrayList<>();
		todo.add(loc);
		for (int i = 0; i < todo.size(); i++) {
			Path path = fs.getPath(todo.get(i));
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
					todo.add(p.toString());
				});
			}
		}
		System.out.println("" + nClasses + " class files and " + nJars + " jars found in " + loc + ".");
		return files;
	}

	public static void main(String[] args) throws IOException {
		ModelFactory.setFactory(() -> new JenaRDF());
		ModelFactory mf = ModelFactory.getInstance();
		jenaRDF = new JenaRDF();
		Dataset dataset = DatasetFactory.create();
		String outFile = "/tmp/data.trig";
		setupDataset(dataset);

		LogCtl.setJavaLogging();

		demoMode = args.length == 0;
		logLevel = args.length == 0 ? 10 : 0;
		List<String> arguments = new ArrayList<>(demoMode ? DEFAULT_CLASSES : Arrays.asList(args));
		// IFactsWriter fw = FactsDb.nTripleFactsWriter("/tmp/data.n3", "C");
		Model model = mf.createModel(jenaRDF.asDataset(dataset), DB_PREFIX);
		String modelName = null;
		boolean jung = false;
		try {
			for (Iterator<String> it = arguments.iterator(); it.hasNext();) {
				String arg = it.next();
				switch (arg) {
				case "-h":
					System.err.println(
							"arguments: [options] [-d dbDir | -o outFile.trig | -m modelName | fileOrDir]... [-s]");
					System.err.println("Inputs: fileOrDir can be zero or more of");
					System.err.println("     *.class        Java class files");
					System.err.println("     *.jar          JAR files (contained class files are extracted)");
					System.err.println("     */             Directory (all classes and jars extracted recursively)");
					System.err.println("General options:");
					System.err.println("    -h     help");
					System.err.println("    -v     verbose logging");
					System.err.println("Special options: (take effect when encountered)");
					System.err.println("    -m modelName    set model name for subsequent input");
					System.err.println("    -d dbDir        set TDB database directory");
					System.err.println("    -jung           starts a JFrame with a visualisation of the graph");
					System.err.println("    -o outFile.trig set output TRiG file (when not using TDB)");
					System.err.println("    -s              start server on http://localhost:3330/");
					break;
				case "-v":
					logLevel = 10;
					break;
				case "-o":
					if (outFile == null)
						throw new IllegalArgumentException("-o option incompatible with -d");
					outFile = it.next();
					break;
				case "-d":
					String dbDir = it.next();
					dataset.close();
					dataset = TDBFactory.createDataset(dbDir);
					setupDataset(dataset);
					model = mf.createModel(jenaRDF.asDataset(dataset), DB_PREFIX);
					outFile = null;
					break;
				case "-s":
					FusekiServer server = FusekiServer.create()//
					.add("/dataset", dataset, true)//
					.enableStats(true)//
					.build();
					server.start();
					TDB.sync(dataset);
					server.join();
					break;
				case "-m":
					modelName = it.next();
					break;
				case "-jung":
					jung = true;
					break;
				default:
					extractModel(dataset, model, modelName, arg);
				}
			}

			if (outFile != null) {
				try (OutputStream output = new FileOutputStream(outFile)) {
					RDFDataMgr.write(output, dataset, Lang.TRIG); // throws java.nio.charset.MalformedInputException
					// when
					// dataset is not UTF-8?
					// http://mail-archives.apache.org/mod_mbox/jena-users/201502.mbox/%3C54E6FFFE.7010308@apache.org%3E
					// bug at
					// https://github.com/apache/jena/blob/master/jena-base/src/main/java/org/apache/jena/atlas/io/IndentedWriter.java,
					// in print, line nr 123: should be codepoints, not chars

					/*
					 * clone Jena Fix bug
					 *
					 */

					// jenaModel.write(output, "TURTLE"); //"N-TRIPLE");
				}
			}
			if (jung) {
				dataset.listNames().forEachRemaining(i -> System.out.println(i));
				/*
				 * http://db.nuthatchery.org/java/jvm-fact-extractor-0.0.1-SNAPSHOT.jar
				 * http://model.nuthatchery.org/java/types/
				 * http://model.nuthatchery.org/maven/project/
				 * http://model.nuthatchery.org/java/
				 *
				 */
				// JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://model.nuthatchery.org/maven/project/"));
				JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://model.nuthatchery.org/java/"));
			}
		} finally {
			if (dataset != null) {
				dataset.close();
			}
		}

		Console console = System.console();
		if (console != null) {
			console.printf("ok\n");
		}
	}

	private static void setupDataset(Dataset dataset) {
		defaultModel = ModelFactory.getInstance().createModel(jenaRDF.asGraph(dataset.getDefaultModel()), DB_PREFIX);

		List<String> list = new ArrayList<>();
		for (Iterator<String> it = dataset.listNames(); it.hasNext(); list.add(it.next())) {
			;
		}
		for (String s : list) {
			System.out.println("setup: " + s);
			defaultModel.add(defaultModel.iri(s), RdfVocabulary.RDFS_IS_DEFINED_BY, defaultModel.iri(s));
		}

		// need to manually add prefixes and things
		dataset.getDefaultModel().removeAll();
		dataset.addNamedModel(JavaFacts.javaPrefix, toJenaModel(JavaFacts.javaModel));
		dataset.addNamedModel(JavaFacts.javaTypesPrefix, toJenaModel(JavaFacts.javaTypesModel));
		dataset.addNamedModel(MavenFacts.mavenProjectPrefix, toJenaModel(MavenFacts.mavenProjectModel));
		dataset.getDefaultModel().setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		dataset.getDefaultModel().setNsPrefix("rdf", RdfVocabulary.RDF_PREFIX);
		dataset.getDefaultModel().setNsPrefix("rdfs", RdfVocabulary.RDFS_PREFIX);
		dataset.getDefaultModel().setNsPrefix("j", JavaFacts.javaPrefix);
		dataset.getDefaultModel().setNsPrefix("m", MavenFacts.mavenProjectPrefix);
		dataset.getDefaultModel().setNsPrefix("jType", JavaFacts.javaTypesPrefix);
		dataset.getDefaultModel().setNsPrefix("jFlag", JavaFacts.javaFlagsPrefix);
		dataset.getDefaultModel().setNsPrefix("db", DB_PREFIX);
		dataset.getDefaultModel().setNsPrefix("nh", CommonVocabulary.PREFIX);
	}

	public static org.apache.jena.rdf.model.Model toJenaModel(Model m) {
		return org.apache.jena.rdf.model.ModelFactory.createModelForGraph(jenaRDF.asJenaGraph(m.getGraph()));
	}

}
