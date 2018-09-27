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

/**
 * @author KEYJA01
 *
 */
@Test
public class WlsCrossClusterLoadTest extends BaseFldLoadTest {
    public static final Logger log = LoggerFactory.getLogger(WlsCrossClusterLoadTest.class);

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        return "wls-xcluster";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        try {
            runSerializedCommandFlowFromRoleAsync(WLS03_CLIENT_ROLE_ID, ClientDeployRole.WLSCC_START_LOAD,
                TimeUnit.DAYS, 28);
        } catch (Exception e) {
            log.warn("Caught exception while starting WLS Cross Cluster Load", e);
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        try {
            runSerializedCommandFlowFromRoleAsync(WLS03_CLIENT_ROLE_ID, ClientDeployRole.WLSCC_STOP_LOAD);
        } catch (Exception e) {
            log.warn("Caught exception while stopping WLS Cross Cluster Load", e);
        }
    }

}
