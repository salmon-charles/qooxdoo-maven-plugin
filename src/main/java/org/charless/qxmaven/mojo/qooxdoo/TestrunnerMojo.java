package org.charless.qxmaven.mojo.qooxdoo;


import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which builds the qooxdoo testrunner
 * 
 * @goal testrunner
 * @execute phase="generate-sources"
 * @author charless
 * @requiresDependencyResolution test
 */
public class TestrunnerMojo extends AbstractGeneratorMojo {
	
    /**
     * Name of the job used to test the application.
     *
     * @parameter expression="${qooxdoo.test.job}"
     * 			  default-value="test"
     */
    protected String testJob;
    
    /**
     * Name of the view used for the testrunner.
     *
     * @parameter expression="${qooxdoo.test.view}"
     * 			  default-value="testrunner.view.widget.Widget"
     */
    protected String testView;
	
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
    	this.setJobName(testJob);
    	super.execute();
    	
    	File index = this.getTestrunnerIndexHtml();
    	if (index.exists()) {
    		try {
    			getLog().info("Open the url "+index.toURI().toURL()+" with a web browser to start the testrunner");
    		} catch(Exception e) {
    			getLog().info("Open the file "+index.getAbsolutePath()+" with a web browser to start the testrunner");
    		}
    	} else  {
    		getLog().error("File "+index.getAbsolutePath()+" has not been created");
    	}
    	
    }
    
    protected String[] getCommandLineOptions() {
    	return new String[] {
    			"-m","TESTRUNNER_VIEW:"+this.testView
    	};
    }
    
    public File getTestrunnerIndexHtml() {
    	File basedir = new File(this.getApplicationTarget(),"test");
    	String index = this.testJob.replaceAll("test", "index")+".html";
    	return new File(basedir, index);
    }

}
