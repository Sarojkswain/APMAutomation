package com.ca.apm.systemtest.fld.plugin.pcap;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Plugin for Pcap manipulation on agent's host.
 *
 * @author zunpa01
 */
public class PcapPluginImpl extends AbstractPluginImpl implements PcapPlugin {

    private static final Logger log = LoggerFactory.getLogger(PcapPluginImpl.class);

    private static final String DEFAULT_ARTIFACTORY_URL = "http://isl-dsdc.ca.com/artifactory/apm-third-party-local";
    private static final String DOWNLOAD_PREFIX = "/opt/CA/pcap/";

    @Autowired
    ArtifactoryDownloadMethod dm;

    @Override
    @ExposeMethod(description = "Check if tcpreplay is installed")
    public PcapCheckResult checkPcap() {
        if (!SystemUtils.IS_OS_LINUX) {
            return (PcapCheckResult.WRONG_OS);
        }
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command("yum", "list", "installed",
            "tcpreplay");
        int exitCode = ProcessUtils.waitForProcess(ProcessUtils.startProcess(pb), 2, TimeUnit.MINUTES, true);
        if (exitCode == 0) {
            return (PcapCheckResult.INSTALLED);
        }
        return (PcapCheckResult.NOT_INSTALLED);
    }

    @Override
    @ExposeMethod(description = "Install tcpreplay")
    public void installTcpReplay() {
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command("yum", "-y", "install",
            "tcpreplay");
        ProcessUtils.waitForProcess(ProcessUtils.startProcess(pb), 10, TimeUnit.MINUTES, true);
    }

    @Override
    @ExposeMethod(description = "Download pcap files from Artifactory")
    public void downloadPcapFilesFromArtifactory(String groupId, String artifactId, String version) {
        ArtifactFetchResult fetchRes = null;
        File downloadFile = new File(DOWNLOAD_PREFIX);
        downloadFile.mkdirs();

        fetchRes = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL,
                groupId, artifactId, version, null, "zip");


        File zipFile = fetchRes.getFile().getAbsoluteFile();
        try {
            ZipFile artifactZip = new ZipFile(zipFile);
            artifactZip.extractAll(downloadFile.getAbsolutePath());
            log.info("Extracted ZIP file {}.", artifactZip.getFile().getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                zipFile.getAbsolutePath());
        }
    }

    @Override
    @ExposeMethod(description = "Run tcpreplay with specified pcap file")
    public int runTcpReplay(String options, String pcapFile) {
        String[] split = options.split(" ");
        String[] command = new String[2 + split.length];
        int index = 0;
        command[index++] = "tcpreplay";
        for (String s : split) {
            command[index++] = s;
        }
        command[index++] = DOWNLOAD_PREFIX + pcapFile;
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command(command);
        Process p = ProcessUtils.startProcess(pb);
        int pid = -1;
        if (p.getClass().getName().compareToIgnoreCase("java.lang.UNIXProcess") == 0) {
            try {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(p);
            } catch (Throwable t) {
            }
        }
        log.info("Running tcpreplay {} {}({})", options, pcapFile, String.valueOf(pid));
        return (pid);
    }

    @Override
    @ExposeMethod(description = "Kill tcpreplay")
    public void killTcpReplay(int pid) {
        // TODO: SECURITY! check what process we're actually killing
        log.info("Killing process with pid {}", String.valueOf(pid));
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command("kill", String.valueOf(pid));
        ProcessUtils.startProcess(pb);
    }

}
