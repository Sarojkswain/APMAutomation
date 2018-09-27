/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author keyja01
 *
 */
public class LogFileTailerTest {

    private Path tmpDir;
    private File logFile;
    private PrintWriter writer;
    private static long elapsed = 0;
    
    private static final String TEMP_LOG = "temp.log";
    
    private class MyTailerListener implements LogFileTailerListener {
        private ArrayList<String> lines = new ArrayList<>();
        private boolean rotated = false;
        private boolean initialized;
        
        @Override
        public void init(LogFileTailer tailer) {
            System.out.println(elapsedMs() + ": Initialized");
            initialized = true;
        }
        
        @Override
        public void handle(Exception ex) {
        }
        
        @Override
        public void handle(String line) {
            System.out.println(elapsedMs() + ": Processing line " + line);
            lines.add(line);
        }
        
        @Override
        public void fileRotated() {
            System.out.println(elapsedMs() + ": File was rotated");
            rotated = true;
        }
        
        @Override
        public void fileNotFound() {
        }
    }


    @Before
    public void setup() throws Exception {
        tmpDir = Files.createTempDirectory("logrotation");
        tmpDir.toFile().deleteOnExit();
        
        logFile = new File(tmpDir.toFile(), TEMP_LOG);
        writer = openLogFile(logFile);
    }
    
    
    @After
    public void after() throws Exception {
        writer.close();
    }
    
    
    /**
     * Attempts to simulate the condition when the file's lastModified timestamp is updated BEFORE
     * changes to the file are actually committed to the filesystem.
     * @throws Exception
     */
    @Test
    public void testFileTouch() throws Exception {
        LogFileTailerListener listener = Mockito.mock(LogFileTailerListener.class);
        LogFileTailer tailer = null;
        
        try {
            tailer = new LogFileTailerImpl(logFile, listener, 100, false, true);
            
            writer.println("Line 1");
            writer.flush();
            // touch the file
            Thread.sleep(1000L);
            long lastModified = logFile.lastModified();
            long now = System.currentTimeMillis();
            assertTrue(now > lastModified);
            logFile.setLastModified(now);
            Thread.sleep(3000L);
            
            verify(listener, times(1)).handle("Line 1");
        } finally {
            if (tailer != null) {
                tailer.stop();
            }
            
        }
    }
    
    
    /**
     * @throws Exception
     */
    @Test
    public void testStartAtEnd() throws Exception {
        LogFileTailerListener listener = Mockito.mock(LogFileTailerListener.class);
        LogFileTailer tailer = null;
        
        try {
            
            writer.println("Line 1");
            writer.println("Line 2");
            writer.println("Line 3");
            writer.flush();

            tailer = new LogFileTailerImpl(logFile, listener, 100, true, true);
            Thread.sleep(1000L);
            writer.println("Line 4");
            writer.println("Line 5");
            writer.println("Line 6");
            writer.flush();
            
            Thread.sleep(500L);
            
            verify(listener).handle("Line 4");
            verify(listener).handle("Line 5");
            verify(listener).handle("Line 6");
        } finally {
            if (tailer != null) {
                tailer.stop();
            }
            
        }
    }

    
    private static double elapsedMs() {
        double val = System.nanoTime() - elapsed; 
        
        return val / 1000000.0;
    }
    

    @Test
    public void testLogRotation() throws Exception {
        LogFileTailer tailer = null;
        try {
            elapsed = System.nanoTime();

            // open a log file
            MyTailerListener listener = new MyTailerListener();

            // create a LogFileTailer for it
            tailer = new LogFileTailerImpl(logFile, listener, 50, false, false);

            // write entries
            writer.println("Line 1");
            writer.println("Line 2");
            writer.println("Line 3");

            // rotate the log
            writer.flush();
            writer.close();
            Thread.sleep(100L);
            File renamedLogFile = new File(tmpDir.toFile(), TEMP_LOG + ".1");
            assertFalse(renamedLogFile.exists());
            // assertTrue(logFile.renameTo(renamedLogFile));

            assertTrue(rotateLogFile(logFile, renamedLogFile, 10));
            
            writer = openLogFile(logFile);
            writer.flush();

            Thread.sleep(100L);
            System.out.println(elapsedMs() + ": Writing more entries");

            // write more entries
            writer.println("Line 4");
            writer.println("Line 5");
            writer.println("Line 6");
            writer.flush();

            Thread.sleep(1000L);
            System.out.println(elapsedMs() + ": Done. Verifying lines");
//            Thread.sleep(120000L);

            // verify that we have all of the entries
            assertTrue(listener.initialized);
            assertTrue(listener.rotated);
            assertTrue(listener.lines.contains("Line 1"));
            assertTrue(listener.lines.contains("Line 2"));
            assertTrue(listener.lines.contains("Line 3"));
            assertTrue(listener.lines.contains("Line 4"));
            assertTrue(listener.lines.contains("Line 5"));
            assertTrue(listener.lines.contains("Line 6"));
            
        } finally {
            if (tailer != null) {
                tailer.stop();
            }
        }

    }

    

    private boolean rotateLogFile(File src, File tgt, int retries) {
        boolean moved = false;
        while (!moved && retries-- >= 0) {
            try {
                Files.move(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING);
                moved = true;
            } catch (IOException e) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e1) {
                }
            }
        }
        
        return moved;
    }
    

    private PrintWriter openLogFile(File file) throws IOException {
        PrintWriter out = new PrintWriter(file);
        return out;
    }
}
