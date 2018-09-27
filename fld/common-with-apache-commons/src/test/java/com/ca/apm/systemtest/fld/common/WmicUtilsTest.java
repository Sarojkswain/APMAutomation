package com.ca.apm.systemtest.fld.common;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link WmicUtils}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class WmicUtilsTest {

    @BeforeClass
    public static void beforeTestSuite() {
        Assume.assumeTrue(ACFileUtils.isWindowsOS());
    }

    @Test
    public void testGetPID() throws IOException {
        ProcessBuilder procBuilder = ProcessUtils.newProcessBuilder();
        Assert.assertNotNull(procBuilder);
        
        procBuilder.command("cmd.exe", "/Q", "/A");
        Process proc = null;
        try {
            proc = ProcessUtils.startProcess(procBuilder);
            Assert.assertNotNull(proc);
            
            Long pid = ProcessUtils.getPid(proc);
            System.out.println("Test process PID (from Java): " + pid);

            Assert.assertNotNull(pid);
            Assert.assertTrue(pid > -1);

            Long pidByWmic = WmicUtils.getPid("cmd", "/Q /A");
            System.out.println("Test process PID (from wmic): " + pidByWmic);

            Assert.assertNotNull(pidByWmic);
            Assert.assertEquals(pid, pidByWmic);
        } finally {
            proc.destroy();
            ProcessUtils.waitForProcess(proc, 10, TimeUnit.SECONDS, false);
        }
        
    }

}
