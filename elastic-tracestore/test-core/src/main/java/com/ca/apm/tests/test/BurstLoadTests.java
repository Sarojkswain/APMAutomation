package com.ca.apm.tests.test;

import org.testng.annotations.Test;

import com.ca.apm.es.ComponentsLoadTest;
import com.ca.apm.es.FullTraceLoadTest;
import com.ca.apm.es.SummaryLoadTest;
import com.ca.apm.tests.role.ElasticSearchRole;
import com.ca.apm.tests.testbed.ElasticOnlyTestBed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

@Test
public class BurstLoadTests extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = ElasticOnlyTestBed.class, executeOn = "loadMachine"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void runSummaryOnlyBurstLoadTest() throws Exception {

        // start elasticsearch
        runSerializedCommandFlowFromRole("esRole", ElasticSearchRole.ELASTICSEARCH_START);

        /*
         * Choose bulk to be less than or equal to 10MB, there apparently is a
         * number of documents which affects the throughput as well in addition to
         * the total size. TODO: investigate
         */
        int bulkCount = 10000;
        try {
            bulkCount = Integer.parseInt(System.getProperty("bulkcount"));
        } catch (Exception e) {
            bulkCount = 10000;
        }

        /*
         * With that bulk size and this thread we got optimal results
         * TODO: revisit again
         */
        int tCount = 5;
        try {
            tCount = Integer.parseInt(System.getProperty("threads"));
        } catch (Exception e) {
            tCount = 5;
        }

        /*
         * ES recommends 30mins or above for proper testing
         * Can always increase to run different tests.
         */
        long duration = 30 * 60 * 1000L;
        try {
            duration = Long.parseLong(System.getProperty("durationinms"));
        } catch (Exception e) {
            duration = 30 * 60 * 1000L;
        }

        String esHost = envProperties.getMachineHostnameByRoleId("esRole");
        SummaryLoadTest loadAndStoreTrace =
            new SummaryLoadTest(bulkCount, tCount, duration, esHost);
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();

        // TODO: add reasonable plausible query load
    }

    @Tas(testBeds = @TestBed(name = ElasticOnlyTestBed.class, executeOn = "loadMachine"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void runComponentsOnlyBurstLoadTest() throws Exception {

        // start elasticsearch
        runSerializedCommandFlowFromRole("esRole", ElasticSearchRole.ELASTICSEARCH_START);

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
        String esHost = envProperties.getMachineHostnameByRoleId("esRole");
        ComponentsLoadTest loadAndStoreTrace =
            new ComponentsLoadTest(bulkCount, tCount, duration, esHost);
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();
        // TODO: add reasonable plausible query load
    }

    @Tas(testBeds = @TestBed(name = ElasticOnlyTestBed.class, executeOn = "loadMachine"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void runFullTraceBurstLoadTest() throws Exception {

        // start elasticsearch
        runSerializedCommandFlowFromRole("esRole", ElasticSearchRole.ELASTICSEARCH_START);

        int bulkCount = 10;
        try {
            bulkCount = Integer.parseInt(System.getProperty("bulkcount"));
        } catch (Exception e) {
            bulkCount = 10;
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
        String esHost = envProperties.getMachineHostnameByRoleId("esRole");
        FullTraceLoadTest loadAndStoreTrace =
            new FullTraceLoadTest(bulkCount, tCount, duration, esHost);
        loadAndStoreTrace.createIndex();
        loadAndStoreTrace.loadAndStore_SpringRest();
        // TODO: add reasonable plausible query load
    }
}
