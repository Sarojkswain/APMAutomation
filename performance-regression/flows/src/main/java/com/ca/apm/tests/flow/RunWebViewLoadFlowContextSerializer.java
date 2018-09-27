/**
 * 
 */
package com.ca.apm.tests.flow;

import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KEYJA01
 *
 */
public class RunWebViewLoadFlowContextSerializer extends AbstractEnvPropertySerializer<RunWebViewLoadFlowContext> {
    private RunWebViewLoadFlowContext ctx;
    
    public RunWebViewLoadFlowContextSerializer(RunWebViewLoadFlowContext ctx) {
        super(RunWebViewLoadFlowContextSerializer.class);
        this.ctx = ctx;
    }
    
    public RunWebViewLoadFlowContextSerializer() {
        super(RunWebViewLoadFlowContextSerializer.class);
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
    public RunWebViewLoadFlowContext deserialize(String key, Map<String, String> serializedData) {
        Map<String, String> map = deserializeMapWithKey(key, serializedData);
        String json = map.get("DATA");
        Gson gson = new Gson();
        if (json != null) {
            RunWebViewLoadFlowContext ctx = gson.fromJson(json, RunWebViewLoadFlowContext.class);
            return ctx;
        }
        
        return new RunWebViewLoadFlowContext();
    }
}
