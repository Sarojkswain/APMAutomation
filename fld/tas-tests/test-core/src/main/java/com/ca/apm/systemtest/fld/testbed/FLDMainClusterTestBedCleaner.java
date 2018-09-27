package com.ca.apm.systemtest.fld.testbed;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxMemoryMonitorWorkDir;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.ca.apm.systemtest.fld.role.DockerEmRole;
import com.ca.tas.flow.docker.DockerRemoveFlow;
import com.ca.tas.flow.docker.DockerStopFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.SshUploadFlowContext;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.flow.ArchiveCreationFlow;
import com.ca.apm.systemtest.fld.flow.ArchiveCreationFlowContext;
import com.ca.apm.systemtest.fld.flow.FileModFlow;
import com.ca.apm.systemtest.fld.flow.KillByPidFileFlow;
import com.ca.apm.systemtest.fld.flow.KillByPidFileFlowContext;
import com.ca.apm.systemtest.fld.flow.RunCommandCheckFlow;
import com.ca.apm.systemtest.fld.role.DelayRole;
import com.ca.apm.systemtest.fld.role.LogMonitorRole;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveEntry;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveType;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedCleanerFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class FLDMainClusterTestBedCleaner
    implements
        TestBedCleanerFactory<FLDMainClusterTestbed>, FLDConstants {
    
    public static final String SSH_DESTINATION_MKDIR_ROLE_ID = "sshDestinationMKDirRoleId";
    
    public static final String DEFAULT_WORK_BACKUP_DIR = "/home/sw/archive";    //dir have to exist on target machine
    public static final String DEFAULT_TAS_BASE_DIR = "/opt/automation/deployed";
//    public static final String DEFAULT_BACKUP_HOST = "fldcoll12c";
//    public static final String DEFAULT_BACKUP_USER = "root";
//    public static final String DEFAULT_BACKUP_USERPASS = "Phox9tai";

    private static final Logger log = LoggerFactory.getLogger(FLDMainClusterTestBedCleaner.class);
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
//    public String getEmVersion() {
//        return fldConfig.getEmVersion();
//    }
//    
//    public String getBackupHost() {
//        return fldConfig.getBackupHost();
//    }
//    
//    public String getBackupUser() {
//        return fldConfig.getBackupUser();
//    }
//    
//    public String getBackupUserPass() {
//        return fldConfig.getBackupPassword();
//    }
    
    @Override
    public ITestbed create(final ITasResolver tasResolver,
        final FLDMainClusterTestbed javaTestBedFactory) {
        ITestbed deployedTestBed = javaTestBedFactory.create(tasResolver);
        Testbed testBed = new Testbed(FLDMainClusterTestBedCleaner.class.getSimpleName());

        // new timestamp for backup folder
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY-HH-mm");
        String time = dateFormat.format(now);
       
        String backupDir = "testrundata_" + fldConfig.getEmVersion() + "_T" + time;
        
        testBed.addMachine(cleanAGCEMsWVsDB(deployedTestBed, AGC_ROLE_ID,
            AGC_MACHINE_ID, tasResolver, backupDir, time));
        
        //clean and backup Main cluster
        for (int i = 0; i < EM_COLL_ROLES.length; i++) {
            testBed.addMachine(cleanEMsAndBackup(deployedTestBed, EM_COLL_ROLES[i],
                COLL_MACHINES[i], false, tasResolver, backupDir, time, false));
        }
        testBed.addMachine(cleanEMsAndBackup(deployedTestBed, EM_WEBVIEW_ROLE_ID,
            WEBVIEW_MACHINE_ID, true, tasResolver, backupDir, time, false));
        testBed.addMachine(cleanEMsAndBackup(deployedTestBed, EM_MOM_ROLE_ID,
            MOM_MACHINE_ID, false, tasResolver, backupDir, time, true));
        testBed.addMachine(cleanStanaloneDB(deployedTestBed, EM_DATABASE_ROLE_ID,
            DATABASE_MACHINE_ID));

        return testBed;
    }
    
    
    private ITestbedMachine cleanEMsAndBackup(ITestbed deployedTestBed, String roleId, String machineId, boolean wv,
                                              ITasResolver tasResolver, String backupDir, String timeSuffix,
                                              boolean killAPMSqlServer) {

        ITestbedMachine emMachine = deployedTestBed.getMachineById(machineId);
        
        String emHost = tasResolver.getHostnameById(roleId);
        boolean isRunning = checkEmIsRunning(deployedTestBed, wv, EM_PORT, emHost);
        ExecutionRole.Builder builder = new ExecutionRole.Builder("stop_" + roleId);
        IRole emRole = deployedTestBed.getRoleById(roleId);

        if (isRunning) {
            if (emRole instanceof DockerEmRole) {
                builder.flow(DockerStopFlow.class, ((DockerEmRole) emRole).getDockerRole().getStopContext());
            } else if (emRole instanceof EmRole) {
                if (wv) {
                    builder.flow(RunCommandCheckFlow.class, ((EmRole) emRole).getWvStopCommandFlowContext());
                } else {
                    builder.flow(RunCommandCheckFlow.class, ((EmRole) emRole).getEmStopCommandFlowContext());
                }
            }
        }
        ExecutionRole stopRole = builder.build();
        
        DelayRole delayRole = new DelayRole.Builder(stopRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRole.after(stopRole);
        
        //backup logs
        String archive = "/home/sw/archive/testrundata_" + machineId + "_" + fldConfig.getEmVersion() + "_T" + timeSuffix + ".tar.gz";
        ExecutionRole archiveRole = getArchiveRole(roleId, archive, fldConfig.isSkipBackup(), wv);
        archiveRole.after(delayRole);

        // SSH transport
        UniversalRole manipulationRole = getSshTransportRole(roleId, tasResolver, fldConfig, backupDir, archive);
        manipulationRole.after(archiveRole);
        
        ExecutionRole.Builder uninstallEmRoleBuilder = new ExecutionRole.Builder("uninstall_" + roleId);

        if (emRole instanceof DockerEmRole) {
            uninstallEmRoleBuilder.flow(DockerRemoveFlow.class, ((DockerEmRole) emRole).getDockerRole().getRemoveContext());
        } else if (emRole instanceof EmRole) {
            uninstallEmRoleBuilder.flow(RunCommandCheckFlow.class, ((EmRole) emRole).getEmUninstallCommandFlowContext());
        }

        ExecutionRole uninstallEmRole = uninstallEmRoleBuilder.build();
        uninstallEmRole.after(manipulationRole);
        
        ExecutionRole deleteEmRole = getDeleteRole(roleId, false);
        deleteEmRole.after(uninstallEmRole);
        
        //log monitor pidFile for log monitor kill
        String pidFile = ((LogMonitorRole) deployedTestBed.getRoleById(machineId+"_"+LOG_MONITOR_LINUX_ROLE_ID))
                        .getDeployLogMonitorFlowContext().getPidFile();
        
        emMachine.empty();

        if (killAPMSqlServer) {
            ExecutionRole stopAPMSqlServerRole = new ExecutionRole.Builder("stop_APMSqlSever_" + roleId)
                .flow(RunCommandCheckFlow.class, apmSqlServerStopCommandFlowContext(roleId))
                .build();
                stopAPMSqlServerRole.before(uninstallEmRole);
                emMachine.addRole(stopAPMSqlServerRole);
        }
        
        addKillMemoryMonitorRole(emMachine, roleId);
        
        return emMachine.addRole(stopRole, delayRole, uninstallEmRole, archiveRole, manipulationRole, deleteEmRole, 
                                getKillLogMonitorRole(roleId, pidFile));
    }
    
    private boolean checkEmIsRunning(ITestbed deloyedTestBed, boolean wv, int emPort, String emHost) {
        
        if(!wv) {
            log.info("host name: "+emHost+" port: " + emPort);
            return NetworkUtils.isServerListening(emHost+".ca.com", emPort);
        } else {
            log.info("host name: "+emHost+" port: "+FLDMainClusterTestbed.WVPORT);
            return NetworkUtils.isServerListening(emHost+".ca.com", FLDMainClusterTestbed.WVPORT);
        }
    }

    private ITestbedMachine cleanStanaloneDB(ITestbed deployedTestBed, String roleId, String machineId) {
        
        IRole emRole = deployedTestBed.getRoleById(roleId);

        ExecutionRole.Builder uninstallEmBuilder = new ExecutionRole.Builder("uninstall_" + roleId);
        if (emRole instanceof DockerEmRole) {
            uninstallEmBuilder.flow(DockerRemoveFlow.class,
                ((DockerEmRole) emRole).getDockerRole().getRemoveContext());
        } else if (emRole instanceof EmRole) {
            uninstallEmBuilder.flow(RunCommandCheckFlow.class,
                    ((EmRole) emRole).getEmUninstallCommandFlowContext());
        }
        ExecutionRole uninstallEmRole = uninstallEmBuilder.build();

        ExecutionRole deleteEmRole = getDeleteRole(roleId, true);
        deleteEmRole.after(uninstallEmRole);

        ITestbedMachine emMachine = deployedTestBed.getMachineById(machineId);
        emMachine.empty();
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

    private ITestbedMachine cleanAGCEMsWVsDB(ITestbed deployedTestBed, String roleId, String machineId, 
                                             ITasResolver tasResolver, String backupDir, String timeSuffix) {
        
        IRole emRole = deployedTestBed.getRoleById(roleId);

        String emHost = tasResolver.getHostnameById(roleId);
        boolean isRunning = checkEmIsRunning(deployedTestBed, false, EM_PORT, emHost);
        boolean isRunningWV = checkEmIsRunning(deployedTestBed, true, EM_PORT, emHost);
        
        ExecutionRole.Builder builder = new ExecutionRole.Builder("stopWV_" + roleId);
        if (isRunningWV) {
            if (emRole instanceof DockerEmRole) {
                builder.flow(DockerStopFlow.class,
                        ((DockerEmRole) emRole).getDockerRole().getStopContext());
            } else if (emRole instanceof EmRole) {
                builder.flow(RunCommandCheckFlow.class,
                        ((EmRole) emRole).getWvStopCommandFlowContext());
            }
        }
        ExecutionRole stopWvRole = builder.build();
        
        DelayRole delayRoleWV = new DelayRole.Builder(stopWvRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRoleWV.after(stopWvRole);
        
        builder = new ExecutionRole.Builder("stopEm_" + roleId);
        if (isRunning) {
            if (emRole instanceof DockerEmRole) {
                builder.flow(DockerStopFlow.class,
                        ((DockerEmRole) emRole).getDockerRole().getStopContext());
            } else if (emRole instanceof EmRole) {
                builder.flow(RunCommandCheckFlow.class,
                        ((EmRole) emRole).getEmStopCommandFlowContext());
            }
        }
        ExecutionRole stopEmRole = builder.build();
        stopEmRole.after(delayRoleWV);
        
        DelayRole delayRoleEM = new DelayRole.Builder(stopEmRole.getRoleId() + "-delay").delaySeconds(30).build();
        delayRoleEM.after(stopEmRole);
        
        //backup logs
        String archive = "/home/sw/archive/testrundata_" + machineId + "_" + fldConfig.getEmVersion() + "_T" + timeSuffix + ".tar.gz";
        ExecutionRole archiveRole = getArchiveRole(roleId, archive, fldConfig.isSkipBackup(), false);
        archiveRole.after(delayRoleEM);

        // SSH transport
        UniversalRole manipulationRole = getSshTransportRole(roleId, tasResolver, fldConfig, backupDir, archive);
        manipulationRole.after(archiveRole);

        ExecutionRole.Builder uninstallEmBuilder = new ExecutionRole.Builder("uninstall_" + roleId);
        if (emRole instanceof DockerEmRole) {
            uninstallEmBuilder.flow(DockerStopFlow.class,
                    ((DockerEmRole) emRole).getDockerRole().getStopContext());
        } else if (emRole instanceof EmRole) {
            uninstallEmBuilder.flow(RunCommandCheckFlow.class,
                    ((EmRole) emRole).getEmUninstallCommandFlowContext());
        }
        ExecutionRole uninstallEmRole = uninstallEmBuilder.build();
        uninstallEmRole.after(manipulationRole);

        ExecutionRole deleteEmRole = getDeleteRole(roleId, true);
        deleteEmRole.after(uninstallEmRole);
        
        //log monitor pidFile for log monitor kill
        String pidFile = ((LogMonitorRole) deployedTestBed.getRoleById(machineId+"_"+LOG_MONITOR_LINUX_ROLE_ID))
                            .getDeployLogMonitorFlowContext().getPidFile();
        
        ITestbedMachine emMachine = deployedTestBed.getMachineById(machineId);
        emMachine.empty();
        
        addKillMemoryMonitorRole(emMachine, roleId);
        
        return emMachine.addRole(stopWvRole, delayRoleWV, stopEmRole, delayRoleEM, manipulationRole, uninstallEmRole, archiveRole, deleteEmRole, 
            getKillLogMonitorRole(roleId, pidFile));
    }
    
    public static ExecutionRole getDeleteRole(String roleId, boolean isDB) {
        FileModifierFlowContext.Builder deleteFlowBuilder =
            new FileModifierFlowContext.Builder()
                .delete(FLDMainClusterTestbed.INSTALL_DIR)
                .delete(FLDMainClusterTestbed.INSTALL_TG_DIR)
                .delete(getLinuxMemoryMonitorWorkDir()); //delete memory-monitoring files
        if (isDB) {
            deleteFlowBuilder
                .delete(FLDMainClusterTestbed.DATABASE_DIR)
                .delete("/etc/init.d/postgresql-9.2");
        }
        return new ExecutionRole.Builder("delete_" + roleId).flow(FileModFlow.class, deleteFlowBuilder.build()).build();
    }

    /*
     *  Get kill Log Monitor role
     */
    public static IRole getKillLogMonitorRole (String roleId, String pidFile) {
        KillByPidFileFlowContext logMonitorKillFlow =
            new KillByPidFileFlowContext.Builder()
                .pidFile(pidFile)
                .build();
        
        ExecutionRole logMonitorKillRole =
            new ExecutionRole.Builder("killLogMonitor_" + roleId).flow(KillByPidFileFlow.class, logMonitorKillFlow)
                .build();
        return logMonitorKillRole;
    }
    
    /*
     *  Kill Memory Monitor role
     */
    public static void addKillMemoryMonitorRole (ITestbedMachine machine, String roleId) {
        
        Collection<String> createKillFile = Arrays
            .asList("kill $(ps aux | grep '[R]unMemoryMonitorFlow' | awk 'NR==1{print $2}')");
        FileModifierFlowContext killSh = new FileModifierFlowContext.Builder()
            .create(DEFAULT_TAS_BASE_DIR + "/killMemMom.sh", createKillFile).build();
        
        ExecutionRole killShRole = new ExecutionRole.Builder("create_kill_sh_" + roleId).flow(FileModifierFlow.class, killSh).build();
    
        RunCommandFlowContext runContext = new RunCommandFlowContext.Builder("/bin/bash")
                .args(Arrays.asList(DEFAULT_TAS_BASE_DIR + "/killMemMom.sh"))
                .doNotPrependWorkingDirectory()
                .ignoreErrors()
                .name(roleId).build();
        
        ExecutionRole killRole = new ExecutionRole.Builder("killMemMonitor_" + roleId)
                .flow(RunCommandCheckFlow.class, runContext)
                .build();
        
        killRole.after(killShRole);
        
        FileModifierFlowContext deleteFlow =
            new FileModifierFlowContext.Builder()
                .delete(DEFAULT_TAS_BASE_DIR + "/killMemMom.sh")
                .build();
        
        ExecutionRole delShRole = new ExecutionRole.Builder("deleteKillSh_" + roleId).flow(FileModFlow.class, deleteFlow).build();
        
        delShRole.after(killRole);
        
        machine.addRole(killShRole, killRole, delShRole);
    }
    
    public static RunCommandFlowContext apmSqlServerStopCommandFlowContext(String nameId) {
        RunCommandFlowContext.Builder builder =
            new RunCommandFlowContext.Builder("/bin/bash")
                .args(Arrays.asList(FLDMainClusterTestbed.INSTALL_DIR + "/APMSqlServer/bin/stopApmsql.sh"))
                .doNotPrependWorkingDirectory()
                .ignoreErrors()
                .name(nameId);
            
        return builder.build();
    }
    
    public static RunCommandFlowContext memoryMonitorStopCommandFlowContext(String nameId) {
        RunCommandFlowContext.Builder builder =
            new RunCommandFlowContext.Builder("kill")//$(ps aux | grep '[R]unMemoryMonitorFlow' | awk 'NR==1{print $2}')
                .args(Arrays.asList("ps", "aux", "|", "grep", "[R]unMemoryMonitorFlow", "|", "awk", "NR==1{print $2}"))
                .doNotPrependWorkingDirectory()
                .ignoreErrors()
                .name(nameId);
            
        return builder.build();
    }
    
    public static ExecutionRole getArchiveRole(String roleId, String archive, boolean skipBackup, boolean wv) {
        ExecutionRole.Builder archiveRoleBuilder = new ExecutionRole.Builder("archive_" + roleId);
        if (!skipBackup) {
            String[] includes = {"**/*.lax"};
            
            ArchiveEntry logs = new ArchiveEntry(FLDMainClusterTestbed.INSTALL_DIR+"/logs");
            ArchiveEntry config = new ArchiveEntry(FLDMainClusterTestbed.INSTALL_DIR+"/config");
            ArchiveEntry laxfile = new ArchiveEntry(FLDMainClusterTestbed.INSTALL_DIR, null, includes , null);
            
            ArchiveCreationFlowContext.Builder zipFlowBuilder = new ArchiveCreationFlowContext.Builder()
                    .type(ArchiveType.TAR)
                    .compression(ArchiveCompression.GZIP)
                    .path(archive)
                    .entry(logs)
                    .entry(config)
                    .entry(laxfile);
            /*if (!wv) {
                ArchiveEntry data = new ArchiveEntry(FLDMainClusterTestbed.INSTALL_DIR+"/data");
                ArchiveEntry traces = new ArchiveEntry(FLDMainClusterTestbed.INSTALL_DIR+"/traces");
                zipFlowBuilder
                    .entry(data)
                    .entry(traces);
            }*/
            ArchiveCreationFlowContext zipFlow = zipFlowBuilder.build();
       
            archiveRoleBuilder.flow(ArchiveCreationFlow.class, zipFlow);
        }
        return archiveRoleBuilder.build();
    }
    
    public static UniversalRole getSshTransportRole(String roleId, ITasResolver tasResolver, FLDConfiguration fldConfig, String backupDir, String archive) {
        UniversalRole.Builder maninpulationBuilder = new UniversalRole.Builder(SSH_SOURCE_ROLE_ID + roleId, tasResolver); 
        if (!fldConfig.isSkipBackup() && fldConfig.getBackupHost() != null && !fldConfig.getBackupHost().isEmpty()) {
            maninpulationBuilder.flow(new SshUploadFlowContext.Builder()
                .destDir(DEFAULT_WORK_BACKUP_DIR+"/"+backupDir)
                .file(archive)
                .host(fldConfig.getBackupHost())
                .user(fldConfig.getBackupUser())
                .password(fldConfig.getBackupPassword())
                .build());
        }
        return maninpulationBuilder.build();
    }

}
