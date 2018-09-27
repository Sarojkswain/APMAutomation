/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Starts the wurlitzer fake agent loads in the FLD
 * @author keyja01
 *
 */
@Test
public class FLDWurlitzerLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    private static final Logger logger = LoggerFactory.getLogger(FLDWurlitzerLoadTest.class);
    private enum State {
        starting, started, stopping, stopped;
    }
    private State state = State.stopped;
    
    @Override
    protected String getLoadName() {
        return "wurlitzer";
    }

    @Override
    protected void startLoad() {
        logger.info("Setting state to \"starting\"");
        synchronized (state) {
            state = State.starting;
        }
        logger.info("Set state to \"starting\"");
        for (String roleId: WURLITZER_LOAD_ROLES) {
            logger.info("Starting wurlitzer fake agent load on "  + roleId);
            boolean wait = false;
            try {
                runSerializedCommandFlowFromRoleAsync(roleId, WurlitzerLoadRole.START_WURLITZER_FLOW_KEY, TimeUnit.DAYS, 28);
                logger.info("Started wurlitzer fake agent load on "  + roleId);
                wait = true;
            } catch (Exception e) {
                logger.warn("Exception while starting wurlitzer fake agent load: {" + e.getClass().getSimpleName() + ":" + e.getMessage() + "}");
            }
            if (state == State.stopping) {
                logger.info("Detected state \"stopping\" - shuttind down test");
                synchronized (state) {
                    state.notifyAll();
                    return;
                }
            }
            if (wait) {
                // we only want to wait if the load was actually started
                shortWait(15000L);
            }
        }
        logger.info("Setting state to \"started\"");
        synchronized (state) {
            state = State.started;
        }
        logger.info("Set state to \"started\"");
    }

    @Override
    protected void stopLoad() {
        logger.info("stopLoad() called");
        if (state == State.stopped) {
            logger.info("Load " + getLoadName() + " already stopped");
        }
        synchronized (state) {
            if (state == State.starting) {
                logger.info("Setting state to \"stopping\"");
                state = State.stopping;
                try {
                    logger.info("Going to wait now");
                    state.wait();
                    logger.info("Done waiting");
                } catch (InterruptedException e) {
                }
            }
            state = State.stopping;
        }
        logger.info("About to call stop load flows for wurlitzer");
        for (String roleId: WURLITZER_LOAD_ROLES) {
            logger.info("Stopping wurlitzer on " + roleId);
            try {
//                runSerializedCommandFlowFromRoleAsync(roleId, WurlitzerLoadRole.STOP_WURLITZER_FLOW_KEY);
            } catch (Exception e) {
                logger.info("Exception while stopping wurlitzer fake agent load: {" + e.getClass().getSimpleName() + ":" + e.getMessage() + "}");
            }
            logger.info("Stopped wurlitzer on " + roleId);
        }
        logger.info("Setting state to \"stopped\"");
        synchronized (state) {
            state = State.stopped;
        }
        logger.info("Set state to \"stopped\"");
    }
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
            // ignore
        }
    }
}
