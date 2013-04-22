package org.charless.qxmaven.mojo.qooxdoo;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.charless.qxmaven.mojo.qooxdoo.archive.JavascriptArtifactManager;
import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Based on the AbstractJavascriptMojo of the Javascript Maven Plugin
 * Initial authors: 
 *  - <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 *  - <a href="mailto:h.iverson@gmail.com">Harlan Iverson</a>
 * @author charless
 * 
 */
public abstract class AbstractQooxdooMojo
    extends AbstractMojo
{
	final static protected String QOOXDOO_SDK_DIRECTORY = "qooxdoo-sdk";
	
	/**
     * Path to the qooxdoo application source directory, containing the application classes
     *
     * @parameter 	expression="${qooxdoo.application.sourcesDirectory}"
     * 				default-value="${project.basedir}/src/main/qooxdoo"
     * @required
     */
    protected File sourcesDirectory;
    
	/**
     * Path to the qooxdoo application test directory, containing the application unit-test classes
     *
     * @parameter 	expression="${qooxdoo.application.testDirectory}"
     * 				default-value="${project.basedir}/src/test/qooxdoo"
     * @required
     */
    protected File testDirectory;
    
	/**
     * Path to the qooxdoo application resources directory
     *
     * @parameter 	expression="${qooxdoo.application.resourcesDirectory}"
     * 				default-value="${project.basedir}/src/main/resources/qooxdoo"
     * @required
     */
    protected File resourcesDirectory;
    
    /**
     * Path to the output cache directory where the cache informations will be stored
     * @parameter   expression="${qooxdoo.application.cacheDirectory}"
     * 				default-value="${project.build.directory}/qooxdoo/cache"
     * @required
     */
    protected File cacheDirectory;
    
    /**
     * Path to the directory containing translation files
     * @parameter   expression="${qooxdoo.application.translationDirectory}"
     * 				default-value="${project.basedir}/src/main/resources/qooxdoo/translation"
     * @required
     */
    protected File translationDirectory;
    
	/**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

	/**
     * The namespace of the qooxdoo application
     *
     * @parameter 	expression="${qooxdoo.application.namespace}"
     * 				default-value="${project.artifactId}"
     * @required
     */
    protected String namespace;
    
    /**
     * Path to the qooxdoo sdk parent directory
     * The parent directory must contains a sub-directory named qooxdoo-sdk, that contains the unpacked qooxdoo sdk.
     * The qooxdoo-sdk is automatically installed in the right place if you are using the qooxdoo-sdk maven dependency in your pom.
     * @parameter   expression="${qooxdoo.sdk.parentDirectory}"
     * 				default-value="${project.build.directory}"
     * @required
     */
    protected File sdkParentDirectory;
    
    /**
     * Path to the output directory where application will be builded
     * @parameter   expression="${qooxdoo.application.outputDirectory}"
     * 				default-value="${project.build.directory}/qooxdoo"
     * @required
     */
    protected File outputDirectory;
    
    /**
     * The character encoding scheme to be applied when filtering resources.
     *
     * @parameter   expression="${project.build.sourceEncoding}"
     * 				default-value="UTF-8"
     */
    protected String encoding;
    
	/**
     * The name of the qooxdoo application configuration file
     *
     * @parameter 	expression="${qooxdoo.application.config}"
     * 				default-value="config.json"
     * @required
     */
    protected String config;
    
    /**
     * Map of plugin artifacts.
     * 
     * @parameter expression="${plugin.artifactMap}"
     * @required
     * @readonly
     */
    protected Map pluginArtifactMap;
    
    /**
     * Path to the sdk directory
     * @readonly
     */
    private File sdkDirectory;
    
    /**
     * Path to the qooxdoo application target
     * @readonly
     */
    private File applicationTarget;
    
    /**
     * Path to the qooxdoo application configuration file target
     * @readonly
     */
    private File configTarget;
    
    /**
     * Name of the job used to build the application.
     *
     * @parameter expression="${qooxdoo.build.job}"
     * 			  default-value="build"
     */
    protected String buildJob;
    
    /**
     * Get the path to the sdk directory, containing the qooxdoo sdk
     * @return Path to the (unpacked) qooxdoo sdk
     */
    public File getSdkDirectory() {
    	if (this.sdkDirectory == null) {
    		this.sdkDirectory = new File(this.sdkParentDirectory,QOOXDOO_SDK_DIRECTORY);
    	}
    	return this.sdkDirectory;
    }
    
    /**
     * Get the path to the application target
     * @return Path to the application target
     */
    public File getApplicationTarget() {
    	if (this.applicationTarget == null) {
    		this.applicationTarget = new File(this.outputDirectory,this.namespace);
    	}
    	return this.applicationTarget;
    }
    
    /**
     * @parameter 	expression="${skipTests}"
     * 				default-value="${maven.test.skip}"
     */
    protected boolean skipTests;
    
    /**
     * Get the path to the configuration file of the application in the target directory
     * @return Path to the target configuration file
     */
    public File getConfigTarget() {
    	if (this.configTarget == null) {
    		this.configTarget = new File(this.getApplicationTarget(),this.config);
    	}
    	return this.configTarget;
    }
    
    public File getConfigDirectory() {
    	File resourcesDir = new File(this.resourcesDirectory,this.namespace);
    	return new File(resourcesDir,"config");
    }
    
    public File getConfigJson() {
    	return new File(getConfigDirectory(),this.config);
    }

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
    
	public String getSdkVersion() {
		Artifact qooxdooSdk = this.getQooxdooSdkArtifact();
		if (qooxdooSdk == null) { return null; }
		return qooxdooSdk.getVersion();
	}
	
    /**
     * Get the qooxdoo-sdk dependency
     */
    @SuppressWarnings("rawtypes")
    public Artifact getQooxdooSdkArtifact() {
        Set dependencies = project.getArtifacts();

		if( dependencies.size() == 0 ) { return null;		}

        ArtifactFilter runtime = new ScopeArtifactFilter( Artifact.SCOPE_COMPILE );
        for ( Iterator iterator = dependencies.iterator(); iterator.hasNext(); )
        {
            Artifact dependency = (Artifact) iterator.next();
            if ( 	!dependency.isOptional() 
            		&& "jar".equals( dependency.getType() )
            		&& "org.qooxdoo".equals(dependency.getGroupId())
            		&& "qooxdoo-sdk".equals(dependency.getArtifactId())
            		&& runtime.include( dependency ) )
            {
                return dependency;
            }
        }
    	return null;
    }

//    /**
//     * @component 
//     */
//    protected JavascriptArtifactManager javascriptArtifactManager;
//
//	protected void unpackJavascriptDependency( String artifact, File dest ) throws MojoExecutionException {
//		unpackJavascriptDependency(artifact, dest, false);
//	}
//
//    /**
//     * Unpack a javascript dependency
//     */
//    protected void unpackJavascriptDependency( String artifact, File dest, boolean useArtifactId )
//        throws MojoExecutionException
//    {
//        if ( !pluginArtifactMap.containsKey( artifact ) )
//        {
//            throw new MojoExecutionException( "Failed to resolve dependency " + artifact
//                + " required by the plugin" );
//        }
//        Artifact javascript = (Artifact) pluginArtifactMap.get( artifact );
//
//        try
//        {
//            javascriptArtifactManager.unpack( javascript, dest, useArtifactId );
//        }
//        catch ( ArchiverException e )
//        {
//            throw new MojoExecutionException( "Failed to unpack javascript dependency " + artifact,
//                e );
//        }
//    }
    
	

}
