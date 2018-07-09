package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SLF4JLogFactory;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

public class FilesystemExplorer {
	private final Stack<ContainerArtifact> containers = new Stack<>();
	private final Stack<ScmContext> scmContexts = new Stack<>();
	private final Stack<PomContext> pomContexts = new Stack<>();
	protected static final Log log = SLF4JLogFactory.getLog("explorer");
	private List<Artifact> arts = new ArrayList<>();
	public static final Set<String> ignoreFolders = new HashSet<>(Arrays.asList());
	public static final List<String> suffixes = Arrays.asList(".class", ".jar", "pom.xml", ".pom");
	public static final TreeFilter treeFilter = OrTreeFilter
			.create(suffixes.stream().map((String x) -> PathSuffixFilter.create(x)).collect(Collectors.toList()));
	public static final Predicate<String> suffixFilter = Pattern.compile(String.join("|", suffixes)).asPredicate();

	public FilesystemExplorer() {
		scmContexts.push(null);
		containers.push(null);
		pomContexts.push(null);
	}

	public static List<Artifact> explore(String fileOrFolder) {
		FilesystemExplorer ex = new FilesystemExplorer();
		log.info("Exploring: " + fileOrFolder);
		Path path = FileSystems.getDefault().getPath(fileOrFolder);
		if (Files.isRegularFile(path)) {
			if (fileOrFolder.endsWith(".class")) {
				ex.addArtifact(new ClassArtifact(path));
			} else if (fileOrFolder.endsWith(".jar")) {
				JarArtifact jar = new JarArtifact(path);
				jar.getInfo();
				ex.addArtifact(jar);
			}
		} else if (Files.isDirectory(path)) {
			ex.visitFolder(path);
		}
		return ex.getArtifacts();
	}

	private List<Artifact> getArtifacts() {
		return new ArrayList<>(arts);
	}

	private void addArtifact(Artifact art) {
		log.info("Adding artifact: " + art);
		arts.add(art);
	}

	public void visitFolder(Path path) {
		PomContext pomContext = PomUtil.getPom(path, path.resolve("pom.xml"));
		String eclipseProjectName = getEclipseProjectName(path);
		Map<String, String> manifest = getManifestInfo(path);
		ScmContext scmContext = GitUtil.getGitContext(path);
		try {
			if (scmContext != null) {
				scmContexts.push(scmContext);
			}
			if (pomContext != null) {
				pomContexts.push(pomContext);
			}
			ProjectContext projectContext = ProjectContext.getProjectContext(path, pomContext, scmContext,
					eclipseProjectName, manifest);
			if (projectContext != null) {
				containers.push(new ContainerArtifact(projectContext));
			}
			// log.info("Adding files from " + path + " to " + containers.peek());
			try {
				Files.list(path).forEachOrdered((Path p) -> {
					if (Files.isDirectory(p)) {
						if (!p.getFileName().toString().startsWith(".")) {
							visitFolder(p);
						}
					} else if (p.toString().endsWith(".class")) {
						ContainerArtifact c = containers.peek();
						ClassArtifact cls = new ClassArtifact(p);
						if (c != null) {
							c.addArtifact(cls);
						} else {
							addArtifact(cls);
						}
					} else if (p.toString().endsWith(".jar")) {
						JarArtifact jar = new JarArtifact(p);
						jar.getInfo();
						addArtifact(jar);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (projectContext != null) {
					containers.pop();
				}
			}
		} finally {
			if (scmContext != null) {
				scmContexts.pop();
			}
			if (pomContext != null) {
				pomContexts.pop();
			}
		}
	}

	private static String getEclipseProjectName(Path path) {
		Path projectPath = path.resolve(".project");
		if (Files.exists(projectPath)) {
			try (InputStream stream = Files.newInputStream(projectPath)) {
				MXParser parser = new MXParser();
				parser.setInput(stream, "UTF-8");
				List<String> tags = new ArrayList<>();
				for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser
						.next()) {
					if (eventType == XmlPullParser.START_TAG) {
						tags.add(parser.getName());
					} else if (eventType == XmlPullParser.END_TAG) {
						tags.remove(tags.size() - 1);
					} else if (eventType == XmlPullParser.TEXT) {
						if (tags.get(tags.size() - 1).equals("name") //
								&& tags.get(tags.size() - 2).equals("projectDescription"))
							return parser.getText();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException | IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static Map<String, String> getManifestInfo(Path path) {
		Path mfPath = path.resolve("META-INF/MANIFEST.MF");
		if (Files.exists(mfPath)) {
			try (InputStream stream = Files.newInputStream(mfPath)) {
				Manifest manifest = new Manifest(stream);
				Map<String, Attributes> entries = manifest.getEntries();
				for (String k : entries.keySet()) {
					Attributes attrs = entries.get(k);
					attrs.forEach((a, b) -> {
						System.out.println(a + " → " + b);
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
