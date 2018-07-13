package org.nuthatchery.analysis.java.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ContainerArtifact extends BaseArtifact implements Artifact {
	private final List<Artifact> artifacts = new ArrayList<>();
	private final ProjectContext ctx;

	public ContainerArtifact(ProjectContext ctx) {
		super(ctx.getPath());
		this.ctx = ctx;
	}

	@Override
	public Stream<Bytecode> stream() {
		return artifacts.stream().flatMap((a) -> {
			ArtifactHandle h = a.open();
			return h.stream().onClose(() -> h.close());
		});

	}
	public void addArtifact(Artifact art) {
		FilesystemExplorer.log.info("Adding artifact: " + art + " to " + ctx.getVName());
		artifacts.add(art);
	}

	public List<Artifact> getArtifacts() {
		return Collections.unmodifiableList(artifacts);
	}

	public ProjectContext getProject() {
		return ctx;
	}

	@Override
	public String toString() {
		return ctx.getVName();
	}
}
