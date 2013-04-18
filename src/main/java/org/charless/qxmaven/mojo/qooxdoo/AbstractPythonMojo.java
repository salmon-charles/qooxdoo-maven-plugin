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
 * An abstract class that inherits from AbstractQooxdooMojo,
 * to use when implementing Mojo that needs to run a Qooxdoo python script
 *  
 * @author charless
 */
public abstract class AbstractPythonMojo extends AbstractQooxdooMojo {
	
	private static String SCRIPT_NAME="script.py";

	/**
     * Used to look up Artifacts in the remote repository.
     * 
     * @parameter expression=
     *  "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;
    
    /**
     * List of Remote Repositories used by the resolver
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List remoteRepositories;

    /**
     * Location of the local repository.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;
    
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
     * Where the compiled code is
     *
     * @parameter expression="${qooxdoo.jython.compile.directory}" default-value="${project.build.directory}/classes"
     * @optional
     */
    protected File jythonCompiledDirectory;

    /**
     * Where the test compiled code is
     *
     * @parameter expression="${qooxdoo.jython.test.compile.directory}" default-value="${project.build.directory}/test-classes"
     * @optional
     */
    protected File jythonTestCompiledDirectory;
    
    /**
     * Run the script
     * 
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
	   	// Launch job
	    if (useEmbeddedJython) {
	    	getLog().info("Starting '"+getSCRIPT_NAME()+"' using build-in Jython interpreter...");
	    	jython();  
	    } else {
	    	getLog().info("Starting '"+getSCRIPT_NAME()+"' using external Python interpreter...");
		    python(); 
	    }
    }
    
    /**
	 * Launch a script with the Jython interpreter
	 * 
	 * @throws MojoExecutionException
	 */
    protected void jython() throws MojoExecutionException
    {
    	String command = "[JYTHON]";
    	for (String o: getJythonCommandLine()) {command += " "+o;}
    	getLog().debug("Command line: '"+command+"'");
    	long starts = System.currentTimeMillis();
    	getLog().info("Starting Jython, please wait...");
        JythonShell shell = getJythonShell(true);
    	shell.execFile(resolvePythonScriptPath().getAbsolutePath());
    	long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
    	getLog().info("DONE in "+passedTimeInSeconds+" seconds");
    }
    
    /**
	 * Launch a script with the external python interpreter
	 * 
	 * @throws MojoExecutionException
	 */
    protected void python() throws MojoExecutionException
    {
    	CommandLine cmdLine = getPythonCommandLine();
    	getLog().debug("Command line: '"+cmdLine.toString()+"'");
		long starts = System.currentTimeMillis();
		try {
	    	DefaultExecutor executor = new DefaultExecutor();
	 	    executor.setExitValue(0);
	    	executor.execute(cmdLine);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(),e );
		}
	    long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
	    getLog().info("DONE in "+passedTimeInSeconds+" seconds");
	     
    }
    
    /** 
     * Resolve the full path to the python script
     * @param pythonScriptName
     * @return The resolved file 
     */
    protected File resolvePythonScriptPath() throws MojoExecutionException {
    	File  pythonScript = new File(getSdkDirectory(),"tool"+File.separator+"bin"+File.separator+getSCRIPT_NAME());
    	// Check script existence
		if (! pythonScript.exists() || ! pythonScript.canRead()) {
			getLog().warn(
					"The python script \'"
					+ pythonScript.getAbsolutePath()
					+"\' does not exist or is not readable !"
			);
     	   throw new MojoExecutionException( "Could not find python script" );
		}
		return pythonScript;
	}
	
    /**
     * Build the paths to the jython dependencies: compile and test-compile directories, qooxdoo toolchain
     * and optionally all the paths to the project dependencies 
     * @param includeArtifactsDependencies Add the project dependencies to the python classpath
     * @return A set of dependencies
     */
    protected Set<String> getJythonClasspath(Boolean includeArtifactsDependencies) {
        Set<Artifact> artifacts = project.getDependencyArtifacts();
        Set<Artifact> transitiveArtifacts = Sets.newHashSet();

        Set<String> paths = Sets.newHashSet();
        paths.add(jythonCompiledDirectory.getAbsolutePath());
        paths.add(jythonTestCompiledDirectory.getAbsolutePath());
        if (includeArtifactsDependencies && artifacts != null) {
	        for(Artifact artifact : artifacts) {
	          try {
	            artifactResolver.resolve(artifact, this.remoteRepositories, this.localRepository);
	            paths.add(artifact.getFile().getAbsolutePath());
	            ArtifactResolutionResult results = artifactResolver.resolveTransitively(artifacts, artifact, localRepository, remoteRepositories, null, null);
	            transitiveArtifacts.addAll(results.getArtifacts());
	          } catch (Exception e) {
	            Throwables.propagate(e);
	          }
	        }
	        for(Artifact artifact : transitiveArtifacts) {
	        	paths.add(artifact.getFile().getAbsolutePath());
	        }
        }
        
        paths.add(new File(getSdkDirectory(),"tool"+File.separator+"pylib").getPath());
        paths.add(new File(getSdkDirectory(),"tool"+File.separator+"bin").getPath());
        
        return paths;
      }
    
    /**
     * To overide
     * @return
     */
    protected String[] getCommandLineOptions() {
    	return new String[0];
    }
    
    /**
     * Return the full command line to use with jython
     * @return The command line
     */
    protected String[] getJythonCommandLine() {
        String[] options = new String[this.getCommandLineOptions().length+1];
    	options[0] = getSCRIPT_NAME();
    	int i = 1;
    	for (String o: this.getCommandLineOptions()) {
    		options[i++] = o;
    	}
    	return options;
    }
    
    /**
     * Return the command line to use with python
     * @return The command line
     */
    protected CommandLine getPythonCommandLine() throws MojoExecutionException {
        Map<String,Object> map = new HashMap<String,Object>();
    	CommandLine cmdLine = new CommandLine(pythonInterpreter);
    	cmdLine.addArgument(resolvePythonScriptPath().getAbsolutePath());
    	for (String o: this.getCommandLineOptions()) {
    		cmdLine.addArgument(o);
    	}
    	
    	/* USE CODE BELOW FOR USING SUBSTITUTION MAP    	
		map.put("config", config);
    	cmdLine.addArgument("--config");
	    cmdLine.addArgument("${config}");
    	cmdLine.setSubstitutionMap(map);*/
    	return cmdLine;
    }
    
    
    /**
     * Create a new Jython shell
     * @param jobName
     * @param includeArtifactsDependencies
     * @return
     */
    protected JythonShell getJythonShell( Boolean includeArtifactsDependencies) {
		Properties properties = new Properties();
        properties.putAll(System.getProperties());
        if (getPluginContext() != null) {
        	properties.putAll(getPluginContext());
        }
        JythonShell shell = new JythonShell(properties, getJythonClasspath(true), getJythonCommandLine());
    	return shell;
	}
    
    public static String getSCRIPT_NAME() {
		return SCRIPT_NAME;
	}

	public static void setSCRIPT_NAME(String sCRIPT_NAME) {
		SCRIPT_NAME = sCRIPT_NAME;
	}
    
}
