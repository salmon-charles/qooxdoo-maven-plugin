package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
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
 * Generate the qooxdoo configuration files (config.json and Manifest.json) into the output directory
 * Generate the html pages into the output directory
 * To generate the files, it filters and copy resources located into:
 *   ${resourcesDirectory}/config
 *   ${resourcesDirectory}/html
 *   
 * @goal resources
 * @phase generate-resources
 * @author charless
 *
 */
public class ResourcesMojo extends AbstractQooxdooMojo {
	/**
     * Path to the qooxdoo application source directory, containing the application classes
     *
     * @parameter 	expression="${qooxdoo.application.sourcesDirectory}"
     * 				default-value="${project.basedir}/src/main/qooxdoo"
     * @required
     */
    protected File sourcesDirectory;
    
	/**
     * Path to the qooxdoo application resources directory
     *
     * @parameter 	expression="${qooxdoo.application.resourcesDirectory}"
     * 				default-value="${project.basedir}/src/main/resources/qooxdoo"
     * @required
     */
    protected File resourcesDirectory;
    
    /**
     * Path to the output cache directory where the cache informations will be stored
     * @parameter   expression="${qooxdoo.application.cacheDirectory}"
     * 				default-value="${project.build.directory}/qooxdoo/cache"
     * @required
     */
    protected File cacheDirectory;
    
    /**
     * Path to the directory containing translation files
     * @parameter   expression="${qooxdoo.application.cacheDirectory}"
     * 				default-value="${project.basedir}/src/main/resources/qooxdoo/translation"
     * @required
     */
    protected File translationDirectory;
    
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
   
   /**
    * Some properties, referring to paths, needs to be relativize before being wrote to config files. 
    * WARNING: the value of the following properties must contains path to directory (not to a file) 
    */
   private static String[] propsDirectoryToRelativize = {
	   "qooxdoo.sdk.parentDirectory",
	   "qooxdoo.application.resourcesDirectory",
	   "qooxdoo.application.sourcesDirectory",
	   "qooxdoo.application.outputDirectory",
	   "qooxdoo.application.cacheDirectory",
	   "qooxdoo.application.translationDirectory"
	};
    
    public void execute() throws MojoExecutionException {
		try {

			if (StringUtils.isEmpty(this.encoding)) {
				getLog().warn(
						"File encoding has not been set, using platform encoding "
								+ ReaderFactory.FILE_ENCODING
								+ ", i.e. build is platform dependent!");
			}
			
			final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
					getQxResources(), this.getApplicationTarget(), this.project,
					this.encoding, null, Collections.EMPTY_LIST,
					this.session);

			mavenResourcesExecution.setEscapeWindowsPaths(this.escapeWindowsPaths);
			mavenResourcesExecution.setInjectProjectBuildFilters(false);
			mavenResourcesExecution.setEscapeString(this.escapeString);
			mavenResourcesExecution.setOverwrite(true);
			mavenResourcesExecution.setIncludeEmptyDirs(false);
			mavenResourcesExecution.setSupportMultiLineFiltering(false);
			
			this.setProperties(true);
			this.mavenResourcesFiltering.filterResources(mavenResourcesExecution);
			this.setProperties(false);

			//executeUserFilterComponents(mavenResourcesExecution);
		} catch (final MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
    
    /**
     * Make sure the required project properties for filtering are defined
     * 
     * @param relativize Some path properties needs to be relativized 
     */
    protected void setProperties(Boolean relativize) {
    	this.project.getProperties().put("qooxdoo.application.namespace",this.namespace);
    	this.project.getProperties().put("qooxdoo.application.config",this.config);
    	this.project.getProperties().put("qooxdoo.application.resourcesDirectory",this.resourcesDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.sourcesDirectory",this.sourcesDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.outputDirectory",this.outputDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.cacheDirectory",this.cacheDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.application.translationDirectory",this.translationDirectory.getAbsolutePath());
    	this.project.getProperties().put("qooxdoo.sdk.version",this.sdkVersion);
    	this.project.getProperties().put("qooxdoo.build.sourceEncoding",this.encoding);
        this.project.getProperties().put("qooxdoo.sdk.parentDirectory",this.sdkParentDirectory.getAbsolutePath());
    	if (relativize) {
    		File target = this.getApplicationTarget();
    		getLog().info("The following path properties will be relativized to the application target '"+target.getAbsolutePath()+"':");
    		for (String prop : propsDirectoryToRelativize) {
    			File path = new File((String)this.project.getProperties().get(prop));
    			try {
    				String relPath = ResourceUtils.getRelativePath(path.getAbsolutePath(),target.getAbsolutePath(),"/",false);
    				getLog().info("+ "+prop+": "+path.getAbsolutePath()+" => "+relPath);
    				this.project.getProperties().put(prop,relPath);
    			} catch (Exception e) {
    				getLog().error("+ "+prop+": "+"Can not relativize path '"+path+"' :"+e.getMessage());
    			}
    		}
    	}
    }
    
    /**
     * Check that required resources exist and return the list of them
     * @return A list of qooxdoo resources to be filtered/copied
     * @throws MojoExecutionException
     */
    protected List<Resource> getQxResources() throws MojoExecutionException {
    	List<Resource> qxResources = new ArrayList<Resource>();
    	File resourcesDir = new File(this.resourcesDirectory,this.namespace);
    	// Config
    	File configDir = new File(resourcesDir,"config");
    	if (! configDir.isDirectory()) {
    		throw new MojoExecutionException("Qooxdoo configuration directory \'"+configDir.getAbsolutePath()+"\' does not exists or is not a directory !");
    	}
    	File manifestJson = new File(configDir,"Manifest.json");
    	if (! manifestJson.isFile()) {
    		throw new MojoExecutionException("Qooxdoo manifest file \'"+manifestJson.getAbsolutePath()+"\' does not exists or is not a file !");
    	}
    	File configJson = new File(configDir,this.config);
    	if (! configJson.isFile()) {
    		throw new MojoExecutionException("Qooxdoo configuration file \'"+configJson.getAbsolutePath()+"\' does not exists or is not a file !");
    	}
    	Resource config = new Resource();
    	config.setFiltering(true);
    	config.setDirectory(configDir.getAbsolutePath());
    	qxResources.add(config);
    	// FIXME: Add html dir for dev build
    	return qxResources;
    }
    
}
