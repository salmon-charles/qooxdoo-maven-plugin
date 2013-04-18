package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.charless.qxmaven.mojo.qooxdoo.utils.ResourceUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

/**
 * An abstract class that inherits from AbstractQooxdooMojo,
 * to use when implementing Mojo that needs to handle resources (copy/filtering) 
 *  
 * @author charless
 */
public abstract class AbstractResourcesMojo extends AbstractQooxdooMojo {	
    
    /**
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;
    
    /**
     * Whether to escape backslashes and colons in windows-style paths.
     *
     * @parameter expression="${qooxdoo.resources.escapeWindowsPaths}"
     *            default-value="true"
     */
    protected boolean escapeWindowsPaths;
    
    /**
     * Expression preceded with the String won't be interpolated \${foo} will be
     * replaced with ${foo}
     *
     * @parameter expression="${qooxdoo.resources.escapeString}"
     */
    protected String escapeString;
    
    /**
    *
    * @component
    *            role="org.apache.maven.shared.filtering.MavenResourcesFiltering"
    *            role-hint="default"
    * @required
    */
   protected MavenResourcesFiltering mavenResourcesFiltering;
    
    public void execute() throws MojoExecutionException {
		try {
			if (StringUtils.isEmpty(this.encoding)) {
				getLog().warn(
						"File encoding has not been set, using platform encoding "
								+ ReaderFactory.FILE_ENCODING
								+ ", i.e. build is platform dependent!");
			}
			
			final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
					this.getResources(), this.getApplicationTarget(), this.project,
					this.encoding, null, Collections.EMPTY_LIST,
					this.session);

			mavenResourcesExecution.setEscapeWindowsPaths(this.escapeWindowsPaths);
			mavenResourcesExecution.setInjectProjectBuildFilters(false);
			mavenResourcesExecution.setEscapeString(this.escapeString);
			mavenResourcesExecution.setOverwrite(true);
			mavenResourcesExecution.setIncludeEmptyDirs(false);
			mavenResourcesExecution.setSupportMultiLineFiltering(false);
			
			this.filterResources(mavenResourcesExecution);

		} catch (final MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
    
    /**
     * Copy and filter the resources
     * You may override these method in the parent class to fit with your needs.
     * 
     * @param mavenResourcesExecution
     * @throws MojoExecutionException
     */
    protected void filterResources(MavenResourcesExecution mavenResourcesExecution) throws MavenFilteringException {
    	this.setProperties();
    	this.mavenResourcesFiltering.filterResources(mavenResourcesExecution);
    }
    
    /**
     * Check that required resources exist and return the list of them
     * You need to override these method in the parent class to fit with your needs
     * @return A list resources to be filtered/copied
     * @throws MojoExecutionException
     */
    protected List<Resource> getResources() throws MojoExecutionException {
    	return new ArrayList<Resource>();
    }
    
    /**
     * Make sure the required project properties for filtering are defined
     * 
     * @param relativize Some path properties needs to be relativized 
     */
    protected void setProperties() {
    	this.project.getProperties().put("qooxdoo.application.namespace",this.namespace);
    	this.project.getProperties().put("qooxdoo.application.config",this.config);
    	this.project.getProperties().put("qooxdoo.application.resourcesDirectory",this.resourcesDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.sourcesDirectory",this.sourcesDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.testDirectory",this.testDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.outputDirectory",this.outputDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.cacheDirectory",this.cacheDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.translationDirectory",this.translationDirectory.getAbsolutePath());
    	if (this.getSdkVersion() != null) {this.project.getProperties().put("qooxdoo.sdk.version",this.getSdkVersion());};
    	this.project.getProperties().put("qooxdoo.build.sourceEncoding",this.encoding);
        this.project.getProperties().put("qooxdoo.sdk.parentDirectory",this.sdkParentDirectory.getAbsolutePath());
    }
    
}
