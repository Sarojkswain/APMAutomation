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

package com.ca.apm.automation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check for expected and unexpected messages in log within given time period.
 * Note: Ignores log rotation and possible other overwrites. Apache Tailer would be preferable, if
 * it handled encoded newlines.
 */
public class LogScanner {
    private static final Logger log = LoggerFactory.getLogger(LogScanner.class);
    /** List of expected messages that have not been seen yet. */
    private final Collection<String> expected;
    /** List of unexpected messages. */
    private final Collection<String> unexpected;
    /** List of found unexpected messages. */
    private final Collection<String> unexpectedFound;
    /** Log file. */
    private final File logFile;
    /** Log character encoding. */
    private final Charset charset;
    /** Initial reading address. */
    private final long position;

    /**
     * Construct log listener.
     *
     * @param log Log file to read.
     * @param charset Character encoding for the log file.
     * @param expected List of expected strings. After {@link #awaitLines(long)} returns, contains
     *        only expected lines that were not found.
     * @param unexpected List of unexpected strings. After {@link #awaitLines(long)} returns,
     *        contains only unexpected lines that were found.
     * @param onlyNewMessages Watch only log contents that appear after construction?
     */
    public LogScanner(File logFile, Charset charset, Collection<String> expected,
        Collection<String> unexpected, boolean onlyNewMessages) {
        this.expected = expected;
        this.unexpected = unexpected;
        unexpectedFound = new LinkedList<>();
        this.logFile = logFile;
        this.charset = charset;
        position = onlyNewMessages ? logFile.length() : 0;
    }

    /**
     * Expect all the expected and no unexpected log lines to appear within timeout.
     * After execution, <b>expected</b> contains all expected strings that were not found, and
     * <b>unexpected</b> all unexpected that were found.
     *
     * @param maxWait Wait time [s].
     * @throws InterruptedException when interrupted
     * @throws IOException When file does not exist or can't be read.
     */
    public void awaitLines(int maxWait) throws InterruptedException, IOException {
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r");
            Reader isr = Channels.newReader(raf.getChannel(), charset.newDecoder(), -1);
            BufferedReader br = new BufferedReader(isr)) {
            raf.seek(position);
            Thread.sleep(maxWait * 1_000);
            String line;
            while ((line = br.readLine()) != null) {
                handle(line);
            }
            unexpected.clear();
            unexpected.addAll(unexpectedFound);
            log.debug("Log messages not found: {}", expected);
            log.debug("Unexpected messages found: {}", unexpected);
        } catch (Exception e) {
            log.warn("Unhandled exception", e);
            throw e;
        }
    }

    /**
     * Process single line.
     *
     * @param line Line to process
     */
    private void handle(String line) {
        boolean logged = false;
        for (Iterator<String> it = expected.iterator(); it.hasNext();) {
            String expected = it.next();
            if (line.contains(expected)) {
                it.remove();
                log.debug("{} +++ {}", line, expected);
                logged = true;
            }
        }
        for (String unexpected : unexpected) {
            if (line.contains(unexpected)) {
                unexpectedFound.add(line);
                log.debug("{} --- {}", line, unexpected);
                logged = true;
            }
        }
        if (!logged) {
            log.debug("{}", line);
        }
    }
}
