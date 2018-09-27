package com.ca.apm.systemtest.fld.testbed;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.flow.RunCommandCheckFlow;
import com.ca.apm.systemtest.fld.role.DelayRole;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;


@TestBedDefinition
public class CleanFLDMainClusterTestBed implements ITestbedFactory, FLDConstants {
    
    public static final String SSH_DESTINATION_MKDIR_ROLE_ID = "sshDestinationMKDirRoleId";
    public static final String UNINSTALL_EXECUTABLE = "Uninstall_Introscope";
    public static final String WEBVIEW_EXECUTABLE = "Introscope_WebView";
    
    public static final String DEFAULT_WORK_BACKUP_DIR = "/home/sw/archive";    //dir have to exist on target machine
//    public static final String DEFAULT_BACKUP_HOST = "fldcoll12c";
//    public static final String DEFAULT_BACKUP_USER = "root";
//    public static final String DEFAULT_BACKUP_USERPASS = "Phox9tai";

    private static final Logger log = LoggerFactory.getLogger(CleanFLDMainClusterTestBed.class);
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public ITestbed create(final ITasResolver tasResolver) {
        
        Testbed testbed = new Testbed("CleanFLDMainClusterTestbed");

        // new timestamp for backup folder
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY-HH-mm");
        String time = dateFormat.format(now);
       
        String backupDir = "testrundata_" + fldConfig.getEmVersion() + "_T" + time;
        
        String agcTemplateId = fldConfig.getMachineTemplateOverrides().get(COLL_AGC_MACHINE_ID);
        if (agcTemplateId == null) {
            agcTemplateId = FLD_AGC_TMPL_ID;
        }
        ITestbedMachine agcMachine = new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID)
                              .templateId(agcTemplateId).bitness(Bitness.b64)
                              .build();
        EmptyRole agcRole = new EmptyRole.Builder(AGC_ROLE_ID, tasResolver).build();
        agcMachine.addRole(agcRole);
        
        testbed.addMachine(cleanAGCEMsWVsDB(testbed, agcRole,
            agcMachine, tasResolver, backupDir, time));
        
        //clean and backup Main cluster
        for (int i = 0; i < EM_COLL_ROLES.length; i++) {
            ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(COLL_MACHINES[i])
                                  .templateId(FLD_COLL_TMPLS[i]).bitness(Bitness.b64)
                                  .build();
            EmptyRole collRole = new EmptyRole.Builder(EM_COLL_ROLES[i], tasResolver).build();
            collectorMachine.addRole(collRole);
            
            testbed.addMachine(cleanEMsAndBackup(testbed, collRole,
                collectorMachine, false, tasResolver, backupDir, time, false));
        }
        
        String wvTemplateId = fldConfig.getMachineTemplateOverrides().get(WEBVIEW_MACHINE_ID);
        if (wvTemplateId == null) {
            wvTemplateId = FLD_WEBVIEW_TMPL_ID;
        }
        //WebView machine
        ITestbedMachine webviewMachine = 
            new TestbedMachine.LinuxBuilder(WEBVIEW_MACHINE_ID)
                              .templateId(wvTemplateId).bitness(Bitness.b64)
                              .build();
        EmptyRole webRole = new EmptyRole.Builder(EM_WEBVIEW_ROLE_ID, tasResolver).build();
        webviewMachine.addRole(webRole);
        
        testbed.addMachine(cleanEMsAndBackup(testbed, webRole,
            webviewMachine, true, tasResolver, backupDir, time, false));
        
