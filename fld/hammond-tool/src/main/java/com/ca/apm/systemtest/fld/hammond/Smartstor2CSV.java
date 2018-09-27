/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.systemtest.fld.hammond;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.server.beans.supportability.ISupportabilityServiceLocal;
import com.wily.introscope.server.beans.supportability.NullSupportabilityService;
import com.wily.introscope.server.enterprise.entity.fsdb.DataFileConstants;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.IMetaDataService;
import com.wily.introscope.server.enterprise.entity.fsdb.metadata.MetadataFileWrangler;
import com.wily.introscope.server.enterprise.entity.fsdb.queryfile.QueryFile;
import com.wily.introscope.server.enterprise.entity.fsdb.queryfile.QueryFileFooterEntry;
import com.wily.introscope.server.enterprise.entity.meta.MetadataStoreSystem;
import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBSystem;
import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;
import com.wily.introscope.spec.server.beans.metricgroup.specifiers.MatchAllDomainSpecifier;
import com.wily.introscope.spec.server.beans.metricgroup.specifiers.MatchAllMetricSpecifier;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;
import com.wily.introscope.util.Log;
import com.wily.util.clock.MasterClock;
import com.wily.util.disk.DiskAccessLock;

public class Smartstor2CSV {
    private IMetaDataService metadataFile;

    private TreeSet<String> archiveFiles;

    private Charset charset = Charset.forName("UTF-8");
    
    public Smartstor2CSV() {
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

    public void openSmartstor(String dataDirName) throws Exception {
        File dataDir = new File(dataDirName);
        boolean useRocksDb = new File(dataDir, "metadata").isDirectory();

        metadataFile = getMetaDataService(useRocksDb, dataDir, new DiskAccessLock());
        archiveFiles = new TreeSet<String>();
        for (Path file : Files.newDirectoryStream(Paths.get(dataDir.toString(), "archive"))) {
            if (file.getFileName().toString().endsWith(".data")) {
                archiveFiles.add(file.toString());
            }
        }
    }

    public void convert(String values) throws IOException {
        OutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(values, "data.csv.gz"))));
        out.write("metricId,time,value,min,max,count\n".getBytes());

        Map<Long, com.wily.introscope.spec.metric.Metric> allMetrics = new HashMap<>(); 

        OutputStream meta = new BufferedOutputStream(new FileOutputStream(new File(values, "meta.csv")));
        meta.write("metricId,metricName\n".getBytes());
        com.wily.introscope.spec.metric.Metric[] metrics = metadataFile.queryMetrics(new MatchAllDomainSpecifier(),
            new MatchAllMetricSpecifier(), 0, Long.MAX_VALUE);

        for (com.wily.introscope.spec.metric.Metric metric : metrics) {
            meta.write((metric.getMetricID() + "," + metric.getMetricUrl().replace(",", "\\,") + "\n").getBytes(charset));
            allMetrics.put(metric.getMetricID(), metric);
        }
        
        for (String file : archiveFiles) {
            readSmartstor(allMetrics, file, out, meta);
        }
        out.close();
    }

    private void readSmartstor(Map<Long, com.wily.introscope.spec.metric.Metric> allMetrics, String file, OutputStream out, OutputStream meta) throws IOException {
        Log.out.info("Extracting " + file);

        DiskAccessLock fLock = new DiskAccessLock();
        
        long startTime = MasterClock.currentTimeMillis();
        QueryFile q = null;
        long notFound = 0;
        long all = 0;
        long allValues = 0;
        try
        {
            q = new QueryFile(fLock, file);
            QueryFileFooterEntry[] footers = q.getFooters();
            if (footers != null) { 
                for(QueryFileFooterEntry footer : footers)
                {
                    com.wily.introscope.spec.metric.Metric metric = allMetrics.get(footer.metricID);
                    if (metric == null) {
                        notFound++;
                        continue;
                    }
                    
                    all++;
                    List<IMetricDataValue> values = q.queryForValuesList(footer, metric.getAttributeType());
                    if (values != null) {
                        for (IMetricDataValue v : values) {
                            allValues++;
                            if (v instanceof LongTimeslicedValue) {
                                LongTimeslicedValue v2 = (LongTimeslicedValue) v;
                                out.write((footer.metricID + "," + v.getStopTimestampInMillis() + "," 
                                        + v2.getValue() + "," 
                                        + v2.getMinimum() + ","
                                        + v2.getMaximum() + ","
                                        + v2.getDataPointCount() + "\n").getBytes(charset));
                            } else if (v instanceof IntegerTimeslicedValue) {
                                IntegerTimeslicedValue v2 = (IntegerTimeslicedValue) v;
                                out.write((footer.metricID + "," + v.getStopTimestampInMillis() + "," 
                                        + v2.getValue() + "," 
                                        + v2.getMinimum() + ","
                                        + v2.getMaximum() + ","
                                        + v2.getDataPointCount() + "\n").getBytes(charset));
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            if (q != null)
            {
                q.close();
            }
        }
        Log.out.info("Extracted " + allValues + " data points from "+ file +" - took " + (MasterClock.currentTimeMillis() - startTime));
        if (notFound > 0) {
            Log.out.warn("Some metrics were not resolved using metadata: " + notFound + "/" + (notFound + all));
        }
    }

    public static void main(String[] args) throws Exception {
        new Activator();
        Log.out = Configuration.instance().createFeedback("Smartstor CSV convertor");

        Configuration cfg = Configuration.instance();
        if (!cfg.parseCSVOptions(args)) {
            return;
        }
        
        Smartstor2CSV ss = new Smartstor2CSV();
        ss.openSmartstor(cfg.getSmartstorFolder());
        ss.convert(cfg.getDataFolder());
        
    }
}
