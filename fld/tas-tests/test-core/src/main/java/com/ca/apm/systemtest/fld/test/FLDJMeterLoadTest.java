/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * @author keyja01
 *
 */
@Test
public class FLDJMeterLoadTest extends BaseFldLoadTest implements FLDLoadConstants {
    private static final class JmeterLoadConfig {
        private final String roleId;
        private final String loadId;
        
        public JmeterLoadConfig(String roleId, String loadId) {
            this.roleId = roleId;
            this.loadId = loadId;
        }
        
        @Override
        public int hashCode() {
            return roleId.hashCode() ^ loadId.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JmeterLoadConfig)) {
                return false;
            }
            JmeterLoadConfig c2 = (JmeterLoadConfig) obj;
            return roleId.equals(c2.roleId) && loadId.equals(c2.loadId);
        }
    }
    private HashSet<JmeterLoadConfig> loadConfigs = new HashSet<>();
    
    @BeforeTest
    public void init() {
        loadConfigs.add(new JmeterLoadConfig(JMETER_LOAD_ROLE_TOMCAT9080_01_ID, "tomcat9080-01"));
        loadConfigs.add(new JmeterLoadConfig(JMETER_LOAD_ROLE_TOMCAT9081_01_ID, "tomcat9081-01"));
    }
    
    @Override
    protected String getLoadName() {
        return "jmeterload-all";
    }

    @Override
    protected void startLoad() {
        shortWait(30000L);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_TOMCAT9080_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_TOMCAT9081_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_6TOMCAT9091_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        //runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_6TOMCAT_LOADTEST_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_7TOMCAT9090_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WAS_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WAS_BRT_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_SOA_WLS7001_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_SOA_WLS7002_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_SOA_WLS7001_02_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_SOA_WLS7002_02_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_APPMAP_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_APPMAP_TEAMCENTER_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_JBOSS6_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_JBOSS7_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET01_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET01_02_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET01_03_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET01_04_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET02_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET02_02_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET02_03_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_FLDNET02_04_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.DAYS, 28);
        
        shortWait(30000L);
    }
    
    
    private String[] roleIds = {
        JMETER_LOAD_ROLE_TOMCAT9080_01_ID, JMETER_LOAD_ROLE_TOMCAT9081_01_ID, JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID,
        JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID, JMETER_LOAD_ROLE_6TOMCAT9091_01_ID, JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID,
        JMETER_LOAD_ROLE_6TOMCAT_LOADTEST_01_ID, JMETER_LOAD_ROLE_7TOMCAT9090_01_ID, JMETER_LOAD_ROLE_WAS_01_ID,   
        JMETER_LOAD_ROLE_WAS_BRT_01_ID, JMETER_LOAD_ROLE_SOA_WLS7001_01_ID, JMETER_LOAD_ROLE_SOA_WLS7002_01_ID,  
        JMETER_LOAD_ROLE_SOA_WLS7001_02_ID, JMETER_LOAD_ROLE_SOA_WLS7002_02_ID, JMETER_LOAD_ROLE_APPMAP_ID, 
        JMETER_LOAD_ROLE_APPMAP_TEAMCENTER_ID, JMETER_LOAD_ROLE_JBOSS6_01_ID, JMETER_LOAD_ROLE_JBOSS7_01_ID,   
        JMETER_LOAD_ROLE_FLDNET01_01_ID, JMETER_LOAD_ROLE_FLDNET01_02_ID, JMETER_LOAD_ROLE_FLDNET01_03_ID, 
        JMETER_LOAD_ROLE_FLDNET01_04_ID, JMETER_LOAD_ROLE_FLDNET02_01_ID, JMETER_LOAD_ROLE_FLDNET02_02_ID, 
        JMETER_LOAD_ROLE_FLDNET02_03_ID, JMETER_LOAD_ROLE_FLDNET02_04_ID

    };
    

    @Override
    protected void stopLoad() {
        for (String roleId: roleIds) {
            runStopLoadThread(roleId);
        }
        
    }
    
    private void runStopLoadThread(final String roleId) {
        Thread th = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    runSerializedCommandFlowFromRoleAsync(roleId, JMeterLoadRole.STOP_LOAD_FLOW_KEY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
        }
    }
    
}
