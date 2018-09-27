package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Created by jirji01 on 5/16/2017.
 */
public class CheckEmConfigFlowContext implements IFlowContext, AutowireCapable {

    private final String fileName;
    private final Map<String, List<String>> properties;

    protected CheckEmConfigFlowContext(Builder builder) {
        this.fileName = builder.fileName;
        this.properties = builder.properties;
    }

    public String getFileName() {
        return fileName;
    }

    public Map<String, List<String>> getProperties() {
        return properties;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return CheckEmConfigFlow.class;
    }

    public static class Builder extends BuilderBase<CheckEmConfigFlowContext.Builder, CheckEmConfigFlowContext> {

        private String fileName;
        private Map<String, List<String>> properties;

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CheckEmConfigFlowContext getInstance() {
            return new CheckEmConfigFlowContext(this);
        }

        @Override
        public CheckEmConfigFlowContext build() {

            final CheckEmConfigFlowContext context = getInstance();
            Args.notNull(fileName, "file name");
            Args.notNull(properties, "properties");
            return context;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return builder();
        }

        public Builder properties(Map<String, List<String>> properties) {
            this.properties = properties;
            return builder();
        }
    }
}
