package com.ca.apm.tests.test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.tests.role.DockerComposeRole;
import com.ca.apm.tests.testbed.DockerTestbed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * @author jirji01
 */
public class DockerTest extends TasTestNgTest {
    @Test(groups = {"docker"})
    @Tas(testBeds = @TestBed(name = DockerTestbed.class, executeOn = DockerTestbed.DOCKER_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test() throws Exception {

        for (final String id : getSerializedIds(DockerTestbed.HAMMOND_ROLE_ID,
                HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(240000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runSerializedCommandFlowFromRole(DockerTestbed.HAMMOND_ROLE_ID, id);
                }
            }.start();
        }

        runSerializedCommandFlowFromRoleAsync(DockerTestbed.EM_CLUSTER_ROLE_ID, DockerComposeRole.UP);

        Thread.sleep(DockerTestbed.getRunDuration(TimeUnit.MILLISECONDS));
    }

    private Iterable<String> getSerializedIds(String roleId, String prefix) {
        Map<String, String> roleProperties =
                Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        HashSet<String> startIds = new HashSet<>();
        for (String key : roleProperties.keySet()) {
            if (key.startsWith(prefix)) {
                startIds.add(key.split("::")[0]);
            }
        }
        return startIds;
    }
}
