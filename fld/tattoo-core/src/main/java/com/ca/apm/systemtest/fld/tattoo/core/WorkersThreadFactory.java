package com.ca.apm.systemtest.fld.tattoo.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by haiva01 on 15.1.2016.
 */
public class WorkersThreadFactory implements ThreadFactory {
    private final Logger log = LoggerFactory.getLogger(WorkersThreadFactory.class);

    private AtomicInteger serial = new AtomicInteger(0);
    private String threadName;

    public WorkersThreadFactory(String threadName) {
        this.threadName = threadName != null ? threadName : "TATTOO worker thread";
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName(threadName + '#' + serial.getAndIncrement());
        thread.setDaemon(true);
        log.debug("Spawning new thread: {}", thread.getName());
        return thread;
    }
}