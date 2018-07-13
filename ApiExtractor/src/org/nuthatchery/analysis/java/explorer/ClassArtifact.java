package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ClassArtifact extends BaseArtifact implements Artifact {

	public ClassArtifact(Path path) {
		super(path);
	}

	@Override
	public Stream<Bytecode> stream() {
		try {
			return Stream.of(new Bytecode(null, path, Files.readAllBytes(path)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
