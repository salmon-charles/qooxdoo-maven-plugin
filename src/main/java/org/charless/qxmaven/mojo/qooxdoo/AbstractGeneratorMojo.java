package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.charless.qxmaven.mojo.qooxdoo.jython.JythonShell;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * An abstract class that inherits from AbstractPythonMojo,
 * to use when implementing Mojo that needs to use the Qooxdoo tool chain 
 * 
 * The class offers an abstraction of the generator, using either the embedded Jython
 * or an external Python interpreter 
 *  
 * @author charless
 */
public abstract class AbstractGeneratorMojo extends AbstractPythonMojo {
	
	private String jobName = "<jobname>";
	
	static {
		setSCRIPT_NAME("generator.py");
	}
	
	/**
	 * Launch a job using the Qooxdoo python generator
	 * 
	 * @throws MojoExecutionException
	 */
	@Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
	    if (useEmbeddedJython) {
	    	getLog().info("Starting '"+getJobName()+"' job using build-in Jython interpreter...");
	    	jython();  
	    } else {
	    	getLog().info("Starting '"+getJobName()+"' job using external Python interpreter...");
		    python(); 
	    }
    }
    
	/**
     * Return optional additional command line options
     * WARNING: Compared to its parent, the "--config" options is automatically set when running the job  
     * @return additional command line options
     */
	 @Override
    protected String[] getCommandLineOptions() {
    	return new String[0];
    }
    
    @Override
    protected String[] getJythonCommandLine() {
    	File config = new File(this.getApplicationTarget(),this.config);
    	String[] options = new String[this.getCommandLineOptions().length+4];
    	options[0] = getSCRIPT_NAME();
    	options[1] = "--config";
    	options[2] = config.getAbsolutePath();
    	options[3] = jobName;
    	int i = 4;
    	for (String o: this.getCommandLineOptions()) {
    		options[i++] = o;
    	}
    	return options;
    }

    @Override
    protected CommandLine getPythonCommandLine() throws MojoExecutionException {
    	Map<String,Object> map = new HashMap<String,Object>();
    	File config = new File(this.getApplicationTarget(),this.config);
	    map.put("config", config);
	    map.put("job", jobName);
    	CommandLine cmdLine = new CommandLine(pythonInterpreter);
    	cmdLine.addArgument(resolvePythonScriptPath().getAbsolutePath());
    	cmdLine.addArgument("--config");
	    cmdLine.addArgument("${config}");
	    cmdLine.addArgument("${job}");
	    for (String o: this.getCommandLineOptions()) {
	    	cmdLine.addArgument(o);
    	}
	    cmdLine.setSubstitutionMap(map);
	    return cmdLine;
    }
	
    

    public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

    
}
