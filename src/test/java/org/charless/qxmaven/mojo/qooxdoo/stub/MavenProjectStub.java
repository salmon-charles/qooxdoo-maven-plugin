package org.charless.qxmaven.mojo.qooxdoo.stub;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.DefaultArtifact;

/**
 * 
 */
public class MavenProjectStub
    extends org.apache.maven.plugin.testing.stubs.MavenProjectStub
{
    private ArtifactStub artifact;
    {
        artifact = new ArtifactStub();
        artifact.setGroupId( "org.qooxdoo" );
        artifact.setArtifactId( "qooxdoo-sdk" );
        artifact.setVersion( "1.5" );
        artifact.setType( "jar" );
        artifact.setScope( DefaultArtifact.SCOPE_COMPILE );
        artifact.setFile( new File( "src/test/resources/qooxdoo-sdk-1.5.jar" ) );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.plugin.testing.stubs.MavenProjectStub#getArtifacts()
     */
    public Set getArtifacts()
    {
        Set artifacts = new HashSet();
        artifacts.add( artifact );
        return artifacts;
    }
}
