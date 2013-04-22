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

/**
 * Goal which (re)builds the translation files
 * Note that goal is not attached to a specific lifecycle phase
 * Just type mvn qooxdoo:translation to update the files
 * 
 * @goal translation
 * @author charless
 */
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
