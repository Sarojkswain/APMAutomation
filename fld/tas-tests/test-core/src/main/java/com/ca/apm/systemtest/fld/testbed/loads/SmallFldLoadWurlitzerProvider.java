/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;
import com.google.common.net.HostAndPort;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * @author keyja01
 *
 */
public class SmallFldLoadWurlitzerProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    public static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
    
    private ITestbedMachine[] machines = new ITestbedMachine[3];
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machines[0] = new TestbedMachine.Builder(WURLITZER_01_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[1] = new TestbedMachine.Builder(WURLITZER_02_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[2] = new TestbedMachine.Builder(WURLITZER_03_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build();
            
        return Arrays.asList(machines);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed,
     * com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        initWurlitzerHost01(testbed, tasResolver);
        initWurlitzerHost02(testbed, tasResolver);
        initWurlitzerHost03(testbed, tasResolver);
    }
    
    private void initWurlitzerHost02(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[1];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE02_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL01_ROLE_ID, WURLITZER_LOAD_BASE06_LOAD01_ROLE_ID, 
            SYSTEM_XML, "3_1agent-1app-2000backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL03_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, 
            SYSTEM_XML, "Portlet-10agents-110apps-1940EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL04_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD02_ROLE_ID, 
            SYSTEM_XML, "WV1-6agents-050-apps-25-backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL05_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD03_ROLE_ID, 
            SYSTEM_XML, "001-agent-200-apps-0010-backends.appmap");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL06_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD03_ROLE_ID, 
            SYSTEM_XML, "WV5-6agents-050-apps-25-backends");
    }
    
    private void initWurlitzerHost03(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[2];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE03_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL08_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD02_ROLE_ID, 
            SYSTEM_XML, "Portlet-20agents-110apps-1940EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL02_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD02_ROLE_ID, 
            SYSTEM_XML, "2_1agent-1app-2000backends");
        
    }
        
    private void initWurlitzerHost01(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[0];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE01_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);

        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL07_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD01_ROLE_ID, 
            SYSTEM_XML, "Complex-25agents-2apps-70frontends-200EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL09_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD03_ROLE_ID, 
            SYSTEM_XML, "Complex-50agents-2apps-70frontends-200EJBsession");
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL10_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD04_ROLE_ID, 
            SYSTEM_XML, "Portlet-05agents-110apps-1940EJBsession");
    }
    
    
    private void addWurlitzer(ITestbed testbed, ITasResolver tasResolver, ITestbedMachine machine,
                              WurlitzerBaseRole wurlitzerBaseRole, String collectorRoleId,
                              String wurlitzerRoleId, String buildFileLocation, 
                              String target) {
        WurlitzerLoadRole wurlitzerLoadrole =
            new WurlitzerLoadRole.Builder(wurlitzerRoleId, tasResolver)
                .overrideEM(HostAndPort.fromParts(tasResolver.getHostnameById(collectorRoleId), EM_PORT))
                .buildFileLocation(buildFileLocation)
                .target(target)
                .logFile(target + ".log")
                .wurlitzerBaseRole(wurlitzerBaseRole).build();
        machine.addRole(wurlitzerLoadrole);
    }
}
