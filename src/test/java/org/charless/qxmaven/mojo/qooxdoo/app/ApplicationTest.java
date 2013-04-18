package org.charless.qxmaven.mojo.qooxdoo.app;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.charless.qxmaven.mojo.qooxdoo.utils.TestUtils;

public class ApplicationTest extends AbstractMojoTestCase {
	
	static final String appname="as7forge";
	
	public void testMavenize() throws Exception {
		 File appdir = new File( getBasedir(), "src/test/resources/qxapp/"+appname );
		Application app = new Application(appdir);
		File target = new File( getBasedir(), "target/unit/mavenize/"+appname );
		TestUtils.removeDirectory(target);
		
		app.setMvnSourcesDirectory(new File( target,"qooxdoo_application_sourcesDirectory"));
		app.setMvnResourcesDirectory(new File( target,"qooxdoo_application_resourcesDirectory"));
		app.setMvnTestDirectory(new File( target,"qooxdoo_application_testDirectory"));
		app.setMvnTranslationDirectory(new File( target,"qooxdoo_application_resourcesDirectory/translation"));
		app.mavenize();
		
		// Sources
		File appSources = new File(app.getMvnSourcesDirectory(),appname);
		assertTrue(new File(appSources,"Application.js").exists());
		File appSourcesTheme = new File(appSources,"theme");
		assertTrue(appSourcesTheme.exists());
		assertTrue(new File(appSourcesTheme,"Appearance.js").exists());
		assertTrue(new File(appSourcesTheme,"Color.js").exists());
		assertTrue(new File(appSourcesTheme,"Decoration.js").exists());
		assertTrue(new File(appSourcesTheme,"Font.js").exists());
		assertTrue(new File(appSourcesTheme,"Theme.js").exists());
		assertFalse(new File(appSources,"test").exists());
		// Tests
		File appTests = new File(app.getMvnTestDirectory(),appname+"/test");
		assertTrue(appTests.exists()&&appTests.isDirectory());
		assertTrue(new File(appTests,"DemoTest.js").exists());
		// Resources
		File appResources = new File(app.getMvnResourcesDirectory(),appname);
		assertTrue(appResources.exists()&&appResources.isDirectory());
		File appResourcesCfg = new File(appResources,"config");
		assertTrue(appResourcesCfg.exists()&&appResourcesCfg.isDirectory());
		assertTrue(new File(appResourcesCfg,"config.json").exists());
		assertTrue(new File(appResourcesCfg,"Manifest.json").exists());
		assertTrue(new File(appResourcesCfg,"UnitTestManifest.json").exists());
		// Translation
		File appTranslation = app.getMvnTranslationDirectory();
		assertTrue(appTranslation.exists()&&appTranslation.isDirectory());
		assertTrue(new File(appTranslation,"readme.txt").exists());
		assertTrue(new File(appTranslation,"en.po").exists());
		assertTrue(new File(appTranslation,"fr.po").exists());
	}
}
