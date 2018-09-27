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
public class FldLoadWurlitzerProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    public static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
    
    private ITestbedMachine[] machines = new ITestbedMachine[7];
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machines[0] = new TestbedMachine.Builder(WURLITZER_01_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[1] = new TestbedMachine.Builder(WURLITZER_02_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[2] = new TestbedMachine.Builder(WURLITZER_03_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[3] = new TestbedMachine.Builder(WURLITZER_04_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[4] = new TestbedMachine.Builder(WURLITZER_05_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build(); 
        machines[5] = new TestbedMachine.Builder(WURLITZER_06_MACHINE_ID).platform(Platform.WINDOWS)
            .bitness(Bitness.b64).templateId("w64").build();
        machines[6] = new TestbedMachine.Builder(WURLITZER_07_MACHINE_ID).platform(Platform.WINDOWS)
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
        initWurlitzerHost04(testbed, tasResolver);
        initWurlitzerHost05(testbed, tasResolver);
        initWurlitzerHost06(testbed, tasResolver);
        initWurlitzerHost07(testbed, tasResolver);
    }
    
    private void initWurlitzerHost02(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[1];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE02_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL02_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD02_ROLE_ID, 
            SYSTEM_XML, "2Complex-100agents-2apps-70frontends-100EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL04_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD04_ROLE_ID, 
            SYSTEM_XML, "3Complex-200agents-2apps-25frontends-100EJBsession");
    }
    
    private void initWurlitzerHost03(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[2];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE03_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL06_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD02_ROLE_ID, 
            SYSTEM_XML, "5Complex-350agents-2apps-18frontends-50EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL07_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD03_ROLE_ID, 
            SYSTEM_XML, "3Portlet-20agents-100apps-1940EJBsession");
    }

    
    private void initWurlitzerHost04(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[3];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE04_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL09_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD01_ROLE_ID, 
            SYSTEM_XML, "6Complex-100agents-2apps-40frontends-200EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL10_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD02_ROLE_ID, 
            SYSTEM_XML, "2_1agent-1app-2000backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL10_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD03_ROLE_ID, 
            SYSTEM_XML, "5Portlet-20agents-40apps-1940EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL08_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD04_ROLE_ID, 
            SYSTEM_XML, "001-agent-100-apps-0010-backends.appmap");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL07_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD05_ROLE_ID, 
            SYSTEM_XML, "001-agent-200-apps-0010-backends.appmap");
    }

    
    private void initWurlitzerHost05(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[4];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE05_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL21_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD01_ROLE_ID, 
            SYSTEM_XML, "001-agent-500-apps-0010-backends.appmap_sample");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL22_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD02_ROLE_ID, 
            SYSTEM_XML, "7Complex-100agents-10apps-20frontends-200EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            AGC_COLL01_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD03_ROLE_ID, 
            SYSTEM_XML, "7Complex-100agents-10apps-20frontends-200EJBsession");
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL10_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD05_ROLE_ID, 
            SYSTEM_XML, "WV5-6agents-050-apps-25-backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL01_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD01_ROLE_ID, 
            SYSTEM_XML, "WV1-6agents-050-apps-25-backends");
    }
    
    
    private void initWurlitzerHost06(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[5];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE06_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL01_ROLE_ID, WURLITZER_LOAD_BASE06_LOAD01_ROLE_ID, 
            SYSTEM_XML, "1Complex-100agents-2apps-70frontends-50EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL03_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, 
            SYSTEM_XML, "1Portlet-23agents-110apps-1000EJBsession");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL09_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD05_ROLE_ID, 
            SYSTEM_XML, "1_1agent-1app-2000backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL05_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD05_ROLE_ID, 
            SYSTEM_XML, "3_1agent-1app-2000backends");
    }

    
    private void initWurlitzerHost01(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[0];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE01_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);

        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL03_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD01_ROLE_ID, 
            SYSTEM_XML, "WV1-10agents-050-apps-25-backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL04_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD02_ROLE_ID, 
            SYSTEM_XML, "WV2-6agents-050-apps-25-backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL05_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD03_ROLE_ID, 
            SYSTEM_XML, "WV3-6agents-050-apps-25-backends");
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL09_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD04_ROLE_ID, 
            SYSTEM_XML, "WV4-6agents-050-apps-25-backends");
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL08_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD04_ROLE_ID, 
            SYSTEM_XML, "4Portlet-20agents-100apps-1940EJBsession");
    }
    
    
    private void initWurlitzerHost07(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine wurlitzerMachine = machines[6];
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE07_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);
        
        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL05_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD01_ROLE_ID, 
            SYSTEM_XML, "4Complex-300agents-2apps-10frontends-100EJBsession");
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
