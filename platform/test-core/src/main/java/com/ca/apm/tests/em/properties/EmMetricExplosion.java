/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : TUUJA01/ JAYARAM PRASAD
 * Date : 11/03/2016
 */
package com.ca.apm.tests.em.properties;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;


public class EmMetricExplosion extends StandAloneEMOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmMetricExplosion.class);
    protected final String emHost;
    protected final String emLibDir;
    protected final String configFileEm;
    protected final String EMlogFile;
    protected final String emSecurePort;
    protected final String emPort;
    protected final String tomcatagentProfileFile;
    protected final String configFileEm_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String testcaseId;
    protected String emErrorLog;
    protected String emInfoLog;
    protected String emDebugLog;
    protected String emVerboseLog;
    protected String emTraceLog;
    protected String emCLWLog;
    protected final String emApmThresholdXmlPath;
    protected final String emaAgentClustersXmlPath;

    protected final String agentConnectionLimitXpath =
        "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.agent.connection.limit\"]/threshold";
    protected final String metricConnectionLimitXpath =
        "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.agent.metrics.limit\"]/threshold";
    protected final String liveMetricsLimitXpath =
        "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.metrics.live.limit\"]/threshold";
    protected final String historicalMetricsLimitXpath =
        "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.metrics.historical.limit\"]/threshold";
    protected final String agentevntthresholdattribute = "value";
    List<String> roleIds = new ArrayList<String>(); 
    
    

    public EmMetricExplosion() {

        tomcatAgentExp = ".*Tomcat.*";
        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
        emSecurePort = ApmbaseConstants.emSSLPort;
        emHost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
        configFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileEm_backup = configFileEm + "_backup";
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        emApmThresholdXmlPath =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "apm-events-thresholds-config.xml";

        emaAgentClustersXmlPath =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "agentclusters.xml";

        
    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        LOGGER.info("Initialize begins here");
        List<String> machines = new ArrayList<String>();
        machines.add(EM_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);

        roleIds.add(EM_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);
        backupPropFiles(roleIds);
        LOGGER.info("Initialize ends here");
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_298636_EMMetricClampingPropertiesAreHotConfigurable() {
        String testCaseId="298636";
        try {
            backupFile(emApmThresholdXmlPath, emApmThresholdXmlPath + "_backup", EM_MACHINE_ID);
            startEM();
            startAgent();
            setattributeinapmthresholdXML(EM_MACHINE_ID, emApmThresholdXmlPath,
                agentConnectionLimitXpath, agentevntthresholdattribute, "40");
            String keyWord =
                "[Clamp : introscope.enterprisemanager.agent.connection.limit, Value : 40]";
            checkEMLogForMsg(keyWord);
            checkAgentLogForMsg("INFO");
        } finally {
            stopEM();
            stopAgent();
            revertFile(emApmThresholdXmlPath + "_backup", emApmThresholdXmlPath, EM_MACHINE_ID);
            renameLogWithTestCaseId(roleIds, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_280491() {
        String testCaseId="280941";
        try {
            backupFile(emApmThresholdXmlPath, emApmThresholdXmlPath + "_backup", EM_MACHINE_ID);
            backupFile(emaAgentClustersXmlPath, emaAgentClustersXmlPath + "_backup", EM_MACHINE_ID);
            String[] metricSpecifiers = {"CPU\\|.*", "Frontends\\|.*"};
            XMLUtil.createAgentCluster(emaAgentClustersXmlPath, "TestAgentCluster", "SuperDomain",
                ".*\\|.*\\|.*", metricSpecifiers);
            setattributeinapmthresholdXML(EM_MACHINE_ID, emApmThresholdXmlPath,
                liveMetricsLimitXpath, agentevntthresholdattribute, "300");
            startEM(EM_ROLE_ID);
            startAgent();
            checkAgentLogForMsg("INFO");
            checkEMLogForMsg("The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents");
        } finally {
            stopEM();
            stopAgent();
            revertFile(emApmThresholdXmlPath + "_backup", emApmThresholdXmlPath, EM_MACHINE_ID);
            revertFile(emaAgentClustersXmlPath + "_backup", emaAgentClustersXmlPath, EM_MACHINE_ID);
            renameLogWithTestCaseId(roleIds, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_304041() {
        String testCaseId="304041";
        try {
            backupFile(emApmThresholdXmlPath, emApmThresholdXmlPath + "_backup", EM_MACHINE_ID);
            setattributeinapmthresholdXML(EM_MACHINE_ID, emApmThresholdXmlPath,
                liveMetricsLimitXpath, agentevntthresholdattribute, "300");
            startEM(EM_ROLE_ID);
            startAgent();
            checkAgentLogForMsg("INFO");
            checkEMLogForMsg("The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents");
        } finally {
            stopEM();
            stopAgent();
            revertFile(emApmThresholdXmlPath + "_backup", emApmThresholdXmlPath, EM_MACHINE_ID);
            renameLogWithTestCaseId(roleIds, testCaseId);
        }
    }

}
