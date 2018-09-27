/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.systemtest.fld.artifact.metricsynth.BasicScenario001Artifact;
import com.ca.apm.systemtest.fld.role.MetricSynthRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * 
 * @author keyja01
 *
 */
public class FldLoadMetricSynthProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    private ITestbedMachine[] machines = new ITestbedMachine[7];

    @Override
    public Collection<ITestbedMachine> initMachines() {
        machines[0] = new TestbedMachine.Builder(METRICSYNTH_01_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[1] = new TestbedMachine.Builder(METRICSYNTH_02_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[2] = new TestbedMachine.Builder(METRICSYNTH_03_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[3] = new TestbedMachine.Builder(METRICSYNTH_04_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[4] = new TestbedMachine.Builder(METRICSYNTH_05_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[5] = new TestbedMachine.Builder(METRICSYNTH_06_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machines[6] = new TestbedMachine.Builder(METRICSYNTH_07_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        
        return Arrays.asList(machines);
    }
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String em01 = tasResolver.getHostnameById(EM_COLL01_ROLE_ID);
        String em02 = tasResolver.getHostnameById(EM_COLL02_ROLE_ID);
        String em03 = tasResolver.getHostnameById(EM_COLL03_ROLE_ID);
        String em04 = tasResolver.getHostnameById(EM_COLL04_ROLE_ID);
        String em05 = tasResolver.getHostnameById(EM_COLL05_ROLE_ID);
        String em06 = tasResolver.getHostnameById(EM_COLL06_ROLE_ID);
        String em07 = tasResolver.getHostnameById(EM_COLL07_ROLE_ID);
        String em08 = tasResolver.getHostnameById(EM_COLL08_ROLE_ID);
        String em09 = tasResolver.getHostnameById(EM_COLL09_ROLE_ID);
        String em10 = tasResolver.getHostnameById(EM_COLL10_ROLE_ID);
        initMetricSynthRole(tasResolver, machines[0], METRICSYNTH_01_ROLE_ID, new String[] {em01, em01, em02}, new String[] {"praha", "amsterdam", "copenhagen"}, BasicScenario001Artifact.Version1_0.getArtifact());
        initMetricSynthRole(tasResolver, machines[1], METRICSYNTH_02_ROLE_ID, new String[] {em02, em03, em03}, new String[] {"tallinn", "paris", "helsinki"}, BasicScenario001Artifact.Version1_0.getArtifact());
        initMetricSynthRole(tasResolver, machines[2], METRICSYNTH_03_ROLE_ID, new String[] {em04, em04, em05}, new String[] {"budapest", "bucharest", "vilnius"}, BasicScenario001Artifact.Version1_0.getArtifact());
        initMetricSynthRole(tasResolver, machines[3], METRICSYNTH_04_ROLE_ID, new String[] {em05, em06, em06}, new String[] {"tbilisi", "athens", "berlin"}, BasicScenario001Artifact.Version1_0.getArtifact());
        initMetricSynthRole(tasResolver, machines[4], METRICSYNTH_05_ROLE_ID, new String[] {em07, em08, null}, new String[] {"warsaw", "podgorica", "monaco"}, BasicScenario001Artifact.Version1_0_2cities.getArtifact());
        initMetricSynthRole(tasResolver, machines[5], METRICSYNTH_06_ROLE_ID, new String[] {em08, em09, em09}, new String[] {"baku", "bratislava", "vaduz"}, BasicScenario001Artifact.Version1_0.getArtifact());
        initMetricSynthRole(tasResolver, machines[6], METRICSYNTH_07_ROLE_ID, new String[] {em10, em10, null}, new String[] {"belgrade", "bern", null}, BasicScenario001Artifact.Version1_0_2cities.getArtifact());
        
    }

    private void initMetricSynthRole(ITasResolver tasResolver, ITestbedMachine machine, String roleId, String[] collectors, String[] cities, Artifact artifact) {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("tas-czfld-na", collectors[0]);
        replaceMap.put("tas-czfld-n39", collectors[1]);
        if (collectors[2] != null) {
            replaceMap.put("tas-czfld-n3c", collectors[2]);
        }
        replaceMap.put("praha", cities[0]);
        replaceMap.put("oslo", cities[1]);
        if (cities[2] != null) {
            replaceMap.put("berlin", cities[2]);
        }
        MetricSynthRole msRole = new MetricSynthRole.Builder(roleId, tasResolver)
            .autostart().maxHeap(2048)
            .collectorMap(replaceMap)
            .loadScenario(artifact)
            .databaseFile("metricsynth")
            .build();
        machine.addRole(msRole);
    }

}
