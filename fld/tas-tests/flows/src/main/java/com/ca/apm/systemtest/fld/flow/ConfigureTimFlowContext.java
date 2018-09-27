package com.ca.apm.systemtest.fld.flow;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * This is flow context for {@link ConfigureTimFlow}.
 *
 * @author haiva01
 */
public class ConfigureTimFlowContext implements IFlowContext, AutowireCapable {
    protected String hostname;
    protected int port;
    protected String username;
    protected String password;
    protected String[] checkIfs;
    protected String[] uncheckIfs;
    protected Map<String, String> additionalProperties;

    
    public static Builder getBuilder() {
        BuilderFactory<ConfigureTimFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(ConfigureTimFlowContext.class, Builder.class);
    }
    
    
    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return ConfigureTimFlow.class;
    }
    
    
    public interface Builder extends IGenericBuilder<ConfigureTimFlowContext> {
        public Builder hostname(String hostname);
        // default to 80
        public Builder port(int port);
        // default to admin
        public Builder username(String username);
        // default to quality
        public Builder password(String password);
        public Builder checkIfs(String[] checkIfs);
        public Builder uncheckIfs(String[] uncheckIfs);
        public Builder additionalProperties(Map<String, String> additionalProperties);
    }
}
