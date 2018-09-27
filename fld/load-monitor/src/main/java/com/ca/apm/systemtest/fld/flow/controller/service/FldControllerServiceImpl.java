/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.flow.controller.FldLoadStatus;
import com.ca.apm.systemtest.fld.flow.controller.HazelcastMapListener;
import com.ca.apm.systemtest.fld.flow.controller.LoadEventListener;
import com.ca.apm.systemtest.fld.flow.controller.MarkerFile;
import com.ca.apm.systemtest.fld.flow.controller.MarkerFileMonitor;
import com.ca.apm.systemtest.fld.flow.controller.TransitionException;
import com.ca.apm.systemtest.fld.flow.controller.vo.LoadStatusVO;
import com.ca.apm.systemtest.fld.flow.controller.vo.LoadsStatusesVO;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author keyja01
 *
 */
@Component("fldControllerServiceBean")
public class FldControllerServiceImpl implements FldControllerService, LoadEventListener, InitializingBean, HazelcastMapListener<String, FldLoadStatus> {
    private static final Logger logger = LoggerFactory.getLogger(FldControllerServiceImpl.class);
    private final Object lock = new Object();
    @Autowired
    private MarkerFileMonitor monitor;
    private Set<String> registeredLoads = new HashSet<>();
    private Thread monitorThread;
    @Autowired
    private HazelcastInstance hc;
    private IMap<String, FldLoadStatus> statusMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        statusMap = hc.getMap("fld");
        statusMap.addEntryListener( this, true);
        monitorThread = new Thread(monitor);
        monitorThread.start();
    }

    @Override
    public void entryAdded(EntryEvent<String, FldLoadStatus> event) {
        synchronized (lock) {
            String loadId = event.getKey();
            if (!registeredLoads.contains(loadId)) {
                try {
                    monitor.registerLoad(loadId, event.getValue());
                    monitor.registerListener(loadId, this);
                    registeredLoads.add(loadId);
                } catch (TransitionException e) {
                    logger.warn("Unable to register new load: " + loadId + ", " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void entryUpdated(EntryEvent<String, FldLoadStatus> event) {
        try {
            FldLoadStatus value = event.getValue();
            monitor.transition(event.getKey(), value);
        } catch (TransitionException e) {
            logger.warn("Unable to process change in load status: " + event.getKey()  + " to " + event.getValue() + ", " + e.getMessage());
        }
    }

    @Override
    public void onLoadEvent(String loadId, FldLoadStatus status) {
        synchronized (lock) {
            if (status == FldLoadStatus.DOSTOP && loadId.equals("FLD")) {
                logger.info("Going to send shutdown signal to all loads");
                for (String load: registeredLoads) {
                    try {
                        monitor.transition(load, FldLoadStatus.DOSHUTDOWN);
                        statusMap.put(load, FldLoadStatus.DOSHUTDOWN);
                        // TODO - fly to the moon and start a thread to log whether the load was successfully shut down or not
                    } catch (Exception e) {
                        logger.warn("Unable to send shutdown to load '" + load + "', error message: " + e.getMessage());
                    }
                }
            } else {
                statusMap.put(loadId, status);
            }
        }
    }

    @Override
    public LoadsStatusesVO getLoadStatuses() {
        Map<String, MarkerFile> statusMap;
        synchronized (lock) {
            statusMap = monitor.getLoadStatuses();
        }

        List<LoadStatusVO> statuses = new ArrayList<>(statusMap.size());
        for (Map.Entry<String, MarkerFile> entry : statusMap.entrySet()) {
            statuses.add(new LoadStatusVO(entry.getKey(), entry.getValue().getStautus()));
        }

        return new LoadsStatusesVO(statuses);
    }

    @Override
    public void startLoad(String loadId) {
        synchronized (lock) {
            try {
                monitor.transition(loadId, FldLoadStatus.DOSTART);
            } catch (TransitionException e) {
                String errMsg = "Unable to start load '" + loadId + "': "; 
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg + e.getMessage(), e);
            }
        }
    }

    @Override
    public void forceStartLoad(String loadId) {
        synchronized (lock) {
            try {
                monitor.transition(loadId, FldLoadStatus.DOSTART);
            } catch (TransitionException e) {
                String errMsg = "Unable to force-start load '" + loadId + "': ";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg + e.getMessage(), e);
            }
        }
    }

    @Override
    public void stopLoad(String loadId) {
        synchronized (lock) {
            try {
                monitor.transition(loadId, FldLoadStatus.DOSTOP);
            } catch (TransitionException e) {
                String errMsg = "Unable to stop load '" + loadId + "': ";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg + e.getMessage(), e);
            }
        }
    }

    @Override
    public void forceStopLoad(String loadId) {
        synchronized (lock) {
            try {
                monitor.transition(loadId, FldLoadStatus.DOSTOP);
            } catch (TransitionException e) {
                String errMsg = "Unable to force-stop load '" + loadId + "': ";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg + e.getMessage(), e);
            }
        }
    }
}
