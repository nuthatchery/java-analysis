package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;

public class JarArtifact extends BaseArtifact implements Artifact {
	private ProjectContext ctx;

	public JarArtifact(Path path) {
		super(path);
	}

	@Override
	public ProjectContext getInfo() {
		if (ctx != null)
			return ctx;
		ctx = ProjectContext.getJarProjectContext(path, null, null);
		return ctx;
	}

	protected ProjectContext getInfo(JarFile jar) {
		ctx = getInfo();
		for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry element = entries.nextElement();
			System.out.println(element.getName());
		}

		return ctx;
	}

	@Override
	public ArtifactHandle open() {
		try {
			return new ArtifactHandle() {
				JarFile jarFile = new JarFile(path.toFile());

				@Override
				public void close() {
					try {
						jarFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				public Path getPath() {
					return path;
				}

				@Override
				public ProjectContext getInfo() {
					return JarArtifact.this.getInfo(jarFile);
				}

				@Override
				public Stream<Bytecode> stream() {
					return jarFile.stream().filter(JarArtifact::isClass).map(e -> {
						try {
							return new Bytecode(JarArtifact.this, path.resolve(e.getName()),
									IOUtils.toByteArray(jarFile.getInputStream(e)));
						} catch (IOException e1) {
							throw new RuntimeException(e1);
						}
					});
				}

			};
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected static boolean isClass(JarEntry e) {
		return !e.isDirectory() && e.getName().endsWith(".class");
	}
}
