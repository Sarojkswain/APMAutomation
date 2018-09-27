/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.HammondDataVersion;
import com.ca.apm.systemtest.fld.role.AGCRegisterRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import com.ca.tas.type.TestBedParallelMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Fifty Providers in one AGC testbed.
 * @author filja01
 *
 */
@TestBedDefinition(parallelMode = TestBedParallelMode.ROLE_LEVEL)
public class FiftyProvidersTestbed implements ITestbedFactory, FLDConstants {
    
    public static final String VESRSION = "99.99.sys-SNAPSHOT";
    public static final String DBVESRSION = "99.99.0.0";
    //public static final String VESRSION = "10.5.1.23";//"99.99.sys-SNAPSHOT";
    //public static final String DBVESRSION = "10.5.1.0";//"99.99.0.0";
    
    public static final int WVPORT = 8082;
    public static final int EMWEBPORT = 8081;
    public static final int EM_PORT = 5001;
    public static final int DB_PORT = 5432;
    
    public static final String DB_ADMIN_USER = "pgadmin";
    public static final String DB_ADMIN_PASSWORD = "password";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "password";
    public static final String DB_NAME = "cemdb";
    
    public static final String INST_DIR_HAM = "C:\\automation\\deployed\\hammond\\";
    public static final String INST_DIR = "E:\\automation\\deployed\\em\\";
    public static final String INSTALLER_DIR = "E:\\automation\\deployed\\installer\\";
    public static final String DATABASE_DIR = "opt/automation/deployed/database";
    public static final String INSTDB_DIR = "opt/automation/deployed/em";
    
