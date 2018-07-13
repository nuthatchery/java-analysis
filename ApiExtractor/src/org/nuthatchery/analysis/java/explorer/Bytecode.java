package org.nuthatchery.analysis.java.explorer;

import java.nio.file.Path;

public class Bytecode {
	private final Artifact container;
	private final Path path;
	private final byte[] bytes;

	public Bytecode(Artifact container, Path path, byte[] bytes) {
		super();
		this.container = container;
		this.path = path;
		this.bytes = bytes;
	}

	/**
	 * @return the container
	 */
	public Artifact getContainer() {
		return container;
	}

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

}
