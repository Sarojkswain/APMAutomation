/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.testbed.smokebeta;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.RebootMachineAndWaitRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LDAP-related testbed.
 *
 * @author shadm01
 */
@TestBedDefinition
public class LDAPAdMachineTestbed implements FLDConstants, ITestbedFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPAdMachineTestbed.class);

    public final static String LDAP_MACHINE_ID = "ldapMachine";

    private ITasResolver tasResolver;
    private TestbedMachine ldapMachine;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;

        ldapMachine = new TestbedMachine.Builder(LDAP_MACHINE_ID)
            .templateId("w64")
            .bitness(Bitness.b64)
            .build();

//        AbstractRole downloadPowerShellRole = downloadPowerShell40();

        List<AbstractRole> roles = Arrays.asList(
//                downloadPowerShellRole,
                downloadPowerShell40(),
                InstallPowerShellTools40(),
//                rebootMachineRole(downloadPowerShellRole),

                InstallServerRole(),
//                rebootMachineRole(downloadPowerShellRole),

                DownloadAnswerFile(),
                DownloadPsExecUtility(),

                CreateFolderDeployed(),
                InstallDomainFromAnswerFile(),
                DownloadLdapExeFile(),
                CreateLdapUsers()
        );

        for (int z = 0; z < roles.size() - 1; z++) {
            roles.get(z).before(roles.get(z + 1));
        }

        ldapMachine.addRoles(roles);

        Testbed ldapTestBed = new Testbed(getClass().getSimpleName());
        ldapTestBed.addMachine(ldapMachine);

        FldTestbedProvider ldapMomMachineTestbed = new LDAPMomMachineTestbed();
        ldapMomMachineTestbed.initMachines();
        ldapMomMachineTestbed.initTestbed(ldapTestBed, tasResolver);

        return ldapTestBed;
    }

    private final String powerShellUpdateFileLocation = "C:/SW/PowerShell-6.1-KB2819745-x64-MultiPkg.msu";
    private final String answerFileLocation = "C:/SW/FldAutomation.answer";
    private final String ldapExeFileLocation = "C:/SW/ADLDS_usersCreation.exe";
    private final String psExecFileLocation = "C:/SW/PsExec.exe";

    private AbstractRole rebootMachineRole(AbstractRole rolename){
        return new RebootMachineAndWaitRole
                .Builder(tasResolver, tasResolver.getHostnameById(rolename.getRoleId()))
                .build();
    }

    private AbstractRole downloadPowerShell40() {
        LOGGER.info("Downloading Powershell 4.0 artifact");

        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.binaries.microsoft", "PowerShell", "x64-MultiPkg", "msu", "6.1-KB2819745");

        return new GenericRole
                .Builder(ldapMachine.getMachineId() + "_Poweshell40Downloader", tasResolver)
                .download(artifact, powerShellUpdateFileLocation)
                .build();
    }

    private AbstractRole InstallPowerShellTools40() {
        LOGGER.info("Installing windows update with Powershell 4.0");
        final String command = "wusa.exe";

        final List<String> arguments = new ArrayList<>();
        arguments.add(powerShellUpdateFileLocation);
        arguments.add("/quiet");
//        arguments.add("/forcerestart");

        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder(command)
                .args(arguments)
                .allowPositiveExitStatus()
                .build();

        //http://www.hiteksoftware.com/knowledge/articles/049.htm
        //Response code = 1641 The requested operation completed successfully. The system will be restarted so the changes can take effect.

        return new ExecutionRole.Builder(ldapMachine.getMachineId() + "_Powershell40Updater")
                .flow(runCmdFlowContext)
                .build();
    }

    private AbstractRole InstallServerRole() {
        LOGGER.info("Installing Service roles to support LDAP");
        final String command = "powershell";

        final List<String> arguments = new ArrayList<>();
        arguments.add("ServerManagerCmd.exe");
        arguments.add("-install");
        arguments.add("ADLDS");
        arguments.add("RSAT-AD-Tools");
//        arguments.add("-restart");

        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder(command)
                .args(arguments)
                .allowPositiveExitStatus()
                .build();

        return new ExecutionRole.Builder(ldapMachine.getMachineId() + "_LDAP_SERVER_COMPONENTS_INSTALLER")
                .flow(runCmdFlowContext)
                .build();
    }

    private AbstractRole DownloadAnswerFile() {
        LOGGER.info("Downloading .answer artifact for automatic installation");

        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.systemtest.fld.activedirectory", "FLDLdap_localhost50016", "answer", "1.0.0");

        return new GenericRole
                .Builder(ldapMachine.getMachineId() + "_DOWNLOAD_PS_EXEC_FILE", tasResolver)
                .download(artifact, answerFileLocation)
                .build();
    }

    private AbstractRole DownloadPsExecUtility() {
        LOGGER.info("Downloading .answer artifact for automatic installation");

        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.binaries.microsoft.sysinternals", "psexec", "exe", "2.11");

        return new GenericRole
                .Builder(ldapMachine.getMachineId() + "_PS_UTILITY_DOWNLOAD", tasResolver)
                .download(artifact, psExecFileLocation)
                .build();
    }

    private AbstractRole InstallDomainFromAnswerFile() {
        LOGGER.info("Creating new domain from .answer file");

        final List<String> arguments = new ArrayList<>();

        arguments.add("/accepteula");
        arguments.add("\\\\127.0.0.1");
        arguments.add("-u");
        arguments.add("root");
        arguments.add("-p");
        arguments.add("Ukmirrorx64");

        arguments.add("c:\\windows\\ADAM\\adaminstall.exe");
        arguments.add("/quiet");
        arguments.add("/answer:" + answerFileLocation);

        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder(psExecFileLocation).
                ignoreErrors()
                .dontUseWindowsShell()
                .doNotPrependWorkingDirectory()
                .args(arguments)
                .allowPositiveExitStatus()
                .build();

        return new ExecutionRole.Builder(ldapMachine.getMachineId() + "_DOMAIN_CREATION_FROM_ANSWER_FILE")
                .flow(runCmdFlowContext)
                .build();
    }

    private AbstractRole DownloadLdapExeFile() {
        LOGGER.info("Downloading Ldap C# tool for users creation");

        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.systemtest.fld.activedirectory", "AD_LDS_namingcontext_population", "exe", "1.0.0");

        return new GenericRole
                .Builder(ldapMachine.getMachineId() + "_LDAP_USER_CREATION_SCRIPT_DOWNLOAD", tasResolver)
                .download(artifact, ldapExeFileLocation)
                .build();
    }

    private AbstractRole CreateLdapUsers() {
        LOGGER.info("Creating Users in current Domain");

        final List<String> arguments = new ArrayList<>();

        arguments.add("/accepteula");
        arguments.add("\\\\127.0.0.1");
        arguments.add("-u");
        arguments.add("root");
        arguments.add("-p");
        arguments.add("Ukmirrorx64");
        arguments.add(ldapExeFileLocation);

        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder(psExecFileLocation).
                ignoreErrors()
                .dontUseWindowsShell()
                .doNotPrependWorkingDirectory()
                .args(arguments)
                .allowPositiveExitStatus()
                .build();

        return new ExecutionRole
                .Builder(ldapMachine.getMachineId() + "_LDAP_USERS_CREATION")
                .flow(runCmdFlowContext)
                .build();

    }

    private AbstractRole CreateFolderDeployed() {
        LOGGER.info("Simple folder creating using complicated technologies");

        final List<String> arguments = new ArrayList<>();
        arguments.add("c:/automation/deployed");
        arguments.add(ldapExeFileLocation);

        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder("md")
                .args(arguments)
                .build();

        return new ExecutionRole
                .Builder(ldapMachine.getMachineId() + "_JUST_CREATING_DEPLOYED_FOLDER")
                .flow(runCmdFlowContext)
                .build();
    }

    //CLEANUP:

    //remove domain:
    //c:\windows\ADAM\adamuninstall.exe /i:FLDLdapInstance /q /force

    //remove all files (use constants)
}
