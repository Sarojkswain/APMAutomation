
package com.ca.apm.systemtest.fld.plugin.agentdownload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ca.apm.systemtest.fld.common.ProcessWithTimeout;

public class AgentDownloadPluginImplTest {

    private static final String AGENT_DOWNLOAD_PLUGIN = "agentDownloadPlugin";
    ApplicationContext ctx = null;
    File f1 = new File("first.txt");
    File f2 = new File("second.txt");
    File f3 = new File("third.txt");

    @Before
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("fldagent-test-context.xml");
        deleteFiles();
    }

    @After
    public void shutdown() {
        deleteFiles();
    }

    private void deleteFiles() {
        f1.delete();
        f2.delete();
        f3.delete();
    }

    // @Test
    public void testBatch() throws IOException, InterruptedException {
        ProcessBuilder pBuild = new ProcessBuilder("cmd", "/c", "target\\classes\\startAgent.bat");
        runScript(pBuild);
        deleteFiles();

        // Cygwin should be installed on Windows
        pBuild =
            new ProcessBuilder("c:\\cygwin64\\bin\\sh.exe", "-c", "target/classes/startAgent.sh");
        runScript(pBuild);
        deleteFiles();
    }

    private void runScript(ProcessBuilder procBuild) throws IOException, InterruptedException {
        String preClasspath =
            "target" + File.separator + "test-classes" + File.pathSeparator + "target"
                + File.separator + "classes";
        procBuild.environment().put("PRE_CLASSPATH", preClasspath);
        procBuild.inheritIO();
        Process proc = procBuild.start();

        int returncode = new ProcessWithTimeout(proc).waitForProcess(5000);
        if (returncode == Integer.MIN_VALUE) {
            proc.destroy();
        }
        assertEquals("Bach is executing too long", Integer.MIN_VALUE, returncode);
        assertEquals(0, proc.exitValue());
        assertTrue("Wrong batch file", f1.exists() && f2.exists() && f3.exists());
    }

    // @Test
    public void testDownloadNewVersion() {
        assertTrue(AGENT_DOWNLOAD_PLUGIN + " is not in SpringContext",
            ctx.containsBean(AGENT_DOWNLOAD_PLUGIN));
        AgentDownloadPluginImpl dwnlPlugin =
            (AgentDownloadPluginImpl) ctx.getBean(AGENT_DOWNLOAD_PLUGIN);

        dwnlPlugin.downloadNewVersion();
        Path curVersion = checkFiles(dwnlPlugin);

        emulateOldVersion(curVersion, 10);

        dwnlPlugin.downloadNewVersion();
        checkFiles(dwnlPlugin);
    }

    private long emulateOldVersion(Path curDir, int shiftTime) {
        String fileName = curDir.getFileName().toString();
        long baseTime = Long.MIN_VALUE;
        try {
            baseTime =
                Long.parseLong(fileName.substring(AgentDownloadPluginImpl.AGENT_PREFIX.length()));
            File renameTo =
                new File(curDir.getParent().toString() + File.separator
                    + AgentDownloadPluginImpl.AGENT_PREFIX + (baseTime - shiftTime));
            curDir.toFile().renameTo(renameTo);
        } catch (NumberFormatException nfe) {
            fail("Wrong filename format " + fileName);
        }
        return baseTime;
    }

    private Path checkFiles(AgentDownloadPluginImpl dwnlPlugin) {
        File link =
            new File(dwnlPlugin.rootDir + File.separator + AgentDownloadPluginImpl.AGENT_PREFIX
                + "current");
        Path pLink = link.toPath();
        assertTrue("No agent-current link on filesystem",
            link.exists() && Files.isSymbolicLink(pLink));
        Path sLinkTarget = null;
        try {
            sLinkTarget = Files.readSymbolicLink(pLink);
            assertTrue("Link is not pointing to directory", Files.isDirectory(sLinkTarget));
            Path zipFile =
                Paths.get(sLinkTarget.getParent().toString(), sLinkTarget.getFileName() + ".zip");
            assertTrue("Zip file " + zipFile.toString() + " was removed", Files.isReadable(zipFile));
        } catch (IOException e) {
            fail("Can not read symbolic link " + link);
        }
        return sLinkTarget;
    }

}
