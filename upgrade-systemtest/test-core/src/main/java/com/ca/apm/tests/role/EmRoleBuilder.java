package com.ca.apm.tests.role;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;

public class EmRoleBuilder extends EmRole.Builder implements IEmRoleBuilder{
    public EmRoleBuilder(String roleId, ITasResolver tasResolver) {
        super(roleId, tasResolver);
    }

    protected EmRole.Builder builder() {
        return this;
    }

    public EmRole.Builder installerDir(String installerDir) {
        this.flowContextBuilder.installerDir(installerDir);
        return this.builder();
    }
}
