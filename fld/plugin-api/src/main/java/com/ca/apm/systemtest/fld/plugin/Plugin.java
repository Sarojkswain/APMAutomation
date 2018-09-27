/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import java.io.File;

import com.ca.apm.systemtest.fld.plugin.vo.Operation;

/**
 * @author keyja01
 *
 */
public interface Plugin {

    public Operation[] listOperations();

    public File createTempDirectory(String tempDirName);

//    public ArtifactFetchResult fetchTempArtifact(String tempDirName, String url, String groupId,
//        String artifactId, String version, String classifier, String type, RepositoryType repoType);

    public void deleteTempDirectory(String tempDirName);
    
    /**
     * Returns the current configuration for this plugin
     * @return
     */
    public PluginConfiguration getPluginConfiguration();
}