    public static final String ADMIN_AUX_TOKEN_HASHED =
        "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    
    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms8192m", "-Xmx8192m",
        "-Dappmap.user=admin", "-Dappmap.token="+FLDMainClusterTestbed.ADMIN_AUX_TOKEN);
    
    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m", "-Xmx1024m",
        "-Dappmap.user=admin", "-Dappmap.token="+FLDMainClusterTestbed.ADMIN_AUX_TOKEN);
  
    public static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
        "-javaagent:./product/webview/agent/wily/Agent.jar",
        "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
        "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1024m", "-Xmx1024m",
        "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError");
    
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("testbed");
        
        List<ITestbedMachine> dbMachines = new ArrayList<ITestbedMachine>();
        for (int i = 0; i < 7; i++) {
        //for (int i = 0; i < 1; i++) {
            ITestbedMachine dbMachine = new TestbedMachine.LinuxBuilder("dbMachine"+i)
                              .platform(Platform.LINUX)
                              .templateId("co65")
                              .build();
            dbMachines.add(dbMachine);
        }
        for (int i = 7; i < 10; i++) {
        //for (int i = 5; i < 5; i++) {    
            ITestbedMachine dbMachine = new TestbedMachine.LinuxBuilder("dbMachine"+i)
                              .platform(Platform.LINUX)
                              .templateId("co66")
                              .build();
            dbMachines.add(dbMachine);
        }
        
        String dbHosts[] = new String[10];
        
        ITestbedMachine agcMachine = new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID)
                              .platform(Platform.LINUX)
                              .templateId("co65_16gb").bitness(Bitness.b64)
                              .build();
        List<ITestbedMachine> providersMachines = new ArrayList<ITestbedMachine>(); 
    
        for (int i = 0; i < 10; i++) {
        //for (int i = 0; i < 1; i++) {    
            ITestbedMachine provMachine = new TestbedMachine.Builder("providerMachine"+i)
                                .templateId("w64_16gb").bitness(Bitness.b64)
                                .build();
            providersMachines.add(provMachine);
        }
        /*
        List<ITestbedMachine> hammMachines = new ArrayList<ITestbedMachine>();
        for (int i = 0; i < 2; i++) {   
            ITestbedMachine hammMachine = new TestbedMachine.Builder("hammMachine"+i)
                                .templateId("w64_16gb").bitness(Bitness.b64)
                                .build();
            hammMachines.add(hammMachine);
        }*/
    
        //DB machines
        int ja = 0;
        
        for (ITestbedMachine dbMachine : dbMachines) {
            String roleId = dbMachine.getMachineId()+"DbRoleId";
            EmRole dbRole = new EmRole.LinuxBuilder(roleId, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database"))
                .dbuser(DB_USER)
                .dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbname(DB_NAME)
                .version(VESRSION)
                //.databaseDir(DATABASE_DIR)
                //.installDir(INSTDB_DIR)
                .build();
            
            dbMachine.addRole(dbRole);
            
            //create databases in DB
            String dbDir = dbRole.getDatabaseDir();
            String instDir = dbRole.getInstallDir();
            Collection<String> data = new ArrayList<String>();
            String scritFile = instDir+"/install/database-scripts/unix/createDB.sh";
            
            data.add("export PGPASSWORD="+DB_PASSWORD);
            /*if (ja == 0) {
                data.add(dbDir+"/bin/createdb "+DB_NAME+"agc -U "+DB_USER);
                data.add(instDir+"/install/database-scripts/unix/createschema.sh -databaseType Postgres -host 127.0.0.1 -port 5432"
                    +" -databaseName "+DB_NAME+"agc -user "+DB_USER+" -password "+DB_PASSWORD+ " -releaseVersion "+DBVESRSION
                    +" -scriptsDir "+instDir+"/install/database-scripts");
            }*/
            for (int i = 0; i < 5; i++) {
            //for (int i = 0; i < 2; i++) {
                data.add(dbDir+"/bin/createdb "+DB_NAME+(i)+" -U "+DB_USER);
                data.add(instDir+"/install/database-scripts/unix/createschema.sh -databaseType Postgres -host 127.0.0.1 -port 5432"
                    +" -databaseName "+DB_NAME+(i)+" -user "+DB_USER+" -password "+DB_PASSWORD+ " -releaseVersion "+DBVESRSION
                    +" -scriptsDir "+instDir+"/install/database-scripts");
            }
            
            FileModifierFlowContext createFileFlow =
                new FileModifierFlowContext.Builder().create(scritFile, data).build();
            ExecutionRole execRole =
                new ExecutionRole.Builder(roleId+"createDBs")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder("createDB.sh").workDir(instDir+"/install/database-scripts/unix").build())
                    .build();
            execRole.after(dbRole);
            dbMachine.addRole(execRole);
            testbed.addMachine(dbMachine);
            
            dbHosts[ja] = tasResolver.getHostnameById(roleId);
            ja++;
        }
        
        //AGC machine
        EmRole agcRole = new EmRole.LinuxBuilder(AGC_ROLE_ID, tasResolver)
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","WebView","Database"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            //.emWebPort(EMWEBPORT)
            //.wvPort(WVPORT)
            //.emPort(EM_PORT)
            .version(VESRSION)
            //.installDir(INST_DIR+AGC_ROLE_ID)
            .dbuser(DB_USER)
            .dbpassword(DB_PASSWORD)
            .dbAdminUser(DB_ADMIN_USER)
            .dbAdminPassword(DB_ADMIN_PASSWORD)
            //.dbname(DB_NAME+"agc")
            //.dbhost(dbHosts[0])
            .configProperty("introscope.apmserver.teamcenter.master", "true")
            .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
            .build();
        
        //agcRole.after(Arrays.asList(dbMachines.get(0).getRoles()));
        agcMachine.addRole(agcRole);
        
        String agcHost = tasResolver.getHostnameById(AGC_ROLE_ID);
        
        int emWebPort = EMWEBPORT + 2;
        int wvPort = WVPORT + 2;
        int emPort = EM_PORT + 2;
        String roleId = null;
        EmRole momRole = null;
        
        String emHost = null;
        AGCRegisterRole regRole = null;
        int j = 0;
        List<IRole> roles = null;
        
        for (ITestbedMachine machine : providersMachines) {
            List<IRole> regRoles = new ArrayList<IRole>();
            List<IRole> hammRoles = new ArrayList<IRole>();
            List<IRole> momRoles = new ArrayList<IRole>();
            IRole lastMom = null;
            for (int i = 0; i < 5; i++) {
            //for (int i = 0; i < 2; i++) {
                roles = Arrays.asList(dbMachines.get(j).getRoles());
                
                emWebPort = EMWEBPORT +2*i;
                wvPort = WVPORT +2*i;
                emPort = EM_PORT +2*i;
                
                roleId = machine.getMachineId()+"momRole"+i;
                momRole = initMom(tasResolver, roleId, emWebPort, wvPort, emPort, i, dbHosts[j]);
                momRole.after(roles);
                momRoles.add(momRole);
                if (lastMom != null) {
                    momRole.after(lastMom);
                }
                lastMom = momRole;
                
                //machine.addRoles(addTradeService(tasResolver, roleId, i, j*10+i, momRole));
                
                emHost = tasResolver.getHostnameById(roleId);
                //if (i < 1) { 
                    hammRoles.add(hammond(tasResolver, i*10+j, emHost+":"+emPort));
                /*} else {
                    if ((i*10+j)%2 == 0)
                        hammMachines.get(0).addRole(hammond(tasResolver, i*10+j, emHost+":"+emPort));
                    else
                        hammMachines.get(1).addRole(hammond(tasResolver, i*10+j, emHost+":"+emPort));
                }*/
                
                RunCommandFlowContext stop = new RunCommandFlowContext.Builder("java")
                    .args(Arrays.asList("-Dport="+emPort,"-jar", momRole.getInstallDir()+ "\\lib\\CLWorkstation.jar", "shutdown"))
                    .name(roleId).ignoreErrors().build();
                
                regRole =
                    new AGCRegisterRole.Builder("registerRole"+roleId, tasResolver)
                        .agcHostName(agcHost)
                        .agcEmWvPort(Integer.toString(EMWEBPORT))
                        .agcWvPort(Integer.toString(WVPORT))
                        .hostName(emHost)
                        .emWvPort(Integer.toString(emWebPort))
                        .wvHostName(emHost)
                        .wvPort(Integer.toString(wvPort))
                        .startCommand(RunCommandFlow.class, ((EmRole) momRole).getEmRunCommandFlowContext())
                        .stopCommand(RunCommandFlow.class, stop)
                        .build();
                
                regRoles.add(regRole);
                machine.addRole(momRole);
            }
            //List<IRole> afterRoles = Arrays.asList(machine.getRoles());
            for (IRole reg : regRoles) {
                //reg.after(afterRoles);
                reg.after(momRoles);
                machine.addRole(reg);
            }
            IRole lastHamm = null;
            for (IRole ham : hammRoles) {
                if (lastHamm != null) {
                    ham.after(lastHamm);
                }
                lastHamm = ham;
                //ham.after(momRoles);
                machine.addRole(ham);
            }
            testbed.addMachine(machine);
            j++;
        }
        testbed.addMachine(agcMachine);
        //testbed.addMachines(hammMachines);
        
        return testbed;
    }

    private EmRole initMom(ITasResolver tasResolver, String roleId, int emWebPort, int wvPort, int emPort, int order, String dbHost) {
        
        List<String> features = Arrays.asList("Enterprise Manager", "WebView");
        
        EmRole momRole = new EmRole.Builder(roleId, tasResolver)
            .silentInstallChosenFeatures(features)
            //.emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            //.configProperty("introscope.enterprisemanager.clustering.mode", "StandAlone")
            .emWebPort(emWebPort)
            .wvPort(wvPort)
            .emPort(emPort)
            .version(VESRSION)
            .dbuser(DB_USER)
            .dbpassword(DB_PASSWORD)
            .dbAdminUser(DB_ADMIN_USER)
            .dbAdminPassword(DB_ADMIN_PASSWORD)
            .dbname(DB_NAME+order)
            .dbhost(dbHost)
            .installDir(INST_DIR+roleId)
            .installerTgDir(INSTALLER_DIR)
            .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
            .build();
        
        return momRole;
    }
    private int RUN_DURATION_SECONDS = 14 * 24 * 60 * 60 * 1000;
    private IRole hammond(ITasResolver tasResolver, int roleId, String hostName) {
        HammondRole hammondRole =
            new HammondRole.Builder("hammond"+roleId, tasResolver).heapMemory("512m").scale(0.5)
                .collector(hostName)
                .installDir(INST_DIR_HAM+roleId)
                .prefix(roleId+"a_")
                .data(HammondDataVersion.FLD_mainframe/*, HammondDataVersion.FLD_tomcat*/)
                .runDuration(RUN_DURATION_SECONDS).build();
        return hammondRole;
    }
}
