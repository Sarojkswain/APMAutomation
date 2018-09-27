/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;


/**
 * @author keyja01
 *
 */
@Test
public class FullLoadDurationTest extends BaseFldLoadTest {

    @Override
    protected String getLoadName() {
        return "FLD";
    }

    @Override
    protected void startLoad() {
        logger.info("Starting Full Duration Load Test");
        logger.info("Delete FLD.started on the FLD controller node to end the test and shut down all other loads, and undeploy the testbed");
    }

    @Override
    protected void stopLoad() {
        logger.info("FLD ended");
    }
}
