package org.charless.qxmaven.mojo.qooxdoo;


import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Goal which builds the qooxdoo testrunner
 * 
 * @goal test-compile
 * @phase test-compile
 * @author charless
 * @requiresDependencyResolution test
 */
public class TestCompileMojo extends TestrunnerMojo {
	
	private static final Map<String,String> SeleniumWebDrivers;
	static {
		Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("firefox","org.openqa.selenium.firefox.FirefoxDriver");
        aMap.put("ie","org.openqa.selenium.ie.InternetExplorerDriver");
        aMap.put("chrome","org.openqa.selenium.chrome.ChromeDriver");
        SeleniumWebDrivers = Collections.unmodifiableMap(aMap);
	}
	
    /**
     * Name of the directory where testrunner will be builded.
     * Strongly encouraged to use a different directory that the default "test" one,
     * as we are using a simplified view for building the testrunner.
     *
     * @parameter expression="${qooxdoo.test.unit.dir}"
     * 			  default-value="testunit"
     */
    protected String testUnitDir;
    
    /**
     * Name of the browser to use for performing unit tests
     * It must be installed on the machine.
     * Currently one of: firefox, ie or chrome
     * 
     * @parameter expression="${qooxdoo.test.unit.browser}"
     * 			  default-value="firefox"
     */
    protected String testUnitBrowser;
    
    /**
     * Path to the Internet-Explorer selenium driver to use for performing unit tests
     * 
     * @parameter expression="${webdriver.ie.driver}"
     */
    protected String testUnitIePath;
    
    /**
     * Path to the Chrome selenium driver to use for performing unit tests
     * 
     * @parameter expression="${webdriver.chrome.driver}"
     */
    protected String testUnitChromePath;
	
	
    public void execute() throws MojoExecutionException, MojoFailureException
    {
    	 if(!skipTests) {
    	    getLog().info("Executing Unit Tests");
	    	// Use a simple dedicated view for automated unit tests
	    	this.testView = "testrunner.view.Reporter";
	    	getLog().info("The testrunner view '"+this.testView+"' will be used for running unit tests");
	    	// Build the testrunner
	    	super.execute();
	    	
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
	    	
  	    } else {
  	    	getLog().warn("Skipping Unit Tests");
  	    }
    }
    
    protected String[] getCommandLineOptions() {
    	return new String[] {
    			"-m","TESTRUNNER_VIEW:"+this.testView,
    			"-m","BUILD_PATH:${ROOT}/"+this.testUnitDir
    	};
    }
    
    public void startSelenium(URL index) throws MojoExecutionException, MojoFailureException {
    	// Check driver
    	String webDriverClass = SeleniumWebDrivers.get(testUnitBrowser.toLowerCase());
    	if (webDriverClass == null) {
    		String msg = "The specified browser '"+testUnitBrowser+"' is not supported: use one of";
    		for (String k : SeleniumWebDrivers.keySet()) {
    			msg += " '"+k+"'";
    		}
    		getLog().error(msg);
    	}
    	if (this.testUnitIePath !=null) {System.setProperty("webdriver.ie.driver", this.testUnitIePath);}
    	if (this.testUnitChromePath!=null) {System.setProperty("webdriver.chrome.driver", this.testUnitChromePath);}
    	
    	getLog().info("Starting Selenium driver '"+testUnitBrowser+"' on "+index.toString());
        // Create a new instance of the driver
    	WebDriver driver;
    	try  {
            driver = (WebDriver) Class.forName(webDriverClass).newInstance();
    	} catch (Exception e) {
    		getLog().error("Can not create selenium driver instance !");
    		throw new MojoExecutionException(e.getMessage());
    	}
        // And now use this to load the testrunner
        driver.get(index.toString());
        // Wait for tests being executed
        JavascriptExecutor js = (JavascriptExecutor) driver;
        boolean stop = false;
        int i = 0; 
        HashMap<String,JSONObject> tests = new HashMap<String,JSONObject>();
        String status = ""; // init,loading, ready, running, finished, aborted, error
        int nbTestsPending = -1;
        while(! stop) {
        	Object response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestSuiteState();");
            if (response != null) {
            	String oldStatus = status;
            	status = response.toString().toLowerCase();
            	if (! oldStatus.equals(status)) {
            		if ("running".equals(status)) {
            			getLog().info("Running tests...");
                	} 
            	}
            	if ("running".equals(status)) {
            		response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestCount();");
                	if (response != null) {
                		try {
                			if (Integer.parseInt(response.toString()) > nbTestsPending ) {
                				nbTestsPending = Integer.parseInt(response.toString());
                				if (nbTestsPending > 0) {
                					getLog().info(nbTestsPending+" tests pending.");
                				}
                			}
                		} catch (Exception e) {
                			getLog().warn(e);
                		}	
                	}
            	}
            	if("finished".equals(status) ||
            			"aborted".equals(status) ||
            			"error".equals(status)) 
            	{
            		stop = true;
            	}
            } else {
            	i++;
            }
            try {
            	Thread.sleep(250);
            } catch (Exception e) {}
            if (i > 10) { stop = true;}  // 10*250 = 2500ms timeout
        }
 
        // Get Report
        Object response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestResults();");
    	String report = null;
        if (response != null) {
    		try {
    			report = response.toString();
    		} catch (Exception e) {
    			getLog().warn(e);
    		}	
    	}
    	
        //Close the browser
        driver.quit();
        
        // Final reporting
        // TODO: parse report as json data, do a real reporting
        if (report==null) {
        	getLog().error("Could not get report !");
        	throw new MojoExecutionException("Could not get report !");
        } else {
        	if (report.matches(".*state=failure, messages.*")) {
        		getLog().error("Unit tests failure !");
        		throw new MojoFailureException("Unit tests failure !");
        	}
        }
       
        getLog().info("ALL TESTS SUCCESSFULL !");
        
    }
    
    public File getTestrunnerIndexHtml() {
    	File basedir = new File(this.getApplicationTarget(),this.testUnitDir);
    	String index = (this.testJob != null ? this.testJob.replaceAll("test", "index") : "index")+".html";
    	return new File(basedir, index);
    }

}
