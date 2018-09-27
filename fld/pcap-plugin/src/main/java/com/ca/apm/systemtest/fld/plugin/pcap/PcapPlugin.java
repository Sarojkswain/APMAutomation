package com.ca.apm.systemtest.fld.plugin.pcap;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD Pcap plugin interface.
 *
 * @author zunpa01
 */
@PluginAnnotationComponent(pluginType = "pcapPlugin")
public interface PcapPlugin extends Plugin {

    enum PcapCheckResult {
        WRONG_OS, // only supported on Linux host for now
        NOT_INSTALLED,
        INSTALLED
    }

    PcapCheckResult checkPcap();

    void installTcpReplay();

    void downloadPcapFilesFromArtifactory(String groupId, String artifactId, String version);

    int runTcpReplay(String options, String pcapFile);

    void killTcpReplay(int pid);

}
