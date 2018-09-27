package com.ca.apm.systemtest.fld.flow.controller.service;

import com.ca.apm.systemtest.fld.flow.controller.FldLoadStatus;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.security.UsernamePasswordCredentials;

/**
 * Test program to populate FLD controller with test load instances through Hazelcast client.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class InsertLoadsTest {

    public void insertTestLoadsThroughHazelcast() {
        
        //create the Hazelcast client and get the distributed map
        ClientConfig cfg = new ClientConfig();
        cfg.setCredentials(new UsernamePasswordCredentials("fld", "quality"));
        ClientNetworkConfig netCfg = new ClientNetworkConfig();
        netCfg.addAddress("localhost:5701");
        cfg.setNetworkConfig(netCfg);
        
        HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(cfg);
        IMap<String, FldLoadStatus> distributedStatusMap = hazelcastClient.getMap("fld");

        distributedStatusMap.put("Started load #1", FldLoadStatus.STARTED);
        distributedStatusMap.put("New load #1", FldLoadStatus.NEW);
        distributedStatusMap.put("Stopped load #1", FldLoadStatus.STOPPED);
        distributedStatusMap.put("New load #2.", FldLoadStatus.NEW);
        distributedStatusMap.put("FLD", FldLoadStatus.STARTED);//Pseudo load that groups all load enabling bulk stop operation on all of them.
        
    }
    
    public static void main(String[] args) {
        InsertLoadsTest test = new InsertLoadsTest();
        test.insertTestLoadsThroughHazelcast();
    }

}
