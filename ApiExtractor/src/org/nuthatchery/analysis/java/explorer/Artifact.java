package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.nio.file.Path;

public interface Artifact {
	Path getPath();

	ProjectContext getInfo();

	ArtifactHandle open();
}

