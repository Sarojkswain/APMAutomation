/**
 * 
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.EnvPropSerializable;

import java.util.Collection;
import java.util.Map;

/**
 * @author KEYJA01
 *
 */
public class RunWebViewLoadFlowContext implements IFlowContext, EnvPropSerializable<RunWebViewLoadFlowContext> {
    protected Map<String, Collection<String>> urlBrowserMap;
    protected String webviewServerHost;
    protected Integer webviewServerPort;
    protected String webViewUser; 
    protected String webViewPassword;
    /**
     * How long to keep browsers open, default is 28 days
     */
    protected long shutdownTimeout = 2419200000L;
    
    public interface Builder extends IGenericBuilder<RunWebViewLoadFlowContext> {
        public Builder urlBrowserMap(Map<String, Collection<String>> urlBrowserMap);
        public Builder webviewServerHost(String webviewServerHost);
        public Builder webviewServerPort(Integer webviewServerPort);
        public Builder webViewUser(String webViewUser);
        public Builder webViewPassword(String webViewPassword);
        public Builder shutdownTimeout(long shutdownTimeout);
    }
    
    
    public static Builder getBuilder() {
        BuilderFactory<RunWebViewLoadFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(RunWebViewLoadFlowContext.class, Builder.class);
    }
    

    @Override
    public RunWebViewLoadFlowContext deserialize(String key, Map<String, String> serializedData) {
        RunWebViewLoadFlowContextSerializer serializer = new RunWebViewLoadFlowContextSerializer();
        
        return serializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        RunWebViewLoadFlowContextSerializer serializer = new RunWebViewLoadFlowContextSerializer(this);
        return serializer.serialize(key);
    }
}
