package org.charless.qxmaven.mojo.qooxdoo;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.charless.qxmaven.mojo.qooxdoo.jython.JythonShell;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * EXPERIMENTAL !
 * Launch a jython console, with the qooxdoo toolchain preloaded.
 * Once in the console, you can type actions like build() or jobs("clean","build")
 * to trigger qooxdoo generator jobs.
 * 
 * Usefull for developpment, to execute generator actions without having to re-build the project with 
 * an mvn command
 * 
 * @goal console
 * @execute phase="compile"
 * @requiresDirectInvocation true
 */
public class ConsoleMojo extends AbstractGeneratorMojo {
	
	@Override
    public void execute() throws MojoExecutionException {
    	JythonShell shell = getJythonShell(true);
    	shell.exec("from qonsole import qxjavainit");
    	shell.exec("qxjavainit()");
    	shell.exec("from qonsole import *");
    	shell.exec("qonsole()");
    	shell.interact();
      }
}
