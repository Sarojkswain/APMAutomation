package com.ca.apm.systemtest.alertstateload.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateAlerts {

    private Logger LOGGER = LoggerFactory.getLogger(GenerateAlerts.class);

    private static final int THREAD_COUNT = 10;

    private String host;
    private int port = 8080;

    private long durationMsNormal;
    private long durationMsAlert;

    private Queue<String> queue = new LinkedList<>();

    public GenerateAlerts(long durationMsNormal, long durationMsAlert) {
        this.durationMsNormal = durationMsNormal;
        this.durationMsAlert = durationMsAlert;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void runLoad(long repeat) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread t = new Thread(new ClientThread());
            t.setDaemon(true);
            t.start();
        }

        LOGGER
            .info("GenerateAlerts.runLoad():: will end at about "
                + new Date(System.currentTimeMillis()
                    + (repeat * (durationMsNormal + durationMsAlert))));
        for (int i = 0; i < repeat; i++) {
            LOGGER.info("GenerateAlerts.runLoad():: repeat iteration: {} / {}", (i + 1), repeat);
            runLoadInternal(true, durationMsNormal);
            runLoadInternal(false, durationMsAlert);
        }
    }

    private void runLoadInternal(boolean normal, long durationMs) {
        LOGGER.info("GenerateAlerts.runLoadInternal():: normal = {}, durationMs = {}", normal,
            durationMs);
        long count = 1000000L;
        long runUntil = System.currentTimeMillis() + durationMs;
        while (System.currentTimeMillis() < runUntil) {
            LOGGER.info("GenerateAlerts.runLoadInternal():: remaining time in loop: {} [s]",
                ((runUntil - System.currentTimeMillis()) / 1000));
            synchronized (queue) {
                if (queue.size() < 10) {
                    for (int i = 0; i < 50; i++) {
                        int wait = normal ? 0 : 1500;
                        String url = getUrl(count++, wait);
                        queue.add(url);
                    }
                    queue.notifyAll();
                }
                try {
                    queue.wait(3000L);
                } catch (InterruptedException e) {}
            }
        }
        if (!normal) {
            synchronized (queue) {
                for (int i = 0; i < 200; i++) {
                    String url = getUrl(count++, 12);
                    queue.add(url);
                }
            }
        }
    }

    private String getUrl(long count, int wait) {
        return (new StringBuilder("http://").append(host).append(':').append(port)
            .append("/tesstest/webapp/bp-").append(count).append(".html?wait=").append(wait))
            .toString();
    }

    private class ClientThread implements Runnable {
        public void run() {
            boolean done = false;
            while (!done) {
                String tessTestUrl = null;
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        try {
                            queue.wait(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (queue.size() > 0) {
                        tessTestUrl = queue.remove();
                    }
                    if (tessTestUrl == null) {
                        continue;
                    }
                }
                try {
                    LOGGER.info("[Thread {}] reading URL {}", Thread.currentThread().getId(),
                        tessTestUrl);
                    URL url = new URL(tessTestUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    readFully(conn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void readFully(HttpURLConnection conn) throws Exception {
            byte[] buf = new byte[2048];
            InputStream in = conn.getInputStream();
            while (in.read(buf) >= 0) {
                // we really don't care, just need to fully read the stream
            }
            in.close();
        }
    }

}
