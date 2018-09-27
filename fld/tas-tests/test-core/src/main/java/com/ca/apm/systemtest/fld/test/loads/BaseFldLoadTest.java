package com.ca.apm.systemtest.fld.test.loads;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.flow.controller.FldLoadStatus;
import com.ca.apm.systemtest.fld.flow.controller.HazelcastMapListener;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.util.DefaultFLDReportClient;
import com.ca.apm.systemtest.fld.testbed.util.FLDReportClient;
import com.ca.apm.systemtest.fld.testbed.util.JsonReportResult;
import com.ca.apm.systemtest.fld.testbed.util.LoadStatus;
import com.ca.tas.test.TasTestNgTest;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.security.UsernamePasswordCredentials;

/**
 * Base class for FLD load tests.  Subclasses should implement the startLoad() and stopLoad() methods
 * to start a load running.
 * @author keyja01
 *
 */
public abstract class BaseFldLoadTest extends TasTestNgTest implements FLDLoadConstants, HazelcastMapListener<String, FldLoadStatus> {
    private boolean waitForStopOnTestEnd = false;
    private boolean testRunning = true;
    private HazelcastInstance hc;
    private IMap<String, FldLoadStatus> distributedStatusMap;
    private FldLoadStatus testStatus = FldLoadStatus.NEW;

    protected static final Logger logger = LoggerFactory.getLogger(BaseFldLoadTest.class);
    
    
    public BaseFldLoadTest() {
    }
    
    public BaseFldLoadTest(boolean waitForStopOnTestEnd) {
        this.waitForStopOnTestEnd = waitForStopOnTestEnd;
    }
    
    @BeforeClass
    public void initializeTest() {
        logger.info(getClass().getName() + ": in @BeforeClass");
        
        // create the Hazelcast client and get the distributed map
        String controllerHost = envProperties.getMachineHostnameByRoleId(FLD_CONTROLLER_ROLE_ID);
        ClientConfig cfg = new ClientConfig();
        cfg.setCredentials(new UsernamePasswordCredentials("fld", "quality"));
        ClientNetworkConfig netCfg = new ClientNetworkConfig();
        netCfg.addAddress(controllerHost + ":" + 5701);
        cfg.setNetworkConfig(netCfg);
        
        hc = HazelcastClient.newHazelcastClient(cfg);
        distributedStatusMap = hc.getMap("fld");
        
        
        logger.info(getClass().getName() + ": exiting @BeforeClass");
    }

    @AfterClass
    public void shutdownTest() {
        logger.info(getClass().getName() + ": in @AfterClass");
        logger.info(getClass().getName() + ": exiting @AfterClass");
    }
    
    @Test
    public void runLoadTest() {
        String controllerHost = envProperties.getMachineHostnameByRoleId(FLD_CONTROLLER_ROLE_ID);
        FLDReportClient reportClient = DefaultFLDReportClient.create(controllerHost);

        try {
            startLoad();  
            distributedStatusMap.put(getLoadName(), FldLoadStatus.STARTED);
            notifyLoadStatus(reportClient, LoadStatus.STARTED, new Date());
        } catch (Throwable t) {
            notifyLoadStatus(reportClient, LoadStatus.START_FAILED, new Date());
            throw t;
        }
        
        long count = 0;
        while (testRunning) {
            // check for changes
            synchronized (this) {
                try {
                    wait(10000L);
                } catch (InterruptedException e) {
                }
                testStatus = distributedStatusMap.get(getLoadName());
                if (++count % 60L == 0) {
                    logger.info("[{}]: status {}", getLoadName(), testStatus);
                }
                switch (testStatus) {
                    case DOSTART:
                        logger.info("[{}]: Starting load", getLoadName());
                        distributedStatusMap.put(getLoadName(), FldLoadStatus.ISSTARTING);
                        try {
                            startLoad();    
                        } catch (Throwable t) {
                            notifyLoadStatus(reportClient, LoadStatus.START_FAILED, new Date());
                            throw t;
                        }
                        
                        logger.info("[{}]: Load started", getLoadName());
                        distributedStatusMap.put(getLoadName(), FldLoadStatus.STARTED);
                        logger.info("[{}]: State stored in distributed status map: {}", getLoadName(), FldLoadStatus.STARTED);

                        notifyLoadStatus(reportClient, LoadStatus.STARTED, new Date());
                        break;
                    case DOSTOP:
                        logger.info("[{}]: Stopping load", getLoadName());
                        distributedStatusMap.put(getLoadName(), FldLoadStatus.ISSTOPPING);
                        try {
                            stopLoad();    
                        } catch (Throwable t) {
                            notifyLoadStatus(reportClient, LoadStatus.STOP_FAILED, new Date());
                            throw t;
                        }
                        
                        logger.info("[{}]: Stopped load", getLoadName());
                        distributedStatusMap.put(getLoadName(), FldLoadStatus.STOPPED);
                        logger.info("[{}]: State stored in distributed status map: {}", getLoadName(), FldLoadStatus.STOPPED);
                        
                        notifyLoadStatus(reportClient, LoadStatus.STOPPED, new Date());
                        break;
                    case DOSHUTDOWN:
                        try {
                            logger.info("[{}]: Stopping hazelcast client for load", getLoadName());
                            hc.shutdown();
                            notifyLoadStatus(reportClient, LoadStatus.SHUTDOWN, new Date());
                        } catch (Exception e) {
                            logger.error("Exception while shutting down the load '" + getLoadName() + "': ", e);
                            notifyLoadStatus(reportClient, LoadStatus.SHUTDOWN_FAILED, new Date());
                        }
                        testRunning = false;
                        break;
                    default:
                        break;
                }
            }
            
        }
        logger.info("[{}]: Ending load", getLoadName());
    }
   
    @Override
    public void entryAdded(EntryEvent<String, FldLoadStatus> event) {
        // we don't care what the other threads do
    }
    
    @Override
    public void entryUpdated(EntryEvent<String, FldLoadStatus> event) {
        logger.info("[{}]: New status in distributed map: {}, {}", event.getKey(), event.getMergingValue(), event.getValue());
        
        if (event.getKey().equals(getLoadName())) {
            logger.info("[{}]: New status in distributed map: {}, {}", getLoadName(), event.getMergingValue(), event.getValue());
        }
    }
    
    protected void notifyLoadStatus(FLDReportClient reportClient, LoadStatus status, Date timestamp) {
        try {
            logger.info("Notifying FLD controller of load named '{}' changed status to '{}' with time stamp = {}", 
                getLoadName(), status.name(), timestamp);
            JsonReportResult result = reportClient.notifyLoadStatus(getLoadName(), status, new Date());
            if (result == null) {
                logger.error("No response from reports REST controller!");
            } else if (result.isError()) {
                logger.error("Load status notification request failed: {}", result);
            }
        } catch (Throwable e) {
            logger.error("Failed to notify controller of a load status: ", e);
        }
    }
    
    protected IMap<String, FldLoadStatus> getDistributedStatusMap() {
        return distributedStatusMap;
    }
    
    protected abstract String getLoadName();
    
    /**
     * Starts the load.  Implementations must return quickly - if they will take a longer period, they should run 
     * asynchronously.  If an exception occurs, the test will be aborted
     */
    protected abstract void startLoad();
    
    /**
     * Stop the load.  Exceptions will be logged, but otherwise ignored.
     */
    protected abstract void stopLoad();

}
