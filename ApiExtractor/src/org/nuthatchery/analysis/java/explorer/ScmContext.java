package org.nuthatchery.analysis.java.explorer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ScmContext {
	private final Map<String, String> metaData;
	private final Set<Path> versionedFiles;
	private final Set<Path> changedFiles;
	private final Set<Path> untrackedFiles;
	private final Path root;
	private final Path repoPath;

	public ScmContext(Path root, Path repoPath, Map<String, String> metaData, Set<Path> versionedFiles,
			Set<Path> changedFiles,
			Set<Path> untrackedFiles) {
		super();
		this.root = root;
		this.repoPath = repoPath;
		this.metaData = Collections.unmodifiableMap(metaData);
		this.versionedFiles = Collections.unmodifiableSet(versionedFiles);
		this.changedFiles = Collections.unmodifiableSet(changedFiles);
		this.untrackedFiles = Collections.unmodifiableSet(untrackedFiles);
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public Set<Path> getVersionedFiles() {
		return versionedFiles;
	}

	public Set<Path> getChangedFiles() {
		return changedFiles;
	}

	public Set<Path> getUntrackedFiles() {
		return untrackedFiles;
	}

	public Path getRoot() {
		return root;
	}

	public Path getRepoPath() {
		return repoPath;
	}

}
