package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfigureTessFlowContext implements IFlowContext, EnvPropSerializable<ConfigureTessFlowContext> {
    
	/**
	 * Enum to specify a preferred browser to use by the configurator.
	 * 
	 * @author Alexander Sinyushkin (sinal04@ca.com)
	 *
	 */
	public enum PreferredBrowser {
    	Firefox, Chrome, IE;
    	
    	public static PreferredBrowser fromString(String str) {
    		for (PreferredBrowser prefBrowser : PreferredBrowser.values()) {
    			if (prefBrowser.name().equals(str)) {
    				return prefBrowser;
    			}
    		}
    		return null;
    	}
    }
    
	public enum TessService {
        StatsAggregation("Stats Aggregation Service"), DbCleanup("DB Cleanup Service"), TimCollection("TIM Collection Service");
        private String serviceName;
        
        private TessService(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public static TessService forServiceName(String serviceName) {
            for (TessService s: TessService.values()) {
                if (s.serviceName.equals(serviceName)) {
                    return s;
                }
            }
            return null;
        }
    }

    protected Set<String> tims;
    protected boolean removeOldTims;
    protected boolean removeOldWebServerFilters;
    protected Map<TessService, String> tessServiceMap;
    protected Map<String, TessWebServerFilterConfig> tessWebServerFilters;
    protected String tessHostname;
    protected int tessPort;
    protected String tessUser;
    protected String tessPassword;
    protected String reportEmail;
    protected String smtpHost;
    protected String seleniumGridHubHostAndPort;
    protected PreferredBrowser preferredBrowser;
    
    private final transient Serializer envPropSerializer = new Serializer(this);
    
    public static Builder getBuilder() {
        BuilderFactory<ConfigureTessFlowContext, Builder> factory = new BuilderFactory<>();
        return factory.newBuilder(ConfigureTessFlowContext.class, Builder.class);
    }
    
    public interface Builder extends IGenericBuilder<ConfigureTessFlowContext> {
        public Builder tims(Set<String> tims);
        public Builder removeOldTims(boolean removeOldTims);
        public Builder removeOldWebServerFilters(boolean removeOldWebServerFilters);
        public Builder tessServiceMap(Map<TessService, String> tessServiceMap);
        public Builder tessWebServerFilters(Map<String, TessWebServerFilterConfig> tessWebServerFilters);
        public Builder tessHostname(String testHostname);
        public Builder tessUser(String tessUser);
        public Builder tessPassword(String tessPassword);
        public Builder tessPort(int port);
        public Builder reportEmail(String reportEmail);
        public Builder smtpHost(String smtpHost);
        public Builder seleniumGridHubHostAndPort(String hostAndPort);
        public Builder preferredBrowser(PreferredBrowser preferredBrowser);
    }

    @Override
    public ConfigureTessFlowContext deserialize(String key, Map<String, String> map) {
        return envPropSerializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }
    
    
    public static class Serializer extends AbstractEnvPropertySerializer<ConfigureTessFlowContext> {

        private static final String TIMS = "TIMS";
        private static final String TESS_FILTERS = "TESS_FILTERS";
        private static final String TESS_SVCS = "TESS_SVCS";
        private static final String TESS_PORT = "TESS_PORT";
        private static final String REMOVE_FILTERS = "REMOVE_FILTERS";
        private static final String REMOVE_TIMS = "REMOVE_TIMS";
        private static final String TESS_USER = "TESS_USER";
        private static final String TESS_PWD = "TESS_PWD";
        private static final String TESS_HOST = "TESS_HOST";
        private static final String SMTP_HOST = "SMTP_HOST";
        private static final String REPORT_EMAIL = "REPORT_EMAIL";
        private static final String SELENIUM_GRID_HUB_HOST_AND_PORT = "SELENIUM_GRID_HUB_HOST_AND_PORT";
        private static final String PREFERRED_BROWSER = "PREFERRED_BROWSER";
        
        private ConfigureTessFlowContext ctx;

        public Serializer(ConfigureTessFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public ConfigureTessFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            
            ConfigureTessFlowContext.Builder builder = ConfigureTessFlowContext.getBuilder();
            Gson gson = new Gson();
            
            builder.tessHostname(deserializedMap.get(TESS_HOST))
                .tessPassword(deserializedMap.get(TESS_PWD))
                .tessPort(Integer.parseInt(deserializedMap.get(TESS_PORT)))
                .tessUser(deserializedMap.get(TESS_USER))
                .removeOldTims(Boolean.valueOf(deserializedMap.get(REMOVE_TIMS)))
                .removeOldWebServerFilters(Boolean.valueOf(deserializedMap.get(REMOVE_FILTERS)))
                .smtpHost(deserializedMap.get(SMTP_HOST))
                .reportEmail(deserializedMap.get(REPORT_EMAIL))
                .seleniumGridHubHostAndPort(deserializedMap.get(SELENIUM_GRID_HUB_HOST_AND_PORT))
                ;
            
            String json = deserializedMap.get(TIMS);
            if (json != null) {
                Set<String> tims = gson.fromJson(json, new TypeToken<Set<String>>(){}.getType());
                builder.tims(tims);
            }
            
            json = deserializedMap.get(TESS_SVCS);
            if (json != null) {
                Map<TessService, String> svcMap = gson.fromJson(json, new TypeToken<Map<TessService, String>>(){}.getType());
                builder.tessServiceMap(svcMap);
            }
            
            json = deserializedMap.get(TESS_FILTERS);
            if (json != null) {
                Map<String, TessWebServerFilterConfig> filters = gson.fromJson(json, 
                    new TypeToken<Map<String, TessWebServerFilterConfig>>(){}.getType());
                builder.tessWebServerFilters(filters);
            }
            
            String prefBrowserStr = deserializedMap.get(PREFERRED_BROWSER);
            if (prefBrowserStr != null) {
            	builder.preferredBrowser(PreferredBrowser.fromString(prefBrowserStr));
            }
            return builder.build();
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Gson gson = new Gson();
            
            Map<String, String> customData = new HashMap<>(10);
            customData.put(TESS_HOST, ctx.tessHostname);
            customData.put(TESS_PWD, ctx.tessPassword);
            customData.put(TESS_USER, ctx.tessUser);
            customData.put(REMOVE_TIMS, Boolean.toString(ctx.removeOldTims));
            customData.put(REMOVE_FILTERS, Boolean.toString(ctx.removeOldWebServerFilters));
            customData.put(TESS_PORT, Integer.toString(ctx.tessPort));
            customData.put(SMTP_HOST, ctx.smtpHost);
            customData.put(REPORT_EMAIL, ctx.reportEmail);
            customData.put(PREFERRED_BROWSER, ctx.preferredBrowser != null ? ctx.preferredBrowser.name() : "NONE");

            if (ctx.seleniumGridHubHostAndPort != null) {
                customData.put(SELENIUM_GRID_HUB_HOST_AND_PORT, ctx.seleniumGridHubHostAndPort);
            }
            if (ctx.tessServiceMap != null) {
                customData.put(TESS_SVCS, gson.toJson(ctx.tessServiceMap));
            }
            if (ctx.tessWebServerFilters != null) {
                customData.put(TESS_FILTERS, gson.toJson(ctx.tessWebServerFilters));
            }
            if (ctx.tims != null) {
                customData.put(TIMS, gson.toJson(ctx.tims));
            }
            
            Map<String, String> serializedData = super.serialize(key);
            serializedData.putAll(serializeMapWithKey(key, customData));
            
            return serializedData;
        }
        
    }
    
}
