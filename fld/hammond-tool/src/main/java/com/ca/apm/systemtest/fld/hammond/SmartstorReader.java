package com.ca.apm.systemtest.fld.hammond;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.garret.perst.AssertionFailed;
import org.garret.perst.StorageError;

import com.ca.apm.systemtest.fld.hammond.data.AppmapData;
import com.ca.apm.systemtest.fld.hammond.data.SmartstorData;
import com.ca.apm.systemtest.fld.hammond.data.TransactionsData;
import com.wily.EDU.oswego.cs.dl.util.concurrent.Semaphore;
import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.server.beans.supportability.ISupportabilityServiceLocal;
import com.wily.introscope.server.beans.supportability.NullSupportabilityService;
import com.wily.introscope.server.enterprise.entity.fsdb.DataFileConstants;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.IMetaDataService;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.MetadataFileWrangler;
import com.wily.introscope.server.enterprise.entity.fsdb.queryfile.QueryFile;
import com.wily.introscope.server.enterprise.entity.fsdb.spoolfile.FastConverter;
import com.wily.introscope.server.enterprise.entity.meta.MetadataStoreSystem;
import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBSystem;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.IDocumentBuilder;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.IPerstStorage;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.IndexManager;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.MultiStorePerstStorage;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.TransactionTracePerstWrapper;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.TransactionTraceStorageException;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricPrefix;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.introscope.spec.server.HistoricalAgentName;
import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;
import com.wily.introscope.spec.server.beans.metricdata.MetricQueryException;
import com.wily.introscope.spec.server.beans.metricgroup.specifiers.MatchAllDomainSpecifier;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import com.wily.introscope.stat.blame.BlameStackSnapshot;
import com.wily.introscope.stat.timeslice.ATimeslicedValue;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;
import com.wily.introscope.stat.timeslice.StringTimeslicedValue;
import com.wily.introscope.util.Log;
import com.wily.util.disk.DiskAccessLock;
import com.wily.util.time.SystemClock;

public class SmartstorReader {
    public static final String DefaultIndexDir = "index";

    public static final long DCU_DURATION = 12 * 60 * 60 * 1000;

    private IMetaDataService metadataService;
    private ArrayList<QueryFile> queryFiles;
    private String dbDirName;

    private List<IPerstStorage> perstStorages = new ArrayList<IPerstStorage>();

    private HashMap<String, AgentMetric> excludedMetrics = new HashMap<>();

    public SmartstorReader() {
        queryFiles = new ArrayList<>();
    }

