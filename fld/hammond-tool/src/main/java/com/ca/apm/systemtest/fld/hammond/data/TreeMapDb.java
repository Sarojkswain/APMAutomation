package com.ca.apm.systemtest.fld.hammond.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.wily.introscope.util.Log;
import com.wily.util.feedback.IModuleFeedbackChannel;

public class TreeMapDb<T> {
    int SAVING_WORKERS = 30;
    int CACHE_SIZE = 2000;
    long READ_CACHE_AGE = 60000;

    private static class AppendingObjectOutputStream extends ObjectOutputStream {

        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }

    private class CacheEntry<E> {
        public CacheEntry() {
            data = new ArrayList<>(CACHE_SIZE);
            semaphore = new Semaphore(1);
        }

        private ArrayList<E> data;
        private Semaphore semaphore;

        public boolean isFull() {
            return data.size() == CACHE_SIZE;
        }
    }

    protected HashMap<Long, Path> timeslices = new HashMap<>();
    protected HashMap<Long, CacheEntry<T>> timeslicesCache = new HashMap<>();

    protected TreeSet<Long> timesliceKeys = new TreeSet<>();
    protected TreeMap<Long, List<T>> timeslicesReadCache = new TreeMap<>();
    protected HashMap<Long, Long> lastAccessMap = new HashMap<>();

    protected ExecutorService executor = Executors.newFixedThreadPool(SAVING_WORKERS);

    private Path dataFolder;

    private IModuleFeedbackChannel log = Log.out;

    private String hit;
    private String miss;

    public TreeMapDb(Path dataFolder, String hit, String miss) {
        this.dataFolder = dataFolder;
        this.hit = hit;
        this.miss = miss;
    }

    public void setReadCacheAge(long age) {
        this.READ_CACHE_AGE = age;
    }

    public void addTimesliceValue(long time, T value) {

        CacheEntry<T> timeslice = timeslicesCache.get(time);
        if (timeslice == null) {
            timeslice = new CacheEntry<>();
            timeslicesCache.put(time, timeslice);
        }
        timeslice.data.add(value);

        if (timeslice.isFull()) {
            Path file = findTimesliceFile(time);
            flushTimeslice(timeslice, file);
        }
    }

    private Path findTimesliceFile(long time) {
        Path file = timeslices.get(time);
        if (file == null) {
            file = Paths.get(dataFolder.toString(), "" + time);
            timeslices.put(time, file);
        };
        return file;
    }

    private void flushTimeslice(CacheEntry<T> timeslice, Path file) {

        try {
            timeslice.semaphore.acquire();

            executor.execute(new Runnable() {
                private Path file;
                private Semaphore semaphore;
                private ArrayList<T> data;

                public Runnable init(Path file, CacheEntry<T> timeslice) {
                    this.file = file;
                    this.semaphore = timeslice.semaphore;
                    this.data = new ArrayList<>(timeslice.data);
                    return this;
                }

                @Override
                public void run() {
                    synchronized (file) {

                        try (ObjectOutputStream outputStream =
                            Files.exists(file) ? new AppendingObjectOutputStream(
                                new GZIPOutputStream(new BufferedOutputStream(Files
                                    .newOutputStream(file, StandardOpenOption.APPEND,
                                        StandardOpenOption.CREATE)))) : new ObjectOutputStream(
                                new GZIPOutputStream(new BufferedOutputStream(Files
                                    .newOutputStream(file, StandardOpenOption.APPEND,
                                        StandardOpenOption.CREATE)))) {}) {
                            for (T o : data) {
                                outputStream.writeObject(o);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        semaphore.release();
                    }
                }
            }.init(file, timeslice));
            timeslice.data.clear();
        } catch (InterruptedException e1) {}
    }


    public TreeSet<Long> getTimesliceKeys() {
        if (timesliceKeys.size() != Math.max(timeslices.size(), timeslicesCache.size())) {
            timesliceKeys.clear();
            timesliceKeys.addAll(timeslices.size() < timeslicesCache.size() ? timeslicesCache
                .keySet() : timeslices.keySet());
        }

        return timesliceKeys;
    }

    protected void addSliceToCache(long time, List<T> slice) {
        synchronized (timeslicesReadCache) {
            // Delete old cache records
            long now = System.currentTimeMillis();
            long old = now - READ_CACHE_AGE;
            int counter = 0;
            Iterator<Entry<Long, Long>> it = lastAccessMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Long, Long> entry = it.next();
                if (entry.getValue() < old) {
                    it.remove();
                    timeslicesReadCache.remove(entry.getKey());
                    counter++;
                }
            }

            if (counter > 0) {
                log.verbose(String.format(
                    "Cache: removing %d item(s); cache size is %d; lastAccessMap size is %d.",
                    counter, timeslicesReadCache.size(), lastAccessMap.size()));
            }

            lastAccessMap.put(time, now);
            timeslicesReadCache.put(time, slice);
        }
    }

    protected List<T> getSliceFromCache(long time) {
        synchronized (timeslicesReadCache) {
            List<T> result = timeslicesReadCache.get(time);

            if (result != null) {
                lastAccessMap.put(time, System.currentTimeMillis());
            }

            return result;
        }
    }

    private List<T> readSliceToCache(long time) {
        long start = System.currentTimeMillis();

        List<T> result = new ArrayList<>();
        log.debug(miss);
        try (ObjectInputStream inputStream =
            new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(
                Files.newInputStream(timeslices.get(time)))))) {

            while (true) {

                try {
                    @SuppressWarnings("unchecked")
                    T obj = (T) inputStream.readObject();
                    result.add(obj);
                } catch (StreamCorruptedException exc) {
                    exc.printStackTrace();
                } catch (EOFException exc) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error(e);
        } catch (NullPointerException e) {
            log.warn(String.format("Slice '%d' in data folder '%s' not found.", time,
                dataFolder.toString()));
        } finally {
            addSliceToCache(time, result);
        }
        
        log.verbose(String.format("Loading timeslice %d (%d records) in %d millis.", time,
            result.size(), System.currentTimeMillis() - start));

        return result;
    }

    public List<T> getTimeslice(long time) {
        List<T> result = getSliceFromCache(time);
        if (result == null) {
            synchronized (timeslicesCache) {
                result = getSliceFromCache(time);
                if (result == null) {
                    result = readSliceToCache(time);
                } else {
                    log.debug(hit);
                }
            }
        } else {
            log.debug(hit);
        }

        return Collections.unmodifiableList(result);
    }

    public void save() {
        for (Entry<Long, CacheEntry<T>> entry : timeslicesCache.entrySet()) {
            flushTimeslice(entry.getValue(), findTimesliceFile(entry.getKey()));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {}
    }

    public void load() {

        try {
            for (Path file : Files.newDirectoryStream(dataFolder)) {
                try {
                    timeslices.put(Long.parseLong(file.getFileName().toString()), file);
                } catch (NumberFormatException e) {}
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void setFeedback(IModuleFeedbackChannel feedback) {
        this.log = feedback;
    }
}
