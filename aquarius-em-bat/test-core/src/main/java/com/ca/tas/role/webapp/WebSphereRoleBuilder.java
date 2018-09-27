package com.ca.tas.role.webapp;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.WebSphereRole.Builder;

public class WebSphereRoleBuilder extends Builder {

    public WebSphereRoleBuilder(String roleId, ITasResolver tasResolver) {
        super(roleId, tasResolver);
    }

    public WebSphereRoleBuilder setUpdateInstallerInstallLocation(String installLocation) {
        installerFlowContextBuilder.installLocation(installLocation);
        return this;
    }
    
}
