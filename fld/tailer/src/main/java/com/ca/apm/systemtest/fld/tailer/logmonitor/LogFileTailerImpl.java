/**
 *
 */

package com.ca.apm.systemtest.fld.tailer.logmonitor;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of tailer that uses two threads to monitor a log file.  One thread
 * actually monitors the log file for changes by comparing its file size only - as bytes are
 * read, they are piped into a PipedOutputStream. A LineNumberReader in a second thread will read
 * data when available from the corresponding PipedInputStream.
 */
public class LogFileTailerImpl implements LogFileTailer {
    private static final Logger log = LoggerFactory.getLogger(LogFileTailerImpl.class);
    private static final int DEFAULT_DELAY_MILLIS = 1000;

    private final File file;
    private final long delayMillis;
    private final boolean end;
    private final LogFileTailerListener listener;
    private PipedOutputStream pipeOut;
    private PipedInputStream pipeIn;

    private long currentPos;

    private RandomAccessFile raf;
    private Thread readerThread;
    private Thread writerThread;
    private LogFileReader reader;
    private LogFileHandler writer;
    private boolean keepOpen;

    /**
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of
     * 1.0s.
     *
     * @param file     The file to follow.
     * @param listener the LogFileTailerListener to use.
     */
    public LogFileTailerImpl(File file, LogFileTailerListener listener) throws IOException {
        this(file, listener, DEFAULT_DELAY_MILLIS, false, true);
    }


    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the LogFileTailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the
     *                    beginning of
     *                    the file.
     * @param keepOpen    if true, keep the log file open. If false, close after each poll cycle.
     * @throws IOException
     */
    public LogFileTailerImpl(File file, LogFileTailerListener listener, long delayMillis,
        boolean end, boolean keepOpen) throws IOException {
        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;

        // Save and prepare the listener
        this.listener = listener;
        this.keepOpen = keepOpen;
        listener.init(this);

        initPipes();
        this.currentPos = 0L;

        reader = new LogFileReader();
        writer = new LogFileHandler();
        readerThread = new Thread(reader, "LogFileTailerImpl-" + file.getName() + "-reader");
        readerThread.setDaemon(true);
        writerThread = new Thread(writer, "LogFileTailerImpl-" + file.getName() + "-writer");
        writerThread.setDaemon(true);

        writerThread.start();
        readerThread.start();
    }

    private void initPipes() throws IOException {
        if (pipeIn != null) {
            close(pipeIn);
        }
        if (pipeOut != null) {
            close(pipeOut);
        }
        pipeIn = new PipedInputStream(64 * 1024);
        pipeOut = new PipedOutputStream(pipeIn);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public long getDelay() {
        return delayMillis;
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // we don't care about the exception, this is just housekeeping
            log.debug("Failed to close {}. Exception: {}", closeable, e.getMessage(), e);
        }
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            // do nothing here
        }
    }

    @Override
    public void stop() {
        reader.setDone(true);
        writer.setDone(true);
        close(pipeOut);
        close(pipeIn);
        try {
            readerThread.join(delayMillis);
        } catch (InterruptedException e) {
            log.warn(
                "Unable to join readerThread, ignoring error, hopefully it will stop itself "
                    + "sometime");
        }
        try {
            writerThread.join(delayMillis);
        } catch (InterruptedException e) {
            log.warn(
                "Unable to join writerThread, ignoring error, hopefully it will stop itself "
                    + "sometime");
        }
    }

    private class LogFileReader implements Runnable {
        private volatile boolean done = false;

        public void setDone(boolean done) {
            this.done = done;
        }

        /**
         * Main run loop for the file monitor
         */
        @Override
        public void run() {
            if (end) {
                currentPos = file.length();
            }
            openFile();
            byte[] buf = new byte[50 * 1024];
            int num = 0;

            // main loop
            while (!done) {
                delay(delayMillis);

                // if the file is shorter than the previous position, assume we've rotated
                long len = file.length();
                log.debug("File length: " + len + ", currentPos: " + currentPos);
                if (len < currentPos) {
                    currentPos = 0;
                    openFile();
                    listener.fileRotated();
                    log.info("After rotating, set currentPos to " + currentPos);
                    // and insert an EOL since there is a high probability
                    // that we missed some text at the end of the previous file
                    // after it rotated
                    String newline = String.format("%n");
                    byte[] eol = newline.getBytes();
                    writeDataToPipe(eol, eol.length);
                } else if (!keepOpen) {
                    openFile();
                }


                // read data
                if (!done && (len > currentPos)) {
                    do {
                        try {
                            num = 0;
                            num = raf.read(buf);
                            currentPos = raf.getFilePointer();
                            log.info("Current pos set to " + currentPos + " for " + raf.getFD());

                            if (!done && num > 0) {
                                boolean ok = writeDataToPipe(buf, num);
                                if (!ok && !done) {
                                    // wait 100 ms and then retry writing to the reopened pipe
                                    delay(100L);
                                    writeDataToPipe(buf, num);
                                }
                            }
                        } catch (IOException e) {
                            log.warn("Unable to read data from monitored log file: {}", file);
                            log.warn(e.getMessage());

                            openFile();
                        }
                    } while (!done && num > 0);
                }

                if (!keepOpen) {
                    close(raf);
                    raf = null;
                }
            }

            if (keepOpen) {
                close(raf);
                raf = null;
            }
        }


        private boolean writeDataToPipe(byte[] buf, int num) {
            try {
                pipeOut.write(buf, 0, num);
                return true;
            } catch (Exception e) {
                close(pipeOut);
                close(pipeIn);
            }
            return false;
        }


        private void openFile() {
            log.info("in openFile()");
            try {
                if (raf != null) {
                    log.info("RAF was not null, closing raf: " + raf);
                    close(raf);
                    raf = null;
                }
                raf = new RandomAccessFile(file, "r");
                raf.seek(currentPos);
                log.info("Opened RandomAccessFile to {}", currentPos);
            } catch (FileNotFoundException e) {
                log.warn("Monitored file not found: {}", file);
                raf = null;
                listener.fileNotFound();
            } catch (IOException e) {
                raf = null;
                log.warn("Exception opening monitored log file: {}", file, e);
            }
        }
    }

    private class LogFileHandler implements Runnable {
        private volatile boolean done = false;
        private LineNumberReader reader;

        public void setDone(boolean done) {
            this.done = done;
        }

        @Override
        public void run() {
            initReader();

            while (!done) {
                try {
                    if (reader.ready()) {
                        String line = reader.readLine();
                        listener.handle(line);
                    } else {
                        delay(delayMillis);
                    }
                } catch (IOException e) {
                    log.warn("Caught exception trying to read from reader, will reopen the reader",
                        e);
                    initReader();
                }
            }

            close(reader);
            reader = null;
        }

        private void initReader() {
            reader = new LineNumberReader(new InputStreamReader(pipeIn));
        }
    }

}
