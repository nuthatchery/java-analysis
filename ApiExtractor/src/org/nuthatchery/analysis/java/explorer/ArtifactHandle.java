package org.nuthatchery.analysis.java.explorer;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface ArtifactHandle extends AutoCloseable {
	Path getPath();

	ProjectContext getInfo();

	Stream<Bytecode> stream();

	@Override
	void close();
}
