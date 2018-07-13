package org.nuthatchery.analysis.java.explorer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;

public class PomContext {
	private final Path path;
	private final Model model;
	private Path pomPath;

	public PomContext(Path path, Path pomPath, Model model) {
		super();
		this.path = path;
		this.pomPath = pomPath;
		this.model = model;
	}

	@Override
	public String toString() {
		return "pom(" + pomPath + ")";
	}

	public Path getPath() {
		return path;
	}

	public Model getModel() {
		return model;
	}

	public String getMavenUri() {
		return String.format("maven://%s/%s/%s", getGroupId(), getArtifactId(), getVersion());
	}

	public static String getMavenUri(Dependency d) {
		return String.format("maven://%s/%s/%s", d.getGroupId(), d.getArtifactId(), d.getVersion());
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getDependencies()
	 */
	public List<Dependency> getDependencies() {
		return model.getDependencies();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getDependencyManagement()
	 */
	public DependencyManagement getDependencyManagement() {
		return model.getDependencyManagement();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getDistributionManagement()
	 */
	public DistributionManagement getDistributionManagement() {
		return model.getDistributionManagement();
	}

	/**
	 * @param key
	 * @return
	 * @see org.apache.maven.model.ModelBase#getLocation(java.lang.Object)
	 */
	public InputLocation getLocation(Object key) {
		return model.getLocation(key);
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getModules()
	 */
	public List<String> getModules() {
		return model.getModules();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getPluginRepositories()
	 */
	public List<Repository> getPluginRepositories() {
		return model.getPluginRepositories();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getProperties()
	 */
	public Properties getProperties() {
		return model.getProperties();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getReporting()
	 */
	public Reporting getReporting() {
		return model.getReporting();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getArtifactId()
	 */
	public String getArtifactId() {
		return model.getArtifactId();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getReports()
	 */
	public Object getReports() {
		return model.getReports();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getBuild()
	 */
	public Build getBuild() {
		return model.getBuild();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.ModelBase#getRepositories()
	 */
	public List<Repository> getRepositories() {
		return model.getRepositories();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getCiManagement()
	 */
	public CiManagement getCiManagement() {
		return model.getCiManagement();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getContributors()
	 */
	public List<Contributor> getContributors() {
		return model.getContributors();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getDescription()
	 */
	public String getDescription() {
		return model.getDescription();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getDevelopers()
	 */
	public List<Developer> getDevelopers() {
		return model.getDevelopers();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getGroupId()
	 */
	public String getGroupId() {
		return model.getGroupId();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getInceptionYear()
	 */
	public String getInceptionYear() {
		return model.getInceptionYear();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getIssueManagement()
	 */
	public IssueManagement getIssueManagement() {
		return model.getIssueManagement();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getLicenses()
	 */
	public List<License> getLicenses() {
		return model.getLicenses();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getMailingLists()
	 */
	public List<MailingList> getMailingLists() {
		return model.getMailingLists();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getModelEncoding()
	 */
	public String getModelEncoding() {
		return model.getModelEncoding();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getModelVersion()
	 */
	public String getModelVersion() {
		return model.getModelVersion();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getName()
	 */
	public String getName() {
		return model.getName();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getOrganization()
	 */
	public Organization getOrganization() {
		return model.getOrganization();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getPackaging()
	 */
	public String getPackaging() {
		return model.getPackaging();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getParent()
	 */
	public Parent getParent() {
		return model.getParent();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getPrerequisites()
	 */
	public Prerequisites getPrerequisites() {
		return model.getPrerequisites();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getProfiles()
	 */
	public List<Profile> getProfiles() {
		return model.getProfiles();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getScm()
	 */
	public Scm getScm() {
		return model.getScm();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getUrl()
	 */
	public String getUrl() {
		return model.getUrl();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getVersion()
	 */
	public String getVersion() {
		return model.getVersion();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getPomFile()
	 */
	public File getPomFile() {
		return model.getPomFile();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getProjectDirectory()
	 */
	public File getProjectDirectory() {
		return model.getProjectDirectory();
	}

	/**
	 * @return
	 * @see org.apache.maven.model.Model#getId()
	 */
	public String getId() {
		return model.getId();
	}

}
