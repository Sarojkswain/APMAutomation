package com.ca.apm.systemtest.fld.test.loads;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.BASE_DIR;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getAbsolutePath;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

@Deprecated
public class FldLoadRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FldLoadRunner.class);

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
    public static final String DEFAULT_MARKER_FILES_DIR_NAME = "fld-control";
    public static final String DEFAULT_FLD_LOAD_MARKER_FILE_NAME = "fld";

    public static final String AUDIT_LOGGING_FLD_STARTED = "FLD_STARTED";
    public static final String AUDIT_LOGGING_FLD_STOPPED = "FLD_STOPPED";
    public static final String AUDIT_LOGGING_FLD_LOAD_STARTING = "FLD_LOAD_STARTING";
    public static final String AUDIT_LOGGING_FLD_LOAD_STARTED = "FLD_LOAD_STARTED";
    public static final String AUDIT_LOGGING_FLD_LOAD_START_FAILED = "FLD_LOAD_START_FAILED";
    public static final String AUDIT_LOGGING_FLD_LOAD_STOPPING = "FLD_LOAD_STOPPING";
    public static final String AUDIT_LOGGING_FLD_LOAD_STOPPED = "FLD_LOAD_STOPPED";
    public static final String AUDIT_LOGGING_FLD_LOAD_STOP_FAILED = "FLD_LOAD_STOP_FAILED";

    // case sensitivity of marker file names and suffixes
    private static boolean caseSensitive = false;

    // marker files working directory
    private String markerFilesDirName = getAbsolutePath(BASE_DIR, DEFAULT_MARKER_FILES_DIR_NAME);

    private String fldLoadMarkerFileName = DEFAULT_FLD_LOAD_MARKER_FILE_NAME;

    private long markerFilesMonitorSleepTime = 5000L; // [ms]

    // specifies whether to start all FLD loads immediately after start (otherwise they should be
    // started explicitly by creating a fldLoadName.dostart marker file)
    private boolean startAllFldLoadsOnStart = false;

    // date format used for a date written into newly created marker files
    private String dateFormat = DEFAULT_DATE_FORMAT;

    private boolean cleanUnknownFiles = true;

    // specifies whether to call runSerializedCommandFlowFromRole() to start/stop
    // FLD load in a separate thread
    private boolean asyncFldLoadExec = true;

    // matters if asyncFldLoadExec is true
    private boolean wait4asyncFldLoadExec = false;

    // matters if asyncFldLoadExec and wait4asyncFldLoadExec are true
    private long fldLoadExecTimeout = 120000L; // [ms]

    private ExecutorService executor;
    private SortedMap<String, FldLoadInfo> fldLoads = new TreeMap<>();
    private volatile boolean continueTest = true;
    private boolean initialized = false;
    private boolean inRun = false;
    private RunCommandFlow runCommandFlow;



    public FldLoadRunner(RunCommandFlow runCommandFlow) {
        Args.notNull(runCommandFlow, "runCommandFlow");
        this.runCommandFlow = runCommandFlow;
    }

    // init method to be run before performFldLoad()
    public void init() {
        LOGGER.info("init():: entry");
        try {
            if (initialized) {
                LOGGER.info("init():: already initialized");
                return;
            }
            continueTest = true;
            checkMarkerFilesDir();
            cleanMarkerFilesDir();
            executor = Executors.newCachedThreadPool();
            fldLoads.clear();
            initialized = true;
        } finally {
            LOGGER.info("init():: exit");
        }
    }

    public void shutDown() {
        shutDown(false);
    }

    // cleanup method to be run after performFldLoad()
    public void shutDown(boolean forceShutdownNow) {
        LOGGER.info("shutDown():: entry");
        try {
            continueTest = false;
            stopAllFldLoads();
            if (!inRun) {
                auditLog(AUDIT_LOGGING_FLD_STOPPED);
            }
            if (executor != null) {
                if (forceShutdownNow) {
                    LOGGER.info("shutDown():: executor.shutdownNow()");
                    executor.shutdownNow();
                } else {
                    LOGGER.info("shutDown():: executor.shutdown()");
                    executor.shutdown();
                }
            }
            executor = null;
            fldLoads.clear();
            cleanMarkerFilesDir();
            createFileFldLoadStopped();
            initialized = false;
            inRun = false;
        } finally {
            LOGGER.info("shutDown():: exit");
        }
    }

    public void registerFldLoad(String roleId, String startFlowKey, String stopFlowKey) {
        FldLoadInfo fldLoad = new FldLoadInfo(roleId, startFlowKey, stopFlowKey);
        String fldLoadName = getFldLoadName(fldLoad);
        fldLoads.put(fldLoadName, fldLoad);
        if (!caseSensitive) {
            fldLoads.put(fldLoadName.toLowerCase(), fldLoad);
        }
    }

    public void performFldLoad() {
        LOGGER.info("performFldLoad():: entry");
        try {
            if (inRun) {
                throw ErrorUtils.logErrorAndThrowException(LOGGER,
                    "performFldLoad() - already running");
            }
            inRun = true;
            if (!initialized) {
                init();
            }
            if (startAllFldLoadsOnStart) {
                startAllFldLoads();
            } else {
                initStoppedStatus();
            }
            createFileFldLoadStarted();
            executor.submit(new MarkerFilesMonitor());
            auditLog(AUDIT_LOGGING_FLD_STARTED);
        } finally {
            LOGGER.info("performFldLoad():: exit");
        }
    }

    public boolean isRunning() {
        return continueTest;
    }



    private void startAllFldLoads() {
        LOGGER.info("startAllFldLoads():: entry");
        LOGGER.debug("startAllFldLoads():: fldLoads.size() = {}", fldLoads.size());
        for (FldLoadInfo fldLoad : fldLoads.values()) {
            startFldLoad(fldLoad);
        }
        LOGGER.info("startAllFldLoads():: exit");
    }

    private void initStoppedStatus() {
        for (FldLoadInfo fldLoad : fldLoads.values()) {
            syncMarkerFiles(fldLoad);
        }
    }

    private int stopAllFldLoads() {
        LOGGER.info("stopAllFldLoads():: entry");
        LOGGER.debug("stopAllFldLoads():: fldLoads.size() = {}", fldLoads.size());
        int count = 0;
        for (FldLoadInfo fldLoad : fldLoads.values()) {
            if (!FldLoadStatus.isOfStopType(fldLoad.status)) {
                boolean stopped = stopFldLoad(fldLoad);
                if (stopped) {
                    count++;
                }
            }
        }
        LOGGER.info("stopAllFldLoads():: exit");
        return count;
    }

    private boolean startFldLoad(final FldLoadInfo fldLoad) {
        if (asyncFldLoadExec) {
            Future<Boolean> future;
            try {
                future = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return startFldLoadInternal(fldLoad);
                    }
                });
            } catch (RejectedExecutionException e) {
                LOGGER
                    .warn("startFldLoad():: unable to execute operation runFldLoadFlow() in another thread");
                return startFldLoadInternal(fldLoad);
            }
            if (wait4asyncFldLoadExec) {
                try {
                    return future.get(fldLoadExecTimeout, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    ErrorUtils.logExceptionFmt(LOGGER, e,
                        "startFldLoad() - cannot execute runFldLoadFlow()");
                    future.cancel(true);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return startFldLoadInternal(fldLoad);
        }
    }

    private boolean startFldLoadInternal(FldLoadInfo fldLoad) {
        auditLog(AUDIT_LOGGING_FLD_LOAD_STARTING, fldLoad);
        updateStatus(fldLoad, FldLoadStatus.ISSTARTING);
        String fldLoadName = getFldLoadName(fldLoad);
        boolean success = runFldLoadFlow(fldLoadName, fldLoad.startFlowKey);
        if (success) {
            auditLog(AUDIT_LOGGING_FLD_LOAD_STARTED, fldLoad);
            updateStatus(fldLoad, FldLoadStatus.STARTED);
            return true;
        } else {
            auditLog(AUDIT_LOGGING_FLD_LOAD_START_FAILED, fldLoad);
            updateStatus(fldLoad, FldLoadStatus.FAILEDSTART);
            return false;
        }
    }

    private boolean stopFldLoad(final FldLoadInfo fldLoad) {
        if (asyncFldLoadExec) {
            Future<Boolean> future;
            try {
                future = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return stopFldLoadInternal(fldLoad);
                    }
                });
            } catch (RejectedExecutionException e) {
                LOGGER
                    .warn("stopFldLoad():: unable to execute operation runFldLoadFlow() in another thread");
                return stopFldLoadInternal(fldLoad);
            }
            if (wait4asyncFldLoadExec) {
                try {
                    return future.get(fldLoadExecTimeout, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    ErrorUtils.logExceptionFmt(LOGGER, e,
                        "stopFldLoad() - cannot execute runFldLoadFlow()");
                    future.cancel(true);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return stopFldLoadInternal(fldLoad);
        }
    }

    private boolean stopFldLoadInternal(FldLoadInfo fldLoad) {
        auditLog(AUDIT_LOGGING_FLD_LOAD_STOPPING, fldLoad);
        updateStatus(fldLoad, FldLoadStatus.ISSTOPPING);
        String fldLoadName = getFldLoadName(fldLoad);
        boolean success = runFldLoadFlow(fldLoadName, fldLoad.stopFlowKey);
        if (success) {
            auditLog(AUDIT_LOGGING_FLD_LOAD_STOPPED, fldLoad);
            updateStatus(fldLoad, FldLoadStatus.STOPPED);
            return true;
        } else {
            auditLog(AUDIT_LOGGING_FLD_LOAD_STOP_FAILED, fldLoad);
            updateStatus(fldLoad, FldLoadStatus.FAILEDSTOP);
            return false;
        }
    }

    private boolean runFldLoadFlow(String roleId, String envPropKey) {
        LOGGER.debug("runFldLoadFlow():: running runSerializedCommandFlowFromRole({}, {})", roleId,
            envPropKey);
        try {
            runCommandFlow.runCommandFlow(roleId, envPropKey);
            LOGGER.debug("runFldLoadFlow():: done runSerializedCommandFlowFromRole({}, {})",
                roleId, envPropKey);
            return true;
        } catch (Throwable e) {
            LOGGER.error("runFldLoadFlow():: failed runSerializedCommandFlowFromRole({}, {})",
                roleId, envPropKey);
            ErrorUtils.logExceptionFmt(LOGGER, e,
                "runFldLoadFlow() - cannot execute runFldLoadFlow: {0}");
            return false;
        }
    }

    private int cleanMarkerFilesDir() {
        return cleanMarkerFiles(null, false, false);
    }

    private int cleanStatusMarkerFiles(String fldLoadName) {
        return cleanMarkerFiles(fldLoadName, true, false);
    }

    private int cleanMarkerFiles(String fldLoadName, boolean onlyStatusMFs, boolean onlyCommandMFs) {
        File[] files = listMarkerFiles(fldLoadName, onlyStatusMFs, onlyCommandMFs);
        for (File file : files) {
            // delete MF
            boolean deleted = deleteFile(file);
            if (!deleted) {
                throw ErrorUtils.logErrorAndThrowException(LOGGER,
                    "cleanAllMarkerFiles() - file WAS NOT deleted: {}", file);
            }
        }
        return files.length;
    }

    private File createFileFldLoadStarted() {
        return writeMarkerFile(fldLoadMarkerFileName, FldLoadStatus.STARTED);
    }

    private File createFileFldLoadStopped() {
        return writeMarkerFile(fldLoadMarkerFileName, FldLoadStatus.STOPPED);
    }

    private void syncMarkerFiles(FldLoadInfo fldLoad) {
        updateStatus(fldLoad, fldLoad.status, fldLoad.lastChange);
    }

    private void updateStatus(FldLoadInfo fldLoad, FldLoadStatus newStatus) {
        updateStatus(fldLoad, newStatus, -1);
    }

    private void updateStatus(FldLoadInfo fldLoad, FldLoadStatus newStatus, long timestamp) {
        String fldLoadName = getFldLoadName(fldLoad);
        cleanStatusMarkerFiles(fldLoadName);
        if (timestamp < 1) {
            timestamp = System.currentTimeMillis();
            fldLoad.lastChange = timestamp;
        }
        writeMarkerFile(fldLoadName, newStatus, timestamp);
        fldLoad.status = newStatus;
    }

    private void processMarkerFiles(Map<String, List<String>> markerFiles) {
        LOGGER.info("processMarkerFiles():: entry");
        try {

            // 1) check presence of fld.started and fld.dostop (or fld.forcedostop)
            boolean isFldStartedMFPresent = false;
            boolean isFldDoStopMFPresent = false;
            List<String> fldMarkerFiles = markerFiles.remove(fldLoadMarkerFileName);
            if (fldMarkerFiles != null) {
                for (String fileName : fldMarkerFiles) {
                    if (FldLoadStatus.resolveBySuffix(fileName) == FldLoadStatus.STARTED) {
                        // fld.started exists
                        isFldStartedMFPresent = true;
                    } else if (FldLoadCommand.isDoStopCommand(fileName)) {
                        // fld.dostop detected
                        isFldDoStopMFPresent = true;
                        break;
                    } else {
                        // another MF like fld.xxxx which should be deleted
                        deleteFile(fileName);
                    }
                }
            }
            if (!isFldStartedMFPresent || isFldDoStopMFPresent) {
                // fld.started is missing or fld.dostop is present
                LOGGER.info("processMarkerFiles():: FLD stop request detected");
                shutDown();
                return;
            }

            // 2) process the other MFs
            Set<String> registeredFldLoadNamesCheckSet = new HashSet<>(fldLoads.keySet());
            for (String markerFile : markerFiles.keySet()) {
                List<String> fldLoadMarkerFiles = markerFiles.get(markerFile);

                // 2.1) handle unknown file
                FldLoadInfo fldLoad = fldLoads.get(markerFile);
                if (fldLoad == null) {
                    LOGGER.warn("processMarkerFiles():: unknown FLD load: {}", markerFile);
                    if (cleanUnknownFiles) {
                        deleteFiles(fldLoadMarkerFiles);
                    }
                    continue;
                }

                String fldLoadName = getFldLoadName(fldLoad);
                registeredFldLoadNamesCheckSet.remove(fldLoadName);
                if (!caseSensitive) {
                    registeredFldLoadNamesCheckSet.remove(fldLoadName.toLowerCase());
                }
                if (fldLoadMarkerFiles != null && !fldLoadMarkerFiles.isEmpty()) {
                    deleteCommandMarkerFiles(fldLoadMarkerFiles);

                    // 2.2) check for the newest force command {forcestart|forcestop} MF,
                    // files are already sorted by a modification time
                    String newestForceCommand = getFirstForceCommand(fldLoadMarkerFiles);
                    if (newestForceCommand != null) {
                        LOGGER.info(
                            "processMarkerFiles():: detected force request for FLD load {}",
                            newestForceCommand);
                        switch (FldLoadCommand.resolveBySuffix(newestForceCommand)) {
                            case FORCEDOSTART: {
                                startFldLoad(fldLoad);
                                break;
                            }
                            case FORCEDOSTOP: {
                                stopFldLoad(fldLoad);
                                break;
                            }
                            default: {}
                        }
                        continue;
                    }

                    // 2.3) check for the newest command {dostart|dostop}
                    String newestExecCommand = getFirstExecCommand(fldLoadMarkerFiles);
                    if (newestExecCommand != null) {
                        LOGGER.info("processMarkerFiles():: detected request for FLD load {}",
                            newestExecCommand);
                        switch (FldLoadCommand.resolveBySuffix(newestExecCommand)) {
                            case DOSTART: {
                                // seems to be running - inner status is one of
                                // {started|isstarting|failedstop}
                                if (FldLoadStatus.isOfStartType(fldLoad.status)) {
                                    LOGGER
                                        .info(
                                            "processMarkerFiles():: FLD load {} seems to be running, skipping request. Try {} if you know what you're doing",
                                            markerFile,
                                            composeMarkerFileName(markerFile,
                                                FldLoadCommand.FORCEDOSTART.suffix));
                                } else {
                                    startFldLoad(fldLoad);
                                }
                                break;
                            }
                            case DOSTOP: {
                                // seems to be stopped - inner status is one of
                                // {stopped|isstopping|failedstart}
                                if (FldLoadStatus.isOfStopType(fldLoad.status)) {
                                    LOGGER
                                        .info(
                                            "processMarkerFiles():: FLD load {} seems to be not running, skipping request. Try {} if you know what you're doing",
                                            markerFile,
                                            composeMarkerFileName(markerFile,
                                                FldLoadCommand.FORCEDOSTOP.suffix));
                                } else {
                                    stopFldLoad(fldLoad);
                                }
                                break;
                            }
                            default: {}
                        }
                        continue;
                    }

                    // 2.4) synchronize internal state with existing status MFs
                    // (create new/leave current + delete the others)
                    // files are already sorted by a modification time
                    String newestStatusMF = fldLoadMarkerFiles.get(0);
                    FldLoadStatus mfStatus = FldLoadStatus.resolveBySuffix(newestStatusMF);
                    if (fldLoad.status == mfStatus) {
                        List<String> statusMFs2Delete =
                            fldLoadMarkerFiles.subList(1, fldLoadMarkerFiles.size());
                        if (!statusMFs2Delete.isEmpty()) {
                            LOGGER.info("processMarkerFiles():: deleting older MFs: {}",
                                statusMFs2Delete);
                            // delete older status MFs
                            deleteFiles(statusMFs2Delete);
                        }
                    } else {
                        // inconsistence between inner state and MF
                        List<String> statusMFs2Delete =
                            filterOutStatus(fldLoadMarkerFiles, fldLoad.status);
                        if (statusMFs2Delete == null) {
                            // stop FLD load request
                            // (current status {started|isstarting|failedstop} MF was deleted)
                            if (FldLoadStatus.isOfStartType(fldLoad.status)) {
                                LOGGER
                                    .info(
                                        "processMarkerFiles():: status marker file {started|isstarting|failedstop} was externally deleted - request to stop FLD load {}",
                                        fldLoad);
                                stopFldLoad(fldLoad);
                            } else {
                                LOGGER
                                    .info(
                                        "processMarkerFiles():: syncing status marker files for FLD load {}",
                                        fldLoad);
                                syncMarkerFiles(fldLoad);
                            }
                        } else {
                            LOGGER
                                .info(
                                    "processMarkerFiles():: cleaning status marker files for FLD load {}",
                                    statusMFs2Delete);
                            deleteFiles(statusMFs2Delete);
                        }
                    }

                }
            }

            // 3) missing status MF according to internal state
            for (String fldLoadName : registeredFldLoadNamesCheckSet) {
                FldLoadInfo fldLoad = fldLoads.get(fldLoadName);
                if (fldLoad != null) {
                    // stop FLD load request
                    // (current status {started|isstarting|failedstop} MF was deleted)
                    if (FldLoadStatus.isOfStartType(fldLoad.status)) {
                        LOGGER
                            .info(
                                "processMarkerFiles():: status marker file {started|isstarting|failedstop} was externally deleted - request to stop FLD load {}",
                                fldLoad);
                        stopFldLoad(fldLoad);
                    } else {
                        LOGGER
                            .info(
                                "processMarkerFiles():: missing status marker file - syncing for FLD load {}",
                                fldLoad);
                        syncMarkerFiles(fldLoad);
                    }
                }
            }

        } finally {
            LOGGER.info("processMarkerFiles():: exit");
        }
    }

    private File[] listAllMarkerFiles() {
        return listMarkerFiles(false, false);
    }

    private File[] listMarkerFiles(boolean selectStatusMFs, boolean selectCommandMFs) {
        return listMarkerFiles(null, selectStatusMFs, selectCommandMFs);
    }

    private File[] listMarkerFiles(String baseName, boolean selectStatusMFs,
        boolean selectCommandMFs) {
        return listFiles(markerFilesDirName, new MarkerFileFilter(baseName, selectStatusMFs,
            selectCommandMFs));
    }

    private File writeMarkerFile(String baseName, FldLoadStatus status) {
        return writeMarkerFile(baseName, status, -1);
    }

    // creates MF and writes current time into it
    private File writeMarkerFile(String baseName, FldLoadStatus status, long timestamp) {
        String fileName = composeMarkerFileName(baseName, status.suffix);
        return writeFile(
            markerFilesDirName,
            fileName,
            new ArrayList<>(Collections.singleton((new SimpleDateFormat(dateFormat))
                .format(timestamp > 0 ? new Date(timestamp) : new Date()))));
    }

    private void checkMarkerFilesDir() {
        checkDir(markerFilesDirName);
    }

    private int deleteCommandMarkerFiles(List<String> fileNames) {
        return deleteFiles(getCommandMarkerFiles(fileNames));
    }

    private int deleteFiles(List<String> fileNames) {
        int count = 0;
        if (fileNames != null) {
            for (String fileName : fileNames) {
                boolean deleted = deleteFile(fileName);
                if (deleted) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean deleteFile(String fileName) {
        File file = getMarkerFileFile(fileName);
        return deleteFile(file);
    }

    private File getMarkerFileFile(String fileName) {
        return new File(getAbsolutePath(markerFilesDirName, fileName));
    }

    private static String getFldLoadName(FldLoadInfo fldLoad) {
        return fldLoad.roleId;
    }

    private static String getBaseName(String fileName) {
        String baseName = FldLoadCommand.getBaseName(fileName);
        if (baseName == null) {
            baseName = FldLoadStatus.getBaseName(fileName);
        }
        return baseName;
    }

    private static boolean isMarkerFile(String fileName) {
        return FldLoadCommand.isMarkerFile(fileName) || FldLoadStatus.isMarkerFile(fileName);
    }

    private static String composeMarkerFileName(String baseName, String suffix) {
        return (new StringBuilder(baseName)).append(effectiveSuffix(suffix)).toString();
    }

    private static String effectiveSuffix(String suffix) {
        return (new StringBuilder()).append('.').append(suffix).toString();
    }

    private static Map<String, List<String>> groupMarkerFiles(File[] files) {
        Map<String, List<String>> markerFiles = new HashMap<>();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (isMarkerFile(fileName)) {
                    String markerFileName = getBaseName(fileName);
                    if (markerFiles.containsKey(markerFileName)
                        && markerFiles.get(markerFileName) != null) {
                        markerFiles.get(markerFileName).add(fileName);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(fileName);
                        markerFiles.put(markerFileName, list);
                    }
                } else {
                    // not a MF
                    LOGGER.warn(
                        "groupMarkerFiles():: skipping unrecognized file - not a marker file: {}",
                        file);
                }
            }
        }
        return markerFiles;
    }

    private static String getFirstForceCommand(List<String> fileNames) {
        if (fileNames == null) {
            return null;
        }
        for (String fileName : fileNames) {
            if (FldLoadCommand.isForceCommand(fileName)) {
                return fileName;
            }
            if (FldLoadCommand.isDoStartCommand(fileName)
                || FldLoadCommand.isDoStopCommand(fileName)) {
                // there is a newer non-force command MF
                return null;
            }
        }
        return null;
    }

    private static String getFirstExecCommand(List<String> fileNames) {
        if (fileNames == null) {
            return null;
        }
        for (String fileName : fileNames) {
            if (FldLoadCommand.isDoStartCommand(fileName)
                || FldLoadCommand.isDoStopCommand(fileName)) {
                return fileName;
            }
        }
        return null;
    }

    private static List<String> getCommandMarkerFiles(List<String> fileNames) {
        List<String> commands = new ArrayList<>();
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (FldLoadCommand.isMarkerFile(fileName)) {
                    commands.add(fileName);
                }
            }
        }
        return commands;
    }

    private static List<String> filterOutStatus(List<String> fileNames, FldLoadStatus status) {
        if (fileNames == null || status == null) {
            return null;
        }
        List<String> filtered = new ArrayList<>(fileNames.size());
        boolean found = false;
        for (String fileName : fileNames) {
            if (status == FldLoadStatus.resolveBySuffix(fileName)) {
                found = true;
                continue;
            } else {
                filtered.add(fileName);
            }
        }
        if (found) {
            return filtered;
        } else {
            return null;
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

    private static File[] listFiles(String dirName, FileFilter fileFilter) {
        File directory = new File(dirName);
        File[] files =
            directory.listFiles(fileFilter == null
                ? ((FileFilter) FileFileFilter.FILE)
                : fileFilter);
        if (files == null) {
            throw ErrorUtils
                .logErrorAndThrowException(
                    LOGGER,
                    "listFiles() - directory does not exist or IO exception occured while reading dir: {}",
                    dirName);
        }
        // the newest file will be the first one
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        return files;
    }

    private static File writeFile(String dirName, String fileName, List<String> content) {
        return writeFile(dirName, fileName, content, true);
    }

    private static File writeFile(String dirName, String fileName, List<String> content,
        boolean overwrite) {
        try {
            File file = new File(dirName, fileName);
            if (file.exists() && overwrite) {
                deleteFile(file);
            }
            FileUtils.writeLines(file, content);
            LOGGER.info("writeFile():: file was created: {}", file);
            return file;
        } catch (IOException e) {
            throw ErrorUtils.logErrorAndThrowException(LOGGER,
                "writeFile() - unable to write file: {}", fileName);
        }
    }

    private static boolean deleteFile(File file) {
        boolean deleted = false;;
        try {
            FileUtils.forceDelete(file);
            deleted = true;
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(LOGGER, e, "Unable to delete file: {}", file);
        }
        if (deleted) {
            LOGGER.info("deleteFile():: file was deleted: {}", file);
        } else {
            LOGGER.warn("deleteFile():: file WAS NOT deleted: {}", file);
        }
        return deleted;
    }

    private static void auditLog(String auditLog) {
        LOGGER.info(auditLog);
    }

    private static void auditLog(String auditLog, Object param) {
        LOGGER.info("{}: {}", auditLog, param);
    }



    private final class MarkerFilesMonitor implements Runnable {
        private File[] previousState;

        @Override
        public void run() {
            LOGGER.info("MarkerFilesMonitor.run():: start");
            while (continueTest) {
                try {
                    File[] markerFiles = listAllMarkerFiles();
                    if (Arrays.equals(markerFiles, previousState)) {
                        LOGGER
                            .debug(
                                "MarkerFilesMonitor.run():: found {} marker files - no change since last time",
                                markerFiles.length);
                    } else {
                        LOGGER.info(
                            "MarkerFilesMonitor.run():: found {} marker files - a change detected",
                            markerFiles.length);
                        previousState = markerFiles;
                        Map<String, List<String>> groupedMarkerFiles =
                            groupMarkerFiles(markerFiles);
                        processMarkerFiles(groupedMarkerFiles);
                    }
                    sleep();
                } catch (Throwable e) {
                    LOGGER.error("Exception occured: ", e);
                    sleep();
                }
            }
            LOGGER.info("MarkerFilesMonitor.run():: exit");
        }

        private void sleep() {
            if (continueTest) {
                try {
                    LOGGER.info("MarkerFilesMonitor.sleep():: sleeping for {} [s]",
                        (markerFilesMonitorSleepTime / 1000));
                    Thread.sleep(markerFilesMonitorSleepTime);
                } catch (InterruptedException e) {
                    LOGGER.debug("MarkerFilesMonitor.sleep():: InterruptedException");
                }
            }
        }
    }



    private static final class FldLoadInfo {
        private final String roleId;
        private final String startFlowKey;
        private final String stopFlowKey;
        private FldLoadStatus status = FldLoadStatus.STOPPED;
        private long lastChange = -1;

        private FldLoadInfo(String roleId, String startFlowKey, String stopFlowKey) {
            this.roleId = roleId;
            this.startFlowKey = startFlowKey;
            this.stopFlowKey = stopFlowKey;
        }

        @Override
        public String toString() {
            return roleId;
        }
    }



    private static final class MarkerFileFilter implements FileFilter {
        private String baseName;
        private boolean selectStatusMFs;
        private boolean selectCommandMFs;

        public MarkerFileFilter(String baseName, boolean selectStatusMFs, boolean selectCommandMFs) {
            this.baseName = baseName;
            this.selectStatusMFs = selectStatusMFs;
            this.selectCommandMFs = selectCommandMFs;
        }

        @Override
        public boolean accept(File file) {
            String fileName = file.getName();
            if (!caseSensitive) {
                fileName = fileName.toLowerCase();
            }

            boolean isFile = file.isFile();
            boolean filterBaseName =
                (baseName == null ? true : fileName.startsWith(caseSensitive ? baseName : baseName
                    .toLowerCase())) && isMarkerFile(fileName);
            boolean filterStatusMF = selectStatusMFs ? FldLoadStatus.isMarkerFile(fileName) : true;
            boolean filterCommandMF =
                selectCommandMFs ? FldLoadCommand.isMarkerFile(fileName) : true;

            return isFile && filterBaseName && filterStatusMF && filterCommandMF;
        }
    }



    private static enum FldLoadStatus {
        ISSTARTING("isstarting"), STARTED("started"), FAILEDSTART("failedstart"), ISSTOPPING(
            "isstopping"), STOPPED("stopped"), FAILEDSTOP("failedstop");

        private String suffix;
        private String effectiveSuffix;

        private FldLoadStatus(String suffix) {
            this.suffix = suffix;
            this.effectiveSuffix = effectiveSuffix(this.suffix);
        }

        private static FldLoadStatus resolveBySuffix(String fileName) {
            if (fileName == null) {
                return null;
            }
            if (!caseSensitive) {
                fileName = fileName.toLowerCase();
            }
            for (FldLoadStatus value : values()) {
                if (fileName.endsWith(caseSensitive ? value.effectiveSuffix : value.effectiveSuffix
                    .toLowerCase())) {
                    return value;
                }
            }
            return null;
        }

        private static String getBaseName(String fileName) {
            if (fileName == null) {
                return null;
            }
            if (!caseSensitive) {
                fileName = fileName.toLowerCase();
            }
            for (FldLoadStatus value : values()) {
                if (fileName.endsWith(caseSensitive ? value.effectiveSuffix : value.effectiveSuffix
                    .toLowerCase())) {
                    int i =
                        fileName.lastIndexOf(caseSensitive
                            ? value.effectiveSuffix
                            : value.effectiveSuffix.toLowerCase());
                    return fileName.substring(0, i);
                }
            }
            return null;
        }

        private static boolean isMarkerFile(String fileName) {
            return getBaseName(fileName) != null;
        }

        private static boolean isOfStartType(FldLoadStatus status) {
            return status == STARTED || status == ISSTARTING || status == FAILEDSTOP;
        }

        private static boolean isOfStopType(FldLoadStatus status) {
            return status == STOPPED || status == ISSTOPPING || status == FAILEDSTART;
        }
    }



    private static enum FldLoadCommand {
        DOSTART("dostart"), DOSTOP("dostop"), FORCEDOSTART("forcedostart"), FORCEDOSTOP(
            "forcedostop");

        private String suffix;
        private String effectiveSuffix;

        private FldLoadCommand(String suffix) {
            this.suffix = suffix;
            this.effectiveSuffix = effectiveSuffix(this.suffix);
        }

        private static FldLoadCommand resolveBySuffix(String fileName) {
            if (fileName == null) {
                return null;
            }
            if (!caseSensitive) {
                fileName = fileName.toLowerCase();
            }
            for (FldLoadCommand value : values()) {
                if (fileName.endsWith(caseSensitive ? value.effectiveSuffix : value.effectiveSuffix
                    .toLowerCase())) {
                    return value;
                }
            }
            return null;
        }

        private static String getBaseName(String fileName) {
            if (fileName == null) {
                return null;
            }
            if (!caseSensitive) {
                fileName = fileName.toLowerCase();
            }
            for (FldLoadCommand value : values()) {
                if (fileName.endsWith(caseSensitive ? value.effectiveSuffix : value.effectiveSuffix
                    .toLowerCase())) {
                    int i =
                        fileName.lastIndexOf(caseSensitive
                            ? value.effectiveSuffix
                            : value.effectiveSuffix.toLowerCase());
                    return fileName.substring(0, i);
                }
            }
            return null;
        }

        private static boolean isMarkerFile(String fileName) {
            return getBaseName(fileName) != null;
        }

        private static boolean isDoStartCommand(String fileName) {
            return isDoStartCommand(resolveBySuffix(fileName));
        }

        private static boolean isDoStartCommand(FldLoadCommand command) {
            return command == DOSTART || command == FORCEDOSTART;
        }

        private static boolean isDoStopCommand(String fileName) {
            return isDoStopCommand(resolveBySuffix(fileName));
        }

        private static boolean isDoStopCommand(FldLoadCommand command) {
            return command == DOSTOP || command == FORCEDOSTOP;
        }

        private static boolean isForceCommand(String fileName) {
            return isForceCommand(resolveBySuffix(fileName));
        }

        private static boolean isForceCommand(FldLoadCommand command) {
            return command == FORCEDOSTART || command == FORCEDOSTOP;
        }
    }



    protected static interface RunCommandFlow {
        void runCommandFlow(String roleId, String envPropKey);
    }



    public static boolean isCaseSensitive() {
        return caseSensitive;
    }

    public static void setCaseSensitive() {
        setCaseSensitive(true);
    }

    public static void setCaseSensitive(boolean caseSensitive) {
        FldLoadRunner.caseSensitive = caseSensitive;
    }

    public String getMarkerFilesDir() {
        return markerFilesDirName;
    }

    public void setMarkerFilesDir(String markerFilesDirName) {
        this.markerFilesDirName = markerFilesDirName;
    }

    public String getFldLoadMarkerFileName() {
        return fldLoadMarkerFileName;
    }

    public void setFldLoadMarkerFileName(String fldLoadMarkerFileName) {
        this.fldLoadMarkerFileName = fldLoadMarkerFileName;
    }

    public long getMarkerFilesMonitorSleepTime() {
        return markerFilesMonitorSleepTime;
    }

    public void setMarkerFilesMonitorSleepTime(long markerFilesMonitorSleepTime) {
        this.markerFilesMonitorSleepTime = markerFilesMonitorSleepTime;
    }

    public boolean isStartAllFldLoadsOnStart() {
        return startAllFldLoadsOnStart;
    }

    public void setStartAllFldLoadsOnStart() {
        setStartAllFldLoadsOnStart(true);
    }

    public void setStartAllFldLoadsOnStart(boolean startAllFldLoadsOnStart) {
        this.startAllFldLoadsOnStart = startAllFldLoadsOnStart;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isCleanUnknownFiles() {
        return cleanUnknownFiles;
    }

    public void setCleanUnknownFiles() {
        setCleanUnknownFiles(true);
    }

    public void setCleanUnknownFiles(boolean cleanUnknownFiles) {
        this.cleanUnknownFiles = cleanUnknownFiles;
    }

    public boolean isAsyncFldLoadExec() {
        return asyncFldLoadExec;
    }

    public void setAsyncFldLoadExec() {
        setAsyncFldLoadExec(true);
    }

    public void setAsyncFldLoadExec(boolean asyncFldLoadExec) {
        this.asyncFldLoadExec = asyncFldLoadExec;
    }

    public boolean isWaitForAsyncFldLoadExec() {
        return wait4asyncFldLoadExec;
    }

    public void setWaitForAsyncFldLoadExec() {
        setWaitForAsyncFldLoadExec(true);
    }

    public void setWaitForAsyncFldLoadExec(boolean wait4asyncFldLoadExec) {
        this.wait4asyncFldLoadExec = wait4asyncFldLoadExec;
    }

    public long getFldLoadExecTimeout() {
        return fldLoadExecTimeout;
    }

    public void setFldLoadExecTimeout(long fldLoadExecTimeout) {
        this.fldLoadExecTimeout = fldLoadExecTimeout;
    }

}
