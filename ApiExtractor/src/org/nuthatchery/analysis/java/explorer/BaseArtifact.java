package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class BaseArtifact implements Artifact, ArtifactHandle {

	protected final Path path;

	public BaseArtifact(Path path) {
		this.path = path;
	}

	public boolean isContainer() {
		return false;
	}

	@Override
	public Stream<Bytecode> stream() {
		return Stream.empty();
	}

	@Override
	public ArtifactHandle open() {
		return this;
	}

	@Override
	public void close() {

	}

	@Override
	public ProjectContext getInfo() {
		return null;
	}

	@Override
	public Path getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return path.toString();
	}


}
