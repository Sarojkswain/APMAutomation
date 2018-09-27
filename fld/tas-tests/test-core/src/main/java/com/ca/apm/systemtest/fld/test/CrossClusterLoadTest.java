/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * @author KEYJA01
 *
 */
@Test
public class CrossClusterLoadTest extends BaseFldLoadTest implements FLDLoadConstants, FLDConstants {
    public static final Logger log = LoggerFactory.getLogger(CrossClusterLoadTest.class);

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        return "was-xcluster";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        try {
            runSerializedCommandFlowFromRoleAsync(WAS_XCLUSTER_CLIENT_ROLE_ID + "_"+ LOAD2_ROLE_ID, ClientDeployRole.WASCC_START_LOAD, 
                TimeUnit.DAYS, 28);
            
            runSerializedCommandFlowFromRoleAsync(WAS_XCLUSTER_CLIENT_ROLE_ID + "_"+ LOAD2_ROLE_ID, ClientDeployRole.STRESSAPP_START_LOAD, 
                TimeUnit.DAYS, 28);
            runSerializedCommandFlowFromRoleAsync(LOAD1_ROLE_ID, ClientDeployRole.STRESSAPP_START_LOAD, 
                TimeUnit.DAYS, 28);
            runSerializedCommandFlowFromRoleAsync(LOAD3_ROLE_ID, ClientDeployRole.STRESSAPP_START_LOAD, 
                TimeUnit.DAYS, 28);
            runSerializedCommandFlowFromRoleAsync(LOAD3_ROLE_ID, ClientDeployRole.JMETER_START_LOAD, 
                TimeUnit.DAYS, 28);
        } catch (Exception e) {
            log.warn("Caught exception while starting WAS Cross Cluster Load", e);
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        try {
            runSerializedCommandFlowFromRoleAsync(WAS_XCLUSTER_CLIENT_ROLE_ID + "_"+ LOAD2_ROLE_ID, ClientDeployRole.WASCC_STOP_LOAD);
        } catch (Exception e) {
            log.warn("Caught exception while stopping WAS Cross Cluster Load", e);
        }
    }

}
