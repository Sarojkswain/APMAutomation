package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.tas.builder.BuilderBase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.apache.http.util.Args.notNull;

/**
 * Created by nick on 8.10.14.
 */
public class FileBackupFlowContext implements AutowireCapable {

    private String operation;
    private List<String> arguments;

    public FileBackupFlowContext(Builder builder) {
        operation = builder.methodName;
        arguments = builder.arguments;

    }

    @NotNull
    @Override
    public Class<? extends FileBackupFlow> autowiredFlow() {
        return FileBackupFlow.class;
    }

    public String getOperation() {
        return operation;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public static class Builder extends BuilderBase<Builder, FileBackupFlowContext> {

        private String methodName;
        private List<String> arguments;

        public Builder methodName(String value) {
            this.methodName = value;
            return this;
        }

        public Builder arguments(List<String> value) {
            this.arguments = value;
            return this;
        }

        @Override
        public FileBackupFlowContext build() {
            FileBackupFlowContext xmlModifierFlowContext = getInstance();
            notNull(xmlModifierFlowContext.operation, "methodName");
            notNull(xmlModifierFlowContext.arguments, "arguments");
            return xmlModifierFlowContext;
        }

        @Override
        protected FileBackupFlowContext getInstance() {
            return new FileBackupFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
