/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

import java.io.File;
import java.util.Map;

/**
 * Encapsulates methods of downloading artifacts for use by plugins.  Implementations may feel free to cache results
 * 
 * @author KEYJA01
 *
 */
public interface ArtifactManager {
	public enum RepositoryType {
		FILESYSTEM, ARTIFACTORY, ARTIFACTORYLITE, HTTP, TRUSS
	}
	
	public static final String KEY_URL = "url";
	public static final String KEY_VERSION = "version";

	// constants for Truss
	public static final String KEY_REPO_BASE = "repo_base";
	public static final String KEY_BUILD_ID = "build_id";
	public static final String KEY_BUILD_SUFFIX = "build_suffix";
	public static final String KEY_CODE_NAME = "code_name";
	public static final String KEY_BUILD_NUMBER = "build_number";
	public static final String KEY_PRODUCT = "product";
	public static final String KEY_OS_ARCHITECTURE = "architecture";
	public static final String KEY_OS_ARCHIVE_EXTENSION = "archive_ext";
	public static final String KEY_FILE_NAME = "file_name";
	public static final String KEY_INSTALLER_PREFIX = "installer_prefix";
    public static final String KEY_APP_SERVER = "app_server";
	
	
	
	/**
	 * Artifact specifications:<br>
	 *   Artifactory: groupId:artifactId:version[:classifier[:type]]<br>
	 *   FILESYSTEM: location on local filesystem - useful for testing<br>
	 *   HTTP: direct URL to artifact<br>
	 *   Truss: can override the default specification, DEFAULT_TRUSS_SPECIFICATION <br>
	 * 
	 * @param artifactSpecification A specification string for the artifact to be downloaded, will vary based on repository type
	 * @param type The type of repository to download from
	 * @param destinationDirectory Where the artifact should be stored after download
	 * @param parameters extra parameters, may vary based on the repository type
	 * @return A {@link ArtifactFetchResult} referencing the downloaded artifact
	 * @throws ArtifactManagerException
	 */
	public ArtifactFetchResult fetchArtifact(String artifactSpecification, RepositoryType type, File destinationDirectory, Map<String, Object> parameters) throws ArtifactManagerException;
}
