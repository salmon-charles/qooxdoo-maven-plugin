package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

/**
 * Goal which creates a build version of the qooxdoo application
 * Equivalent to 'generator.py build'  
 * 
 * @goal compile
 * @phase compile
 * @author charless
 * @requiresDependencyResolution compile
 */
public class CompileMojo extends AbstractQooxdooMojo {
	
    public void execute()
        throws MojoExecutionException
    {
    	// Init 
    	File qooxdooSdkPath = getSdkDirectory();
    	String pythonScriptName = "generator.py";
    	File pythonScript = QxEmbeddedJython.resolvePythonScriptPath(qooxdooSdkPath,pythonScriptName);
		// Check script existence
		if (! pythonScript.exists() || ! pythonScript.canRead()) {
			getLog().error(
					"The python script \'"
					+ pythonScript.getAbsolutePath()
					+"\' does not exist or is not readable !"
			);
			System.exit(1);
		}
		// Options
		File config = new File(this.getApplicationTarget(),this.config);
		
    	String[] options = new String[] {
    			"--config",config.getAbsolutePath()
    	};
    	// Start compilation
		long starts = System.currentTimeMillis();
		getLog().info("Initializing Jython...");
		QxEmbeddedJython qx = new QxEmbeddedJython(qooxdooSdkPath);
        long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
        getLog().info("Jython initialized in "+passedTimeInSeconds+" seconds");
        starts = System.currentTimeMillis();
		getLog().info("Starting qooxdoo build...");
		getLog().info("Config:"+config.getAbsolutePath());
		try {
			qx.run(pythonScriptName,options);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(),e );
		}
        passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
        getLog().info("DONE in "+passedTimeInSeconds+" seconds");
    }

}
