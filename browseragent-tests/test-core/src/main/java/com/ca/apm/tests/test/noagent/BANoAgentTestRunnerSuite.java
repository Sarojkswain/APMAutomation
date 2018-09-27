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



package com.ca.apm.tests.test.noagent;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.tests.test.selenium.AJAXTests;
import com.ca.apm.tests.test.selenium.AXATests;
import com.ca.apm.tests.test.selenium.ConfigurationTests;
import com.ca.apm.tests.test.selenium.ExtensionTests;
import com.ca.apm.tests.test.selenium.GeneralTests;
import com.ca.apm.tests.test.selenium.JSErrorsTests;
import com.ca.apm.tests.test.selenium.JSFunctionTests;
import com.ca.apm.tests.test.selenium.PageLoadTests;
import com.ca.apm.tests.test.selenium.SPATests;
import com.ca.apm.tests.test.selenium.SeleniumBase;
import com.ca.apm.tests.test.selenium.SnippetTests;
import com.ca.apm.tests.test.selenium.URLIncludeExcludeTests;
import com.ca.apm.tests.testbed.BrowserAgentTomcatChromeWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatFirefoxWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatIEWinTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

/**
 * This class is a launching point for all selenium tests that derive from SeleniumBase.
 * Users should write their tests in child classes of SeleniumBase, then hook them into the test
 * bed.
 * SeleniumBase class provides ability to develop/run selenium locally
 */