    public void openSmartstor(String dataDirName, boolean useRocksDb) throws Exception {
        File dataDir = new File(dataDirName);
        if (!dataDir.isDirectory()) {
            return;
        }

        DiskAccessLock accessLock = new DiskAccessLock();

        metadataService = getMetaDataService(useRocksDb, dataDir, accessLock);

        HashSet<String> queryFileNames = new HashSet<>();
        Path archiveDataDir = Paths.get(dataDir.toString(), "archive");
        if (Files.isDirectory(archiveDataDir)) {
            for (Path file : Files.newDirectoryStream(archiveDataDir)) {
                if (file.getFileName().toString().endsWith(".data")) {
                    if (queryFileNames.add(file.getFileName().toString())) {
                        queryFiles.add(new QueryFile(accessLock, file.toString()));
                    }
                }
            }
        }
        for (Path file : Files.newDirectoryStream(dataDir.toPath())) {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(".data")) {
                if (queryFileNames.add(file.getFileName().toString())) {
                    queryFiles.add(new QueryFile(accessLock, file.toString()));
                }
            } else if (fileName.endsWith(".spool")) {
                try {
                    Path queryFile =
                        Paths.get(dataDirName, fileName.substring(0, fileName.length() - 6)
                            + ".data");
                    if (!Files.exists(queryFile)
                        && queryFileNames.add(queryFile.getFileName().toString())) {

                        FastConverter fastConverter =
                            new FastConverter(accessLock, file.toString(), queryFile.toString());
                        fastConverter.convert();
                        fastConverter.close();

                        queryFiles.add(new QueryFile(accessLock, queryFile.toString()));
                    }
                } catch (InterruptedException e) {
                    Log.out.error("Cannot convert spool file to the query file.", e);
                }
            }
        }
    }

    private IMetaDataService getMetaDataService(boolean useRocksDb, File dataDir, DiskAccessLock accessLock) throws Exception {
        if (useRocksDb) {
            Properties properties = new Properties();
            properties.setProperty(DataFileConstants.DataFileDirectoryKey, dataDir.toString());
            properties.setProperty(DataFileConstants.ArchiveDirKeyName,
                    new File(dataDir, "archive").toString());
            properties.setProperty(DataFileConstants.MetadataDirectoryKey,
                    new File(dataDir, "metadata").toString());

            RocksDBSystem dbSrc = new RocksDBSystem(properties);
            return new MetadataStoreSystem(Log.out, 1, null, dbSrc, properties, null, false);
        } else {
            MetadataFileWrangler wrangler =
                new MetadataFileWrangler(accessLock,
                    (ISupportabilityServiceLocal) new NullSupportabilityService(), dataDir, new File(
                        dataDir, "archive"), false);
            return wrangler.getMetadataFile();
        }

    }
    public void openPerstDb(String dataDirName) {
        String[] parts = dataDirName.split(",");
        for (String part : parts) {
            File dataDir = new File(part);
            if (!dataDir.isDirectory()) {
                return;
            }
    
            DiskAccessLock diskAccessLock = new DiskAccessLock();
            Semaphore insertSemaphore = new Semaphore(1000);
    
            IDocumentBuilder docBuilder = new IDocumentBuilder() {
                @Override
                public Document buildDocument(TransactionTracePerstWrapper arg0) throws IOException,
                    ParseException {
                    return null;
                }
            };
    
            IndexManager fIndexManager;
            try {
                fIndexManager =
                    new IndexManager(dataDir, DefaultIndexDir, docBuilder, 8192, diskAccessLock,
                        insertSemaphore, Log.out);
    
                perstStorages.add(
                    new MultiStorePerstStorage(dataDir.getAbsolutePath(), new SystemClock(),
                        fIndexManager, docBuilder, diskAccessLock, Log.out));
            } catch (IOException | TransactionTraceStorageException e) {
                Log.out.error(e);
            }
        }
    }

    public void openDatabase(String dataDirName) {
        this.dbDirName = dataDirName;
    }

    /*
     * Hardcoded for now until we can figure out how to reliably detect them as a
     * "calculator metric"
     */
    private static String[] excludeNames =
        {
                "Frontends|Apps|multiws2|URLs|Default|Called Backends|WebServices:Average Response Time (ms)",
                "Frontends|Apps|multiws2|URLs|Default|Called Backends|WebServices:Errors Per Interval",
                "Frontends|Apps|multiws2|URLs|Default|Called Backends|WebServices:Responses Per Interval",
                "Frontends|Apps|multiws2|URLs|Default|Called Backends|WebServices:Stall Count",
                "Frontends|Apps|multiws|URLs|Default|Called Backends|WebServices:Average Response Time (ms)",
                "Frontends|Apps|multiws|URLs|Default|Called Backends|WebServices:Errors Per Interval",
                "Frontends|Apps|multiws|URLs|Default|Called Backends|WebServices:Responses Per Interval",
                "Frontends|Apps|multiws|URLs|Default|Called Backends|WebServices:Stall Count",};

    private boolean filterMetric(AgentMetric agentMetric) {
        for (String s : excludeNames) {
            if (s.equals(agentMetric.toString())) {
                return true;
            }
        }

        AgentMetricPrefix prefix = agentMetric.getAgentMetricPrefix();
        if (prefix.getSegmentCount() > 0) {
            String firstSegment = agentMetric.getAgentMetricPrefix().getSegment(0);
            if (firstSegment.equals("Enterprise Manager") || firstSegment.equals("Variance")
                || firstSegment.equals("Alerts")) {
                return true;
            }
        }

        // if (agentMetric.toString().startsWith("Frontends")) {
        // out.println(agentMetric.toString());
        // out.flush();
        // }

        List<Filter> filters = new ArrayList<>();
        filters.add(new SimpleFilter(".*Frontends\\|Apps\\|.*", 3));
        // filters.add(new
        // SimpleFilter(".*Frontends\\|Apps\\|MetroClient\\|URLs\\|Default\\|Called Backends\\|WebServices.*"));

        filters.add(new MetricNameFilter(
            ".*Frontends\\|Apps\\|.*\\|URLs\\|.*\\|Called Backends\\|WebServices.*", null,
            new String[] {"Average Response Time (ms)", "Errors Per Interval",
                    "Responses Per Interval", "Stall Count"}, true));
        filters.add(new MetricNameFilter(
            ".*Frontends\\|Apps\\|MetroClient\\|URLs\\|Default\\|Called Backends\\|WebServices.*",
            null, new String[] {"Average Response Time (ms)", "Errors Per Interval",
                    "Responses Per Interval", "Stall Count"}, true));

        filters.add(new MetricNameFilter(".*Agent Stats\\|Resources.*", 2, new String[] {
                "% CPU Utilization (Host)", "% Time Spent in GC", "Threads in Use"}, true));

        filters.add(new MetricNameFilter(".*GC Monitor.*", 1, new String[] {"GC Policy",
                "Jvm Type", "Percentage of Java Heap Used"}, false));
        filters.add(new MetricNameFilter(".*GC Heap.*", 1, new String[] {"Bytes In Use",
                "Bytes Total"}, false));

        filters.add(new MetricNameFilter(".*GC Monitor\\|Garbage Collectors\\|.*", 3, new String[] {
                "GC Algorithm", "GC Invocation Total Count", "Total GC Time (ms)"}, false));

        filters.add(new MetricNameFilter(".*GC Monitor\\|Memory Pools\\|.*", 3, new String[] {
                "Amount of Space Used (bytes)", "Current Capacity (bytes)",
                "Maximum Capacity (bytes)", "Memory Type",
                "Percentage of Maximum Capacity Currently Used"}, false));

        filters.add(new MetricNameFilter(".*CEM\\|.*", null, new String[] {"Defects Per Interval",
                "Total Performance Defective Transactions Per Interval",
                "Average Response Time (ms)", "Total Transactions Per Interval",
                "Total Defects Per Interval", "Defect %", "Total Defect Ratio (%)"}, true));

        filters.add(new MetricNameFilter(".*\\|Bussiness Service\\|.*", null, new String[] {
                "Defects Per Interval", "Total Performance Defective Transactions Per Interval",
                "Average Response Time (ms)", "Total Transactions Per Interval",
                "Total Defects Per Interval", "Defect %", "Total Defect Ratio (%)"}, true));

        // filters.add(new SimpleFilter(".*GC Monitor\\|Garbage Collectors\\|.*", 3));
        // filters.add(new SimpleFilter(".*GC Monitor\\|Memory Pools\\|.*", 3));
        // filters.add(new SimpleFilter(".*GC Monitor:.*", 1));

        for (Filter f : filters) {
            if (f.metricIsExcluded(agentMetric)) {
                excludedMetrics.put(agentMetric.toString(), agentMetric);
                return true;
            }
        }

        return false;
    }

    private boolean filterAgent(HistoricalAgentName an) {
        if (an.getAgentName().getHost().contains("(Virtual)")) {
            return true;
        }
        return false;
    }

    private void readDatabase(AppmapData appmap) throws IOException, UnsupportedEncodingException {
        appmap.importCsv(Paths.get(dbDirName));
    }

    private void readTraces(TransactionsData transactions) {

        for (IPerstStorage perstStorage : perstStorages) {
            int totalTraces = perstStorage.getTotalTraceCount();
            int percent = 0;
            int counter = 0;
            int acceptedCounter = 0;

            @SuppressWarnings("unchecked")
            Iterator<TransactionTracePerstWrapper> it = perstStorage.iteratorOverAllTraces();
            while (it.hasNext()) {
                try {
                    counter++;
                    TransactionTracePerstWrapper ttw = it.next();
                    long timestamp = ttw.getTimestamp();
                    Object traceData = ttw.getTraceData();
                    if (traceData instanceof TransactionTraceData) {
                        if (transactions.putTimeslice(timestamp, (TransactionTraceData) traceData)) {
                            acceptedCounter++;
                        }
                    }

                    if (totalTraces > 0) {
                        int p = counter * 100 / totalTraces;
                        if (p >= percent + 10) {
                            percent = p;
                            Log.out.info("Loaded " + counter + " traces - (" + percent + "%)");
                        }
                    } else {
                        if (0 == (counter % 500)) {
                            Log.out.info("Loaded " + counter + " traces");
                        }
                    }
                } catch (StorageError error) {
                    Log.out.error("Cannot read transaction: ", error);
                } catch (AssertionFailed | Exception error) {
                    Log.out.error("Database corrupted: ", error);
                    break;
                }
            }

            Log.out.info("Finished transaction traces loading. Total trace count: " + counter
                + ", accepted traces: " + acceptedCounter);
        }
    }

    private void readSmartstor(SmartstorData store) throws IOException {
        long from = store.getSlicesRangeFrom() != null ? store.getSlicesRangeFrom() : 0L;
        long to = store.getSlicesRangeTo() != null ? store.getSlicesRangeTo() : Long.MAX_VALUE;

        HistoricalAgentName[] historicalAgents =
            metadataService.queryHistoricalAgents(new MatchAllDomainSpecifier(), from, to);

        long counter = 0L;
        long metrics = 0L;
        long percent = 0L;

        for (HistoricalAgentName agentName : historicalAgents) {
            if (filterAgent(agentName)) {
                continue;
            }

            AgentMetric[] agentMetrics =
                metadataService.queryAgentMetrics(agentName.getAgentName(), from, to);

            for (AgentMetric agentMetric : agentMetrics) {
                if (filterMetric(agentMetric)) {
                    continue;
                }

                int agentId = agentName.getAgentName().getPersistentID();
                store.putAgent(agentId, agentName.getAgentName());

                int agentMetricId = agentMetric.getPersistentID();
                store.putMetric(agentMetricId, agentMetric);


                for (QueryFile queryFile : queryFiles) {
                    long metricDataId =
                        metadataService.getMetricID(agentName.getAgentName(), agentMetric);
                    int metricDataType = agentMetric.getAttributeType();
                    IMetricDataValue[] result = queryFile.query(metricDataId, metricDataType);
                    for (IMetricDataValue dv : result) {
                        if (dv instanceof IntegerTimeslicedValue
                            || dv instanceof LongTimeslicedValue
                            || dv instanceof StringTimeslicedValue) {

                            BlameStackSnapshot bss =
                                ((ATimeslicedValue) dv).getBlameStackSnapshot();
                            if (bss != null) {
                                System.err.println(bss);
                            }

                            Metric metric =
                                new Metric(agentId, agentMetricId, (ATimeslicedValue) dv);
                            if (store.putTimesliceValue(dv.getStartTimestampInMillis(), metric)) {
                                metrics++;
                            };
                        }
                    }
                }
            }
            long p = ++counter * 100 / historicalAgents.length;
            if (p > percent) {
                percent = p;
                Log.out.info("Loaded " + metrics + " metrics - (" + percent + "%)");
            }
        }

        for (Entry<String, AgentMetric> e : excludedMetrics.entrySet()) {
            Log.out.info("Excluded: " + e.getKey());
        }
        Log.out.info("Finished smartstor loading. Total accepted metrics count: " + metrics);
    }

    public void close() {
        for (IPerstStorage perstStorage : perstStorages) {
            perstStorage.close();
        }
    }

    public static String type2string(int type) {
        switch (type) {
            case MetricTypes.kBackendIntegerAggregatingFluctuatingCounter:
                return "kBackendIntegerAggregatingFluctuatingCounter";
            case MetricTypes.kBackendIntegerDuration:
                return "kBackendIntegerDuration";
            case MetricTypes.kBackendIntegerFluctuatingCounter:
                return "";
            case MetricTypes.kBackendLongIntervalCounter:
                return "";
            case MetricTypes.kBizTrxIntegerAggregatingFluctuatingCounter:
                return "";
            case MetricTypes.kBizTrxIntegerDuration:
                return "";
            case MetricTypes.kBizTrxIntegerFluctuatingCounter:
                return "";
            case MetricTypes.kBizTrxLongIntervalCounter:
                return "";
            case MetricTypes.kBooleanBaseType:
                return "";
            case MetricTypes.kDoNotAggregateType:
                return "";
            case MetricTypes.kDoubleBaseType:
                return "";
            case MetricTypes.kFloatCounter:
                return "";
            case MetricTypes.kFloatPercentage:
                return "";
            case MetricTypes.kFrontendIntegerAggregatingFluctuatingCounter:
                return "";
            case MetricTypes.kFrontendIntegerDuration:
                return "";
            case MetricTypes.kFrontendIntegerFluctuatingCounter:
                return "";
            case MetricTypes.kFrontendLongIntervalCounter:
                return "";
            case MetricTypes.kIntegerAggregatingFluctuatingCounter:
                return "";
            case MetricTypes.kIntegerConstant:
                return "";
            case MetricTypes.kIntegerDuration:
                return "";
            case MetricTypes.kIntegerFluctuatingCounter:
                return "";
            case MetricTypes.kIntegerMonotonicallyIncreasingCounter:
                return "";
            case MetricTypes.kIntegerPercentage:
                return "";
            case MetricTypes.kIntegerRate:
                return "";
            case MetricTypes.kIntegerSaturation:
                return "";
            case MetricTypes.kLongConstant:
                return "";
            case MetricTypes.kLongDuration:
                return "";
            case MetricTypes.kLongFluctuatingCounter:
                return "";
            case MetricTypes.kLongIntervalCounter:
                return "";
            case MetricTypes.kLongMonotonicallyIncreasingCounter:
                return "";
            case MetricTypes.kLongTimestamp:
                return "";
            case MetricTypes.kLongTimestampConstant:
                return "";
            case MetricTypes.kStringBaseType:
                return "";
            case MetricTypes.kStringCaughtException:
                return "";
            case MetricTypes.kStringCollating:
                return "";
            case MetricTypes.kStringConstant:
                return "";
            case MetricTypes.kStringIndividualEvents:
                return "";
            case MetricTypes.kStringSystemLog:
                return "";
            case MetricTypes.kStringThrownException:
                return "";
            case MetricTypes.kTreatZeroCountAsHole:
                return "";
            case MetricTypes.kUnknownType:
                return "";
            default:
                return "unknown";
        }
    }

    public static void main(String[] args) throws Exception, MetricQueryException, JAXBException, InterruptedException, ExecutionException {
        new Activator();
        Log.out = Configuration.instance().createFeedback("Smartstor Reader");

        long start = System.currentTimeMillis();

        Configuration cfg = Configuration.instance();
        if (!cfg.parseReaderOptions(args)) {
            return;
        }

        File dataFolder = new File(cfg.getDataFolder());
        if (dataFolder.isDirectory()) {
            FileUtils.deleteDirectory(dataFolder);
        }
        dataFolder.mkdir();

        if (cfg.isTracesOnly()) {
            TransactionsData transactions = new TransactionsData(cfg.getDataFolder());

            long startTime = cfg.getFrom() != null ? cfg.getFrom() : Long.MIN_VALUE;
            long endTime = cfg.getTo() != null ? cfg.getTo() : Long.MAX_VALUE;
            transactions.setSlicesRange(startTime, endTime);

            SmartstorReader sp = new SmartstorReader();
            sp.openPerstDb(cfg.getTracesFolder());
            sp.readTraces(transactions);
            sp.close();

            transactions.saveXmls();
        } else {

            boolean skipTraces = false;
            boolean skipEdges = false;

            SmartstorData store = new SmartstorData(cfg.getDataFolder());
            TransactionsData transactions = new TransactionsData(cfg.getDataFolder());
            AppmapData appmap = new AppmapData(cfg.getDataFolder());

            SmartstorReader sp;


            DcuFileHandler dcuFileHandler = null;

            boolean rocksdb = cfg.isRocksdb();

            if (cfg.getDcu() != null) {
                dcuFileHandler = new DcuFileHandler(cfg);

                dcuFileHandler.extractData();
                rocksdb = dcuFileHandler.isRocksdb();
            }

            if (cfg.getSmartstorFolder() != null && Files.isDirectory(Paths.get(cfg.getSmartstorFolder()))) {
                sp = new SmartstorReader();
                sp.openSmartstor(cfg.getSmartstorFolder(), rocksdb);
            } else {
                Log.out.error(String.format("Smartstor folder '%s' does not exist.",
                    cfg.getSmartstorFolder()));
                return;
            }
            if (cfg.getTracesFolder() != null && !cfg.getTracesFolder().isEmpty()) {
                sp.openPerstDb(cfg.getTracesFolder());
            } else {
                Log.out.warn(String.format(
                    "Traces folder '%s' does not exist. Traces processing disabled.",
                    cfg.getTracesFolder()));
                skipTraces = true;
            }
            if (cfg.getDatabaseFolder() != null && Files.isDirectory(Paths.get(cfg.getDatabaseFolder()))) {
                sp.openDatabase(cfg.getDatabaseFolder());
            } else {
                Log.out.warn(String.format(
                    "Database folder '%s' does not exist. Database processing disabled.",
                    cfg.getDatabaseFolder()));
                skipEdges = true;
            }

            if (cfg.getDcu() != null && cfg.getFrom() == null && cfg.getTo() == null) {
                store.setSlicesRange(dcuFileHandler.getEndTime() - DCU_DURATION, dcuFileHandler.getEndTime());
            } else {
                store.setSlicesRange(cfg.getFrom(), cfg.getTo());
            }
            sp.readSmartstor(store);
            store.save();
            // store.load();

            Long startTime = store.getSlicesRangeFrom();
            Long endTime = store.getSlicesRangeTo();

            if (startTime == null || endTime == null) {
                Log.out
                    .error("No data time range specified. Aborting transaction traces and edges loading.");
                return;
            }

            transactions.setSlicesRange(startTime, endTime);
            appmap.setSlicesRange(startTime, endTime);
            appmap.setAgentNames(store.getAgents());

            store = null;
            Runtime.getRuntime().gc();

            if (!skipTraces) {
                sp.readTraces(transactions);
                Log.out.info("Saving transactions");
                transactions.save();
            }
            transactions = null;
            Runtime.getRuntime().gc();

            if (!skipEdges) {
                sp.readDatabase(appmap);
                Log.out.info("Saving edges");
                appmap.save();
            }
            appmap = null;
            Runtime.getRuntime().gc();

            sp.close();

            Log.out.info("Data loaded in " + ((System.currentTimeMillis() - start) / 1000) + "s.");

            if (cfg.getDcu() != null) {
                dcuFileHandler.packData();

                dcuFileHandler.cleanTmpFiles();
            }
        }
    }
}
