package com.ca.apm.es;

import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * Created by venpr05 on 2/21/2017.
 */
public class SummaryLoadTest extends AbstractLoadTest {

    public SummaryLoadTest(int bulkCount, int tCount, long duration, String esHost) {

        super(bulkCount, tCount, duration, esHost, "ttsummary", "burst/ttsummary_mapping.json");
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

        return mapper.writeValueAsString(summary);
    }

    public static void main(String[] args) throws Exception {

        int bulkCount = 10000;
        try {
            bulkCount = Integer.parseInt(System.getProperty("bulkcount"));
        } catch (Exception e) {
            bulkCount = 10000;
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
        SummaryLoadTest loadAndStoreTrace =
            new SummaryLoadTest(bulkCount, tCount, duration, "localhost");
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();
    }
}
