package com.ca.apm.tests.role;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;

public class EmRoleLinuxBuilder extends EmRole.LinuxBuilder implements IEmRoleBuilder {
    public EmRoleLinuxBuilder(String roleId, ITasResolver tasResolver) {
        super(roleId, tasResolver);
    }

    protected EmRole.Builder builder() {
        return this;
    }

    @Override
    public EmRole.Builder installerDir(String installerDir) {
        this.flowContextBuilder.installerDir(installerDir);
        return this.builder();
    }
}