@Tas(snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE, testBeds = {
        @TestBed(name = BrowserAgentTomcatChromeWinTestbed.class, executeOn = BrowserAgentTomcatChromeWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatFirefoxWinTestbed.class, executeOn = BrowserAgentTomcatFirefoxWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatIEWinTestbed.class, executeOn = BrowserAgentTomcatIEWinTestbed.BROWSERAGENT_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "gupra04")
@Test(description = "Sample test for the new collector")
public class BANoAgentTestRunnerSuite extends BANoAgentBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BANoAgentTestRunnerSuite.class);

    private final Map<String, SeleniumBase> suiteOfTestClasses = new HashMap<>();

    /**
     * Test categories, steps to adding a new category:
     * 1. Define private member here as seen below
     * 2. Initialize the member in private method createAllTests
     * 3. Add a new public method for each category test you wish to call
     * see examples in this class.
     */

    private AJAXTests ajaxTests = null;
    private AXATests axaTests = null;
    private ConfigurationTests configurationTests = null;
    private ExtensionTests extensionTests = null;
    private GeneralTests generalTests = null;
    private JSErrorsTests jsErrorsTests = null;
    private JSFunctionTests jsFunctionTests = null;
    private PageLoadTests pageLoadTests = null;
    private SnippetTests snippetTests = null;
    private SPATests spaTests = null;
    private URLIncludeExcludeTests urlIncludeExcludeTests = null;

    public BANoAgentTestRunnerSuite() {

        // Called from the parent to setup env vars, etc.
        testSuiteSetUp();

        // helper call in this class to create test instances
        createAllTests();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of AJAXTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454733 454739 454742; Test Ajax valid values, threshold")
    public void AJAXTests_testAjaxValidValues_454733_454739_454742() {
        ajaxTests.testAjaxValidValues_454733_454739_454742();
    }
    
    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 455850; jQuery")
    public void AJAXTests_testJquerySupport_455850() {
        ajaxTests.testJquerySupport_455850();
    }

    /**
     * --------------------------------- End of AJAXTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of AXATests
     */

    // @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Smoke"}, description =
    // "Testcase ID:454759 ; Validates valid finger print")
    // public void AXATests_testFingerprint_454759() {
    // axaTests.testFingerprint_454759();
    // }

    /**
     * --------------------------------- End of AXATests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of ConfigurationTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454821 ; When settings are changed and 204 is sent back, config is downloaded and re-applied")
    public void ConfigurationTests_test204UpdatedConfigIsValid_454821() {
        configurationTests.test204UpdatedConfigIsValid_454821();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454830 ; test of a higher metric frequency, batch mode")
    public void ConfigurationTests_testMetricFrequency_454830() {
        configurationTests.testMetricFrequency_454830();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454826 ; test BA enable/disable")
    public void ConfigurationTests_testBAEnabled_454826() {
        configurationTests.testBAEnabled_454826();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454832 ; test valid collector url")
    public void ConfigurationTests_testValidConfigUrl_454832() {
        configurationTests.testValidConfigUrl_454832();
    }

    /**
     * --------------------------------- End of ConfigurationTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of ExtensionTests
     */


    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 455003")
    public void ExtensionTests_testNonCollidingTracers_455003() {
        extensionTests.testNonCollidingTracers_455003();
    }

    /**
     * --------------------------------- End of ExtensionTests
     */



    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of GeneralTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454928 454929 454926 454931 ; test json")
    public void GeneralTests_testJSON_454928_454929_454926_454931() {
        generalTests.testJSON_454928_454929_454926_454931();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454932 ; test geolocation")
    public void GeneralTests_testGeoLocation_454932() {
        generalTests.testGeoLocation_454932();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:455014 ; test ThinkTime Valid Value")
    public void GeneralTests_testThinkTimeValidValues_455014() {
        generalTests.testThinkTimeValidValues_455014();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:455015 ; test ThinkTime Updated Config")
    public void GeneralTests_testThinkTimeUpdatedConfig_455015() {
        generalTests.testThinkTimeUpdatedConfig_455015();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:455016 ; test ThinkTime Open Link in new window")
    public void GeneralTests_testThinkTimeOpenLinkInNewWindow_455016() {
        generalTests.testThinkTimeOpenLinkInNewWindow_455016();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:455101 ; page load flag testing")
    public void GeneralTests_testPageLoad_455101() {
        generalTests.testPageLoad_455101();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454734 ; Cookie Snapshot testing")
    public void GeneralTests_testCookieSnapshot_454734() {
        generalTests.testCookieSnapshot_454734();
    }


    /**
     * --------------------------------- End of GeneralTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of JSErrorsTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454885 ; ")
    public void JSErrorsTests_testJSErrorsEnabled_454885() {
        jsErrorsTests.testJSErrorsEnabled_454885();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454886 ; ")
    public void JSErrorsTests_testJSInvalidProfile_454886() {
        jsErrorsTests.testJSInvalidProfile_454886();
    }

    /**
     * --------------------------------- End of JSErrorsTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of JSFunctionTests
     */



    // UPDATE: This test is no longer valid, keep perhaps used for extension testing
    // @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description =
    // "Testcase ID:454883 ; basic JS function test")
    // public void JSFunctionTests_testJSFunction_454883() {
    // jsFunctionTests.testJSFunction_454883();
    // }

    /**
     * --------------------------------- End of JSFunctionTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of PageLoadTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454728 454731 454730 454732; basic Page function test, navigation timing, threshold")
    public void PageLoadTests_testPage_454728_454731_454730_454732() {
        pageLoadTests.testPage_454728_454731_454730_454732();
    }

    /**
     * --------------------------------- End of PageLoadTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of SnippetTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID:454735 454736; ")
    public void SnippetTests_testValidIdSrcTags_454735_454736() {
        snippetTests.testValidIdSrcTags_454735_454736();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454740 454741 454738; snippet attribute verification")
    public void SnippetTests_testSnipptAttrs_454740_454741_454738() {
        snippetTests.testSnipptAttrs_454740_454741_454738();
    }

    /**
     * --------------------------------- End of SnippetTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of SPA Tests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 455156; Basic json payload")
    public void SPATests_testBasicDataIntegrityTest_455156() {
        spaTests.testBasicDataIntegrityTest_455156();
    }

    /**
     * --------------------------------- End of SPA Tests
     */


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * --------------------------------- Start of URLIncludeExcludeTests
     */

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454760; Test URL Exclude")
    public void URLIncludeExcludeTests_testURLExclude_454760() {
        urlIncludeExcludeTests.testURLExclude_454760();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454761; Test URL include ")
    public void URLIncludeExcludeTests_testURLInclude_454761() {
        urlIncludeExcludeTests.testURLInclude_454761();
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 454762; Include/Exclude ")
    public void URLIncludeExcludeTests_testURLIncludeExclude_454762() {
        urlIncludeExcludeTests.testURLIncludeExclude_454762();
    }


    /**
     * --------------------------------- End of URLIncludeExcludeTests
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Returns the working directory for this test suite that all tests will use when
     * starting their test collectors. Most likely C:\automation\deployed\BATestCollectorWorkingDir
     * If this directory doesnt exist, then this method will also create it.
     * 
     * @return String the working directory
     */

    private String getCollectorWorkingDirectory() {
        String workingDir =
            automationDirectoryString + File.separator + "BATestCollectorWorkingDir";

        try {
            File file = new File(workingDir);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return workingDir;
    }

    /**
     * Helper method for the constructor to create all the test categories
     */

    private void createAllTests() {

        // For the AJAX related tests.
        ajaxTests = new AJAXTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("AJAXTests", ajaxTests);

        // for axa related items
        axaTests = new AXATests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("AXATests", axaTests);

        // Profile Config stuff
        configurationTests =
            new ConfigurationTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("ConfigurationTests", configurationTests);

        extensionTests =
            new ExtensionTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("ExtensionTests", extensionTests);

        // Non specific tests...
        generalTests = new GeneralTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("GeneralTests", generalTests);

        // JavaScript Errors
        jsErrorsTests =
            new JSErrorsTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("JSErrorsTests", jsErrorsTests);

        // Java Script function
        jsFunctionTests =
            new JSFunctionTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("JSFunctionTests", jsFunctionTests);

        // Page load
        pageLoadTests =
            new PageLoadTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("PageLoadTests", pageLoadTests);

        // Snippet
        snippetTests = new SnippetTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("SnippetTests", snippetTests);

        // SPA
        spaTests = new SPATests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("SPATests", spaTests);

        // Url include/exclude
        urlIncludeExcludeTests =
            new URLIncludeExcludeTests(seleniumData, seleniumUrl, getCollectorWorkingDirectory());
        suiteOfTestClasses.put("URLIncludeExcludeTests", urlIncludeExcludeTests);

        // To simplify setting values in the future... .
        Collection<SeleniumBase> testClasses = suiteOfTestClasses.values();
        for (SeleniumBase baseClass : testClasses) {
            baseClass.setTestAppDirectoryPath(testAppFileLocation);
            // / others as needed....
        }

        // Since this depends on tests being created...
        modifyPages();
    }

    /**
     * This modifies needed brtmtestapp pages. Maybe temp if another (better solution presents
     * itself)
     */

    private void modifyPages() {

        try {

            Collection<SeleniumBase> testClasses = suiteOfTestClasses.values();

            Set<String> pageSet = new HashSet<String>();
            // For each registered test class, get all its test pages, add them to a set
            // using a set because various test page could return same page.
            // only need to process once
            for (SeleniumBase baseClass : testClasses) {
                String[] pages = baseClass.getTestPages();

                for (int i = 0; pages != null && i < pages.length; i++) {
                    pageSet.add(pages[i]);
                }
            }

            // now process the set
            for (String testPage : pageSet) {
                String fileName = testAppFileLocation + testPage;
                SeleniumBase.insertSnippetIntoPage(fileName);
            }
        } catch (Exception e) {
            LOGGER.error("modifyPages failed: " + e.getMessage());
        }
    }

    @BeforeTest
    public void beforeTest() throws Exception {
        for (SeleniumBase seleniumTest : suiteOfTestClasses.values()) {
            invokeObjectMethodWithAnnotation(seleniumTest, BeforeTest.class);
        }
    }

    @AfterTest
    public void afterTest() throws Exception {
        for (SeleniumBase seleniumTest : suiteOfTestClasses.values()) {
            invokeObjectMethodWithAnnotation(seleniumTest, AfterTest.class);
        }
        List<String> waitEntries = BATestCollectorUtils.getPerfMetrics();

        // BUG: why wont these print with the logger??
        System.out.println("Printing out the summary of collector wait results actual vs. allowed");
        System.out.println("---------------------------------------------------------------------");
        for (String entry : waitEntries) {
            System.out.println(entry);
        }
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        for (SeleniumBase seleniumTest : suiteOfTestClasses.values()) {
            invokeObjectMethodWithAnnotation(seleniumTest, BeforeClass.class);
        }
    }

    @AfterClass
    public void afterClass() throws Exception {
        for (SeleniumBase seleniumTest : suiteOfTestClasses.values()) {
            invokeObjectMethodWithAnnotation(seleniumTest, AfterClass.class);
        }
    }

    @BeforeMethod
    public void beforeMethod(Method method, ITestContext testContext) throws Exception {
        String[] testMethodNameParts = StringUtils.split(method.getName(), "_", 2);
        SeleniumBase seleniumTest = suiteOfTestClasses.get(testMethodNameParts[0]);
        if (seleniumTest == null) {
            throw new IllegalStateException("beforeMethod Unable to find " + method.getName());

        }
        invokeObjectMethodWithAnnotation(seleniumTest, BeforeMethod.class, method, testContext);
    }

    @AfterMethod
    public void afterMethod(Method method, ITestContext testContext, ITestResult testResult)
        throws Exception {
        String[] testMethodNameParts = StringUtils.split(method.getName(), "_", 2);
        SeleniumBase seleniumTest = suiteOfTestClasses.get(testMethodNameParts[0]);
        if (seleniumTest == null) {
            throw new IllegalStateException("afterMethod Unable to find " + method.getName());
        }
        invokeObjectMethodWithAnnotation(seleniumTest, AfterMethod.class, method, testContext,
            testResult);
    }

    /**
     * 
     * @param seleniumTest
     * @param annotationClass
     * @param optionalArgs
     */

    protected void invokeObjectMethodWithAnnotation(SeleniumBase seleniumTest,
        Class<? extends Annotation> annotationClass, Object... optionalArgs) {

        if (seleniumTest == null) {
            return;
        }

        try {
            Method annotatedMethod = null;
            for (Method method : seleniumTest.getClass().getMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    // there should be only one annotated method for Test lifecycle
                    annotatedMethod = method;
                }
            }

            if (annotatedMethod == null) {
                LOGGER.debug("No method found with  " + annotationClass.getName()
                    + " annotation in UI test " + seleniumTest.getClass().getName());
                return;
            }
            LOGGER.debug("Found " + annotatedMethod.getName() + " method with "
                + annotationClass.getName() + " annotation in UI test "
                + seleniumTest.getClass().getName());

            Object[] parameters = new Object[] {};
            if (annotatedMethod.getParameterTypes().length != 0) {
                parameters = new Object[annotatedMethod.getParameterTypes().length];
                for (Object optionalArg : optionalArgs) {
                    for (int i = 0; i < annotatedMethod.getParameterTypes().length; i++) {
                        Class<?> parameterType = annotatedMethod.getParameterTypes()[i];
                        if (parameterType.isAssignableFrom(optionalArg.getClass())) {
                            parameters[i] = optionalArg;
                        }
                    }
                }
            };
            annotatedMethod.invoke(seleniumTest, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to invoke method of UI test annotated with "
                + annotationClass);
        }
    }

}
