package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.google.gson.Gson;

/**
 * @author bocto01
 *
 */
public class RunNetworkTrafficMonitorFlowContextSerializer extends AbstractEnvPropertySerializer<RunNetworkTrafficMonitorFlowContext> {

    private RunNetworkTrafficMonitorFlowContext ctx;

    public RunNetworkTrafficMonitorFlowContextSerializer(RunNetworkTrafficMonitorFlowContext ctx) {
        super(RunNetworkTrafficMonitorFlowContextSerializer.class);
        this.ctx = ctx;
    }
    
    public RunNetworkTrafficMonitorFlowContextSerializer() {
        super(RunNetworkTrafficMonitorFlowContextSerializer.class);
    }
    
    @Override
    public Map<String, String> serialize(String key) {
        Map<String, String> data = super.serialize(key);
        Map<String, String> customData = new HashMap<>(1);
        if (ctx != null) {
            Gson gson = new Gson();
            String json = gson.toJson(ctx);
            customData.put("DATA", json);
        }
        data.putAll(serializeMapWithKey(key, customData));
        return data;
    }

    @Override
    public RunNetworkTrafficMonitorFlowContext deserialize(String key, Map<String, String> serializedData) {
        Map<String, String> map = deserializeMapWithKey(key, serializedData);
        String json = map.get("DATA");
        Gson gson = new Gson();
        if (json != null) {
            RunNetworkTrafficMonitorFlowContext ctx = gson.fromJson(json, RunNetworkTrafficMonitorFlowContext.class);
            return ctx;
        }
        return new RunNetworkTrafficMonitorFlowContext();
    }
    
}
