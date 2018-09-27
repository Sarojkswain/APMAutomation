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
 */
package com.ca.apm.tests.agentextension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounterContext;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.envproperty.EnvironmentPropertyContext;

public class SqlParamsExtensionTest
    extends StandAloneEMOneTomcatTestsBase
{

    public static final String  EXTENSION_LOC_WIN   = TasBuilder.WIN_SOFTWARE_LOC
                                                      + "extension"
                                                      + TasBuilder.WIN_SEPARATOR;

    public static final String TRACE_FILE_PATH = "C:\\SW";
    
    public static final String TRACE_FILE = "Trace.xml";
    
    protected String            extensionLocation;
    
    String                      tomcatagentInstall;

    String                      tomcatagentwebappInstall;

    String                      tomcatLogFile;
    
    String                      testIdName;

    CLWCommons                  clw               = new CLWCommons();

    TestUtils                   utility           = new TestUtils();

    static int                  counter           = 1;

    private static final Logger LOGGER            = LoggerFactory
                                                          .getLogger(SqlParamsExtensionTest.class);

    TestApplications            TA                = new TestApplications();

    /**
     * Constructor
     */
    public SqlParamsExtensionTest()
    {
        extensionLocation = EXTENSION_LOC_WIN;
        

        tomcatagentInstall = envProperties
                .getRolePropertyById(TOMCAT_ROLE_ID,
                                     DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                             + "\\wily";
        String date = currentDate();

        tomcatagentwebappInstall = envProperties
                .getRolePropertyById(TOMCAT_ROLE_ID,
                                     DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                                   + "\\webapps";

        tomcatLogFile = envProperties
                .getRolePropertyById(TOMCAT_ROLE_ID,
                                     DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "\\logs\\catalina." + date + ".log";

    }

    @BeforeClass(alwaysRun = true)
    public void initialize()
    {

        LOGGER.info("Initialize begins here");

        LOGGER.info("Invoked testmethod in extensionjava");

        copyDir(extensionLocation + "/wily", tomcatagentInstall,
                envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));
        replaceProp("introscope.autoprobe.directivesFile=tomcat-full.pbl,hotdeploy",
                    "introscope.autoprobe.directivesFile=SQLParam.pbd,tomcat-full.pbl,hotdeploy",
                    envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
                    tomcatagentProfileFile);
        List<String> sqlProps = new ArrayList<String>();
        
        sqlProps.add("##################################################");
        sqlProps.add("#SQL Params Agent Properties");
        sqlProps.add("##################################################");
        
        sqlProps.add("introscope.agent.sqlagent.showparams=true");
        sqlProps.add("introscope.agent.sqlagent.maxparamlength=400");
        sqlProps.add("introscope.agent.sqlagent.useblame=true");
        sqlProps.add("introscope.agent.sqlagent.resolveBindParams=true");
        
        appendProp(sqlProps, envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID), tomcatagentProfileFile);
        
        copyDir(extensionLocation + "\\webapps", tomcatagentwebappInstall,
                envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));

        
        startEM();
        
        startAgent();
        checkTomcatStartupMessage();

        renameLogFiles("initializetest");

    }

    //Test No. 1
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456296_CallableStatement() throws Exception
    {
        LOGGER.info("This is to verify ALM_456296_CallableStatement");

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "call INSERTuser(10,call,Test)");
        testIdName = "ALM_456296_CallableStatement";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
    }

    //Test No.2
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456290_PreparedStatement() throws Exception
    {
        LOGGER.info("This is to verify ALM_456290_PreparedStatement");

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "INSERT INTO users(userid, firstname,lastname) VALUES (1234, John, Cena )");
        testIdName = "ALM_456290_PreparedStatement";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
    }
    
    //Test No.3
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456295_DynamicStatement() throws Exception
    {
        LOGGER.info("This is to verify ALM_456295_DynamicStatement");

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "SELECT * FROM USERS");
        
        testIdName = "ALM_456295_DynamicStatement";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
    }
    
    
  //Test No.4
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456289_VerifyLog() throws Exception
    {
        LOGGER.info("This is to verify ALM_456289_VerifyLog");

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        harvestWait(10);
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, "SQL Param Agent");
        LOGGER.info("Found keyword!!!");
        
        testIdName = "ALM_456289_VerifyLog";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
    }
    
    //Test No.5
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456288_VerifySqlBindParameters() throws Exception
    {
        LOGGER.info("This is to verify ALM_456288_VerifySqlBindParameters");

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        harvestWait(10);
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "SQL Bind Param1");
        
        testIdName = "ALM_456288_VerifySqlBindParameters";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
    }
    
    //Test No.6
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456299_VerifyParamLength() throws Exception
    {
        LOGGER.info("This is to verify ALM_456299_VerifyParamLength");
        
        replaceProp("introscope.agent.sqlagent.maxparamlength=400", "introscope.agent.sqlagent.maxparamlength=10", envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID), tomcatagentProfileFile);

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        harvestWait(10);
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudAppParamLength(url);
        harvestWait(40);
        
        isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "INSERT INTO users(userid, firstname,lastname) VALUES (12345, JohnMikeGe..., CenaAlexan... )");
        
        testIdName = "ALM_456299_VerifyParamLength";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
        replaceProp("introscope.agent.sqlagent.maxparamlength=10", "introscope.agent.sqlagent.maxparamlength=400", envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID), tomcatagentProfileFile);
        
    }
    
    //Test No.7
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456297_VerifyAverageResultRows() throws Exception
    {
        LOGGER.info("This is to verify ALM_456297_VerifyAverageResultRows");
        
        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/konakart/Welcome.action";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        
        TA.KonakartApp(url);
        harvestWait(15);
        
        String metricExpression1 = "Backends\\|orcl swasa02\\-win14\\-1521 \\(Oracle DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT COUNTER_SEQ\\.NEXTVAL FROM DUAL:Average Result Rows Processed";
        
        String sqlParamsComponentMetric = getDataUsingCLW(metricExpression1);
        
        Assert.assertFalse("Spring Component metric didn't reported  ",sqlParamsComponentMetric.equals("-1"));
        
        stopAgent();
        
        testIdName = "ALM_456297_VerifyAverageResultRows";
        renameLogFiles(testIdName);
        
        
    }
    
    //Test No.8
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456286_VerifyExtensionDisable() throws Exception
    {
        LOGGER.info("This is to verify ALM_456286_VerifyExtensionDisable");
        
        replaceProp("introscope.agent.sqlagent.showparams=true", "introscope.agent.sqlagent.showparams=false", envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID), tomcatagentProfileFile);

        String url = "http://"
                     + tomcatHost
                     + ":"
                     + envProperties
                             .getRolePropertyById(TOMCAT_ROLE_ID,
                                                  DeployTomcatFlowContext.ENV_TOMCAT_PORT)
                     + "/JSPCrud/user.jsp";

        LOGGER.info("URL Formed : "+ url);
        
        startAgent();
        checkTomcatStartupMessage();
        harvestWait(10);
        LOGGER.info("Before Trace!!");       
        new Thread(new Runnable() {
            public void run() {
                LOGGER.info("Trace time started!!");
                createTransactioFile(user, password, EMHost, Integer.parseInt(emPort), emLibDir);
                LOGGER.info("Trace time completed!!");
            }
        }).start();
        
        LOGGER.info("After Trace!!");
        
        harvestWait(10);
        TA.JspCrudApp(url);
        harvestWait(40);
        
        boolean extensionStatus = false;
        try{
            isKeywordInFile(envProperties, EM_MACHINE_ID, TRACE_FILE_PATH+"\\"+TRACE_FILE, "SQL Resolved");
            LOGGER.info("Found keyword!!!");
            extensionStatus = true;
            Assert.assertFalse("SQL Params Extension not disabled.", extensionStatus);
        }catch(IllegalStateException ie){
            LOGGER.info("SQL Params Extension is turned off.");
        }
        
        testIdName = "ALM_456286_VerifyExtensionDisable";
        renameLogFiles(testIdName);
        renameTraceFile(testIdName);
        
        replaceProp("introscope.agent.sqlagent.showparams=false", "introscope.agent.sqlagent.showparams=true", envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID), tomcatagentProfileFile);
        
    }
    
    @AfterMethod
    public void tearDown()
    {
        try
        {

            checkFileExistenceOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
                                             tomcatLogFile);
            LOGGER.info("entered into tearDown try method");
            stopAgent();
            LOGGER.info("counter value :"+counter);
            renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
                                    "testFailed" + counter);
            counter++;
            LOGGER.info("counter value increased");
            
        } catch (Exception e)
        {
            // Do nothing
            LOGGER.info("Tomcat log not available do nothing");

        }

    }
    
    public void checkFileExistenceOneTimeCounter(EnvironmentPropertyContext envProps,
                                                 String machineId,
                                                 String filePath)
    {
        CheckFileExistenceFlowOneTimeCounterContext checkFileExistenceFlowOneTimeCounterContext = new CheckFileExistenceFlowOneTimeCounterContext.Builder()
                .filePath(filePath).build();
        runFlowByMachineId(machineId,
                           CheckFileExistenceFlowOneTimeCounter.class,
                           checkFileExistenceFlowOneTimeCounterContext);
    }

    
    public String currentDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();
        String date1 = dateFormat.format(date);
        return date1;

    }    

    public void checkTomcatStartupMessage()
    {

        LOGGER.info(tomcatLogFile);
        boolean message = false;
        while (!message)
            try
            {
                isKeywordInFileOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
                                              tomcatLogFile,
                                              "Server startup in");
                message = true;
                LOGGER.info("Tomcat started successfully ");
                break;

            } catch (Exception e)
            {
                message = false;
                LOGGER.info("Tomcat not yet started");
            }

    }
    
    public void renameLogFiles(String testIdName){
        try{
                stopAgent();
                LOGGER.info("Tomcat Stopped Successfully");
            }catch(Exception e){
                LOGGER.info("Tomcat Stopping Failed");
            }
            
            finally{
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,testIdName);
            renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,testIdName);
            }
        }
    
    public void renameTraceFile(String testIdName){
       
            renameLogWithTestCaseID(TRACE_FILE_PATH+"\\"+TRACE_FILE, EM_MACHINE_ID,testIdName);
       
        }

    
    public List<String> createTransactioFile(String user,
                                             String password,
                                             String host,
                                             int port,
                                             String emLibDir)
    {

        LOGGER.info("Inside getTranscationTraces");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner = clwBuilder
                .host(host)
                .port(port)
                .clwWorkStationDir(emLibDir)
                .user(user)
                .password(password)
                .addTransactionTraceProperty("introscope.clw.tt.filename",
                                             TRACE_FILE)
                .addTransactionTraceProperty("introscope.clw.tt.dirname",
                                             TRACE_FILE_PATH).build();

        final String command = (new StringBuilder(
                                                  "trace transactions exceeding 1 ms in agents matching \".*\" for 60 seconds"))
                .toString();

        LOGGER.info(command);
        return clwRunner.runClw(command);

    }
    
    public String getDataUsingCLW(String metricExpression)
    {   
        String metricData;
        String agentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        
        metricData = clw.getLatestMetricValue(user, password, agentExpression, metricExpression, EMHost, Integer.parseInt(emPort), emLibDir);
        
        LOGGER.info("Metric data : "+ metricData);
        
        return metricData;
    }
}



