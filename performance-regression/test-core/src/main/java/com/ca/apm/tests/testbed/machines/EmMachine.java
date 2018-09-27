/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.CsvToXlsTemplateVersion;
import com.ca.apm.tests.flow.INetShareUser;
import com.ca.apm.tests.role.*;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Machine containing Weblogic Server + StockTrader Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class EmMachine implements INetShareUser {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_EM";

    public static final String EM_ROLE_ID = "_emRoleId";
    public static final String CSV_TO_XLS_ROLE_ID = "_csvToXlsRoleId";
    public static final String QC_UPLOAD_ROLE_ID = "_qcUploadToolRoleId";

    public static final String SHARE_DIR = "c:\\share";
    public static final String SHARE_DIR_NAME = "share";
    protected static Map<String, String> sheetsMapping = new HashMap<>();

    static {
        // CPU
        sheetsMapping.put("cpu_tomcat8.noagent.csv", "tomcat_cpu_noagent");
        sheetsMapping.put("cpu_tomcat8.nosi.CURRENT.csv", "tomcat_cpu_nosi");
        sheetsMapping.put("cpu_tomcat8.acc.CURRENT.csv", "tomcat_cpu_acc");
        sheetsMapping.put("cpu_tomcat8.nosi.brtm.CURRENT.csv", "tomcat_cpu_nosi_nospm_brtm");
        sheetsMapping.put("cpu_tomcat8.si.CURRENT.csv", "tomcat_cpu_si_nospm");
        sheetsMapping.put("cpu_tomcat8.nosi.PREV.csv", "tomcat_cpu_prev_nosi_nospm");
        sheetsMapping.put("cpu_tomcat8.si.PREV.csv", "tomcat_cpu_prev_si_nospm");

        sheetsMapping.put("cpu_weblogic103.noagent.csv", "wls_cpu_noagent");
        sheetsMapping.put("cpu_weblogic103.nosi.CURRENT.csv", "wls_cpu_nosi");
        sheetsMapping.put("cpu_weblogic103.si.CURRENT.csv", "wls_cpu_si_nospm");
        sheetsMapping.put("cpu_weblogic103.nosi.PREV.csv", "wls_cpu_prev_nosi_nospm");
        sheetsMapping.put("cpu_weblogic103.si.PREV.csv", "wls_cpu_prev_si_nospm");

        sheetsMapping.put("cpu_websphere85.noagent.csv", "was_cpu_noagent");
        sheetsMapping.put("cpu_websphere85.nosi.CURRENT.csv", "was_cpu_nosi");
        sheetsMapping.put("cpu_websphere85.acc.CURRENT.csv", "was_cpu_acc");
        sheetsMapping.put("cpu_websphere85.si.CURRENT.csv", "was_cpu_si_nospm");
        sheetsMapping.put("cpu_websphere85.nosi.PREV.csv", "was_cpu_prev_nosi_nospm");
        sheetsMapping.put("cpu_websphere85.si.PREV.csv", "was_cpu_prev_si_nospm");

        sheetsMapping.put("cpu_iis75.noagent.csv", "iis_cpu_noagent");
        sheetsMapping.put("cpu_iis75.nosi.CURRENT.csv", "iis_cpu_nosi");
        sheetsMapping.put("cpu_iis75.si.CURRENT.csv", "iis_cpu_si_nospm");
        sheetsMapping.put("cpu_iis75.nosi.PREV.csv", "iis_cpu_prev_nosi_nospm");
        sheetsMapping.put("cpu_iis75.si.PREV.csv", "iis_cpu_prev_si_nospm");

        // MEMORY
        sheetsMapping.put("mem_tomcat8.noagent.csv", "tomcat_mem_noagent");
        sheetsMapping.put("mem_tomcat8.nosi.CURRENT.csv", "tomcat_mem_nosi");
        sheetsMapping.put("mem_tomcat8.acc.CURRENT.csv", "tomcat_mem_acc");
        sheetsMapping.put("mem_tomcat8.nosi.brtm.CURRENT.csv", "tomcat_mem_nosi_nospm_brtm");
        sheetsMapping.put("mem_tomcat8.si.CURRENT.csv", "tomcat_mem_si_nospm");
        sheetsMapping.put("mem_tomcat8.nosi.PREV.csv", "tomcat_mem_prev_nosi_nospm");
        sheetsMapping.put("mem_tomcat8.si.PREV.csv", "tomcat_mem_prev_si_nospm");

        sheetsMapping.put("mem_weblogic103.noagent.csv", "wls_mem_noagent");
        sheetsMapping.put("mem_weblogic103.nosi.CURRENT.csv", "wls_mem_nosi");
        sheetsMapping.put("mem_weblogic103.si.CURRENT.csv", "wls_mem_si_nospm");
        sheetsMapping.put("mem_weblogic103.nosi.PREV.csv", "wls_mem_prev_nosi_nospm");
        sheetsMapping.put("mem_weblogic103.si.PREV.csv", "wls_mem_prev_si_nospm");

        sheetsMapping.put("mem_websphere85.noagent.csv", "was_mem_noagent");
        sheetsMapping.put("mem_websphere85.nosi.CURRENT.csv", "was_mem_nosi");
        sheetsMapping.put("mem_websphere85.acc.CURRENT.csv", "was_mem_acc");
        sheetsMapping.put("mem_websphere85.si.CURRENT.csv", "was_mem_si_nospm");
        sheetsMapping.put("mem_websphere85.nosi.PREV.csv", "was_mem_prev_nosi_nospm");
        sheetsMapping.put("mem_websphere85.si.PREV.csv", "was_mem_prev_si_nospm");

        sheetsMapping.put("mem_iis75.noagent.csv", "iis_mem_noagent");
        sheetsMapping.put("mem_iis75.nosi.CURRENT.csv", "iis_mem_nosi");
        sheetsMapping.put("mem_iis75.si.CURRENT.csv", "iis_mem_si_nospm");
        sheetsMapping.put("mem_iis75.nosi.PREV.csv", "iis_mem_prev_nosi_nospm");
        sheetsMapping.put("mem_iis75.si.PREV.csv", "iis_mem_prev_si_nospm");

        // REQUESTS
        sheetsMapping.put("jmeter_tomcat8.noagent.modified.csv", "tomcat_req_noagent");
        sheetsMapping.put("jmeter_tomcat8.nosi.CURRENT.modified.csv", "tomcat_req_nosi");
        sheetsMapping.put("jmeter_tomcat8.acc.CURRENT.modified.csv", "tomcat_req_acc");
        sheetsMapping.put("jmeter_tomcat8.nosi.brtm.CURRENT.modified.csv", "tomcat_req_nosi_nospm_brtm");
        sheetsMapping.put("jmeter_tomcat8.si.CURRENT.modified.csv", "tomcat_req_si_nospm");
        sheetsMapping.put("jmeter_tomcat8.nosi.PREV.modified.csv", "tomcat_req_prev_nosi_nospm");
        sheetsMapping.put("jmeter_tomcat8.si.PREV.modified.csv", "tomcat_req_prev_si_nospm");

        sheetsMapping.put("jmeter_weblogic103.noagent.modified.csv", "wls_req_noagent");
        sheetsMapping.put("jmeter_weblogic103.nosi.CURRENT.modified.csv", "wls_req_nosi");
        sheetsMapping.put("jmeter_weblogic103.si.CURRENT.modified.csv", "wls_req_si_nospm");
        sheetsMapping.put("jmeter_weblogic103.nosi.PREV.modified.csv", "wls_req_prev_nosi_nospm");
        sheetsMapping.put("jmeter_weblogic103.si.PREV.modified.csv", "wls_req_prev_si_nospm");

        sheetsMapping.put("jmeter_websphere85.noagent.modified.csv", "was_req_noagent");
        sheetsMapping.put("jmeter_websphere85.nosi.CURRENT.modified.csv", "was_req_nosi");
        sheetsMapping.put("jmeter_websphere85.acc.CURRENT.modified.csv", "was_req_acc");
        sheetsMapping.put("jmeter_websphere85.si.CURRENT.modified.csv", "was_req_si_nospm");
        sheetsMapping.put("jmeter_websphere85.nosi.PREV.modified.csv", "was_req_prev_nosi_nospm");
        sheetsMapping.put("jmeter_websphere85.si.PREV.modified.csv", "was_req_prev_si_nospm");

        sheetsMapping.put("jmeter_iis75.noagent.modified.csv", "iis_req_noagent");
        sheetsMapping.put("jmeter_iis75.nosi.CURRENT.modified.csv", "iis_req_nosi");
        sheetsMapping.put("jmeter_iis75.si.CURRENT.modified.csv", "iis_req_si_nospm");
        sheetsMapping.put("jmeter_iis75.nosi.PREV.modified.csv", "iis_req_prev_nosi_nospm");
        sheetsMapping.put("jmeter_iis75.si.PREV.modified.csv", "iis_req_prev_si_nospm");

        // REQUESTS STATS
        sheetsMapping.put("jmeter_tomcat8.noagent.stat.csv", "tomcat_req_stat_noagent");
        sheetsMapping.put("jmeter_tomcat8.nosi.CURRENT.stat.csv", "tomcat_req_stat_nosi");
        sheetsMapping.put("jmeter_tomcat8.acc.CURRENT.stat.csv", "tomcat_req_stat_acc");
        sheetsMapping.put("jmeter_tomcat8.nosi.brtm.CURRENT.stat.csv", "tomcat_req_stat_nosi_nospm_brtm");
        sheetsMapping.put("jmeter_tomcat8.si.CURRENT.stat.csv", "tomcat_req_stat_si_nospm");
        sheetsMapping.put("jmeter_tomcat8.nosi.PREV.stat.csv", "tomcat_req_stat_prev_nosi_nospm");
        sheetsMapping.put("jmeter_tomcat8.si.PREV.stat.csv", "tomcat_req_stat_prev_si_nospm");

        sheetsMapping.put("jmeter_weblogic103.noagent.stat.csv", "wls_req_stat_noagent");
        sheetsMapping.put("jmeter_weblogic103.nosi.CURRENT.stat.csv", "wls_req_stat_nosi");
        sheetsMapping.put("jmeter_weblogic103.si.CURRENT.stat.csv", "wls_req_stat_si_nospm");
        sheetsMapping.put("jmeter_weblogic103.nosi.PREV.stat.csv", "wls_req_stat_prev_nosi_nospm");
        sheetsMapping.put("jmeter_weblogic103.si.PREV.stat.csv", "wls_req_stat_prev_si_nospm");

        sheetsMapping.put("jmeter_websphere85.noagent.stat.csv", "was_req_stat_noagent");
        sheetsMapping.put("jmeter_websphere85.nosi.CURRENT.stat.csv", "was_req_stat_nosi");
        sheetsMapping.put("jmeter_websphere85.acc.CURRENT.stat.csv", "was_req_stat_acc");
        sheetsMapping.put("jmeter_websphere85.si.CURRENT.stat.csv", "was_req_stat_si_nospm");
        sheetsMapping.put("jmeter_websphere85.nosi.PREV.stat.csv", "was_req_stat_prev_nosi_nospm");
        sheetsMapping.put("jmeter_websphere85.si.PREV.stat.csv", "was_req_stat_prev_si_nospm");

        sheetsMapping.put("jmeter_iis75.noagent.stat.csv", "iis_req_stat_noagent");
        sheetsMapping.put("jmeter_iis75.nosi.CURRENT.stat.csv", "iis_req_stat_nosi");
        sheetsMapping.put("jmeter_iis75.si.CURRENT.stat.csv", "iis_req_stat_si_nospm");
        sheetsMapping.put("jmeter_iis75.nosi.PREV.stat.csv", "iis_req_stat_prev_nosi_nospm");
        sheetsMapping.put("jmeter_iis75.si.PREV.stat.csv", "iis_req_stat_prev_si_nospm");

        // AGENT INFO
        sheetsMapping.put("agent_tomcat.csv", "tomcat_info_agents");
        sheetsMapping.put("agent_was.csv", "was_info_agents");
        sheetsMapping.put("agent_wls.csv", "wls_info_agents");
        sheetsMapping.put("agent_iis.csv", "iis_info_agents");
    }

    protected final String machineId;
    protected final ITasResolver tasResolver;

    protected boolean deployEm = false;
    protected boolean predeployedEm = true;

    public EmMachine(String machineId, ITasResolver tasResolver) {
        this.machineId = machineId;
        this.tasResolver = tasResolver;
    }

    public EmMachine(String machineId, ITasResolver tasResolver, boolean deployEm, boolean predeployedEm) {
        this(machineId, tasResolver);
        this.deployEm = deployEm;
        this.predeployedEm = predeployedEm;
    }

    public TestbedMachine init() {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).bitness(Bitness.b64).build();

        ///////////////////////////////////////////
        // DEPLOY EM
        ///////////////////////////////////////////

        Map<String, String> emProps = new HashMap<>();
        emProps.put("enable.default.BusinessTransaction", "false");

        IRole realEmRole = null;
        if (deployEm && !predeployedEm) {
            realEmRole =  (new EmRole.Builder(machineId + EM_ROLE_ID + "_real", tasResolver)).installDir("c:\\sw\\wily\\em").build();
            machine.addRole(realEmRole);
        }

        IRole emEmptyRole = new EmEmptyRole.Builder(machineId + EM_ROLE_ID, tasResolver)
                .installLocation("C:/sw/wily/em")
                .properties(emProps)
                .reStartAfterDeploy()
                .build();

        if (deployEm && !predeployedEm) {
            emEmptyRole.after(realEmRole);
        }

        machine.addRole(emEmptyRole);

        ///////////////////////////////////////////
        // DEPLOY CSV2XLS TEMPLATE
        ///////////////////////////////////////////

        CsvToXlsTemplateRole csvToXlsTemplateRole = new CsvToXlsTemplateRole.Builder(machineId + "_csvToXlsTemplateRoleId", tasResolver)
                .installPath("c:/sw/wily/csvToXls").version(getCsvToXlsTemplateVersion()).build();

        machine.addRole(csvToXlsTemplateRole);

        ///////////////////////////////////////////
        // DEPLOY CSV2XLS
        ///////////////////////////////////////////

        CsvToXlsRole csvToXlsRole = new CsvToXlsRole.Builder(machineId + CSV_TO_XLS_ROLE_ID, tasResolver)
                .installPath("c:/sw/wily/csvToXls")
                .shareDir(SHARE_DIR)
                .templateFileName(csvToXlsTemplateRole.getTemplateFilePath())
                .outputFileName(SHARE_DIR + "\\Results_40min.xls")
                .sheetsMapping(sheetsMapping).build();
        machine.addRole(csvToXlsRole);

        ///////////////////////////////////////////
        // CREATE SHARE
        ///////////////////////////////////////////

        RunCommandFlowContext createShareDirFlowContext = new RunCommandFlowContext.Builder("if")
                .args(Arrays.asList("not", "exist", SHARE_DIR, "mkdir", SHARE_DIR)).build();
        ExecutionRole createShareDirRole = new ExecutionRole.Builder(machineId + "_createShareDirRoleId")
                .syncCommand(createShareDirFlowContext).build();
        machine.addRole(createShareDirRole);

        RunCommandFlowContext createShareFlowContext = new RunCommandFlowContext.Builder("if")
                .args(Arrays.asList("not", "exist", "\\\\localhost\\" + SHARE_DIR_NAME, "net", SHARE_DIR_NAME, "share=" + SHARE_DIR, "/GRANT:Everyone,FULL")).build();
        ExecutionRole createShareRole = new ExecutionRole.Builder(machineId + "_createShareRoleId")
                .syncCommand(createShareFlowContext).build();

        createShareRole.after(createShareDirRole);
        machine.addRole(createShareRole);

        RunCommandFlowContext setCredentials4shareFlowContext = new RunCommandFlowContext.Builder("net")
                .args(Arrays.asList("use", "\\\\localhost\\" + SHARE_DIR_NAME, "/user:" + DEFAULT_COPY_RESULTS_USER, DEFAULT_COPY_RESULTS_PASSWORD)).build();
        ExecutionRole setCredentials4shareRole = new ExecutionRole.Builder(machineId + "_setCredentials4shareRoleId")
                .syncCommand(setCredentials4shareFlowContext).build();

        setCredentials4shareRole.after(createShareRole);
        machine.addRole(setCredentials4shareRole);

        ///////////////////////
        // DEPLOY JAVA
        ///////////////////////

        JavaRole javaRole = new PerfJavaRole.Builder(machineId + "_javaRoleId", tasResolver)
                .version(JavaBinary.WINDOWS_32BIT_JDK_17)
                .dir("c:/sw/java7_32")
                .build();
        machine.addRole(javaRole);

        ///////////////////////////////////////////
        // DEPLOY QC UPLOAD TOOL
        ///////////////////////////////////////////

        QcUploadToolRole qcUploadToolRole = new QcUploadToolRole.Builder(machineId + QC_UPLOAD_ROLE_ID, tasResolver)
                .deploySourcesLocation("c:/sw/wily/qcUploadTool")
                .javaHome(javaRole.getInstallDir()) // has to be 32bit
                .predeployed(!deployEm && predeployedEm) // TODO verify - DLLs in some versions are broken and can't be registered, resolve and then detect predeployed
                .build();
        machine.addRole(qcUploadToolRole);

        return machine;
    }

    protected CsvToXlsTemplateVersion getCsvToXlsTemplateVersion() {
        return CsvToXlsTemplateVersion.AGENT_VER_10_2;
    }

    public EmMachine setDeployEm(boolean deployEm) {
        this.deployEm = deployEm;
        return this;
    }

    public EmMachine setPredeployedEm(boolean predeployedEm) {
        this.predeployedEm = predeployedEm;
        return this;
    }

}
