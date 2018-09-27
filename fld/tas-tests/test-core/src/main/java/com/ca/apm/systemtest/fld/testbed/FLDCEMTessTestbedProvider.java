package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.systemtest.fld.role.CEMTessLoadRole;
import com.ca.apm.systemtest.fld.role.PortForwardingRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

public class FLDCEMTessTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    private ITestbedMachine cemTessLoadMachine;

    private static final Logger log = LoggerFactory.getLogger(FLDCEMTessTestbedProvider.class);
    
    private static final int CEM_TESS_LOAD_FRW_PORT = 8011;
    private static final int CEM_TESS_LOAD_TOMCAT_PORT = 8080;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        cemTessLoadMachine = new TestbedMachine
                .Builder(CEM_TESS_LOAD_MACHINE_ID)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .build();
        return Arrays.asList(cemTessLoadMachine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        
        String dbHost = tasResolver.getHostnameById(EM_DATABASE_ROLE_ID);
        
        String targetHost6Tomcat = tasResolver.getHostnameById(TOMCAT_6_ROLE_ID);
        
        ITestbedMachine timMachine = testbed.getMachineById(FLDConstants.TIM03_MACHINE_ID);
        String timHost = tasResolver.getHostnameById(FLDConstants.TIM03_ROLE_ID);
        
        int targetPort = CEM_TESS_LOAD_TOMCAT_PORT;
        int forwardPort = CEM_TESS_LOAD_FRW_PORT;
        
        forwardLoadViaTIM(tasResolver, timMachine, targetHost6Tomcat, targetPort, forwardPort);
        
        createCEMTessLoadRole(tasResolver, dbHost, timHost+":"+forwardPort);
    }
    
    private void createCEMTessLoadRole(ITasResolver tasResolver, String dbHost, String tessAppUrl) {
        
        CEMTessLoadRole cemTessLoadRole = new CEMTessLoadRole.Builder(CEM_TESS_LOAD_ROLE_ID, tasResolver)
                .setTestAppUrl(tessAppUrl)//socat on tim
                .setDatabase(dbHost)//db host name
                .setDbUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)//db user
                .setDbPass(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)//db pass 
                //.setDefects(15) // 15% of transactions are with defect
                .setSpeedRate(2) //higher number = longer sleep between iterations -> lower load
                .build();
        
        cemTessLoadMachine.addRole(cemTessLoadRole);
    }
    
    private void forwardLoadViaTIM(ITasResolver tasResolver, ITestbedMachine timMachine, 
                              String targetHost, int targetPort, int forwardPort) {
        log.info("Configuring forwarding -> {}:{}", targetHost, targetPort);
        
        String name = "portforward-"+CEM_TESS_LOAD_ROLE_ID;
        PortForwardingRole pfRole = new PortForwardingRole.Builder(name)
            .listenPort(forwardPort).targetIpAddress(targetHost).targetPort(targetPort)
            .workDir(name).build();
        timMachine.addRole(pfRole);
        targetPort = forwardPort;
        log.info("Configuring port forwarding: tim={}, listen={}, targetAddr={}, targetPort={}, workDir={}", timMachine, forwardPort, targetHost, targetPort, name);
    }

}
