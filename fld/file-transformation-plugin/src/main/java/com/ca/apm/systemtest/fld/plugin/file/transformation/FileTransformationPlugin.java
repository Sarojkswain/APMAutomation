package com.ca.apm.systemtest.fld.plugin.file.transformation;

import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * This is FLD File Transformation plugin interface.
 *
 * @author haiva01
 */

@PluginAnnotationComponent(pluginType = FileTransformationPlugin.PLUGIN)
public interface FileTransformationPlugin extends Plugin {
    public static final String PLUGIN = "fileTransformation";
    
    enum ConfigurationFormat {
        XML, JSON
    }

    void transform(String configuration, ConfigurationFormat format, Map<String, Object> vars);

    void transformUrl(String configurationUrl, ConfigurationFormat format,
        Map<String, Object> vars);

    void createTempDir();

    void deleteTempDir();
}
