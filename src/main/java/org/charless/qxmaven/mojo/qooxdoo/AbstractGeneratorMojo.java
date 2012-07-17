package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

/**
 * An abstract class that inherits from AbstractQooxdooMojo,
 * to use when implementing Mojo that needs to use the Qooxdoo tool chain 
 * 
 * The class offers an abstraction of the generator, using either the embedded Jython
 * or an external python interpreter 
 *  
 * @author charless
 */
public class AbstractGeneratorMojo extends AbstractQooxdooMojo {
	
	private final static String SCRIPT_NAME="generator.py";
	
    /**
     * Name of the python interpreter or full path to it
     *
     * @parameter expression="${qooxdoo.build.python}"
     * 			  default-value="python"
     */
    protected String pythonInterpreter;
    
    /**
     * WARNING: EXPERIMENTAL FEATURE !!
     * Use the embedded Jython, which does not require having an external python interpreter installed.
     * Note that the embedded Jython is still an experimental feature, 
     * and will consume probably more resources (cpu, memory) than an external python interpreter 
     * @parameter   expression="${qooxdoo.build.useEmbeddedJython}"
     * 				default-value="false"
     * @required
     */
    protected Boolean useEmbeddedJython;
	
	/**
	 * Launch a job using the Qooxdoo python generator
	 * 
	 * @param jobName Name of the job
	 * @throws MojoExecutionException
	 */
    protected void generator(String jobName) throws MojoExecutionException
    {
    	//FIXME: jobName not supported !

    	// Init 
    	File qooxdooSdkPath = getSdkDirectory();
    	File pythonScript = QxEmbeddedJython.resolvePythonScriptPath(qooxdooSdkPath,SCRIPT_NAME);
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
		if (useEmbeddedJython) {
			getLog().info("Initializing Jython...");
			QxEmbeddedJython qx = new QxEmbeddedJython(qooxdooSdkPath);
	        long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
	        getLog().info("Jython initialized in "+passedTimeInSeconds+" seconds");
	        starts = System.currentTimeMillis();
			getLog().info("Starting qooxdoo build...");
			getLog().info("Config:"+config.getAbsolutePath());
			try {
				qx.run(SCRIPT_NAME,options);
			} catch (Exception e) {
				throw new MojoExecutionException(e.getMessage(),e );
			}
	        passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
	        getLog().info("DONE in "+passedTimeInSeconds+" seconds");
		} else {
			throw new MojoExecutionException("Pytyhon is not yet supported !" );
		}
    }

    
}
