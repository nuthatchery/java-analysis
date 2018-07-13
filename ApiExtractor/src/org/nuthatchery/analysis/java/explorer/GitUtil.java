package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitUtil {
	public static ScmContext getGitContext(Path path) {
		Path gitPath = path.resolve(".git");
		if (Files.isDirectory(gitPath)) {
			try (Repository repos = new FileRepository(gitPath.toFile())) {
				Map<String, String> meta = new HashMap<>();
				Set<Path> versionedFiles = new HashSet<>();
				Set<Path> changedFiles = new HashSet<>();
				Set<Path> untrackedFiles = new HashSet<>();

				Ref head = repos.exactRef(Constants.HEAD);
				ObjectId obj = head.getLeaf().getObjectId();

				Config config = repos.getConfig();
				meta.put("scmOrigin", config.getString("remote", "origin", "url"));
				Path repoPath = GitUtil.repoPath(path, meta.get("origin"));
				meta.put("scmName", GitUtil.repoName(path, meta.get("origin")));
				meta.put("scmTool", "https://git-scm.com/");
				String hash = "@" + java.time.Instant.now().toString();
				;
				try (Git git = new Git(repos)) {
					Status status = git.status().call();
					if (obj != null) {
						RevCommit headCommit = repos.parseCommit(obj);
						RevTree tree = headCommit.getTree();
						Set<String> uncommittedChanges = status.getUncommittedChanges();
						status.getUntrackedFolders().stream().forEach((s) -> untrackedFiles.add(path.resolve(s)));
						status.getUntracked().stream().filter(FilesystemExplorer.suffixFilter)
						.forEach((s) -> untrackedFiles.add(path.resolve(s)));
						try (TreeWalk walk = new TreeWalk(repos)) {
							walk.addTree(tree);
							walk.setRecursive(true);
							walk.setFilter(FilesystemExplorer.treeFilter);
							while (walk.next()) {
								String s = walk.getPathString();
								versionedFiles.add(path.resolve(s));
								if (uncommittedChanges.contains(s)) {
									changedFiles.add(path.resolve(s));
								}
							}
						}
						meta.put("lastCommitHash", obj.getName());
						meta.put("lastCommitDescription", headCommit.getShortMessage());
						addMeta(meta, "lastCommitAuthor", headCommit.getAuthorIdent());
						meta.put("scmAuthor", meta.get("lastCommitAuthor"));
						addMeta(meta, "lastCommitCommitter", headCommit.getCommitterIdent());
						Map<ObjectId, String> names = git.nameRev().addPrefix("refs/tags/").add(obj).call();
						meta.put("lastCommitTag", names.get(obj));
					}
					if (status.hasUncommittedChanges()) {
						addMeta(meta, "scmAuthor", new PersonIdent(repos));
					} else if (obj != null) {
						hash = obj.getName();
					}
				}
				meta.put("scmVersion", hash);
				if (meta.get("scmOrigin") != null) {
					meta.put("scmVersionUri", meta.get("scmOrigin") + "/" + hash);
				} else {
					meta.put("scmVersionUri", "git://" + meta.get("scmName") + ".git/" + hash);
				}
				return new ScmContext(path, repoPath, meta, versionedFiles, changedFiles, untrackedFiles);
			} catch (JGitInternalException | GitAPIException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private static Path repoPath(Path path, String origin) {
		if (origin != null) {
			try {
				URI uri = new URI(origin);
				String p = uri.getPath();
				if (p.endsWith(".git")) {
					p = p.substring(0, p.length() - 4);
				}
				return Paths.get(p);
			} catch (URISyntaxException e) {
			}
		}

		return path.toAbsolutePath().getFileName();
	}

	private static void addMeta(Map<String, String> map, String key, PersonIdent value) {
		if (value != null) {
			map.put(key + "Name", value.getName());
			map.put(key + "Email", value.getEmailAddress());
			map.put(key + "Time", value.getWhen().toInstant().toString());
		}
	}

	public static String repoName(Path path, String origin) {
		if (origin != null) {
			Matcher matcher = Pattern.compile("^.*[:/]([^:/]+)\\.git$").matcher(origin);
			if (matcher.matches())
				return matcher.group(1);
		}

		return path.toAbsolutePath().getFileName().toString();
	}

}
