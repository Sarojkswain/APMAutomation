package com.ca.apm.tests.system;

import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.testbed.NodeJSLoadGATestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

/**
 * @author kurma05
 */
public class JmeterLoadGAProbeTest extends JmeterLoadTest {

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_normal"})
    public void testTixChangeLoad() {

        super.testTixChangeLoad();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_errors"})
    public void testTixChangeErrors() {

        super.testTixChangeErrors();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_ttsampling"})
    public void testTixChangeTTSampling() {

        super.testTixChangeTTSampling();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_stalls"})
    public void testTixChangeStalls() {

        super.testTixChangeStalls();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_urls_clamp"})
    public void testTixChangeManyUrlGroupsMetricClamp() {

        super.testTixChangeManyUrlGroupsMetricClamp();
    }


    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_urls"})
    public void testTixChangeManyUrlGroups() {

        super.testTixChangeManyUrlGroups();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_mongodb"})
    public void testTixChangeMongoDb() {

        super.testTixChangeMongoDb();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_httpbackend"})
    public void testTixChangeHttpBackend() {

        super.testTixChangeHttpBackend();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_httpbackendnofrontend"})
    public void testTixChangeHttpBackendNoFrontend() {

        super.testTixChangeHttpBackendNoFrontend();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_async"})
    public void testTixChangeAsync() {

        super.testTixChangeAsync();
    }

    @Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSLoadGATestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MAMMOTH, owner = OWNER)
    @Test(groups = {"nodeagent_system_gaprobe", "nodeagent_system_gaprobe_restart_collector"})
    public void testTixChangeRestartCollector() {

        super.testTixChangeRestartCollector();
    }
}