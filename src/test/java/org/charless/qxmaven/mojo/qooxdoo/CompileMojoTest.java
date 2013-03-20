package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.charless.qxmaven.mojo.qooxdoo.utils.TestUtils;


public class CompileMojoTest extends AbstractMojoTestCase
{
	static boolean skip_jython = true;
	static boolean skip_python = true;
	
    public void testCompileJython()
        throws Exception
    {
    	if (! skip_jython) {
    		File testPom = new File( getBasedir(), "src/test/resources/compile.pom" );
            CompileMojo mojo =  (CompileMojo)lookupMojo( "compile", testPom );
            assertNotNull( "Failed to configure the plugin", mojo );
            
            // Unpack sdk
            if (! mojo.getSdkDirectory().exists()) {
                TestUtils.removeDirectory(mojo.getSdkDirectory());
                TestUtils.unjar(new File("src/test/resources/qooxdoo-sdk-1.5.jar"),mojo.sdkParentDirectory);
            }
            // Copy test application
            FileUtils.copyDirectory(new File("src/test/resources/compile_app"), new File("target/test-target/qooxdoo/compile_app"));
            
            mojo.execute();
    	}
    }
    
    public void testCompilePython()
            throws Exception
        {
        	if (! skip_python) {
        		File testPom = new File( getBasedir(), "src/test/resources/compile-python.pom" );
                CompileMojo mojo =  (CompileMojo)lookupMojo( "compile", testPom );
                assertNotNull( "Failed to configure the plugin", mojo );
                
                // Unpack sdk
                if (! mojo.getSdkDirectory().exists()) {
                    TestUtils.removeDirectory(mojo.getSdkDirectory());
                    TestUtils.unjar(new File("src/test/resources/qooxdoo-sdk-1.5.jar"),mojo.sdkParentDirectory);
                }
                // Copy test application
                FileUtils.copyDirectory(new File("src/test/resources/compile_app"), new File("target/test-target/qooxdoo/compile_app"));
                
                mojo.execute();
        	}
            
        }
}

