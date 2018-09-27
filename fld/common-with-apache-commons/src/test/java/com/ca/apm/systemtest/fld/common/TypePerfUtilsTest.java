package com.ca.apm.systemtest.fld.common;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

/**
 * Unit tests for {@link TypePerfUtils}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TypePerfUtilsTest {
    private static final String NO_VALID_COUNTERS_LOW_CASED = "no valid counters";
    
    @BeforeClass
    public static void beforeTestSuite() {
        Assume.assumeTrue(ACFileUtils.isWindowsOS());
    }
    
    @Test
    public void testGetProcessInstanceNameByPID() throws Exception {
        
        Assume.assumeTrue(canRunTypePerf());
        
        
        Process proc = null;
        String procInstName = null;
        
        try {
            ProcessBuilder procBuilder = ProcessUtils.newProcessBuilder();
            Assert.assertNotNull(procBuilder);
            
            procBuilder.command("cmd.exe");
            proc = ProcessUtils.startProcess(procBuilder);
            Assert.assertNotNull(proc);

            Long pid = ProcessUtils.getPid(proc);
            System.out.println("Test process PID: " + pid);

            Assert.assertNotNull(pid);
            Assert.assertTrue(pid > -1);

            procInstName = TypePerfUtils.getProcessInstanceNameByPID(pid, "cmd*");
        } finally {
            if (proc != null) {
                proc.destroy();
                ProcessUtils.waitForProcess(proc, 10, TimeUnit.SECONDS, false);
            }

            System.out.println("Found process instance name: " + procInstName);

            int exitValue = proc.exitValue();
            System.out.println("Test process exit value: " + exitValue);

        }
        
        Assert.assertNotNull(procInstName);
        Assert.assertTrue(procInstName.startsWith("cmd"));
                
        
    }
    
    private static boolean canRunTypePerf() {
        List<String> args = new ArrayList<>(10);
        args.add("cmd.exe");
        args.add("/C");
        args.add(TypePerfUtils.TYPEPERF_COMMAND);
        args.add(MessageFormat.format(TypePerfCounter.PROCESS_ID_COUNTER_TEMPLATE, "svchost*"));
        args.add("-sc");
        args.add("1");
        args.add("-y");
        
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
        ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor(stdOut, stdErr).command(args);
        StartedProcess process = ProcessUtils2.startProcess(procExecutor);
        ProcessUtils2.waitForProcess(process, 1, TimeUnit.MINUTES, true);
        String stdOutStr = new String(stdOut.toByteArray());
        String stdErrStr = new String(stdErr.toByteArray());
        System.out.println("STD OUT: " + stdOutStr);
        System.out.println("STD ERR: " + stdErrStr);

        stdOutStr = stdOutStr.toLowerCase();
        stdErrStr = stdErrStr.toLowerCase();
        if (stdOutStr.contains(NO_VALID_COUNTERS_LOW_CASED) || 
            stdErrStr.contains(NO_VALID_COUNTERS_LOW_CASED)) {
            return false;
        }
        return true;
    }

}
