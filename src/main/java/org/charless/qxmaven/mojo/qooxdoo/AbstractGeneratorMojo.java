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
import org.charless.qxmaven.mojo.qooxdoo.jython.JythonShell;
import org.qooxdoo.charless.build.QxEmbeddedJython;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

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
	 * Launch a job using the Qooxdoo python generator
	 * 
	 * @param jobName Name of the job
	 * @throws MojoExecutionException
	 */
    protected void generator(String jobName) throws MojoExecutionException
    {
    	// Check 
    	File qooxdooSdkPath = getSdkDirectory();
    	File pythonScript = resolvePythonScriptPath(SCRIPT_NAME);
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
	    	getLog().info("Starting '"+jobName+"' job using build-in Jython interpreter...");
	    	String[] options = new String[this.getCommandLineOptions().length+3];
	    	options[0] = "--config";
	    	options[1] = config.getAbsolutePath();
	    	options[2] = jobName;
	    	int i = 3;
	    	for (String o: this.getCommandLineOptions()) {
	    		options[i++] = o;
	    	}
	    	String command = "[JYTHON]";
	    	for (String o: options) {
	    		command += " "+o;
	    	}
	    	getLog().debug("Command line: '"+command+"'");
	    	jythonGenerator(jobName);  
	    } else {
	    	getLog().info("Starting '"+jobName+"' job using external Python interpreter...");
	    	Map<String,Object> map = new HashMap<String,Object>();
		    map.put("config", config);
		    map.put("job", jobName);
	    	CommandLine cmdLine = new CommandLine(pythonInterpreter);
	    	cmdLine.addArgument(pythonScript.getAbsolutePath());
	    	cmdLine.addArgument("--config");
		    cmdLine.addArgument("${config}");
		    cmdLine.addArgument("${job}");
		    for (String o: this.getCommandLineOptions()) {
		    	cmdLine.addArgument(o);
	    	}
		    cmdLine.setSubstitutionMap(map);
		    getLog().debug("Command line: '"+cmdLine.toString()+"'");
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
    private void jythonGenerator(String jobName) throws MojoExecutionException
    {
    	long starts = System.currentTimeMillis();
    	getLog().info("Starting Jython, please wait...");
        JythonShell shell = getJythonShell(jobName,true);
    	shell.execFile(resolvePythonScriptPath(SCRIPT_NAME).getAbsolutePath());
    	long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
    	getLog().info("DONE in "+passedTimeInSeconds+" seconds");
    	
    }
    
	/**
	 * Launch a job using the Qooxdoo python generator, with the external python interpreter
	 * 
	 * @param cmdLine 			Command line
	 * @param qooxdooSdkPath 	Path to the qooxdoo sdk
	 * 
	 * @throws MojoExecutionException
	 */
    private void pythonGenerator(CommandLine cmdLine, File qooxdooSdkPath) throws MojoExecutionException
    {
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
     * Return optional additional command line options
     * Dont specify the "--config" options which is automatically set when running the job  
     * @return
     */
    protected String[] getCommandLineOptions() {
    	return new String[0];
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
        if (includeArtifactsDependencies) {
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
    
	public  File resolvePythonScriptPath(String pythonScriptName) {
		return new File(getSdkDirectory(),"tool"+File.separator+"bin"+File.separator+pythonScriptName);
	}
	
	public JythonShell getJythonShell( String jobName, Boolean includeArtifactsDependencies) {
		Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(getPluginContext());
		JythonShell shell = new JythonShell(properties, getJythonClasspath(true),
				new String[] {"generator.py","-c",getConfigTarget().getPath(),jobName});
    	// Bug fix: The gc.disable method throw an exception in the Jython implementation
		shell.exec("import gc");
    	shell.exec("gc.disable=gc.enable");
    	return shell;
	}
    
}
