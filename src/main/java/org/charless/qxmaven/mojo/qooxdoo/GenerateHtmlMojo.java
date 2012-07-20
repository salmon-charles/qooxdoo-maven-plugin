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
 * Generate the html files into the output directory
 * 
 * To generate the files, it filters and copy resources located into:
 *   ${resourcesDirectory}/html
 *   
 * @goal generate-html
 * @phase process-resources
 * @author charless
 *
 */
public class GenerateHtmlMojo extends AbstractResourcesMojo {
    
    
    /**
     * Check that required resources exist and return the list of them
     * @return A list of html resources to be filtered/copied
     * @throws MojoExecutionException
     */
    protected List<Resource> getResources() throws MojoExecutionException {
    	List<Resource> resources = new ArrayList<Resource>();
    	File resourcesDir = new File(this.resourcesDirectory,this.namespace);
    	// HTML
    	File configDir = new File(resourcesDir,"html");
    	if (! configDir.isDirectory()) {
    		throw new MojoExecutionException("Qooxdoo configuration directory \'"+configDir.getAbsolutePath()+"\' does not exists or is not a directory !");
    	}
    	Resource config = new Resource();
    	config.setFiltering(true);
    	config.setDirectory(configDir.getAbsolutePath());
    	resources.add(config);
    	return resources;
    }
    
}
