package com.ca.apm.systemtest.fld.hammond.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.ca.apm.systemtest.fld.hammond.Configuration;
import com.thoughtworks.xstream.XStream;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import com.wily.introscope.util.transactiontrace.TransactionTraceTextDump;
import com.wily.util.feedback.ApplicationFeedback;

public class TransactionsData {
    private static final long INTERVAL = 300000L;

    private ApplicationFeedback feedback;
    private TreeMapDb<TransactionTraceData> data;
    private Path transactionsFileXml;

    private long slicesStartTime = Long.MIN_VALUE;
    private long slicesEndTime = Long.MAX_VALUE;

    public TransactionsData(String dataFolderName) throws IOException {
        feedback = Configuration.instance().createFeedback("TransactionData");

        Path dataFolder = Paths.get(dataFolderName, "transactions");
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder);
        } else if (!Files.isDirectory(dataFolder)) {
            Files.delete(dataFolder);
            Files.createDirectory(dataFolder);
        }

        data = new TreeMapDb<>(dataFolder, "t", "T");
        data.setFeedback(feedback);

        transactionsFileXml = Paths.get(dataFolderName, "transactions.xml");
    }

    public void saveXml() {
        try {
            Set<AgentName> agents = new HashSet<>();
            long startTime = Long.MAX_VALUE;
            long endTime = 0;

            data.save();

            Files.createDirectories(transactionsFileXml.getParent());

            try (BufferedOutputStream stream =
                new BufferedOutputStream(Files.newOutputStream(transactionsFileXml,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))) {

                XStream xstream = new XStream();

                for (Long time : data.getTimesliceKeys()) {
                    List<TransactionTraceData> slice = data.getTimeslice(time);
                    for (TransactionTraceData trace : slice) {
                        agents.add(trace.getAgent());
                        startTime = Math.min(startTime, trace.getStartTime());
                        endTime = Math.max(endTime, trace.getStartTime() + trace.getDuration());

                        xstream.toXML(trace, stream);
                    }
                }
            }

            SmartstorData smartstor = new SmartstorData(Configuration.instance().getDataFolder());
            smartstor.setSlicesRange(startTime, endTime);
            int agentId = 0;
            for (AgentName agent : agents) {
                smartstor.putAgent(agentId++, agent);
            }
            smartstor.save();
            
        } catch (IOException e) {
            feedback.error(e);
        }
    }

    public void saveXmls() throws JAXBException, InterruptedException, ExecutionException {
        try {
            Set<AgentName> agents = new HashSet<>();
            long startTime = Long.MAX_VALUE;
            long endTime = 0;

            data.save();

            if (! Configuration.instance().isSkipTracesXml()) {
                Files.createDirectories(transactionsFileXml.getParent());
    
                FileOutputStream fos = new FileOutputStream(transactionsFileXml.toFile() + ".zip");
                ZipOutputStream zos = new ZipOutputStream(fos);
    
                ThreadPoolExecutor pool = new ThreadPoolExecutor(16, 16, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                
                int total = 0;
                for (Long time : data.getTimesliceKeys()) {
                    List<TransactionTraceData> slice = data.getTimeslice(time);
                    
                    final List<ByteArrayOutputStream> result = new ArrayList<>();
                    List<Future<ByteArrayOutputStream>> futures = new ArrayList<>();
    
                    int i = 0;
                    for (int o = 0; o < slice.size(); o++) {
                        result.add(null);
                    }
                    
                    for (final TransactionTraceData trace : slice) {
                        final int index = i;
                        futures.add(pool.submit(new Callable<ByteArrayOutputStream>() {
                            @Override
                            public ByteArrayOutputStream call() throws Exception {
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                TransactionTraceTextDump.writeXMLToStream(feedback, out, "admin", new TransactionTraceData[] { trace }, "UTF-8");
                                result.set(index, out);
                                return out;
                            }
                        }));
                        i++;
                    }
                    
                    for (Future<ByteArrayOutputStream> f : futures) {
                        f.get();
                    }
                    
                    i = 0;
                    for (TransactionTraceData trace : slice) {
                        ZipEntry ze = new ZipEntry(time + "/" + String.format("%06d", i) + ".xml");
                        zos.putNextEntry(ze);
                        
                        ByteArrayOutputStream out = result.get(i);
                        
                        agents.add(trace.getAgent());
                        startTime = Math.min(startTime, trace.getStartTime());
                        endTime = Math.max(endTime, trace.getStartTime() + trace.getDuration());
    
                        zos.write(out.toByteArray());
                        
                        zos.closeEntry();
                        
                        i++;
                        total++;
                        if ((total % 50000) == 0) {
                            zos.close();
                            
                            fos = new FileOutputStream(transactionsFileXml.toFile() + "." + total + ".zip");
                            zos = new ZipOutputStream(fos);
                        }
                    }
                }
                
                zos.close();
                
                pool.shutdownNow();
            }

            SmartstorData smartstor = new SmartstorData(Configuration.instance().getDataFolder());
            smartstor.setSlicesRange(startTime, endTime);
            int agentId = 0;
            for (AgentName agent : agents) {
                smartstor.putAgent(agentId++, agent);
            }
            smartstor.save();
            
        } catch (IOException e) {
            feedback.error(e);
        }
    }
    
    public void save() {
        data.save();
    }


    public void load() {
        data.load();
    }

    private TreeSet<Long> getTimesliceKeys() {
        return data.getTimesliceKeys();
    }

    private long calculateSliceTime(long time) {
        long base = 0;
        if (slicesStartTime != Long.MIN_VALUE) {
            base = slicesStartTime;
        }
        return ((time - base) / INTERVAL) * INTERVAL + base;
    }

    public List<TransactionTraceData> getTimeslice(long time) {
        return data.getTimeslice(time);
    }

    public List<TransactionTraceData> findTimeslice(long time, long offset, long duration) {
        ArrayList<TransactionTraceData> result = new ArrayList<>();
        TreeSet<Long> keys = getTimesliceKeys();
        long to = time - offset;
        long from = to - duration;

        Long floor = keys.floor(from);
        if (floor == null) {
            floor = from;
        }
        NavigableSet<Long> subKeys = keys.subSet(floor, true, to, true);

        for (Long key : subKeys) {
            List<TransactionTraceData> traces = data.getTimeslice(key);

            for (TransactionTraceData trace : traces) {
                long timestamp = trace.getStartTime() + trace.getDuration();
                if (timestamp > from && timestamp <= to) {
                    result.add(trace);
                }
            }
        }
        return result;
    }

    public boolean putTimeslice(long time, TransactionTraceData traceData) {

        if (time > slicesStartTime && time <= slicesEndTime) {
            data.addTimesliceValue(calculateSliceTime(time), traceData);
            return true;
        }
        return false;
    }

    public void setSlicesRange(Long startTime, Long endTime) {
        if (startTime != null && endTime != null && startTime < endTime) {
            this.slicesStartTime = startTime;
            this.slicesEndTime = endTime;
        }
    }
}
