package com.ca.apm.es;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by venpr05 on 2/21/2017.
 */
public class FullTraceLoadTest extends AbstractLoadTest {

    public FullTraceLoadTest(int bulkCount, int tCount, long duration, String esHost) {

        super(bulkCount, tCount, duration, esHost, "tt", "burst/tt_mapping.json");
    }

    @Override
    String formSingleDocumentData() throws JsonProcessingException {
        EsTraceSummaryData summary = new EsTraceSummaryData();
        summary.setAgent(gen.getAgent());
        summary.setAppName(gen.getApp());
        summary.setCallerTxnTraceId(gen.getCallerId());
        summary.setCompCount(gen.getCompCount());
        summary.setCorKeys(gen.getCorKeys());
        summary.setDescription(gen.getUrl());
        summary.setDuration(gen.getDuration());
        summary.setResource(gen.getResource());
        summary.setStartTime(gen.getTime());
        summary.setTraceId(gen.getTraceId());
        summary.setType(gen.getType());

        List<EsTraceComponentData> listC = new ArrayList<EsTraceComponentData>();
        for (int i = 0; i < summary.getCompCount(); i++) {
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
            listC.add(component);
        }
        EsTraceData trace = new EsTraceData(summary, listC);
        counter.recordComp(trace.getSummary().getCompCount());
        return mapper.writeValueAsString(trace);
    }

    public static void main(String[] args) throws Exception {

        int bulkCount = 20;
        try {
            bulkCount = Integer.parseInt(System.getProperty("bulkcount"));
        } catch (Exception e) {
            bulkCount = 20;
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
        FullTraceLoadTest loadAndStoreTrace =
            new FullTraceLoadTest(bulkCount, tCount, duration, "localhost");
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();
    }
}
