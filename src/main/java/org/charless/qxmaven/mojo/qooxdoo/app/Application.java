package org.charless.qxmaven.mojo.qooxdoo.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.charless.qxmaven.mojo.qooxdoo.json.Json;

/**
 * A class that represents a qooxdoo application
 * Offers the mavenize command to easily import a qooxdoo application into a maven project
 * @author charless
 *
 */
public class Application {
	
	/**
     * The name of the qooxdoo application configuration file
     */
    private String configFileName ;
    
    /**
     * The name of the qooxdoo application configuration file
     */
    private String manifestFileName ;
    
    /**
     * Path where the current qooxdoo application is stored
     * Example: /path/to/qooxdoo_applications/appname 
     */
    private File applicationDirectory;
    
    private Config config;
    private Manifest manifest;
    private File srcSourceDir;
    private File srcResourceDir;
    private File srcClassDir;
    private File srcTranslationDir;
    private String namespace;
    private String name;
    private final static String unitTestManifestFilename = "UnitTestManifest.json";

	/**
     * Path to the qooxdoo application source directory, containing the application classes,
     * in the maven project (${qooxdoo.application.sourcesDirectory})
     */
    protected File mvnSourcesDirectory;
    
	/**
     * Path to the qooxdoo application test directory, containing the application unit-test classes,
     * in the maven project (${qooxdoo.application.testDirectory})
     */
    protected File mvnTestDirectory;
    
	/**
     * Path to the qooxdoo application resources directory,
     * in the maven project (${qooxdoo.application.resourcesDirectory})
     */
    protected File mvnResourcesDirectory;
    
    /**
     * Path to the qooxdoo application translation directory,
     * in the maven project (${qooxdoo.application.translationDirectory})
     */
    protected File mvnTranslationDirectory;

	public Application(File applicationDir) {
		this(applicationDir, "config.json", "Manifest.json");
	}
    
	public Application(File applicationDir, String configFileName, String manifestFileName) {
		super();
		this.configFileName = configFileName;
		this.manifestFileName = manifestFileName;
		this.applicationDirectory = applicationDir;
	}



	/**
     * Import an existing qooxdoo application, ie:
     *   + appname
     *      + source
     *          + class
     *          + resource
     *          + script
     *          + translation
     *          index.html
     *      config.json
     *      Manifest.json
     *      
     * To the maven project:
     *   appname/source/class => ${qooxdoo.application.sourcesDirectory}
     *   appname/source/class/namespace/test => ${qooxdoo.application.testDirectory}/namespace
     *   appname/source/translation => ${qooxdoo.application.translationDirectory}
     *   appname/source/resource => ${qooxdoo.application.resourcesDirectory}/namespace
     *   appname/*.json => ${qooxdoo.application.resourcesDirectory}/namespace/config
     *      
     * @throws Exception
     */
    public void mavenize() throws Exception {
    	setup();
    	
    	String namespaceDir = this.manifest.providesGet("namespace").replaceAll("\\.", "/");
    	
    	// Sources
    	// Copy  appname/source/class to ${qooxdoo.application.sourcesDirectory}
    	FileUtils.copyDirectory(this.srcClassDir, this.getMvnSourcesDirectory());
    	File targetSources =  new File(this.getMvnSourcesDirectory(),namespaceDir);
    	checkFileExists("classes directory",targetSources);
    	// Test
    	// Move ${qooxdoo.application.sourcesDirectory}/namespace/test to ${qooxdoo.application.testDirectory}/namespace/test
    	File srcTest = new File(targetSources,"test");
    	File targetTest =  new File(this.getMvnTestDirectory(),namespaceDir+"/test");
    	if (srcTest.exists() && srcTest.isDirectory()) {
    		FileUtils.copyDirectory(srcTest, targetTest );
    		FileUtils.deleteDirectory(srcTest);
    	} else {
    		targetTest.mkdirs();
    	}
    	checkFileExists("test directory",targetTest);
    	// Translation
    	// Copy  appname/source/translation to ${qooxdoo.application.translationDirectory}
    	FileUtils.copyDirectory(this.srcTranslationDir, this.getMvnTranslationDirectory());
    	checkFileExists("translation directory",this.getMvnTranslationDirectory());
    	// Make sure translations files exists
    	@SuppressWarnings("unchecked")
		ArrayList<String> locales = (ArrayList<String>)this.config.letGet("LOCALES");
    	for (String l : locales) {
    		File local = new File(this.getMvnTranslationDirectory(),l+".po");
    		if (! local.exists()) local.createNewFile();
    	}
    	// Resources
    	// Copy appname/source/resource to ${qooxdoo.application.resourcesDirectory}/namespace
    	FileUtils.copyDirectory(this.srcResourceDir, this.getMvnResourcesDirectory());
    	File targetResources =  new File(this.getMvnResourcesDirectory(),namespaceDir);
    	checkFileExists("resources directory",targetResources);
    	// HTML
    	File targetResourcesHtml =  new File(targetResources,"html");
    	FileUtils.copyFileToDirectory(new File(this.srcSourceDir,"index.html"), targetResourcesHtml);
    	// Config
    	File targetConfig =  new File(targetResources,"config");
    	targetConfig.mkdirs();
    	// config.json
    	this.config.letPut("APPLICATION", "${qooxdoo.application.namespace}");
    	this.config.letPut("QOOXDOO_PATH", "${qooxdoo.sdk.parentDirectory}/qooxdoo-sdk");
    	this.config.letPut("CACHE","${qooxdoo.application.cacheDirectory}");
    	this.config.letPut("ROOT", "${qooxdoo.application.outputDirectory}/${qooxdoo.application.namespace}");
    	this.config.jobsPut("test",Json.getMapper().readValue(
				"{\"library\" : [ {\"manifest\" : \"./"+unitTestManifestFilename+"\"} ]}",Map.class));
    	this.config.jobsPut("test-source",Json.getMapper().readValue(
				"{\"library\" : [ {\"manifest\" : \"./"+unitTestManifestFilename+"\"} ]}",Map.class));
    	this.config.write( new File(targetConfig,this.getConfigFileName()));
    	// manifest.json
    	this.manifest.infoPut("name", "${qooxdoo.application.name}");
    	this.manifest.infoPut("version", "${project.version}");
    	this.manifest.infoPut("qooxdoo-versions",Json.getMapper().readValue( "[\"${qooxdoo.sdk.version}\"]",ArrayList.class));
    	this.manifest.providesPut("namespace", "${qooxdoo.application.namespace}");
    	this.manifest.providesPut("encoding", "${qooxdoo.build.sourceEncoding}");
    	this.manifest.providesPut("class", "${qooxdoo.application.sourcesDirectory}");
    	this.manifest.providesPut("resource", "${qooxdoo.application.resourcesDirectory}/${qooxdoo.application.namespace}");
    	this.manifest.providesPut("translation", "${qooxdoo.application.translationDirectory}");
    	this.manifest.write( new File(targetConfig,this.getManifestFileName()));
    	// manifest for unit tests
    	Manifest unitTestManifest = this.manifest.copy();
    	unitTestManifest.write( new File(targetConfig,unitTestManifestFilename));
    }
    
