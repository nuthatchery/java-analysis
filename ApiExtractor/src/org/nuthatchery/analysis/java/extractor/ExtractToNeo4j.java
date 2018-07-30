package org.nuthatchery.analysis.java.extractor;
//import net.rootdev.jenajung.JenaJungJFrame;

/* Uncertain as to difference between Neo4j for java developers, vs embedded */

// https://neo4j.com/developer/java/
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import static org.neo4j.driver.v1.Values.parameters;

// https://neo4j.com/docs/java-reference/current/tutorials-java-embedded/
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.nuthatchery.analysis.java.explorer.Artifact;
import org.nuthatchery.analysis.java.explorer.ArtifactHandle;
import org.nuthatchery.analysis.java.explorer.FilesystemExplorer;
import org.nuthatchery.analysis.java.explorer.PomContext;
import org.nuthatchery.analysis.java.explorer.ProjectContext;
import org.nuthatchery.ontology.basic.CommonVocabulary;
import org.objectweb.asm.ClassReader;

public class ExtractToNeo4j {
	private static final String DB_PREFIX = "https://db.nuthatchery.org/";
	private static final List<String> DEFAULT_CLASSES = Arrays.asList("-m", "demo", "..", "../../../../../Test.class",
			"../../../../../Test$1.class", "../../../../../Test$Bar.class", "../../../../../IMutablePosition.class");
	private static int logLevel;
	private static boolean demoMode = false;
	private static boolean addGraphToDataset = false;
	private static boolean addOntologyToDataset = false;
	// "/home/anya/.m2/repository/com/lowagie/itext/2.1.5/itext-2.1.5.jar");
	public static final Map<String, String> prefixMapping = new ConcurrentHashMap<>();
	static {
		prefixMapping.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixMapping.put("rdf", RDF.uri);
		prefixMapping.put("rdfs", RDFS.uri);
		prefixMapping.put("j", JavaFacts.J);
		prefixMapping.put("jvm", JavaFacts.JVM);
		prefixMapping.put("m", MavenFacts.M);
		// prefixMapping.put("jType", JavaFacts.JT);
		// prefixMapping.put("jFlag", JavaFacts.JF);
		prefixMapping.put("db", DB_PREFIX);
		prefixMapping.put("nh", CommonVocabulary.NS);
	}

