package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

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
 * @goal translation
 * @phase compile
 * @author charless
 * @requiresDependencyResolution compile
 */
// FIXME: Which phase for translation ? Do we need to tie it to a phase ?
public class TranslationMojo extends AbstractGeneratorMojo {
	
    public void execute()
        throws MojoExecutionException
    {
    	this.generator("translation");
    }

}
