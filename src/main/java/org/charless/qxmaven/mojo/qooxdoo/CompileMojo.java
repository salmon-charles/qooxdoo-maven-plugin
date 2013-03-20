package org.charless.qxmaven.mojo.qooxdoo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which builds the qooxdoo application
 * 
 * A build contains all the necessaries javascript dependencies and the application sources,
 * generally compacted into a single file
 * 
 * Qooxdoo supports two types of build:
 *  - The development one, that works directly on the application sources, useful to develop your application
 *  (no need to recompile the application on every changes) 
 *  - The production one, that create an optimized javascript file, or multiple ones by using packages
 * 
 * @goal compile
 * @phase compile
 * @author charless
 * @requiresDependencyResolution compile
 */
public class CompileMojo extends AbstractGeneratorMojo {
	
    /**
     * Name of the job used to build the application.
     *
     * @parameter expression="${qooxdoo.build.job}"
     * 			  default-value="build"
     */
    protected String buildJob;
	
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
    	this.setJobName(buildJob);
    	super.execute();
    }

}