	private static void extractModel(Dataset dataset, String modelName, String arg)
			throws IOException, FileNotFoundException {
		List<Artifact> artifacts = FilesystemExplorer.explore(arg);
		List<IOException> errors = new ArrayList<>();


		artifacts.parallelStream().forEach((art) -> {
			ProjectContext info = art.getInfo();
			String groupId = info.getGroupId();
			String artifactId = info.getArtifactId();
			String version = info.getVersion();
			Model model = ModelFactory.createDefaultModel();
			String mName = DB_PREFIX + info.getRdfId() + "/";
			try {
				mName = new URI(mName).normalize().toString();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
			Resource modelId = model.createResource(mName);
			prefixMapping.put(artifactId + "-" + version, modelId.getURI());
			model.setNsPrefixes(prefixMapping);
			String prefix = "java://";
			model.setNsPrefix("", modelId.getURI());
			if (groupId != null) {
				modelId.addLiteral(MavenFacts.groupId, groupId);
			}
			if (artifactId != null) {
				modelId.addLiteral(MavenFacts.artifactId, artifactId);
			}
			if (version != null) {
				modelId.addLiteral(MavenFacts.version, version);
			}

			PomContext pomContext = info.getPomContext();
			if (pomContext != null) {
				Resource mvnCoord = model.createResource(pomContext.getMavenUri(), MavenFacts.MavenCoordinate);
				modelId.addProperty(RDF.type, MavenFacts.MavenProject);
				modelId.addProperty(MavenFacts.hasCoord, mvnCoord);
				for (Dependency d : pomContext.getDependencies()) {
					// TODO intention is to bind dependency property directly to the other node
					String depUri = PomContext.getMavenUri(d);
					mvnCoord.addProperty(MavenFacts.dependsOn,
							model.createResource(depUri, MavenFacts.MavenCoordinate));
					System.out.println(pomContext.getMavenUri() + " depends on " + depUri);
				}
				synchronized (dataset) {
					dataset.getDefaultModel().add(mvnCoord.listProperties());
				}
			}
			synchronized (dataset) {
				dataset.getDefaultModel().add(modelId.listProperties());
			}

			try (ArtifactHandle h = art.open()) {
				h.stream().forEach((bytes) -> {
					ClassFactExtractor ea = new ClassFactExtractor(model, prefix, JavaUtil.logger(logLevel));
					ClassReader cr = new ClassReader(bytes.getBytes());
					cr.accept(ea, ClassReader.EXPAND_FRAMES);
				});
			}
			if (Files.exists(art.getPath())) {
				Path outPath = Paths.get(art.getPath().toString() + ".ttl");
				try (OutputStream output = Files.newOutputStream(outPath)) {
					System.out.println("Writing " + outPath);
					RDFDataMgr.write(output, model, Lang.TURTLE);
				} catch (IOException e) {
					errors.add(e);
					e.printStackTrace();
				}
			}
			if (addGraphToDataset) {
				dataset.addNamedModel(model.getNsPrefixURI(""), model);
			}
			boolean reasoning = false;
			if (reasoning) {

				Resource config = ModelFactory.createDefaultModel().createResource()
						.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "full");
				Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);
				InfModel inf = ModelFactory.createInfModel(reasoner, dataset.getUnionModel());
				// from https://jena.apache.org/documentation/inference/index.html#owl
				// Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
				// reasoner = reasoner.bindSchema(dataset.getDefaultModel());
				// System.out.println(dataset.getDefaultModel().listNameSpaces().toList()); //
				// output: [https://model.nuthatchery.org/maven/]
				InfModel infmodel = ModelFactory.createInfModel(reasoner, model);

				try {
					inf.write(new FileOutputStream("model.xml"), "TURTLE");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Resource resource = inf.getResource("m:project");
				System.out.println("'m:project':");
				Model m = inf;
				Resource s = resource;
				Property p = null;
				Resource o = null;
				System.out.println("first statement");
				for (StmtIterator i = m.listStatements(s, p, o); i.hasNext();) {
					System.out.println("another statement");
					Statement stmt = i.nextStatement();
					System.out.println(" - " + PrintUtil.print(stmt));
				}

			}

		});

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

	public static void main(String[] args) throws IOException {
		// Dataset dataset = DatasetFactory.create();
		// String outFile = "/tmp/data.trig";
		// setupDataset(dataset);
		//
		// LogCtl.setJavaLogging();
		//
		// demoMode = args.length == 0;
		// logLevel = args.length == 0 ? 10 : 0;
		// logLevel = 0;
		// List<String> arguments = new ArrayList<>(demoMode ? DEFAULT_CLASSES :
		// Arrays.asList(args));
		// // IFactsWriter fw = FactsDb.nTripleFactsWriter("/tmp/data.n3", "C");
		// String modelName = null;
		// boolean jung = false;
		// try {
		// for (Iterator<String> it = arguments.iterator(); it.hasNext();) {
		// String arg = it.next();
		// switch (arg) {
		// case "-h":
		// System.err.println(
		// "arguments: [options] [-d dbDir | -o outFile.trig | -m modelName |
		// fileOrDir]... [-s]");
		// System.err.println("Inputs: fileOrDir can be zero or more of");
		// System.err.println(" *.class Java class files");
		// System.err.println(" *.jar JAR files (contained class files are extracted)");
		// System.err.println(" */ Directory (all classes and jars extracted
		// recursively)");
		// System.err.println("General options:");
		// System.err.println(" -h help");
		// System.err.println(" -v verbose logging");
		// System.err.println("Special options: (take effect when encountered)");
		// System.err.println(" -m modelName set model name for subsequent input");
		// System.err.println(" -ont add ontology to dataset");
		// System.err.println(" -mod add models as named graphs in dataset");
		// System.err.println(" -db dbDir set TDB database directory");
		// System.err.println(" -jung starts a JFrame with a visualisation of the
		// graph");
		// System.err.println(" -o outFile.trig set output TRiG file (when not using
		// TDB)");
		// System.err.println(" -s start server on http://localhost:3330/");
		// break;
		// case "-v":
		// logLevel = 10;
		// break;
		// case "-o":
		// if (outFile == null)
		// throw new IllegalArgumentException("-o option incompatible with -d");
		// outFile = it.next();
		// break;
		// case "-mod":
		// addGraphToDataset = true;
		// break;
		// case "-ont":
		// addOntologyToDataset = true;
		// break;
		// case "-db":
		// String dbDir = it.next();
		// dataset.close();
		// dataset = TDBFactory.createDataset(dbDir);
		// setupDataset(dataset);
		// outFile = null;
		// break;
		// case "-s":
		// FusekiServer server = FusekiServer.create()//
		// .add("/dataset", dataset, true)//
		// .enableStats(true)//
		// .build();
		// server.start();
		// TDB.sync(dataset);
		// server.join();
		// break;
		// case "-m":
		// modelName = it.next();
		// break;
		// case "-jung":
		// jung = true;
		// break;
		// default:
		// extractModel(dataset, modelName, arg);
		// }
		// }
		//
		// if (outFile != null) {
		// try (OutputStream output = new FileOutputStream(outFile)) {
		// dataset.getDefaultModel().setNsPrefixes(prefixMapping);
		// RDFDataMgr.write(output, dataset, Lang.TRIG); // throws
		// java.nio.charset.MalformedInputException
		// // when
		// // dataset is not UTF-8?
		// //
		// http://mail-archives.apache.org/mod_mbox/jena-users/201502.mbox/%3C54E6FFFE.7010308@apache.org%3E
		// // bug at
		// //
		// https://github.com/apache/jena/blob/master/jena-base/src/main/java/org/apache/jena/atlas/io/IndentedWriter.java,
		// // in print, line nr 123: should be codepoints, not chars
		//
		// /*
		// * clone Jena Fix bug
		// *
		// */
		//
		// // jenaModel.write(output, "TURTLE"); //"N-TRIPLE");
		// }
		// }
		// if (jung) {
		// System.out.println("all named models: ");
		// dataset.listNames().forEachRemaining(i -> System.out.println(i));
		// /*
		// * http://db.nuthatchery.org/java/jvm-fact-extractor-0.0.1-SNAPSHOT.jar
		// * http://model.nuthatchery.org/java/types/
		// * http://model.nuthatchery.org/maven/project/
		// * http://model.nuthatchery.org/java/
		// *
		// */
		// System.out.println(dataset.getDefaultModel());
		// System.out.println(dataset.getUnionModel());
		// //
		// JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://model.nuthatchery.org/maven/project/"));
		// // TODO let you pick which graph to visualise
		// // JenaJungJFrame.makeJFrame(dataset.getDefaultModel());
		// //
		// JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://db.nuthatchery.org/java/jvm-fact-extractor-0.0.1-SNAPSHOT.jar"));
		// //
		// JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://model.nuthatchery.org/maven/project/"));
		// //
		// JenaJungJFrame.makeJFrame(dataset.getNamedModel("http://model.nuthatchery.org/java/"));
		// }
		// } finally {
		// if (dataset != null) {
		// dataset.close();
		// }
		// }
		//
		// Console console = System.console();
		// if (console != null) {
		// console.printf("ok\n");
		// }
		// ANNA: load file from outFile to neo4j
		Driver driver = GraphDatabase.driver("bolt://localhost:7687",
				AuthTokens.basic("neo4j", "password"));
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(new File("graphdb"));

		try (Session session = driver.session()) {
			String greeting = session.writeTransaction(new TransactionWork<String>() {
				@Override
				public String execute(Transaction tx) {
					StatementResult result = tx.run(
							"CREATE (a:Greeting) " + "SET a.message = {message} "
									+ "RETURN a.message + ', from node ' + id(a)",
									parameters("message", "Hello Anna!"));
					return result.single().get(0).asString();
				}
			});
			System.out.println(greeting);

		}
		graphDb.shutdown();
	}

	private static void setupDataset(Dataset dataset) {

		if (addOntologyToDataset) {
			List<String> list = new ArrayList<>();
			Model defaultModel = dataset.getDefaultModel();
			for (Iterator<String> it = dataset.listNames(); it.hasNext(); list.add(it.next())) {
				;
			}
			for (String s : list) {
				System.out.println("setup: " + s);
				defaultModel.add(defaultModel.createResource(s), RDFS.isDefinedBy, defaultModel.createResource(s));
			}

			// need to manually add prefixes and things
			dataset.getDefaultModel().removeAll();
			dataset.addNamedModel(JavaFacts.J, JavaFacts.javaModel);
			// dataset.addNamedModel(JavaFacts.JT, JavaFacts.javaTypesModel);
			dataset.addNamedModel(MavenFacts.M, MavenFacts.mavenModel);
		}
		else {
			// force class loading / initialization
			JavaFacts.javaModel.size();
		}
		dataset.getDefaultModel().setNsPrefixes(prefixMapping);
	}
}
