/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * Manages the marker files used to represent the status of loads.
 * 
 * @author bocto01
 * @author keyja01
 *
 */
public class MarkerFileMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerFileMonitor.class);
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
    private volatile boolean continueTest = true;
    private File markerFilesDir;
    private Map<String, MarkerFile> statusMap = new HashMap<>(10);
    private Map<String, List<LoadEventListener>> listeners = new HashMap<>(10);
    private long markerFilesMonitorSleepTime = 5000L;
    private final Object lock = new Object();
    
    public MarkerFileMonitor(String markerFilesDirName, boolean emptyMarkerDirOnStart) {
        this.markerFilesDir = new File(markerFilesDirName);
        checkDir(markerFilesDirName);
    }

    
    public void registerListener(String loadId, LoadEventListener listener) {
        synchronized (lock) {
            List<LoadEventListener> list = listeners.get(loadId);
            if (list == null) {
                LOGGER.warn("Unknown loadId '{}', not registering LoadEventListener", loadId);
                return;
            }
            list.add(listener);
        }
    }
    
    
    /**
     * @param loadId load ID
     */
    public void registerLoad(String loadId, FldLoadStatus status) throws TransitionException {
        synchronized (lock) {
            if (statusMap.containsKey(loadId)) {
                LOGGER.warn("Load ID '{}' is already registered", loadId);
                return;
            }
            MarkerFile mf = new MarkerFile(loadId, this.markerFilesDir);
            mf.transition(status);
            statusMap.put(loadId, mf);
            listeners.put(loadId, new ArrayList<LoadEventListener>(1));
        }
    }
    
    
    public void transition(String loadId, FldLoadStatus newStatus) throws TransitionException {
        MarkerFile mf = statusMap.get(loadId);
        if (mf == null) {
            throw new TransitionException("Unknown load '" + loadId + "'");
        }
        mf.transition(newStatus);
    }
    

    @Override
    public void run() {
        LOGGER.debug("MarkerFilesMonitor.run():: start");
        while (continueTest) {
            synchronized (lock) {
                for (Entry<String, MarkerFile> entry : statusMap.entrySet()) {
                    try {
                        MarkerFile mf = entry.getValue();
                        FldLoadStatus newStatus = mf.checkStatus();
                        if (newStatus != null) {
                            notifyListeners(entry.getKey(), newStatus);
                        }
                    } catch (TransitionException e) {
                        LOGGER.warn("An exception occured while checking marker file status for '{}': {}", 
                            entry.getKey(), e.getMessage());
                    }
                }
            }
            
            try {
                sleep();
            } catch (Throwable e) {
                LOGGER.error("Exception occurred: ", e);
            }
        }
        LOGGER.debug("MarkerFilesMonitor.run():: exit");
    }
    
    
    
    private void notifyListeners(final String loadId, final FldLoadStatus status) {
        List<LoadEventListener> list = listeners.get(loadId);
        if (list == null) {
            return;
        }
        for (LoadEventListener listener: list) {
            final LoadEventListener l = listener;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        l.onLoadEvent(loadId, status);
                    } catch (Exception e) {
                        LOGGER.warn("Unable to send load event {} to listener", status, e);
                    }
                }
            }).start();
        }
    }



    private void sleep() {
        if (continueTest) {
            try {
                LOGGER.trace("MarkerFilesMonitor.sleep():: sleeping for {} [s]",
                    (markerFilesMonitorSleepTime  / 1000));
                Thread.sleep(markerFilesMonitorSleepTime);
            } catch (InterruptedException e) {
                LOGGER.trace("MarkerFilesMonitor.sleep():: InterruptedException");
            }
        }
    }

    private static boolean checkDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean dirCreated = dir.mkdirs();
            if (dirCreated) {
                LOGGER.info("checkDir():: directory was created: {}", dirName);
                return true;
            } else {
                throw ErrorUtils.logErrorAndThrowException(LOGGER,
                    "checkDir() - directory WAS NOT created: {}", dirName);
            }
        }
        return false;
    }

    public static void shortWait(long ms) {
        try {
            TimeUnit.MILLISECONDS.wait(ms);
        } catch (InterruptedException e) {
            LOGGER.error("shortWait({}) got interrupted", ms, e);
        }
    }

    /**
     * Get statuses of all loads.
     * @return map of loads to their statuses
     */
    public Map<String, MarkerFile> getLoadStatuses() {
        synchronized (lock) {
            return new TreeMap<>(statusMap);
        }
    }
}
