package com.ca.apm.systemtest.fld.plugin.prelert;

import java.nio.file.Path;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * FLD Prelert plugin interface.
 *
 * @author filja01
 */
@PluginAnnotationComponent(pluginType = "prelertPlugin")
public interface PrelertPlugin extends Plugin {
    
    class Configuration {
        public String prelertInstallDir;
        
        public String learnonlytimeCfg = "600";

        public String codeName;
        public String buildId;
        public String buildNumber;
        public String trussServer;
        
        public OperatingSystemFamily platform = OperatingSystemFamily.Windows;
    }

    boolean isServerRunning(String strUrl);
    
    void install(Configuration config);
    
    void uninstall(Configuration config, Path installerDir);

    boolean start(Configuration config);
    
    boolean stop(Configuration config);
}