        //MOM machine
        ITestbedMachine momMachine = 
            new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID)
                              .templateId(FLD_MOM_TMPL_ID).bitness(Bitness.b64)
                              .build();
        EmptyRole momRole = new EmptyRole.Builder(EM_MOM_ROLE_ID, tasResolver).build();
        momMachine.addRole(momRole);
        
        testbed.addMachine(cleanEMsAndBackup(testbed, momRole,
            momMachine, false, tasResolver, backupDir, time, true));
        
        String dbTemplateId = fldConfig.getMachineTemplateOverrides().get(DATABASE_MACHINE_ID);
        if (dbTemplateId == null) {
            dbTemplateId = FLD_DATBASE_TMPL_ID;
        }
        //database machine
        ITestbedMachine databaseMachine =
            new TestbedMachine.LinuxBuilder(DATABASE_MACHINE_ID)
                              .templateId(dbTemplateId)
                              .bitness(Bitness.b64)
                              .build();
        EmptyRole dbRole = new EmptyRole.Builder(EM_DATABASE_ROLE_ID, tasResolver).build();
        databaseMachine.addRole(dbRole);
        
        testbed.addMachine(cleanStanaloneDB(testbed, dbRole, databaseMachine));

        return testbed;
    }
    
    
    private ITestbedMachine cleanEMsAndBackup(ITestbed deployedTestBed, EmptyRole emRole, ITestbedMachine emMachine, boolean wv,
                                              ITasResolver tasResolver, String backupDir, String timeSuffix, boolean killAPMSqlServer) {
        String roleId = emRole.getRoleId();
        String emHost = tasResolver.getHostnameById(roleId);
        boolean isRunning = checkEmIsRunning(deployedTestBed, wv, emHost);
        ExecutionRole.Builder builder = new ExecutionRole.Builder("stop_" + roleId);
        if (isRunning) {
            if (wv) {
                builder.flow(RunCommandCheckFlow.class,
                    wvStopCommandFlowContext(roleId));
            } else {
                builder.flow(RunCommandCheckFlow.class,
                    emStopCommandFlowContext(roleId));
            }
        }
        ExecutionRole stopRole = builder.build();
        
        DelayRole delayRole = new DelayRole.Builder(stopRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRole.after(stopRole);
           
        //backup logs
        String archive = "/home/sw/archive/testrundata_" + emMachine.getMachineId() + "_" + fldConfig.getEmVersion() + "_T" + timeSuffix + ".tar.gz";
        ExecutionRole archiveRole = FLDMainClusterTestBedCleaner.getArchiveRole(roleId, archive, fldConfig.isSkipBackup(), wv);
        archiveRole.after(delayRole);

        // SSH transport
        UniversalRole manipulationRole = FLDMainClusterTestBedCleaner.getSshTransportRole(roleId, tasResolver, fldConfig, backupDir, archive);
        manipulationRole.after(archiveRole);

        ExecutionRole uninstallEmRole;
        if (fldConfig.isDockerMode()) {
            RunCommandFlowContext context = new RunCommandFlowContext.Builder("/bin/bash")
                    .args(Arrays.asList("-c", "docker rm -f $(docker ps -a -q) ; docker rmi $(docker images -q)"))
                    .doNotPrependWorkingDirectory()
                    .build();
            uninstallEmRole = new ExecutionRole.Builder("remove_docker_" + roleId)
                    .flow(RunCommandCheckFlow.class, context)
                    .build();
        } else {
            uninstallEmRole = new ExecutionRole.Builder("uninstall_" + roleId).flow(RunCommandCheckFlow.class,
                    emUninstallCommandFlowContext(roleId)).build();
        }
        uninstallEmRole.after(manipulationRole);

        ExecutionRole deleteEmRole = FLDMainClusterTestBedCleaner.getDeleteRole(roleId, false);
        deleteEmRole.after(uninstallEmRole);

        if (killAPMSqlServer) {
            ExecutionRole stopAPMSqlServerRole = new ExecutionRole.Builder("stop_APMSqlSever_" + roleId)
                .flow(RunCommandCheckFlow.class, FLDMainClusterTestBedCleaner.apmSqlServerStopCommandFlowContext(roleId))
                .build();
                stopAPMSqlServerRole.before(uninstallEmRole);
                emMachine.addRole(stopAPMSqlServerRole);
        }

        FLDMainClusterTestBedCleaner.addKillMemoryMonitorRole(emMachine, roleId);
        
        //log monitor pidFile for log monitor kill
        String pidFile = "/opt/automation/deployed/log-monitor/tailer.pid";
        
        return emMachine.addRole(stopRole, uninstallEmRole, archiveRole, manipulationRole, deleteEmRole, 
                                FLDMainClusterTestBedCleaner.getKillLogMonitorRole(roleId, pidFile), delayRole);
    }
    
    private boolean checkEmIsRunning(ITestbed deloyedTestBed, boolean wv, String emHost) {
        
        if(!wv) {
            log.info("host name: "+emHost+" port: 5001");
            return NetworkUtils.isServerListening(emHost+".ca.com", 5001);
        } else {
            log.info("host name: "+emHost+" port: "+FLDMainClusterTestbed.WVPORT);
            return NetworkUtils.isServerListening(emHost+".ca.com", FLDMainClusterTestbed.WVPORT);
        }
    }

    private ITestbedMachine cleanStanaloneDB(ITestbed deployedTestBed, EmptyRole emRole, ITestbedMachine emMachine) {
        
        String roleId = emRole.getRoleId();
        ExecutionRole uninstallEmRole;

        if (fldConfig.isDockerMode()) {
            RunCommandFlowContext context = new RunCommandFlowContext.Builder("/bin/bash")
                    .args(Arrays.asList("-c", "docker rm -f $(docker ps -a -q) ; docker rmi $(docker images -q)"))
                    .doNotPrependWorkingDirectory()
                    .build();
            uninstallEmRole = new ExecutionRole.Builder("remove_docker_" + roleId)
                    .flow(RunCommandCheckFlow.class, context)
                    .build();
        } else {
            uninstallEmRole = new ExecutionRole.Builder("uninstall_" + roleId).flow(RunCommandCheckFlow.class,
                    emUninstallCommandFlowContext(roleId)).build();
        }

        ExecutionRole deleteEmRole = FLDMainClusterTestBedCleaner.getDeleteRole(roleId, true);
        deleteEmRole.after(uninstallEmRole);

        if (fldConfig.isOracleMode()) {
            Collection<String> data = Arrays.asList(
                "export ORACLE_HOME=" + FLDMainClusterTestbed.ORACLE_HOME,
                FLDMainClusterTestbed.ORACLE_HOME + FLDMainClusterTestbed.SQLPLUS_LOCATION + " " 
                    + FLDMainClusterTestbed.ORACLE_SYSDB_USERNAME + "/" 
                    + FLDMainClusterTestbed.ORACLE_SYSDB_PASSWORD + "@" 
                    + FLDMainClusterTestbed.ORACLE_SID + " as SYSDBA <<EOF\n"
                    + "alter session set \"_oracle_script\"=true;\n"
                    + "BEGIN\n"
                    + "  FOR c IN (\n"
                    + "    SELECT s.sid,s.serial# FROM v$session s\n"
                    + "    WHERE (s.username = '"+FLDMainClusterTestbed.ORACLE_DB_USERNAME+"') "
                    + "OR (s.username = '"+FLDSecondClusterTestbed.ORACLE_DB_USERNAME+"') "
                    + "OR (s.username = '"+FLDAGCTestbed.ORACLE_DB_USERNAME+"')\n"
                    + "  )\n"
                    + "  LOOP\n"
                    + "    EXECUTE IMMEDIATE 'alter system disconnect session ''' ||" 
                    + " c.sid || ',' || c.serial# || ''' immediate';\n"
                    + "  END LOOP;\n"
                    + "END;\n"
                    //+ "drop user "+FLDMainClusterTestbed.ORACLE_DB_USERNAME+" cascade;\n"
                    //+ "drop user "+FLDSecondClusterTestbed.ORACLE_DB_USERNAME+" cascade;\n"
                    //+ "drop user "+FLDAGCTestbed.ORACLE_DB_USERNAME+" cascade;\n"
                    + "EOF");
            
            FileModifierFlowContext createFileFlow =
                new FileModifierFlowContext.Builder().create("/tmp/"+EM_DATABASE_ROLE_ID+"_drop.sh", data).build();
            ExecutionRole execRole =
                new ExecutionRole.Builder(EM_DATABASE_ROLE_ID + "_drop")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+EM_DATABASE_ROLE_ID+"_drop.sh").build()).build();
            execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, "/tmp/"+EM_DATABASE_ROLE_ID+"_drop.sh");
            emMachine.addRole(execRole);
        }
        emMachine.addRole(uninstallEmRole, deleteEmRole);
                
        return emMachine;
    }

    private ITestbedMachine cleanAGCEMsWVsDB(ITestbed deployedTestBed, EmptyRole agcRole, ITestbedMachine agcMachine, 
                                             ITasResolver tasResolver, String backupDir, String timeSuffix) {
        
        String roleId = agcRole.getRoleId();
        String emHost = tasResolver.getHostnameById(roleId);
        boolean isRunning = checkEmIsRunning(deployedTestBed, false, emHost);
        boolean isRunningWV = checkEmIsRunning(deployedTestBed, true, emHost);
        
        ExecutionRole.Builder builder = new ExecutionRole.Builder("stopWV_" + roleId);
        if (isRunningWV) {
                builder.flow(RunCommandCheckFlow.class,
                    wvStopCommandFlowContext(roleId));
        }
        ExecutionRole stopWvRole = builder.build();
        
        DelayRole delayRoleWV = new DelayRole.Builder(stopWvRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRoleWV.after(stopWvRole);
        
        builder = new ExecutionRole.Builder("stopEm_" + roleId);
        if (isRunning) {
                builder.flow(RunCommandCheckFlow.class,
                    emStopCommandFlowContext(roleId));
        }
        ExecutionRole stopEmRole = builder.build();
        stopEmRole.after(delayRoleWV);
        
        DelayRole delayRoleEM = new DelayRole.Builder(stopEmRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRoleEM.after(stopEmRole);
        
        //backup logs
        String archive = "/home/sw/archive/testrundata_" + agcMachine.getMachineId() + "_" + fldConfig.getEmVersion() + "_T" + timeSuffix + ".tar.gz";
        ExecutionRole archiveRole = FLDMainClusterTestBedCleaner.getArchiveRole(roleId, archive, fldConfig.isSkipBackup(), false);
        archiveRole.after(delayRoleEM);
       
        // SSH transport
        UniversalRole manipulationRole = FLDMainClusterTestBedCleaner.getSshTransportRole(roleId, tasResolver, fldConfig, backupDir, archive);
        manipulationRole.after(archiveRole);
        
        ExecutionRole uninstallEmRole =
            new ExecutionRole.Builder("uninstall_" + roleId).flow(RunCommandCheckFlow.class,
                emUninstallCommandFlowContext(roleId)).build();
        uninstallEmRole.after(manipulationRole);
        
        ExecutionRole deleteEmRole = FLDMainClusterTestBedCleaner.getDeleteRole(roleId, true);
        deleteEmRole.after(uninstallEmRole);
        
        FLDMainClusterTestBedCleaner.addKillMemoryMonitorRole(agcMachine, roleId);

        //log monitor pidFile for log monitor kill
        String pidFile = "/opt/automation/deployed/log-monitor/tailer.pid";
        
        return agcMachine.addRole(stopWvRole, stopEmRole, manipulationRole, uninstallEmRole, archiveRole, 
                                  deleteEmRole, FLDMainClusterTestBedCleaner.getKillLogMonitorRole(roleId, pidFile), 
                                  delayRoleWV, delayRoleEM);
    }
    
    private RunCommandFlowContext emStopCommandFlowContext(String nameId) {
        RunCommandFlowContext.Builder builder = 
            new RunCommandFlowContext.Builder("java")
                .args(
                    Arrays.asList(
                        "-jar",
                        FLDMainClusterTestbed.INSTALL_DIR + "/lib" + "/CLWorkstation.jar"
                        , "shutdown"))
                .name(nameId);
        
        return builder.build();
    }

    private RunCommandFlowContext emUninstallCommandFlowContext(String nameId) {
        RunCommandFlowContext.Builder builder =
            new RunCommandFlowContext.Builder(UNINSTALL_EXECUTABLE)
                .args(Arrays.asList("-i", "silent"))
                .workDir(FLDMainClusterTestbed.INSTALL_DIR + "/UninstallerData" + "/base")
                .name(nameId);
        
        return builder.build();
    }

    private RunCommandFlowContext wvStopCommandFlowContext(String nameId) {
        RunCommandFlowContext.Builder builder =
            new RunCommandFlowContext.Builder("WVCtrl.sh")
                .args(Arrays.asList("stop"))
                .workDir(FLDMainClusterTestbed.INSTALL_DIR + "/bin")
                .name(nameId);
            
        return builder.build();
    }
}
