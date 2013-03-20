package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

/**
 * Goal which (re)builds the translation files
 * 
 * @goal translation
 * @phase generate-resources
 * @author charless
 */
// FIXME: Which phase for translation ? Do we need to tie it to a phase ?
public class TranslationMojo extends AbstractGeneratorMojo {
	
    public void execute() throws MojoExecutionException, MojoFailureException
    {
    	this.setJobName("translation");
    	super.execute();
    }

}
