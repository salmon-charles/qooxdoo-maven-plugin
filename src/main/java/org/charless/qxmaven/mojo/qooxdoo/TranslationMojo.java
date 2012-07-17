package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.qooxdoo.charless.build.QxEmbeddedJython;

/**
 * Goal which builds the translation files
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
