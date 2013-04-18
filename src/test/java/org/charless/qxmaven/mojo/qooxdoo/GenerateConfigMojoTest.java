package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.charless.qxmaven.mojo.qooxdoo.app.Config;
import org.charless.qxmaven.mojo.qooxdoo.app.Manifest;

public class GenerateConfigMojoTest extends AbstractMojoTestCase
{
    public void testConfig()
        throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/generate-config.pom" );
        GenerateConfigMojo mojo = (GenerateConfigMojo) lookupMojo( "generate-config", testPom );
        assertNotNull( "Failed to configure the plugin", mojo );

        
        mojo.execute();
        
        // Check if resources have been copied
        assertTrue(mojo.getApplicationTarget().exists());
        assertTrue(mojo.getConfigTarget().exists());
        assertTrue(new File(mojo.getApplicationTarget(),"Manifest.json").exists());
        // Test the filtering for config.json
        Config cfg = Config.read(mojo.getConfigTarget());
        assertEquals("resources_app",cfg.letGet("APPLICATION"));
        assertEquals("..",cfg.letGet("ROOT"));
        assertEquals("../cache",cfg.letGet("CACHE"));
        assertEquals("../../qooxdoo-sdk",cfg.letGet("QOOXDOO_PATH"));
        // Test the filtering for Manifest.json
        Manifest m = Manifest.read(new File(mojo.getApplicationTarget(),"Manifest.json"));
        assertEquals("resources_app",m.providesGet("namespace"));
        assertEquals("UTF-8",m.providesGet("encoding"));
        assertEquals("../../../../src/main/qooxdoo",m.providesGet("class"));
        assertEquals("../../../../src/test/resources",m.providesGet("resource"));
        assertEquals("../../../../src/test/resources/translation",m.providesGet("translation"));
        //assertEquals("[1.5]",m.infoGet("qooxdoo-versions").toString());
    }
}

