package org.charless.qxmaven.mojo.qooxdoo;


import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Goal which builds the qooxdoo testrunner
 * 
 * @goal test-compile
 * @phase test-compile
 * @author charless
 * @requiresDependencyResolution test
 */
public class TestCompileMojo extends TestrunnerMojo {
	
	
    public void execute()
        throws MojoExecutionException
    {
    	if (! "testrunner.view.Reporter".equals(this.testView)) {
    		this.testView = "testrunner.view.Reporter";
    		getLog().warn("The testrunner view has been forced to "+this.testView);
    	}
    	this.generator(testJob);
    	URL index = null ;
    	if (this.getTestrunnerIndexHtml().exists()) {
    		try {
    			index = this.getTestrunnerIndexHtml().toURI().toURL();
    		} catch(Exception e) {
    			e.printStackTrace();
    			getLog().error("Unexpected error while getting URL of testrunner index file.  Tests aborted.");
    		}
    	} else  {
    		getLog().error("File "+this.getTestrunnerIndexHtml().getAbsolutePath()+" has not been created. Tests aborted.");
    	}
    	startSelenium(index);
    }
    
    public void startSelenium(URL index) {
    	getLog().info("Starting Selenium FireFox driver on "+index.toString());
        // Create a new instance of the driver
        WebDriver driver = new FirefoxDriver();
        // And now use this to load the testrunner
        driver.get(index.toString());
        // FIXME: how to know when the tests have been loaded ?Timeout
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebElement element;
        for (int i=0;i<50;i++) {
        	element = driver.findElement(By.id("info"));
        	getLog().info(i+":"+element.getText());
        	try {
        		Thread.sleep(50);
        	} catch (Exception e) {};
        	
        }
       
        
        
        // Results
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object response = js.executeScript("return qx.core.Init.getApplication().runner.view.getFailedResults();");
        if (response != null) {
        	getLog().debug(response.toString());
        }
        
        //Close the browser
        driver.quit();
    }
    
    public File getTestrunnerIndexHtml() {
    	File basedir = new File(this.getApplicationTarget(),"test");
    	String index = (this.testJob != null ? this.testJob.replaceAll("test", "index") : "index")+".html";
    	return new File(basedir, index);
    }

}
