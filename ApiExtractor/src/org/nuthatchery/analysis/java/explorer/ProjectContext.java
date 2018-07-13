package org.nuthatchery.analysis.java.explorer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectContext {

	private static Pattern jarName = Pattern.compile("^(.+)-([0-9]+\\.[0-9a-zA-Z.-]+)\\.jar$");

	private static void buildName(ProjectContext ctx) {
		StringBuilder b = new StringBuilder();
		if (ctx.groupId != null) {
			b.append(ctx.groupId);
			b.append(":");
		}
		if (ctx.artifactId != null) {
			b.append(ctx.artifactId);
			ctx.name = b.toString();
			b.append(":");
		} else {
			ctx.name = b.toString();
		}
		if (ctx.version != null) {
			b.append(ctx.version);
		}
		ctx.vName = b.toString();
		ctx.rdfId = String.format("/%s/%s/%s", ctx.groupId != null ? ctx.groupId : "_", // )
				ctx.artifactId != null ? ctx.artifactId : "_", //
						ctx.version != null ? ctx.version : "_");
	}

	private static String getEclipseInfo(String eclipseProjectName, ProjectContext ctx) {
		String source;
		int i = eclipseProjectName.indexOf('.');
		if (i > -1) {
			ctx.groupId = eclipseProjectName.substring(i);
			ctx.artifactId = eclipseProjectName.substring(0, i);
		} else {
			ctx.artifactId = eclipseProjectName;
		}
		FilesystemExplorer.log.info("found .project: groupId=" + ctx.groupId + ", artifactId=" + ctx.artifactId);
		source = "Eclipse";
		return source;
	}

	private static String getGitInfo(Path path, ScmContext scmContext, ProjectContext ctx) {
		String source;
		Map<String, String> data = scmContext.getMetaData();
		Path relPath = scmContext.getRoot().relativize(path);
		System.out.println("relPath: " + relPath);
		Path repoPath = scmContext.getRepoPath();
		if (relPath != null) {
			repoPath = repoPath.resolve(relPath);
		}
		FilesystemExplorer.log.info("repoPath: " + repoPath.toAbsolutePath().normalize());
		Path fileName = repoPath.toAbsolutePath().normalize().getFileName();
		Path parent = repoPath.getParent();
		FilesystemExplorer.log.info("fileName: " + fileName);
		FilesystemExplorer.log.info("parent: " + parent);
		ctx.artifactId = fileName != null ? fileName.toString().replace('/', '.') : null;
		ctx.groupId = parent != null ? parent.toString().replace('/', '.') : null;
		ctx.version = data.get("scmVersion");
		ctx.uri = data.get("scmUri");
		ctx.vName = data.get("scmVersionUri");
		ctx.vUri = data.get("scmVersionUri");
		ctx.scmContext = scmContext;
		FilesystemExplorer.log.info("found .git: groupId=" + ctx.groupId + ", artifactId=" + ctx.artifactId);
		source = "git";
		return source;
	}

	private static String getJarNameInfo(Path path, ProjectContext ctx) {
		String fileName = path.getFileName().toString();
		Matcher matcher = jarName.matcher(fileName);
		if (matcher.matches()) {
			ctx.artifactId = matcher.group(1);
			ctx.version = matcher.group(2);
		}
		return "jarfilename";
	}

	public static ProjectContext getJarProjectContext(Path path, PomContext pomContext, Map<String, String> manifest) {
		ProjectContext ctx = new ProjectContext();
		ctx.path = path;
		String source = getJarNameInfo(path, ctx);
		if (pomContext == null) {
			pomContext = PomUtil.getPom(path, Paths.get(path.toString().replaceAll("\\.jar$", ".pom")));
		}
		if (pomContext != null) {
			source = getPomInfo(pomContext, ctx);
		}
		buildName(ctx);

		if (ctx.name != null) {
			FilesystemExplorer.log.info("Found " + source + " project: " + ctx.vName);
			return ctx;
		} else
			return null;
	}

	private static String getPomInfo(PomContext pomContext, ProjectContext ctx) {
		String source;
		ctx.artifactId = pomContext.getArtifactId();
		ctx.groupId = pomContext.getGroupId();
		ctx.version = pomContext.getVersion();
		ctx.uri = "maven://" + ctx.groupId + "/" + ctx.artifactId;
		ctx.vUri = "maven://" + ctx.groupId + "/" + ctx.artifactId + "/" + ctx.version;
		ctx.pomContext = pomContext;
		source = "pom";
		return source;
	}

	public static ProjectContext getProjectContext(Path path, PomContext pomContext, ScmContext scmContext,
			String eclipseProjectName, Map<String, String> manifest) {
		if (pomContext == null && scmContext == null && eclipseProjectName == null && manifest == null)
			return null;

		ProjectContext ctx = new ProjectContext();
		ctx.path = path;
		String source = "nowhere";
		if (eclipseProjectName != null) {
			source = getEclipseInfo(eclipseProjectName, ctx);
		}
		if (scmContext != null) {
			source = getGitInfo(path, scmContext, ctx);
		}
		if (pomContext != null) {
			source = getPomInfo(pomContext, ctx);
		}
		buildName(ctx);

		if (ctx.name != null) {
			FilesystemExplorer.log.info("Found " + source + " project: " + ctx.vName);
			return ctx;
		} else
			return null;
	}

	private String artifactId;

	private String groupId;

	private String version;

	private String name;

	private String uri;
	private String vName;
	private String vUri;
	private PomContext pomContext;
	private ScmContext scmContext;
	private Path path;
	private String rdfId;

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public Path getPath() {
		return path;
	}

	/**
	 * @return the pomContext
	 */
	public PomContext getPomContext() {
		return pomContext;
	}

	public String getRdfId() {
		return rdfId;
	}

	/**
	 * @return the scmContext
	 */
	public ScmContext getScmContext() {
		return scmContext;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the vName
	 */
	public String getVName() {
		return vName;
	}

	/**
	 * @return the vUri
	 */
	public String getvUri() {
		return vUri;
	}

}
