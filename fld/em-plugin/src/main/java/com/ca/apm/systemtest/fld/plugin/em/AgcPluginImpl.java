
package com.ca.apm.systemtest.fld.plugin.em;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;

/**
 * Plugin for installation and uninstallation of AGC Master
 * 
 * @author filja01
 *
 */

@PluginAnnotationComponent(pluginType = "agcPlugin")
public class AgcPluginImpl extends EmPluginImpl implements EmPlugin {

    private static final Logger log = LoggerFactory.getLogger(AgcPluginImpl.class);

    public AgcPluginImpl() {}

    @ExposeMethod(description = "Install AGC Master into given prefix.")
    @Override
    public String install(final InstallationParameters config) {
        config.installerType = InstallerType.AGC;
        return super.install(config);
    }
}
