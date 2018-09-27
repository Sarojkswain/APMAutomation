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
 */
package com.ca.apm.test.atc.performance;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.testbed.performance.TeamCenterPerformanceTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;

@Tas(testBeds = @TestBed(name = TeamCenterPerformanceTestBed.class, executeOn = "endUserMachine"), owner = "jirji01", size = SizeType.DEBUG, snapshot = SnapshotMode.LIVE)
@Test(groups = {"atc", "performance"})
public class PerformanceRegressionChromeSuite extends WebViewTestNgTest {

    private final Logger log = Logger.getLogger(getClass());

    // private FiltersPerformanceTest filtersPerformanceTest = new FiltersPerformanceTest();
    private TimelinePerformanceTest timelinePerformanceTest = new TimelinePerformanceTest();
    private ViewTabsPerformanceTest viewTabsPerformanceTest = new ViewTabsPerformanceTest();


    private final Map<String, UITest> testDelegationMapping = new HashMap<>();

    public PerformanceRegressionChromeSuite() {
        testDelegationMapping.put("timelinePerformanceTest", timelinePerformanceTest);
        testDelegationMapping.put("viewTabsPerformanceTest", viewTabsPerformanceTest);
    }

    @BeforeSuite
    public void beforeSuite() throws Exception {
        
        log.info("Ramp up load before test suite is started");
        Thread.sleep(300000);
        log.info("Continue suite startup.");
        
        checkWebview("introscope");

        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, BeforeSuite.class);
        }
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, AfterSuite.class);
        }
    }

    @BeforeTest
    public void beforeTest() throws Exception {
        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, BeforeTest.class);
        }

    }

    @AfterTest
    public void afterTest() throws Exception {
        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, AfterTest.class);
        }

    }

    @BeforeClass
    public void beforeClass() throws Exception {
        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, BeforeClass.class);
        }

    }

    @AfterClass
    public void afterClass() throws Exception {
        for (UITest uiTest : testDelegationMapping.values()) {
            invokeObjectMethodWithAnnotation(uiTest, AfterClass.class);
        }

    }

    @BeforeMethod
    public void beforeMethod(Method method, ITestContext ctx) throws Exception {
        String[] testMethodNameParts = StringUtils.split(method.getName(), "_", 2);
        UITest uiTest = testDelegationMapping.get(testMethodNameParts[0]);
        if (uiTest == null) {
            throw new IllegalStateException(format(
                "There is no UI test registered for test method %s", method.getName()));
        }
        invokeObjectMethodWithAnnotation(uiTest, BeforeMethod.class, method, ctx);
    }

    @AfterMethod
    public void afterMethod(Method method, ITestContext ctx, ITestResult testResult)
        throws Exception {
        String[] testMethodNameParts = StringUtils.split(method.getName(), "_", 2);
        UITest uiTest = testDelegationMapping.get(testMethodNameParts[0]);
        if (uiTest == null) {
            throw new IllegalStateException(format(
                "There is no UI test registered for test method %s", method.getName()));
        }
        invokeObjectMethodWithAnnotation(uiTest, AfterMethod.class, method, ctx, testResult);
    }

    // @Test
    // public void filtersPerformanceTest_testFiltersActivation() throws Exception {
    // filtersPerformanceTest.testFiltersActivation(100);
    // }

    @Test
    public void timelinePerformanceTest_testDecreasingEndTime() throws Exception {
        timelinePerformanceTest.testDecreasingEndTime(120);
    }

    @Test
    public void viewTabsPerformanceTest_testSwitchingTabs() throws Exception {
        viewTabsPerformanceTest.testSwitchingTabs(200);
    }

    /**
     * 
     * @param uiTest
     * @param annotationClass
     * @param optionalArgs
     */
    protected void invokeObjectMethodWithAnnotation(UITest uiTest,
        Class<? extends Annotation> annotationClass, Object... optionalArgs) {

        if (uiTest == null) {
            return;
        }

        try {
            Method annotatedMethod = null;
            for (Method method : uiTest.getClass().getMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    // there should be only one annotated method for Test lifecycle
                    annotatedMethod = method;
                }
            }

            if (annotatedMethod == null) {
                log.debug(format("No method found with %s annotation in UI test %s",
                    annotationClass.getName(), uiTest.getClass().getName()));
                return;
            }
            log.debug(format("Found %s method with %s annotation in UI test %s",
                annotatedMethod.getName(), annotationClass.getName(), uiTest.getClass().getName()));

            Object[] parameters = ArrayUtils.EMPTY_OBJECT_ARRAY;
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
            annotatedMethod.invoke(uiTest, parameters);
        } catch (Exception e) {
            throw new IllegalStateException(format(
                "Unable to invoke method of UI test annotated with %s", annotationClass), e);
        }
    }
}
