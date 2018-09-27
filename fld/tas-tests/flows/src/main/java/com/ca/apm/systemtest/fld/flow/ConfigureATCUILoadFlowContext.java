package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.flow.tess.TessConfigurer.RecordType;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;

/**
 * Context which holds settings for ATCUI Load. 
 * 
 * @author filja01
 *
 */
public class ConfigureATCUILoadFlowContext implements IFlowContext, EnvPropSerializable<ConfigureATCUILoadFlowContext> {

	protected String webviewHost;
    protected String user;
    protected String password;
    protected String webviewPort;
    protected Integer numberOfBrowsers;
    
    private final transient Serializer envPropSerializer = new Serializer(this);

    public String getWebviewHost() {
		return webviewHost;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
	
	public String getWebviewPort() {
		return webviewPort;
	}
	
	public Integer getNumberOfBrowsers() {
        return numberOfBrowsers;
    }

	public static Builder getBuilder() {
        BuilderFactory<ConfigureATCUILoadFlowContext, Builder> factory = new BuilderFactory<>();
        return factory.newBuilder(ConfigureATCUILoadFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<ConfigureATCUILoadFlowContext> {
        public Builder webviewHost(String webviewHost);
        public Builder user(String user);
        public Builder password(String password);
        public Builder webviewPort(String webviewPort);
        public Builder numberOfBrowsers(Integer numberOfBrowsers);
    }

    @Override
    public ConfigureATCUILoadFlowContext deserialize(String key, Map<String, String> map) {
        return envPropSerializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }
    
    public static class Serializer extends AbstractEnvPropertySerializer<ConfigureATCUILoadFlowContext> {

        private static final String WEBVIEW_PORT = "WEBVIEW_PORT";
        private static final String USER = "USER";
        private static final String PWD = "PWD";
        private static final String WEBVIEW_HOST = "WEBVIEW_HOST";
        private static final String NUMB_BRWS = "NUMB_BRWS";
        
        private ConfigureATCUILoadFlowContext ctx;

        public Serializer() {
            super(Serializer.class);
        }

        public Serializer(ConfigureATCUILoadFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public ConfigureATCUILoadFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            
            ConfigureATCUILoadFlowContext.Builder builder = ConfigureATCUILoadFlowContext.getBuilder();
            
            builder.webviewHost(deserializedMap.get(WEBVIEW_HOST))
            	.password(deserializedMap.get(PWD))
                .webviewPort(deserializedMap.get(WEBVIEW_PORT))
                .user(deserializedMap.get(USER))
                .numberOfBrowsers(Integer.parseInt(deserializedMap.get(NUMB_BRWS)));
            
            return builder.build();
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> recordingParams = new HashMap<>();
            if (ctx != null) {
                if (ctx.webviewHost != null) {
                	recordingParams.put(WEBVIEW_HOST, ctx.webviewHost);	
                }
            	if (ctx.password != null) {
            		recordingParams.put(PWD, ctx.password);	
            	}
                if (ctx.user != null) {
                	recordingParams.put(USER, ctx.user);	
                }
                if (ctx.webviewPort != null) {
                    recordingParams.put(WEBVIEW_PORT, ctx.webviewPort);    
                }
                if (ctx.numberOfBrowsers != null) {
                    recordingParams.put(NUMB_BRWS, ctx.numberOfBrowsers.toString());
                }
            }
            
            Map<String, String> serializedData = super.serialize(key);
            serializedData.putAll(serializeMapWithKey(key, recordingParams));
            
            return serializedData;
        }
        
    }

}
