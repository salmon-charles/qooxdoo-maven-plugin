package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.charless.qxmaven.mojo.qooxdoo.utils.TestUtils;


public class SdkUnpackMojoTest extends AbstractMojoTestCase
{
	
    public void testUnpack()
        throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/sdk-unpack.pom" );
        SdkUnpackMojo mojo =  (SdkUnpackMojo)lookupMojo( "sdk-unpack", testPom );
        assertNotNull( "Failed to configure the plugin", mojo );
        
        File sdkDirectory = mojo.getSdkDirectory();
        TestUtils.removeDirectory(sdkDirectory);
        mojo.execute();
        
        // Check that qooxdoo-sdk has been created
        assertTrue(sdkDirectory.exists());
        // Check the qooxdoo-sdk content
        assertTrue(new File(sdkDirectory,"version.txt").exists());
        
    }
}