    protected void setup() throws FileNotFoundException, Exception {
    	if (this.getMvnSourcesDirectory() == null) {
    		throw new Exception("You must specify the sourcesDirectory");
    	}
    	if (this.getMvnTestDirectory() == null) {
    		throw new Exception("You must specify the testDirectory");
    	}
    	if (this.getMvnResourcesDirectory() == null) {
    		throw new Exception("You must specify the ressourcesDirectory");
    	}
    	if (this.getMvnTranslationDirectory() == null) {
    		this.setMvnTranslationDirectory(new File(this.getMvnResourcesDirectory(),"translation"));
    	}
    	this.getMvnSourcesDirectory().mkdirs();
    	this.getMvnTestDirectory().mkdirs();
    	this.getMvnResourcesDirectory().mkdirs();
    	this.getMvnTranslationDirectory().mkdirs();
    	
    	// Check application to mavenize
    	if (this.getApplicationDirectory() == null) {
    		throw new Exception("You must specify the applicationDirectory");
    	} 
    	checkFileExists("applicationDirectory",this.getApplicationDirectory());
    	this.config = Config.read(checkFileExists("qooxdoo application configuration file",new File(this.getApplicationDirectory(), this.getConfigFileName())));
		this.manifest = Manifest.read(checkFileExists("qooxdoo application manifest file",new File(this.getApplicationDirectory(), this.getManifestFileName())));
    	this.srcSourceDir = checkFileExists("qooxdoo application source directory",new File(this.getApplicationDirectory(),"source"));
		this.srcResourceDir = checkFileExists("qooxdoo application resource directory",new File(this.srcSourceDir,"resource"));
    	this.srcClassDir = checkFileExists("qooxdoo application class directory",new File(this.srcSourceDir,"class"));
    	this.srcTranslationDir = checkFileExists("qooxdoo application translation directory",new File(this.srcSourceDir,"translation"));
    	this.namespace = (String)this.manifest.providesGet("namespace");
    	this.name = (String)this.manifest.infoGet("name");
    	if (this.namespace == null) {
    		throw new Exception("The namespace is null ! Please check the "+this.getManifestFileName()+" file !");
    	}
    }
    
    private File checkFileExists(String desc, File file) throws FileNotFoundException {
    	if (desc == null) {desc = file.getName();}
    	if(! (file.exists() && file.canRead()) ) {
    		throw new FileNotFoundException("The "+desc+" '"+  file.getAbsolutePath()+"' does not exist or is not readable");
    	}
    	return file;
    }
    
    /*****************************************************
     *  ACCESSORS
     *****************************************************/
	
    public String getConfigFileName() {
		return configFileName;
	}
	

	public File getApplicationDirectory() {
		return applicationDirectory;
	}

	public String getManifestFileName() {
		return manifestFileName;
	}
	
    public File getMvnSourcesDirectory() {
		return mvnSourcesDirectory;
	}

	public void setMvnSourcesDirectory(File mvnSourcesDirectory) {
		this.mvnSourcesDirectory = mvnSourcesDirectory;
	}

	public File getMvnTestDirectory() {
		return mvnTestDirectory;
	}

	public void setMvnTestDirectory(File mvnTestDirectory) {
		this.mvnTestDirectory = mvnTestDirectory;
	}

	public File getMvnResourcesDirectory() {
		return mvnResourcesDirectory;
	}

	public void setMvnResourcesDirectory(File mvnResourcesDirectory) {
		this.mvnResourcesDirectory = mvnResourcesDirectory;
	}

	public File getMvnTranslationDirectory() {
		return mvnTranslationDirectory;
	}

	public void setMvnTranslationDirectory(File mvnTranslationDirectory) {
		this.mvnTranslationDirectory = mvnTranslationDirectory;
	}
    
    
}
