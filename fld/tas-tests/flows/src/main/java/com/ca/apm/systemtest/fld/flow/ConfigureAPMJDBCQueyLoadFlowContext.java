package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;

/**
 * Context which holds settings for JDBC Query Load. 
 * 
 * @author filja01
 *
 */
public class ConfigureAPMJDBCQueyLoadFlowContext implements IFlowContext, EnvPropSerializable<ConfigureAPMJDBCQueyLoadFlowContext> {

	protected String apmServer;
    
    private final transient Serializer envPropSerializer = new Serializer(this);

    public String getApmServer() {
		return apmServer;
	}

	public static Builder getBuilder() {
        BuilderFactory<ConfigureAPMJDBCQueyLoadFlowContext, Builder> factory = new BuilderFactory<>();
        return factory.newBuilder(ConfigureAPMJDBCQueyLoadFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<ConfigureAPMJDBCQueyLoadFlowContext> {
        public Builder apmServer(String apmServer);
    }

    @Override
    public ConfigureAPMJDBCQueyLoadFlowContext deserialize(String key, Map<String, String> map) {
        return envPropSerializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }
    
    public static class Serializer extends AbstractEnvPropertySerializer<ConfigureAPMJDBCQueyLoadFlowContext> {

        private static final String APM_SERVER = "APM_SERVER";
        
        private ConfigureAPMJDBCQueyLoadFlowContext ctx;

        public Serializer() {
            super(Serializer.class);
        }

        public Serializer(ConfigureAPMJDBCQueyLoadFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public ConfigureAPMJDBCQueyLoadFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            
            ConfigureAPMJDBCQueyLoadFlowContext.Builder builder = ConfigureAPMJDBCQueyLoadFlowContext.getBuilder();
            
            builder.apmServer(deserializedMap.get(APM_SERVER));
            
            return builder.build();
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> recordingParams = new HashMap<>();
            if (ctx != null) {
                if (ctx.apmServer != null) {
                	recordingParams.put(APM_SERVER, ctx.apmServer);	
                }
            }
            
            Map<String, String> serializedData = super.serialize(key);
            serializedData.putAll(serializeMapWithKey(key, recordingParams));
            
            return serializedData;
        }
        
    }

}
