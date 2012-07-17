package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.plugin.MojoExecutionException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

/**
 * An abstract class that inherits from AbstractQooxdooMojo,
 * to use when implementing Mojo that needs to use the Qooxdoo tool chain 
 * 
 * The class offers an abstraction of the generator, using either the embedded Jython
 * or an external Python interpreter 
 *  
 * @author charless
 */
public abstract class AbstractGeneratorMojo extends AbstractQooxdooMojo {
	
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
    	// Check 
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
		
	   	// Launch job
    	File config = new File(this.getApplicationTarget(),this.config);
	    if (useEmbeddedJython) {
	    	getLog().info("Using build-in Jython interpreter");
	    	String[] options = new String[] {
	    			"--config",config.getAbsolutePath(),"build"
	    	};
	    	String command = "[JYTHON]";
	    	for (String o: options) {
	    		command += " "+o;
	    	}
	    	getLog().info("Starting '"+command+"'");
	    	jythonGenerator(options, qooxdooSdkPath);  
	    } else {
	    	Map<String,Object> map = new HashMap<String,Object>();
		    map.put("config", config);
		    map.put("job", jobName);
	    	getLog().info("Using python interpreter '"+pythonInterpreter+"'");
	    	CommandLine cmdLine = new CommandLine(pythonInterpreter);
	    	cmdLine.addArgument(pythonScript.getAbsolutePath());
	    	cmdLine.addArgument("--config");
		    cmdLine.addArgument("${config}");
		    cmdLine.addArgument("${job}");
		    cmdLine.setSubstitutionMap(map);
		    getLog().info("Starting '"+cmdLine.toString()+"'");
		    pythonGenerator(cmdLine, qooxdooSdkPath); 
	    }
    }
    
	/**
	 * Launch a job using the Qooxdoo python generator, with the Jython interpreter
	 * 
	 * @param options 			Script options
	 * @param qooxdooSdkPath 	Path to the qooxdoo sdk
	 * 
	 * @throws MojoExecutionException
	 */
    private void jythonGenerator(String[] options, File qooxdooSdkPath) throws MojoExecutionException
    {
    	// Start job
		long starts = System.currentTimeMillis();
		getLog().info("Initializing Jython...");
		QxEmbeddedJython qx = new QxEmbeddedJython(qooxdooSdkPath);
        long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
        getLog().info("Jython initialized in "+passedTimeInSeconds+" seconds");
        starts = System.currentTimeMillis();
		getLog().info("Starting qooxdoo build...");
		try {
			qx.run(SCRIPT_NAME,options);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(),e );
		}
        passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
        getLog().info("DONE in "+passedTimeInSeconds+" seconds");
    }
    
	/**
	 * Launch a job using the Qooxdoo python generator, with the Jython interpreter
	 * 
	 * @param cmdLine 			Command line
	 * @param qooxdooSdkPath 	Path to the qooxdoo sdk
	 * 
	 * @throws MojoExecutionException
	 */
    private void pythonGenerator(CommandLine cmdLine, File qooxdooSdkPath) throws MojoExecutionException
    {
	    // Start job
		long starts = System.currentTimeMillis();
		getLog().info("Starting qooxdoo build...");
		try {
	    	DefaultExecutor executor = new DefaultExecutor();
	 	    executor.setExitValue(0);
	 	    ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
	 	    executor.setWatchdog(watchdog);
	    	executor.execute(cmdLine);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(),e );
		}
	    long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
	    getLog().info("DONE in "+passedTimeInSeconds+" seconds");
	     
    }
    
}
