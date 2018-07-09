package org.nuthatchery.analysis.java.explorer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PomUtil {
	public static PomContext getPom(Path path, Path pomPath) {
		if (Files.exists(pomPath)) {
			try (InputStream stream = Files.newInputStream(pomPath)) {
				MavenXpp3Reader reader = new MavenXpp3Reader();
				org.apache.maven.model.Model mavenModel = reader.read(stream);
				return new PomContext(path, pomPath, mavenModel);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
