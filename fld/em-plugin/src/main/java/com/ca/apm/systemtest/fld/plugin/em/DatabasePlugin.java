package com.ca.apm.systemtest.fld.plugin.em;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;

/**
 * Created by haiva01 on 2.7.2015.
 */
@PluginAnnotationComponent(pluginType = "dbPlugin")
public interface DatabasePlugin extends EmPlugin {
    void importDomainConfig(String cemDbExportFile, String targetRelease);
}
