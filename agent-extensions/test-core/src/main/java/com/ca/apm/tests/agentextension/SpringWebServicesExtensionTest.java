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
 * DATE: 07/18/2017
 * AUTHOR: MARSA22/SAI KUMAR MAROJU
 */
package com.ca.apm.tests.agentextension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounterContext;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.envproperty.EnvironmentPropertyContext;

public class SpringWebServicesExtensionTest
    extends StandAloneEMOneTomcatTestsBase
{

    public static final String  EXTENSION_LOC_WIN = TasBuilder.WIN_SOFTWARE_LOC
                                                    + "extension"
                                                    + TasBuilder.WIN_SEPARATOR;

    protected String            extensionLocation;

    String                      tomcatagentInstall;

    String                      tomcatagentwebappInstall;

    String                      tomcatLogFile;

    String                      testIdName;

    String                      url;

    CLWCommons                  clw               = new CLWCommons();

    TestUtils                   utility           = new TestUtils();

    static int                  counter           = 1;

    private static final Logger LOGGER            = LoggerFactory
                                                          .getLogger(SpringWebServicesExtensionTest.class);

    TestApplications            TA                = new TestApplications();

    /**
     * Constructor
     */
    public SpringWebServicesExtensionTest()
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

        copyDir(extensionLocation + "\\wily", tomcatagentInstall,
                envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));
        replaceProp("introscope.autoprobe.directivesFile=tomcat-full.pbl,hotdeploy",
                    "introscope.autoprobe.directivesFile=spring-mvc.pbd,spring-ws.pbd,springws-toggles.pbd,tomcat-full.pbl,hotdeploy",
                    envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
                    tomcatagentProfileFile);

        copyDir(extensionLocation + "\\webapps", tomcatagentwebappInstall,
                envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));

        startEM();
        startAgent();
        checkTomcatStartupMessage();

        renameLogFiles("initializetest");

    }

    // Test No. 1
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454089_Springwebservices()
    {
        LOGGER.info("This is to verify_ALM_454089_Springwebservices");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-ws-client-wss4j/krams/main";

        startAgent();

        checkTomcatStartupMessage();

        TA.accessWebservicetestapp(url);
        harvestWait(15);

        String metricExpressionClient = "WebServices\\|Client\\|http_//krams915.blogspot.com/ws/schema/oss\\|subscriptionRequest:Responses Per Interval";
        String metricExpressionServer = "WebServices\\|Server\\|http_//krams915.blogspot.com/ws/schema/oss\\|subscriptionRequest:Responses Per Interval";

        String webServicesClientMetric = getDataUsingCLW(metricExpressionClient);
        String webServicesServerMetric = getDataUsingCLW(metricExpressionServer);

        Assert.assertFalse("Webservice Client Metrics didn't reported  ",
                           webServicesClientMetric.contains("-1"));
        Assert.assertFalse("Webservice Server Metrics didn't reported  ",
                           webServicesServerMetric.contains("-1"));

        testIdName = "ALM_454089_Springwebservices";
        renameLogFiles(testIdName);

    }

    // Test No. 2
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454092_SpringComponent()
    {
        LOGGER.info("This is to verify_ALM_454092_SpringComponent");
        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/greenhouse/signup";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringComponentApp(url);

        harvestWait(15);

        String metricExpression = "Spring\\|MVC\\|Component\\|AccountMapper\\|newAccount:Responses Per Interval";

        String springComponentMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Component metric didn't reported  ",
                           springComponentMetric.equals("-1"));

        testIdName = "ALM_454092_SpringComponent";
        renameLogFiles(testIdName);

    }

    // Test No. 3
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456956_SpringController()
    {
        LOGGER.info("This is to verify_ALM_456956_SpringController");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";
        LOGGER.info(url);
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringControllerApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|MVC\\|Controller\\|OwnerController\\|initCreationForm:Responses Per Interval";

        String springControllerMetrc = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Controller metric didn't reported  ",
                           springControllerMetrc.equals("-1"));

        testIdName = "ALM_456956_SpringController";
        renameLogFiles(testIdName);

    }

    // Test No. 4
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456958_SpringService()
    {
        LOGGER.info("This is to verify_ALM_456958_SpringService");
        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringServiceApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|MVC\\|Service\\|ClinicServiceImpl\\|saveOwner:Responses Per Interval";

        String springServiceMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Service metric didn't reported  ",
                           springServiceMetric.equals("-1"));

        testIdName = "ALM_456958_SpringService";
        renameLogFiles(testIdName);

    }

    // Test No. 5
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456957_SpringRepository()
    {
        LOGGER.info("This is to verify_ALM_456957_SpringRepository");
        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRepositoryApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|MVC\\|Repository\\|JpaOwnerRepositoryImpl\\|save:Responses Per Interval";

        String springRepositoryMetric = getDataUsingCLW(metricExpression);
        Assert.assertFalse("Spring Repository metric didn't reported  ",
                           springRepositoryMetric.equals("-1"));

        testIdName = "ALM_456957_SpringRepository";
        renameLogFiles(testIdName);

    }

    // Test No. 6
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456406_SpringValidator()
    {
        LOGGER.info("This is to verify_ALM_456406_Springvalidator");
        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringValidatorApp(url);

        harvestWait(15);

        String metricExpression = "Spring\\|Validator\\|PetValidator:Responses Per Interval";

        String springValidatorMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("spring validator metric didn't reported  ",
                           springValidatorMetric.equals("-1"));

        testIdName = "ALM_456406_Springvalidator";
        renameLogFiles(testIdName);

    }

    // Test No. 7
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454096_SpringAspect()
    {
        LOGGER.info("This is to verify_ALM_454096_SpringAspect");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringAspectApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|Aspect\\|CallMonitoringAspect\\|invoke:Responses Per Interval";

        String springAspectMetric = getDataUsingCLW(metricExpression);
        Assert.assertFalse("Spring Aspect metric didn't reported  ",
                           springAspectMetric.equals("-1"));

        testIdName = "ALM_454096_SpringAspect";
        renameLogFiles(testIdName);

    }

    // Test No. 8
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454091_SpringRequestMapping()
    {
        LOGGER.info("This is to verify_ALM_454091_SpringRequestMapping");
        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRequestMappingApp(url);

        harvestWait(15);

        String metricExpression = "Spring\\|Web\\|RequestMapping\\|PetController\\|initCreationForm:Responses Per Interval";

        String springRequestMappingMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("requestmapping metric didn't reported  ",
                           springRequestMappingMetric.contains("-1"));

        testIdName = "ALM_454091_SpringRequestMapping";
        renameLogFiles(testIdName);

    }

    // Test No. 9
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454090_SpringRestWebServices()
    {
        LOGGER.info("This is to verify_ALM_454090_SpringRestWebServices");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/greenhouse/signup";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRestWebservicesApp(url);

        harvestWait(15);

        String metricExpression = "WebServices\\|Client\\|https_//api.twitter.com/oauth/request_token\\|POST:Responses Per Interval";

        String springRestWebServicesMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring RestServices metric didn't reported  ",
                           springRestWebServicesMetric.contains("-1"));

        testIdName = "ALM_454090_SpringRestWebServices";
        renameLogFiles(testIdName);

    }

    // Test No. 10
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454088_SpringTransactionManagement()
    {
        LOGGER.info("This is to verify_ALM_454088_SpringTransactionManagement");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-petclinic/owners/new";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringTransactionManagementApp(url);

        harvestWait(15);

        String metricExpression = "Spring\\|Transaction Management\\|commit:Responses Per Interval";

        String springTMMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Transaction Management metric didn't reported  ",
                           springTMMetric.contains("-1"));

        testIdName = "ALM_454088_SpringTransactionManagement";
        renameLogFiles(testIdName);

    }

    // Test No. 11
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454094_SpringRemotingRMI()
    {
        LOGGER.info("This is to verify_ALM_454094_SpringRemotingRMI");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-remoting-webclient";
        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRemotingRMIApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|Remoting\\|RMI\\|RmiServiceExporter\\|invoke:Responses Per Interval";

        String springRemotingRMIMetric = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Remoting RMI metric didn't reported  ",
                           springRemotingRMIMetric.contains("-1"));

        testIdName = "ALM_454094_SpringRemotingRMI";
        renameLogFiles(testIdName);

    }

    // Test No. 12
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456960_SpringRemotingHessian()
    {
        LOGGER.info("This is to verify_ALM_456960_SpringRemotingHessian");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/hessian/";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRemotingHessainApp(url);

        harvestWait(15);

        String metricExpression = "Spring\\|Remoting\\|Hessian\\|HessianServiceExporter\\|handleRequest:Responses Per Interval";

        String springRemotingHessian = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Remoting Hessian metric didn't reported  ",
                           springRemotingHessian.contains("-1"));

        testIdName = "ALM_456960_SpringRemotingHessian";
        renameLogFiles(testIdName);

    }

    // Test No. 13
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_456959_SpringRemotingHttpInvoker()
    {
        LOGGER.info("This is to verify_ALM_456959_SpringRemotingHTTPInvoker");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/httpinvoker/process.jsp?number=5";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringRemotinghttpinvokerApp(url);
        harvestWait(15);

        String metricExpression = "Spring\\|Remoting\\|HTTP Invoker\\|HttpInvokerServiceExporter\\|handleRequest:Responses Per Interval";

        String springRemotingHttpInvoker = getDataUsingCLW(metricExpression);

        Assert.assertFalse("Spring Remoting HTTP Invoker metric didn't reported  ",
                           springRemotingHttpInvoker.contains("-1"));

        testIdName = "ALM_456959_SpringRemotingHTTPInvoker";
        renameLogFiles(testIdName);

    }

    // Test No. 14
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_454095_SpringJasperReports()
    {
        LOGGER.info("This is to verify_ALM_454094_SpringJasperReports");

        url = "http://"
              + tomcatHost
              + ":"
              + envProperties
                      .getRolePropertyById(TOMCAT_ROLE_ID,
                                           DeployTomcatFlowContext.ENV_TOMCAT_PORT)
              + "/spring-jasper-integration/krams/main/download/pdf";

        startAgent();
        checkTomcatStartupMessage();
        TA.SpringJasperReportsApp(url);

        harvestWait(15);

        String metricExpression1 = "Spring\\|Jasper Reports\\|Report Filling:Responses Per Interval";
        String metricExpression2 = "Spring\\|Jasper Reports\\|Report Loading:Responses Per Interval";
        String metricExpression3 = "Spring\\|Jasper Reports\\|Report Rendering:Responses Per Interval";

        String tempResult1 = getDataUsingCLW(metricExpression1);
        String tempResult2 = getDataUsingCLW(metricExpression2);
        String tempResult3 = getDataUsingCLW(metricExpression3);

        Assert.assertFalse("Spring Jasper Reports Report Filling metric didn't reported  ",
                           tempResult1.contains("-1"));
        Assert.assertFalse("Spring Jasper Reports Report Loading metric didn't reported  ",
                           tempResult2.contains("-1"));
        Assert.assertFalse("Spring Jasper Reports Report Rendering metric didn't reported  ",
                           tempResult3.contains("-1"));

        testIdName = "ALM_454094_SpringJasperReports";
        renameLogFiles(testIdName);

    }

    @AfterMethod
    public void tearDown()
    {
        try
        {
            checkFileExistenceOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
                                             tomcatLogFile);
            LOGGER.info("Entered into tearDown try method");
            stopAgent();
            renameLogFiles("TestFailed_" + counter);
            
            counter++;
            LOGGER.info("counter value increated");

        } catch (Exception e)
        {
            // Do nothing
            LOGGER.info("Tomcat log not available do nothing");

        }

    }

	public void checkFileExistenceOneTimeCounter(
			EnvironmentPropertyContext envProps, String machineId,
			String filePath) {
		CheckFileExistenceFlowOneTimeCounterContext checkFileExistenceFlowOneTimeCounterContext = new CheckFileExistenceFlowOneTimeCounterContext.Builder()
				.filePath(filePath).build();
		runFlowByMachineId(machineId,CheckFileExistenceFlowOneTimeCounter.class,checkFileExistenceFlowOneTimeCounterContext);
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

    public String getDataUsingCLW(String metricExpression)
    {
        String metricData;
        String agentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";

        metricData = clw.getLatestMetricValue(user, password, agentExpression,
                                              metricExpression, EMHost,
                                              Integer.parseInt(emPort),
                                              emLibDir);

        LOGGER.info("Metric data : " + metricData);

        return metricData;
    }

    public void renameLogFiles(String testIdName)
    {
        try
        {
            stopAgent();
            LOGGER.info("Tomcat Stopped Successfully");
        } catch (Exception e)
        {
            LOGGER.info("Tomcat Stopping Failed");
        }

        finally
        {
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
                                    testIdName);
            renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
                                    testIdName);
        }
    }

}
