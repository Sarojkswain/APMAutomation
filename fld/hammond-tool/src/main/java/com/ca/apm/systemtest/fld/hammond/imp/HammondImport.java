/*
 * Copyright (c) 2017 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.systemtest.fld.hammond.imp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompressionType;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.ca.apm.systemtest.fld.hammond.Filter;
import com.ca.apm.systemtest.fld.hammond.MetricNameFilter;
import com.ca.apm.systemtest.fld.hammond.SimpleFilter;
import com.wily.EDU.oswego.cs.dl.util.concurrent.Semaphore;
import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.server.enterprise.entity.fsdb.DataFileConstants;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.IMetaDataService;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.MetadataUtils;
import com.wily.introscope.server.enterprise.entity.fsdb.queryfile.QueryFile;
import com.wily.introscope.server.enterprise.entity.fsdb.queryfile.QueryFileFooterEntry;
import com.wily.introscope.server.enterprise.entity.fsdb.spoolfile.FastConverter;
import com.wily.introscope.server.enterprise.entity.meta.MetadataStoreSystem;
import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBSystem;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.IDocumentBuilder;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.IndexManager;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.MultiStorePerstStorage;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.TransactionTracePerstWrapper;
import com.wily.introscope.server.enterprise.entity.transactiontrace.storage.TransactionTraceStorageException;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricPrefix;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.server.HistoricalAgentName;
import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;
import com.wily.introscope.spec.server.beans.metricdata.MetricQueryException;
import com.wily.introscope.spec.server.beans.metricgroup.specifiers.MatchAllDomainSpecifier;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import com.wily.introscope.util.Log;
import com.wily.util.disk.DiskAccessLock;
import com.wily.util.time.SystemClock;

public class HammondImport {
    public static final String DefaultIndexDir = "index";


    public HammondImport(String output) {
    }

    public static void importSmartstor(String smartstorDir, String output) throws Exception {
        Log.out.info("Import Smartstor: " + smartstorDir);
        File dataDir = new File(smartstorDir);
        if (!dataDir.isDirectory()) {
            Log.out.error("Smartstor does not exists: " + smartstorDir);
            return;
        }

        DiskAccessLock accessLock = new DiskAccessLock();

        IMetaDataService metadataService = getMetaDataService(dataDir);

        ArrayList<QueryFile> queryFiles = new ArrayList<>();

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
                        Paths.get(smartstorDir, fileName.substring(0, fileName.length() - 6)
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
        
        HistoricalAgentName[] hAgents = metadataService.queryHistoricalAgents(new MatchAllDomainSpecifier(), 0, Long.MAX_VALUE);
        Map<Integer, AgentName> agents = new HashMap<>();
        for (HistoricalAgentName a : hAgents) {
            if (filterAgent(a)) {
                continue;
            }
            agents.put(a.getAgentName().getPersistentID(), a.getAgentName());
        }
        
        Map<Integer, AgentMetric> attributes = new HashMap<>();
        
        RocksDB db = openOutputDB(output, false);
        
        HammondTables hammondDB = new HammondTables(db);
        
        int fileCount = 0;
        for (QueryFile queryFile : queryFiles) {
            QueryFileFooterEntry[] footers = queryFile.getFooters();
            
            Set<Integer> agentsDone = new HashSet<>();
            
            Set<Integer> agentsTotal = new HashSet<>();
            for (QueryFileFooterEntry footer : footers) {
                Integer agentId = MetadataUtils.decomposeGetAgentID(footer.metricID);
                if (agents.get(agentId) == null) {
                    continue;
                }
                agentsTotal.add(agentId);
            }
            agentsTotal.retainAll(agents.keySet());
            
            while (true) {
                
                int agentTodo = -1;
                
                for (QueryFileFooterEntry footer : footers) {
                    Integer attributeId = MetadataUtils.decomposeGetAttributeID(footer.metricID);
                    Integer agentId = MetadataUtils.decomposeGetAgentID(footer.metricID);
                    if (agents.get(agentId) == null) {
                        continue;
                    }
                    
                    AgentMetric attribute = attributes.get(attributeId);
                    if (attribute == null) {
                        if (!attributes.containsKey(attributeId)) {
                            attribute = metadataService.getAgentMetricByID(attributeId);
                            attributes.put(attributeId, attribute);
                        }
                    }
                    
                    if (attribute == null) {
                        continue;
                    }
                    
                    if (!agentsDone.contains(agentId)) {
                        agentsDone.add(agentId);
                        agentTodo = agentId;
                        break;
                    }
                }
                if (agentTodo == -1) {
                    break;
                }
                
                // Read values for all agent metrics
                Map<Long, List<SliceDataValues.SliceDataValue>> time2Values =
                        readAgentSlices(metadataService, attributes, hammondDB, queryFile, footers, agentTodo);

                // Store agent timeslices
                for (Map.Entry<Long, List<SliceDataValues.SliceDataValue>> slice : time2Values.entrySet()) {
                    hammondDB.store(agents.get(agentTodo), slice.getKey(), slice.getValue());
                }
                
                Log.out.info("Processed " + agentsDone.size() + "/" + agentsTotal.size() + " agents and " + fileCount + "/" + queryFiles.size() + " files.");
            }
            
            fileCount++;
            Log.out.info("Processed " + agentsDone.size() + "/" + agentsTotal.size() + " agents and " + fileCount + "/" + queryFiles.size() + " files.");
        }

        db.close();
    }

    
    
    public static Map<Long, List<SliceDataValues.SliceDataValue>> readAgentSlices(
            IMetaDataService metadataService, Map<Integer, AgentMetric> attributes,
            HammondTables hammondDB, QueryFile queryFile, QueryFileFooterEntry[] footers,
            int agentToRead) throws IOException, RocksDBException {
        Map<Long, Map<Integer, SliceDataValues.SliceDataValue>> time2Values = new HashMap<>();
        for (QueryFileFooterEntry footer : footers) {
            Integer attributeId = MetadataUtils.decomposeGetAttributeID(footer.metricID);
            Integer agentId = MetadataUtils.decomposeGetAgentID(footer.metricID);
            if (agentId != agentToRead) {
                continue;
            }
            
            AgentMetric attribute = attributes.get(attributeId);
            
            if (attribute == null) {
                if (!attributes.containsKey(attributeId)) {
                    attribute = metadataService.getAgentMetricByID(attributeId);
                    attributes.put(attributeId, attribute);
                }
            }
            
            if (attribute == null) {
                continue;
            }
            
            if (HammondMetricFilter.filterMetric(attribute)) {
                continue;
            }
            
            List<IMetricDataValue> values = queryFile.queryForValuesList(footer, attribute.getAttributeType());
            Integer hammondAttributeId = hammondDB.getAttributeId(attribute);
            
            for (IMetricDataValue value : values) {
                Map<Integer, SliceDataValues.SliceDataValue> vals = time2Values.get(value.getStopTimestampInMillis());
                if (vals == null) {
                    vals = new HashMap<>();
                    time2Values.put(value.getStopTimestampInMillis(), vals);
                }
                vals.put(hammondAttributeId, new SliceDataValues.SliceDataValue(value, hammondAttributeId));
            }
        }
        
        Map<Long, List<SliceDataValues.SliceDataValue>> result = new HashMap<>();
        for (Map.Entry<Long, Map<Integer, SliceDataValues.SliceDataValue>> slice : time2Values.entrySet()) {
            result.put(slice.getKey(), new ArrayList<>(slice.getValue().values()));
        }

        return result;
    }

    public static RocksDB openOutputDB(String output, boolean readOnly) throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setWriteBufferSize(1024 * 1024 * 1024);
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        options.setTargetFileSizeBase(1024 * 1024 * 1024);
        
        BlockBasedTableConfig blockConfig = new BlockBasedTableConfig();
        blockConfig.setBlockCacheSize(512 * 1024 * 1024);
        blockConfig.setFormatVersion(2);
        options.setTableFormatConfig(blockConfig);

        if (readOnly) {
            return RocksDB.openReadOnly(options, output);
        }
        return RocksDB.open(options, output);
    }

    private static IMetaDataService getMetaDataService(File dataDir) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(DataFileConstants.DataFileDirectoryKey, dataDir.toString());
        properties.setProperty(DataFileConstants.ArchiveDirKeyName,
                new File(dataDir, "archive").toString());
        properties.setProperty(DataFileConstants.MetadataDirectoryKey,
                new File(dataDir, "metadata").toString());

        RocksDBSystem dbSrc = new RocksDBSystem(properties);
        return new MetadataStoreSystem(Log.out, 1, null, dbSrc, properties, null, false);
    }
    
    public static void importPerstDb(String perstDir, String output) throws RocksDBException {
        Log.out.info("Import traces: " + perstDir);
        File dataDir = new File(perstDir);
        if (!dataDir.isDirectory()) {
            Log.out.error("Traces directory does not exists: " + perstDir);
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

        RocksDB db = openOutputDB(output, false);
        
        HammondTables hammondDB = new HammondTables(db);
        
        IndexManager fIndexManager;
        try {
            fIndexManager =
                new IndexManager(dataDir, DefaultIndexDir, docBuilder, 8192, diskAccessLock,
                    insertSemaphore, Log.out);

            MultiStorePerstStorage perstStorage = new MultiStorePerstStorage(dataDir.getAbsolutePath(), new SystemClock(),
                    fIndexManager, docBuilder, diskAccessLock, Log.out);
            
            int totalTraces = perstStorage.getTotalTraceCount();
            int percent = 0;
            int counter = 0;
            int acceptedCounter = 0;

            @SuppressWarnings("unchecked")
            Iterator<TransactionTracePerstWrapper> it = perstStorage.iteratorOverAllTraces();
            while (it.hasNext()) {
                counter++;
                TransactionTracePerstWrapper ttw = it.next();
                long timestamp = 15000L * (ttw.getTimestamp() / 15000);
                Object traceData = ttw.getTraceData();
                if (traceData instanceof TransactionTraceData) {
                    TransactionTraceData trace = (TransactionTraceData) traceData;
                    hammondDB.storeTrace(trace.getAgent(), timestamp, trace);
                    acceptedCounter++;
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
            }

            perstStorage.close();
            
            Log.out.info("Finished transaction traces loading. Total trace count: " + counter
                + ", accepted traces: " + acceptedCounter);
            

        } catch (IOException | TransactionTraceStorageException e) {
            Log.out.error(e);
        }
        
        db.close();
    }

    private static boolean filterAgent(HistoricalAgentName an) {
        return an.getAgentName().getHost().contains("(Virtual)");
    }


    public static void main(String[] args) throws Exception, MetricQueryException, JAXBException, InterruptedException, ExecutionException {
        new Activator();
        Log.out = HammondImportConfiguration.instance().createFeedback("Hammond import");

        long start = System.currentTimeMillis();

        HammondImportConfiguration cfg = HammondImportConfiguration.instance();
        if (!cfg.parseReaderOptions(args)) {
            return;
        }

        if (cfg.getFiltersPath() != null) {
            HammondMetricFilter.importMetricNameFileters(cfg.getFiltersPath());
        }

        File outputFolder = new File(cfg.getOutput());
        if (outputFolder.isDirectory()) {
            Log.out.info("Output folder exists. Appending....");
        } else {
            outputFolder.mkdirs();
        }

        if (cfg.isCompact()) {
            Log.out.info("Compacting output");
            RocksDB db = openOutputDB(cfg.getOutput(), false);
            FlushOptions flushOptions = new FlushOptions();
            flushOptions.setWaitForFlush(true);
            db.flush(flushOptions);
            db.compactRange();
            db.close();
            Log.out.info("Compacting output " + ((System.currentTimeMillis() - start) / 1000) + "s.");
            return;
        } else {
            importSmartstor(cfg.getSmartstor(), cfg.getOutput());
            importPerstDb(cfg.getTraces(), cfg.getOutput());
            Log.out.info("Data loaded in " + ((System.currentTimeMillis() - start) / 1000) + "s.");
        }

    }
}
