package com.ca.apm.systemtest.fld.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * Created by haiva01 on 27.11.2014.
 */
public final class ProcessUtils {
    private static final String JPS_EXE = "jps.exe";
    private static Logger log = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * Start process specified by given ProcessBuilder.
     *
     * @param pb ProcessBuilder instance
     * @return new Process
     */
    public static Process startProcess(final ProcessBuilder pb) {
        log.debug("About to start process: {}", pb.command());
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to start process {1}. Exception: {0}", pb.command());
        }
        log.info("Started process: {} ", pb.command());
        return process;
    }

    /**
     * Wait for process to exit for given amount of time.
     *
     * @param process        process to wait for
     * @param amount         amount of time to wait
     * @param unit           unit of time
     * @param abortOnTimeout kill process on timeout
     * @return process exit code
     * @throws RuntimeException thrown when process has not exited in timely manner
     */
    public static int waitForProcess(final Process process, final int amount, final TimeUnit unit,
        boolean abortOnTimeout) throws RuntimeException {
        int exitCode = 0;

        int returncode = new ProcessWithTimeout(process).waitForProcess(unit.toMillis(amount));
        if (returncode != Integer.MIN_VALUE) {
            exitCode = process.exitValue();
            log.debug("Process exit code: {}", exitCode);
        } else {
            final String msg =
                MessageFormat.format("Process did not exit after {0} second(s).",
                    unit.toSeconds(amount));
            log.error(msg);
            if (abortOnTimeout) {
                log.warn("Killing process after timeout has elapsed.");
                process.destroy();
            }

            ErrorUtils.throwRuntimeException(msg);
        }
        return exitCode;
    }

    /**
     * New PorocessBuilder with some defaults.
     *
     * @param inheritIo inherit standard input and output handles from this this
     * @return new ProcessBuilder instance
     */
    public static ProcessBuilder newProcessBuilder(boolean inheritIo) {
        final ProcessBuilder.Redirect redir =
            inheritIo ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE;

        return new ProcessBuilder().redirectErrorStream(true).redirectOutput(redir)
            .redirectInput(redir);
    }

    /**
     * This function returns a ProcessBuilder instance which inherits IO handles
     * and forces stderr to stdout.
     *
     * @return
     */
    public static ProcessBuilder newProcessBuilder() {
        return newProcessBuilder(true);
    }

    private static Long windowsProcessId(Process process) {
        if (process.getClass().getName().equals("java.lang.Win32Process")
            || process.getClass().getName().equals("java.lang.ProcessImpl")) {
            /* determine the pid on windows plattforms */
            try {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(process);

                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                int ret = kernel.GetProcessId(handle);
                log.debug("Detected pid: {}", ret);
                return (long) ret;
            } catch (Throwable e) {
                log.error("Exception.", e);
            }
        }
        return null;
    }

    private static Long unixLikeProcessId(Process process) {
        Class<?> clazz = process.getClass();
        try {
            if (clazz.getName().equals("java.lang.UNIXProcess")) {
                Field pidField = clazz.getDeclaredField("pid");
                pidField.setAccessible(true);
                Object value = pidField.get(process);
                if (value instanceof Integer) {
                    log.debug("Detected pid: {}", value);
                    return ((Integer) value).longValue();
                }
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException
            | NoSuchFieldException ex) {
            log.error("Exception.", ex);
        }
        return null;
    }

    public static Long getPid(Process process) {
        Long pid = unixLikeProcessId(process);
        if (pid == null) {
            pid = windowsProcessId(process);
        }
        return pid;
    }

    public static boolean killWindowsProcess(long pid) {
        try {
            Process p =
                ProcessUtils.newProcessBuilder().command("taskkill", "/F", "/pid", "" + pid)
                    .start();
            p.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("Kill windows process failed.", e);
        }
        return false;
    }

    public static Long findJavaProcessPid(CharSequence key) {
        Long result = null;
        try {
            Path jpsFile = null;

            String javaHome = System.getProperty("java.home", System.getenv("JAVA_HOME"));
            if (javaHome != null) {
                jpsFile = FileUtils.search(Paths.get(javaHome), JPS_EXE);
            }

            if (jpsFile == null || !Files.exists(jpsFile)) {
                jpsFile = FileUtils.searchAll(JPS_EXE);
            }

            if (jpsFile == null || !Files.exists(jpsFile)) {
                log.error("Cannot find {}", JPS_EXE);
                return null;
            }

            ProcessBuilder jpsBuilder =
                new ProcessBuilder(jpsFile.toString(), "-mlvV").redirectErrorStream(true);
            Process jpsProcess = jpsBuilder.start();

            BufferedReader reader =
                new BufferedReader(new InputStreamReader(jpsProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(key)) {
                    String pid = line.substring(0, line.indexOf(' '));
                    log.info("JPS found process PID: {}", pid);
                    result = Long.parseLong(pid);
                    break;
                }
            }
        } catch (NumberFormatException | IOException e) {
            log.error("Problem with searching java process with key: {}", key, e);
        }
        return result;
    }

}
