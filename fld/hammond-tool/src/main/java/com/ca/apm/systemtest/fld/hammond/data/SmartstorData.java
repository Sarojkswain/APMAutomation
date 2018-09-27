package com.ca.apm.systemtest.fld.hammond.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ca.apm.systemtest.fld.hammond.Configuration;
import com.ca.apm.systemtest.fld.hammond.Metric;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.util.feedback.IModuleFeedbackChannel;

public class SmartstorData {

    protected HashMap<Integer, AgentName> agents = new HashMap<>();
    protected HashMap<Integer, AgentMetric> metrics = new HashMap<>();
    protected TreeMapDb<Metric> mapDb;

    protected Path dataFolder;
    protected Path metadataFile;

    private IModuleFeedbackChannel log = Configuration.instance().createFeedback("SmartstorData");

    private Long startTime;
    private Long endTime;

    public SmartstorData(String dataFolderName) throws IOException {

        metadataFile = Paths.get(dataFolderName, "metadata");

        Path dataFolder = Paths.get(dataFolderName, "metrics");
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder);
        } else if (!Files.isDirectory(dataFolder)) {
            Files.delete(dataFolder);
            Files.createDirectory(dataFolder);
        }
        mapDb = new TreeMapDb<>(dataFolder, "s", "S");
        mapDb.setFeedback(log);
    }

    public void putAgent(int agentId, AgentName agentName) {
        agents.put(agentId, agentName);
    }

    public AgentName getAgent(int agentId) {
        return agents.get(agentId);
    }

    public Set<Integer> getAgentKeys() {
        return new TreeSet<>(agents.keySet());
    }

    public List<AgentName> getAgents() {
        return new ArrayList<>(agents.values());
    }

    public boolean putTimesliceValue(long time, Metric metric) {
        if ((startTime == null || time >= startTime) && (endTime == null || time <= endTime)) {
            mapDb.addTimesliceValue(time, metric);
            return true;
        }
        return false;
    }

    private TreeSet<Long> getTimesliceKeys() {
        return mapDb.getTimesliceKeys();
    }

    public Long findTimesliceKey(long time, long offset) {
        return getTimesliceKeys().floor(time - offset);
    }

    public List<Metric> getTimeslice(long time) {
        return mapDb.getTimeslice(time);
    }

    public void putMetric(int metricId, AgentMetric agentMetric) {
        metrics.put(metricId, agentMetric);
    }

    public AgentMetric getMetric(int metricId) {
        return metrics.get(metricId);
    }

    public void save() {
        try (ObjectOutputStream outputStream =
            new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(metadataFile,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)))) {
            // save properties
            StoreMetadata meta = new StoreMetadata();
            meta.put(StoreMetadata.VERSION, 2);

            TreeSet<Long> keys = getTimesliceKeys();
            if (keys.size() < 2) {
                if (startTime == null || endTime == null) {
                    log.error("Cannot calculate data range. No metrics loaded from smartstor. "
                        + "Please set --from and --to parameters to specify data range.");
                } else {
                    meta.put(StoreMetadata.START_TIME, startTime);
                    meta.put(StoreMetadata.END_TIME, endTime);
                }
            } else {
                if (startTime == null) {
                    startTime = keys.first();
                }
                meta.put(StoreMetadata.START_TIME, startTime);

                if (endTime == null) {
                    long last = keys.last();
                    long interval = last - keys.floor(last - 1);
                    endTime = last + interval;
                }
                meta.put(StoreMetadata.END_TIME, endTime);
            }
            outputStream.writeObject(meta);

            // save agents and metrics
            outputStream.writeObject(agents);
            outputStream.writeObject(metrics);

            mapDb.save();
        } catch (IOException e) {
            log.error(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {

        try (ObjectInputStream inputStream =
            new ObjectInputStream(new BufferedInputStream(Files.newInputStream(metadataFile)))) {
            Object object = inputStream.readObject();
            if (object instanceof StoreMetadata) {
                StoreMetadata meta = (StoreMetadata) object;

                setSlicesRange((Long) meta.get(StoreMetadata.START_TIME),
                    (Long) meta.get(StoreMetadata.END_TIME));

                object = inputStream.readObject();
            }
            agents = (HashMap<Integer, AgentName>) object;
            metrics = (HashMap<Integer, AgentMetric>) inputStream.readObject();

            mapDb.load();

            TreeSet<Long> keys = getTimesliceKeys();
            if (keys.size() >= 2) {
                if (startTime == null) {
                    startTime = keys.first();
                }

                long last = keys.last();
                long interval = last - keys.floor(last - 1);
                if (endTime == null) {
                    endTime = keys.last() + interval;
                }
            }
        } catch (NoSuchFileException e) {

        } catch (IOException | ClassNotFoundException e) {
            log.error(e);
        }
    }

    public void setSlicesRange(Long startTime, Long endTime) {
        if (startTime != null && endTime != null && startTime < endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public Long getSlicesRangeFrom() {
        return startTime;
    }

    public Long getSlicesRangeTo() {
        return endTime;
    }
}
