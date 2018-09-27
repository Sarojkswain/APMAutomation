package com.ca.apm.nextgen.tests.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author haiva01
 */
public class WiserSmtpServer extends Wiser implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(WiserSmtpServer.class);

    private final Semaphore sema = new Semaphore(1);

    public WiserSmtpServer(int port) {
        super(port);
        try {
            sema.acquire();
        } catch (InterruptedException e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Interrupted.");
        }
    }

    public void waitForEmail(long amount, TimeUnit timeUnit) {
        try {
            sema.tryAcquire(amount, timeUnit);
        } catch (InterruptedException ex) {
            throw ErrorReport.logExceptionAndWrapFmt(log, ex,
                "Email did not arrive in timely manner. Exception: {0}");
        }
    }

    @Override
    public void deliver(String from, String recipient,
        InputStream data) throws IOException {
        log.info("You've got mail!");
        super.deliver(from, recipient, data);
        sema.release();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    public void clearDeliverdMessages() {
        messages.clear();
    }

    public int getPort() {
        return getServer().getPort();
    }

    public String getHost() {
        return getServer().getHostName();
    }
}
