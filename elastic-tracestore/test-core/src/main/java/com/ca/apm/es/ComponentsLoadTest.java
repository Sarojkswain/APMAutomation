package com.ca.apm.es;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by venpr05 on 2/21/2017.
 */
public class ComponentsLoadTest extends AbstractLoadTest {

    public ComponentsLoadTest(int bulkCount, int tCount, long duration, String esHost) {

        super(bulkCount, tCount, duration, esHost, "ttcomponents",
            "burst/ttcomponents_mapping.json");
    }

    @Override
    String formSingleDocumentData() throws JsonProcessingException {
        EsTraceComponentData component = new EsTraceComponentData();
        component.setDescription(gen.getUrl());
        component.setDuration(gen.getDuration());
        component.setResource(gen.getResource());
        component.setStartTime(gen.getTime());
        component.setTraceId(gen.getTraceId());
        component.setPosId(gen.getPosId());
        component.setFlags(gen.getFlags());
        component.setSubNodeCount(gen.getCompCount());
        component.setParameters(gen.getParameters());

        return mapper.writeValueAsString(component);
    }

    public static void main(String[] args) throws Exception {

        int bulkCount = 1000;
        try {
            bulkCount = Integer.parseInt(System.getProperty("bulkcount"));
        } catch (Exception e) {
            bulkCount = 1000;
        }

        int tCount = 5;
        try {
            tCount = Integer.parseInt(System.getProperty("threads"));
        } catch (Exception e) {
            tCount = 5;
        }

        long duration = 30 * 60 * 1000L;
        try {
            duration = Long.parseLong(System.getProperty("durationinms"));
        } catch (Exception e) {
            duration = 30 * 60 * 1000L;
        }
        ComponentsLoadTest loadAndStoreTrace =
            new ComponentsLoadTest(bulkCount, tCount, duration, "localhost");
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();
    }
}
