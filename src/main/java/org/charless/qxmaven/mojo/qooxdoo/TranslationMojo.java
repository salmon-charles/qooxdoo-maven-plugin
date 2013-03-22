package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.charless.qxmaven.mojo.qooxdoo.app.Config;
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
    	// Make sure the translation files exists
    	try {
			Config cfg = Config.read(getConfigJson());
			@SuppressWarnings("unchecked")
			ArrayList<String> locales = (ArrayList<String>)cfg.letGet("LOCALES");
	    	for (String l : locales) {
	    		File local = new File(this.translationDirectory,l+".po");
	    		if (! local.exists()) local.createNewFile();
	    	}
    	} catch (Exception e) {}
    	
    	// Launch the job
    	this.setJobName("translation");
    	super.execute();
    }

}
