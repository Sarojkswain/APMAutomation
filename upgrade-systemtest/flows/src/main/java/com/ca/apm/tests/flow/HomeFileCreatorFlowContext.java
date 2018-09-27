package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jirji01 on 7/19/2017.
 */
public class HomeFileCreatorFlowContext extends FileCreatorFlowContext {
    @Nullable
    private String permissions;

    protected HomeFileCreatorFlowContext(Builder builder) {
        super(builder);
        this.permissions = builder.permissions;
    }

    @Nullable
    public String getPermissions() {
        return this.permissions;
    }

    public static class Builder extends FileCreatorFlowContext.Builder {
        String permissions = "644";

        public HomeFileCreatorFlowContext.Builder permissions(String value) {
            this.permissions = value;
            return this.builder();
        }
        protected HomeFileCreatorFlowContext.Builder builder() {
            return this;
        }

        public HomeFileCreatorFlowContext build() {
            return (HomeFileCreatorFlowContext) super.build();
        }

        protected HomeFileCreatorFlowContext getInstance() {
            return new HomeFileCreatorFlowContext(this);
        }
    }
}
