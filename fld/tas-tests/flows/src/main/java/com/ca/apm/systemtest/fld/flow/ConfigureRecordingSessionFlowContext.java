package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.flow.tess.TessConfigurer.RecordType;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;

/**
 * Context which holds settings for Agent or TIM recording sessions. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ConfigureRecordingSessionFlowContext implements IFlowContext, EnvPropSerializable<ConfigureRecordingSessionFlowContext> {

	protected String tessHostname;
    protected String tessUser;
    protected String tessPassword;
    protected String clientIP;
    protected int tessPort;
    protected int recordingDurationMillis;
    protected RecordType recordType;
    
    private final transient Serializer envPropSerializer = new Serializer(this);

    public String getTessHostname() {
		return tessHostname;
	}

	public String getTessUser() {
		return tessUser;
	}

	public String getTessPassword() {
		return tessPassword;
	}

	public String getClientIP() {
		return clientIP;
	}

	public int getTessPort() {
		return tessPort;
	}

	public int getRecordingDurationMillis() {
		return recordingDurationMillis;
	}

	public RecordType getRecordType() {
		return recordType;
	}

	public static Builder getBuilder() {
        BuilderFactory<ConfigureRecordingSessionFlowContext, Builder> factory = new BuilderFactory<>();
        return factory.newBuilder(ConfigureRecordingSessionFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<ConfigureRecordingSessionFlowContext> {
        public Builder tessHostname(String testHostname);
        public Builder tessUser(String tessUser);
        public Builder tessPassword(String tessPassword);
        public Builder tessPort(int port);
        public Builder clientIP(String clientIP);
        public Builder recordingDurationMillis(int recordingDurationMillis);
        public Builder recordType(RecordType recordType);
    }

    @Override
    public ConfigureRecordingSessionFlowContext deserialize(String key, Map<String, String> map) {
        return envPropSerializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }
    
    public static class Serializer extends AbstractEnvPropertySerializer<ConfigureRecordingSessionFlowContext> {

        private static final String TESS_PORT = "TESS_PORT";
        private static final String TESS_USER = "TESS_USER";
        private static final String TESS_PWD = "TESS_PWD";
        private static final String TESS_HOST = "TESS_HOST";
        private static final String CLIENT_IP = "CLIENT_IP";
        private static final String RECORDING_DURATION_MILLIS = "RECORDING_DURATION_MILLIS";
        private static final String RECORD_TYPE = "RECORD_TYPE";
        
        private ConfigureRecordingSessionFlowContext ctx;

        public Serializer() {
            super(Serializer.class);
        }

        public Serializer(ConfigureRecordingSessionFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public ConfigureRecordingSessionFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            
            ConfigureRecordingSessionFlowContext.Builder builder = ConfigureRecordingSessionFlowContext.getBuilder();
            
            builder.tessHostname(deserializedMap.get(TESS_HOST))
            	.tessPassword(deserializedMap.get(TESS_PWD))
                .tessPort(Integer.parseInt(deserializedMap.get(TESS_PORT)))
                .tessUser(deserializedMap.get(TESS_USER))
                .clientIP(deserializedMap.get(CLIENT_IP))
                .recordingDurationMillis(Integer.parseInt(deserializedMap.get(RECORDING_DURATION_MILLIS)))
                .recordType(RecordType.fromString(deserializedMap.get(RECORD_TYPE)));
            
            return builder.build();
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> recordingParams = new HashMap<>();
            if (ctx != null) {
                if (ctx.tessHostname != null) {
                	recordingParams.put(TESS_HOST, ctx.tessHostname);	
                }
            	if (ctx.tessPassword != null) {
            		recordingParams.put(TESS_PWD, ctx.tessPassword);	
            	}
                if (ctx.tessUser != null) {
                	recordingParams.put(TESS_USER, ctx.tessUser);	
                }
                if (ctx.clientIP != null) {
                	recordingParams.put(CLIENT_IP, ctx.clientIP);
                }
                
                recordingParams.put(TESS_PORT, Integer.toString(ctx.tessPort));
                recordingParams.put(RECORDING_DURATION_MILLIS, Integer.toString(ctx.recordingDurationMillis));
                recordingParams.put(RECORD_TYPE, ctx.recordType.name());
            }
            
            Map<String, String> serializedData = super.serialize(key);
            serializedData.putAll(serializeMapWithKey(key, recordingParams));
            
            return serializedData;
        }
        
    }

}
