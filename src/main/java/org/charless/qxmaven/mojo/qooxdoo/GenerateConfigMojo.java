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
 * 
 * To generate the files, it filters and copy resources located into:
 *   ${resourcesDirectory}/config
 *   
 * @goal generate-config
 * @phase generate-sources
 * @author charless
 *
 */
public class GenerateConfigMojo extends AbstractResourcesMojo {
   /**
    * Some properties, referring to paths, need to be relativized before being wrote into config files. 
    * WARNING: the value of the following properties must contains path to directory (not to a file) 
    */
   private static String[] propsDirectoryToRelativize = {
	   "qooxdoo.sdk.parentDirectory",
	   "qooxdoo.application.resourcesDirectory",
	   "qooxdoo.application.sourcesDirectory",
	   "qooxdoo.application.testDirectory",
	   "qooxdoo.application.outputDirectory",
	   "qooxdoo.application.cacheDirectory",
	   "qooxdoo.application.translationDirectory"
	};
    
   /**
    * Copy and filter the resources
    * You may override these method in the parent class to fit with your needs.
    * 
    * @param mavenResourcesExecution
    * @throws MojoExecutionException
    */
   protected void filterResources(MavenResourcesExecution mavenResourcesExecution) throws MavenFilteringException {
   	this.setProperties(true);
   	this.mavenResourcesFiltering.filterResources(mavenResourcesExecution);
   	this.setProperties(false);
   }
   
    /**
     * Make sure the required project properties for filtering are defined
     * 
     * @param relativize Some path properties needs to be relativized 
     */
    protected void setProperties(Boolean relativize) {
    	super.setProperties();
    	if (relativize) {
    		File target = this.getApplicationTarget();
    		getLog().info("The following path properties will be relativized to the application target '"+target.getAbsolutePath()+"':");
    		for (String prop : propsDirectoryToRelativize) {
    			try {
        			File path = new File((String)this.project.getProperties().get(prop));
    				String relPath = ResourceUtils.getRelativePath(path.getAbsolutePath(),target.getAbsolutePath(),"/",false);
    				getLog().info("  - "+prop+": "+path.getAbsolutePath()+" => "+relPath);
    				this.project.getProperties().put(prop,relPath);
    			} catch (Exception e) {
    				getLog().error("  - "+prop+": "+"Can not relativize path '"+this.project.getProperties().get(prop)+"' :"+e.getMessage());
    			}
    		}
    	}
    }
    
    /**
     * Check that required resources exist and return the list of them
     * @return A list of qooxdoo resources to be filtered/copied
     * @throws MojoExecutionException
     */
    protected List<Resource> getResources() throws MojoExecutionException {
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
    	File unitTestManifestJson = new File(configDir,"UnitTestManifest.json");
    	if (! unitTestManifestJson.isFile()) {
    		throw new MojoExecutionException("Qooxdoo unit-test manifest file \'"+unitTestManifestJson.getAbsolutePath()+"\' does not exists or is not a file !");
    	}
    	Resource config = new Resource();
    	config.setFiltering(true);
    	config.setDirectory(configDir.getAbsolutePath());
    	qxResources.add(config);
    	return qxResources;
    }
    
}
