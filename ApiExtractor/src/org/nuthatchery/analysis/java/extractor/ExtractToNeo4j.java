// package org.nuthatchery.analysis.java.extractor;
//
/// * Uncertain as to difference between Neo4j for java developers, vs embedded
// */
//
// import java.io.Console;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.nio.file.FileSystem;
// import java.nio.file.FileSystems;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Enumeration;
// import java.util.Iterator;
// import java.util.List;
// import java.util.jar.JarEntry;
// import java.util.jar.JarFile;
//
// import org.apache.jena.atlas.logging.LogCtl;
// import org.apache.jena.fuseki.embedded.FusekiServer;
// import org.apache.jena.query.Dataset;
// import org.apache.jena.query.DatasetFactory;
// import org.apache.jena.riot.Lang;
// import org.apache.jena.riot.RDFDataMgr;
// import org.apache.jena.tdb.TDB;
// import org.apache.jena.tdb.TDBFactory;
// import org.nuthatchery.ontology.basic.CommonVocabulary;
// import org.objectweb.asm.ClassReader;
//
// public class ExtractToNeo4j {
// private static final String DB_PREFIX = "http://db.nuthatchery.org/java/";
// private static final List<String> DEFAULT_CLASSES = Arrays.asList("-m",
// "demo", "../../../../../Test.class",
// "../../../../../Test$1.class", "../../../../../Test$Bar.class",
// "../../../../../IMutablePosition.class");
// private static int logLevel;
// private static boolean demoMode = false;
// // "/home/anya/.m2/repository/com/lowagie/itext/2.1.5/itext-2.1.5.jar");
// private static JenaRDF jenaRDF;
// private static Model defaultModel;
//
// private static void addModelName(IRI s) {
// defaultModel.add(s, RdfVocabulary.RDFS_IS_DEFINED_BY, s);
// }
//
// private static void extractModel(Dataset dataset, Model mainModel, String
// modelName, String arg)
// throws IOException, FileNotFoundException {
// String prc = "Processing: ";
// String chk = "(checkpoint) ";
// String msg = prc;
// List<String> files = findFiles(arg);
// Console console = System.console();
// int i = 0;
// int n = files.size();
//
// if (modelName == null) {
// modelName = "";
// }
// Model model = mainModel.model(DB_PREFIX + modelName, "this:"); // DB_PREFIX +
// modelName + "/");
// for (String file : files) {
// System.out.println("Processing: " + file);
// if (console != null) {
// console.printf("[%02d%%] %s%s\r", (i * 100) / n, msg, fill(file, 60, "…",
// true));
// }
// if (demoMode && file.endsWith(".class")) {
// addModelName(model.getName());
// ClassFactExtractor ea = new ClassFactExtractor(model,
// JavaUtil.logger(logLevel));
// ClassReader cr = new
// ClassReader(ExtractToNeo4j.class.getResourceAsStream(file));
// cr.accept(ea, ClassReader.EXPAND_FRAMES);
// } else if (file.endsWith(".class")) {
// addModelName(model.getName());
// try (InputStream stream = new FileInputStream(file)) {
// ClassFactExtractor ea = new ClassFactExtractor(model,
// JavaUtil.logger(logLevel));
// ClassReader cr = new ClassReader(stream);
// cr.accept(ea, ClassReader.EXPAND_FRAMES);
// }
// } else if (file.endsWith(".jar")) {
// try (JarFile jarFile = new JarFile(file)) {
// String name = file;
// if (name.contains("/")) {
// name = name.substring(name.lastIndexOf("/") + 1, name.length());
// }
// Model m = model.model(DB_PREFIX + name, "this:");
// addModelName(m.getName());
// ClassFactExtractor ea = new ClassFactExtractor(m, JavaUtil.logger(logLevel));
// int nEntries = jarFile.size();
// int j = 0;
// for (Enumeration<JarEntry> entries = jarFile.entries();
// entries.hasMoreElements();) {
// JarEntry nextElement = entries.nextElement();
// if (nextElement.getName().endsWith(".class")) {
// try (InputStream stream = jarFile.getInputStream(nextElement)) {
//
// if (console != null) {
// console.printf("[%2d%%] JAR: %2d%% %s\r", (i * 100) / n, (j * 100) /
// nEntries,
// fill(file, 60, "…", true));
// }
// ClassReader cr = new ClassReader(stream);
// cr.accept(ea, ClassReader.EXPAND_FRAMES);
// }
// } else if (console != null) {
// console.printf("[%2d%%] JAR: %2d%% %s\r", (i * 100) / n, (j * 100) /
// nEntries,
// fill("", 60, "…", true));
// }
// j++;
// }
// }
// }
// if (i++ % 10 == 0) {
// TDB.sync(dataset);
// msg = chk;
// } else if (i % 10 == 5) {
// msg = prc;
// }
// }
// }
//
// public static String fill(String s, int size, String ellipsis, boolean
// flushRight) {
// if (s.length() > size) {
// if (flushRight)
// return ellipsis + s.substring(s.length() - (size - ellipsis.length()),
// s.length());
// else
// return s.substring(0, s.length() - (size - ellipsis.length())) + ellipsis;
// }
// if (flushRight) {
// for (; s.length() < size; s = " " + s) {
// ;
// }
// } else {
// for (; s.length() < size; s = s + " ") {
// ;
// }
// }
// return s;
//
// }
//
// private static List<String> findFiles(String loc) throws IOException {
// List<String> files = new ArrayList<>();
// if (demoMode) {
// files.add(loc);
// return files;
// }
// FileSystem fs = FileSystems.getDefault();
// int nClasses = 0;
// int nJars = 0;
// List<String> todo = new ArrayList<>();
// todo.add(loc);
// for (int i = 0; i < todo.size(); i++) {
// Path path = fs.getPath(todo.get(i));
// String str = path.toString();
// if (Files.isRegularFile(path)) {
// if (str.endsWith(".class")) {
// files.add(path.toString());
// nClasses++;
// } else if (str.endsWith(".jar")) {
// files.add(path.toString());
// nJars++;
// }
// } else if (Files.isDirectory(path)) {
// System.out.println("Adding files from " + path);
// Files.list(path).forEach((Path p) -> {
// todo.add(p.toString());
// });
// }
// }
// System.out.println("" + nClasses + " class files and " + nJars + " jars found
// in " + loc + ".");
// return files;
// }
//
// public static void main(String[] args) throws IOException {
// ModelFactory.setFactory(() -> new JenaRDF());
// ModelFactory mf = ModelFactory.getInstance();
// jenaRDF = new JenaRDF();
// Dataset dataset = DatasetFactory.create();
// String outFile = "/tmp/data.trig";
// setupDataset(dataset);
//
// LogCtl.setJavaLogging();
//
// demoMode = args.length == 0;
// logLevel = args.length == 0 ? 10 : 0;
// List<String> arguments = new ArrayList<>(demoMode ? DEFAULT_CLASSES :
// Arrays.asList(args));
// // IFactsWriter fw = FactsDb.nTripleFactsWriter("/tmp/data.n3", "C");
// Model model = mf.createModel(jenaRDF.asDataset(dataset), DB_PREFIX);
// String modelName = null;
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
// System.err.println(" -d dbDir set TDB database directory");
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
// case "-d":
// String dbDir = it.next();
// dataset.close();
// dataset = TDBFactory.createDataset(dbDir);
// setupDataset(dataset);
// model = mf.createModel(jenaRDF.asDataset(dataset), DB_PREFIX);
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
// default:
// extractModel(dataset, model, modelName, arg);
// }
// }
//
// if (outFile != null) {
// try (OutputStream output = new FileOutputStream(outFile)) {
// RDFDataMgr.write(output, dataset, Lang.TRIG);
// // jenaModel.write(output, "TURTLE"); //"N-TRIPLE");
// }
// }
// dataset.close();
//
// // ANNA: load file from outFile to neo4j
// Driver driver = GraphDatabase.driver("bolt://localhost:7687",
// AuthTokens.basic("neo4j", "password"));
// GraphDatabaseService graphDb = new GraphDatabaseFactory()
// .newEmbeddedDatabase(new File("../../../../../graphdb"));
//
// try (Session session = driver.session()) {
// String greeting = session.writeTransaction(new TransactionWork<String>() {
// @Override
// public String execute(Transaction tx) {
// StatementResult result = tx.run(
// "CREATE (a:Greeting) " + "SET a.message = {message} "
// + "RETURN a.message + ', from node ' + id(a)", parameters("message", "Hello
// Anna!"));
// return result.single().get(0).asString();
// }
// });
// System.out.println(greeting);
//
// }
// graphDb.shutdown();
// }
//
// private static void setupDataset(Dataset dataset) {
// defaultModel =
// ModelFactory.getInstance().createModel(jenaRDF.asGraph(dataset.getDefaultModel()),
// DB_PREFIX);
//
// List<String> list = new ArrayList<>();
// for (Iterator<String> it = dataset.listNames(); it.hasNext();
// list.add(it.next())) {
// ;
// }
// for (String s : list) {
// System.out.println("setup: " + s);
// defaultModel.add(defaultModel.iri(s), RdfVocabulary.RDFS_IS_DEFINED_BY,
// defaultModel.iri(s));
// }
//
// dataset.getDefaultModel().removeAll();
// dataset.addNamedModel(JavaFacts.javaPrefix,
// toJenaModel(JavaFacts.javaModel));
// dataset.addNamedModel(JavaFacts.javaTypesPrefix,
// toJenaModel(JavaFacts.javaTypesModel));
// dataset.getDefaultModel().setNsPrefix("xsd",
// "http://www.w3.org/2001/XMLSchema#");
// dataset.getDefaultModel().setNsPrefix("rdf", RdfVocabulary.RDF_PREFIX);
// dataset.getDefaultModel().setNsPrefix("rdfs", RdfVocabulary.RDFS_PREFIX);
// dataset.getDefaultModel().setNsPrefix("j", JavaFacts.javaPrefix);
// dataset.getDefaultModel().setNsPrefix("jType", JavaFacts.javaTypesPrefix);
// dataset.getDefaultModel().setNsPrefix("jFlag", JavaFacts.javaFlagsPrefix);
// dataset.getDefaultModel().setNsPrefix("db", DB_PREFIX);
// dataset.getDefaultModel().setNsPrefix("nh", CommonVocabulary.PREFIX);
// }
//
// private static org.apache.jena.rdf.model.Model toJenaModel(Model m) {
// return
// org.apache.jena.rdf.model.ModelFactory.createModelForGraph(jenaRDF.asJenaGraph(m.getGraph()));
// }
//
// }
