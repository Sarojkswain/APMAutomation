/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;

/**
 * @author KEYJA01
 *
 */
public class DynamicInstrumentationFlowContext implements IFlowContext, EnvPropSerializable<DynamicInstrumentationFlowContext> {
    protected String diHome;
    protected String clwJar;
    protected String emHost;
    protected String emUser;
    protected String emPassword;
    protected int emPort;
    protected String agentHost;
    protected String agentName;
    protected int agentPort;
    protected int servlets;
    protected String urlFormat;
    protected String javaHome;
    private final transient Serializer serializer = new Serializer(this);
    
    
    public static Builder getBuilder() {
        BuilderFactory<DynamicInstrumentationFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(DynamicInstrumentationFlowContext.class, Builder.class);
    }
    
    public interface Builder extends IGenericBuilder<DynamicInstrumentationFlowContext> {
        public Builder diHome(String diHome);
        public Builder clwJar(String clwJar);
        public Builder emHost(String emHost);
        public Builder emPort(int emPort);
        public Builder emUser(String emUser);
        public Builder emPassword(String emPassword);
        public Builder agentHost(String agentHost);
        public Builder agentName(String agentName);
        public Builder agentPort(int agentPort);
        public Builder servlets(int servlets);
        public Builder urlFormat(String urlFormat);
        public Builder javaHome(String javaHome);
    }
    
    public static class Serializer extends AbstractEnvPropertySerializer<DynamicInstrumentationFlowContext> {

        private static final String DATA = "data";
        private DynamicInstrumentationFlowContext ctx;

        public Serializer(DynamicInstrumentationFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public DynamicInstrumentationFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            Gson gson = new Gson();
            String data = deserializedMap.get(DATA);
            DynamicInstrumentationFlowContext ctx = gson.fromJson(data, DynamicInstrumentationFlowContext.class);
            return ctx;
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> map = super.serialize(key);
            
            Map<String, String> customData = new HashMap<>(1);
            Gson gson = new Gson();
            customData.put(DATA, gson.toJson(ctx));
            
            map.putAll(serializeMapWithKey(key, customData));
            
            return map;
        }
        
    }
    

    @Override
    public DynamicInstrumentationFlowContext deserialize(String key, Map<String, String> map) {
        return serializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return serializer.serialize(key);
    }
}
