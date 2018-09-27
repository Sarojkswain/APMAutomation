/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author keyja01
 * 
 */
public class MarkerFile {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerFile.class);
        
    private FldLoadStatus status;
    private FldLoadStatus oldStatus;
    private String name;
    private File markerFileDir;
    private static final Map<FldLoadStatus, Set<FldLoadStatus>> transitionMap;

    /**
     * 
     */
    protected MarkerFile(String name, File markerFileDir) {
        this.name = name;
        status = FldLoadStatus.NEW;
        this.markerFileDir = markerFileDir;
    }
    
    /**
     * Transition to a new state.
     * @param newStatus
     */
    public void transition(FldLoadStatus newStatus) throws TransitionException {
        if (status == newStatus) {
            LOGGER.info("No transition for load='{}' needed, already at state={}", name, newStatus);
            return;
        }
        
        Set<FldLoadStatus> allowed = transitionMap.get(status);
        LOGGER.info("Allowed transition statuses for status={}: {}", status, allowed);
        
        if (!allowed.contains(newStatus)) {
            throw new TransitionException("Cannot transition '" + name + "' from " + status + " to " + newStatus);
        }
        
        try {
            // delete old file(s)
            deleteOldFiles();
            
            // create new file
            writeMarkerFile(newStatus);
        } catch (Exception e) {
            throw new TransitionException("Cannot transition '" + name + "' from " + status + " to " + newStatus + ": " + e.getMessage());
        }
        
        oldStatus = status;
        status = newStatus;
    }
    
    private void writeMarkerFile(FldLoadStatus loadStatus) throws Exception {
        FileOutputStream out = new FileOutputStream(new File(markerFileDir, filename(loadStatus)));
        String msg = name + ": " + loadStatus;
        out.write(msg.getBytes());
        out.flush();
        out.close();
    }
    
    private void deleteOldFiles() {
        FilenameFilter filter = new RegexFileFilter(name + "\\..*");
        String[] list = markerFileDir.list(filter);
        for (String fname: list) {
            File f = new File(markerFileDir, fname);
            if (f.exists()) {
                f.delete();
            }
        }
    }
    
    /**
     * Checks if the marker file on disk has been deleted or otherwise changed, and 
     * returns the new status.  If unchanged, returns null.
     * @return
     */
    public FldLoadStatus checkStatus() throws TransitionException {
        File file = new File(markerFileDir, filename());
        
        switch (status) {
            case FAILEDSTOP:
            case STARTED:
                File doStop = new File(markerFileDir, filename(FldLoadStatus.DOSTOP));
                if (!file.exists() || doStop.exists()) {
                    transition(FldLoadStatus.DOSTOP);
                    return status;
                }
                break;
            case FAILEDSTART:
            case STOPPED:
                File doStart = new File(markerFileDir, filename(FldLoadStatus.DOSTART));
                if (doStart.exists()) {
                    transition(FldLoadStatus.DOSTART);
                    return status;
                }
                break;
            case DOSTART:
            case DOSTOP:
            case ISSHUTTINGDOWN:
            case ISSTARTING:
            case ISSTOPPING:
            case DOSHUTDOWN:
                if (!file.exists()) {
                    try {
                        writeMarkerFile(status);
                    } catch (Exception e) {
                        // TODO log me
                    }
                }
                if (oldStatus != null && oldStatus != status) {
                    oldStatus = status;
                    return status;
                }
                break;
            default:
                break;
        }
        
        return null;
    }

    public FldLoadStatus getStautus() {
        return status;
    }

    public String getName() {
        return name;
    }
    
    private String filename() {
        return filename(status);
    }
    
    private String filename(HasSuffix sfx) {
        return name + sfx.getEffectiveSuffix();
    }

    public static void main(String[] args) throws Exception {
        MarkerFile mf = new MarkerFile("foobulous", new File("c:\\foo\\markers"));
        mf.transition(FldLoadStatus.STARTED);
        FldLoadStatus status = mf.checkStatus();
        System.out.println("Status: " + status);
        System.out.println("done");
    }
    
    static {
        transitionMap = new HashMap<>();
        transitionMap.put(FldLoadStatus.NEW, new HashSet<>(Arrays.asList(FldLoadStatus.STARTED, FldLoadStatus.STOPPED, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.STARTED, new HashSet<>(Arrays.asList(FldLoadStatus.DOSTOP, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.DOSTOP, new HashSet<>(Arrays.asList(FldLoadStatus.ISSTOPPING, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.ISSTOPPING, new HashSet<>(Arrays.asList(FldLoadStatus.STOPPED, FldLoadStatus.DOSHUTDOWN, FldLoadStatus.FAILEDSTOP)));
        transitionMap.put(FldLoadStatus.STOPPED, new HashSet<>(Arrays.asList(FldLoadStatus.DOSTART, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.DOSTART, new HashSet<>(Arrays.asList(FldLoadStatus.ISSTARTING, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.ISSTARTING, new HashSet<>(Arrays.asList(FldLoadStatus.STARTED, FldLoadStatus.DOSHUTDOWN, FldLoadStatus.FAILEDSTART)));
        transitionMap.put(FldLoadStatus.ISSTOPPING, new HashSet<>(Arrays.asList(FldLoadStatus.STOPPED, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.FAILEDSTART, new HashSet<>(Arrays.asList(FldLoadStatus.ISSTARTING, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.FAILEDSTOP, new HashSet<>(Arrays.asList(FldLoadStatus.ISSTOPPING, FldLoadStatus.DOSHUTDOWN)));
        transitionMap.put(FldLoadStatus.DOSHUTDOWN, Collections.<FldLoadStatus>emptySet());
    }
}
