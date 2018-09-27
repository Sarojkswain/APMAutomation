/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.test.em.agc;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.Browser;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.tas.test.em.agc.SimpleAgcTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;

@Tas(testBeds = @TestBed(name = SimpleAgcTestBed.class, executeOn = "master"), owner = "korzd01", size = SizeType.DEBUG, snapshot = SnapshotMode.LIVE)
@Test(groups = {"agc", "regression"})
public class AGCRegisterTest extends WebViewTestNgTest {

    private final Logger log = Logger.getLogger(getClass());
    
    private final String etcUrl;
    private final String etcVWUrl;
    private final String vwUrl;
    private final String followerApiUrl;
    
    AGCRegisterTest() {
        etcUrl = getEmWebUrl(SimpleAgcTestBed.MASTER_ROLE_ID);
        etcVWUrl = getVWUrl(SimpleAgcTestBed.MASTER_ROLE_ID);
        vwUrl = getVWUrl(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        followerApiUrl = getEmWebUrl(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
    }
    
    @BeforeSuite
    public void beforeSuite() {
        //start EM, WV
        killWebview(SimpleAgcTestBed.MASTER_ROLE_ID);
        killWebview(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        killEM(SimpleAgcTestBed.MASTER_ROLE_ID);
        killEM(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        
        startEmAndWebview(SimpleAgcTestBed.MASTER_ROLE_ID);
        startEmAndWebview(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
    }
    @BeforeMethod
    public void beforeMethod() {
        
    }

    @Test
    public void registrationTestWithCancelingRegistrationFromMaster() throws Exception {
        Browser browser = new Browser();

        runStep(new BrowserRunnable(browser, "testRegistration") {
            public void run() throws Exception {
                registerSteps.registerTest(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
        
        runStep(new BrowserRunnable(browser, "testCancelRegistrationFromMaster") {
            public void run() throws Exception {
                registerSteps.testCancelRegistrationFromMaster(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
    }
    
    @Test
    public void registrationTestWithCancelingRegistrationFromFollower() throws Exception {
        Browser browser = new Browser();

        runStep(new BrowserRunnable(browser, "testRegistration") {
            public void run() throws Exception {
                registerSteps.registerTest(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
        
        runStep(new BrowserRunnable(browser, "testCancelRegistrationFromFollower") {
            public void run() throws Exception {
                registerSteps.testCancelRegistrationFromFollower(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
    }
    
    @Test
    public void registrationTestWithGracefulDeregistration() throws Exception {
        registerTest();
        testDeregistrationAndCancelDeregistration();
        testGracefulUnregister();
    }

    
    @Test
    public void registrationTestWithForceDeregistration() throws Exception {
        registerTest();
        testForceUnregister();
    }
   
    
    private void registerTest() throws Exception {
        
        Browser browser = new Browser();
        
        runStep(new BrowserRunnable(browser, "registerTest") {
            public void run() throws Exception {
                registerSteps.registerTest(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
             
        restartFollower();

        runStep(new BrowserRunnable(browser, "testCheckFollowerOnline") {
            public void run() throws Exception {
                registerSteps.testCheckFollowerOnline(etcUrl, etcVWUrl, vwUrl,
                    followerApiUrl);
            }
        });
        
    }

    private void testDeregistrationAndCancelDeregistration() throws Exception {
        
        Browser browser = new Browser();
        
        runStep(new BrowserRunnable(browser, "testDeregistrationFromFollower") {
            public void run() throws Exception {
                registerSteps.testDeregistrationFromFollower(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });

        runStep(new BrowserRunnable(browser, "testCancelDeregistrationFromFollower") {
            public void run() throws Exception {
                registerSteps.testCancelDeregistrationFromFollower(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
        
        runStep(new BrowserRunnable(browser, "testDeregistrationFromMaster") {
            public void run() throws Exception {
                registerSteps.testDeregistrationFromMaster(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });

        runStep(new BrowserRunnable(browser, "testCancelDeregistrationFromMaster") {
            public void run() throws Exception {
                registerSteps.testCancelDeregistrationFromMaster(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
    }
    
    private void testGracefulUnregister() throws Exception {
        
        Browser browser = new Browser();
        
        runStep(new BrowserRunnable(browser, "testDeregistrationFromFollower") {
            public void run() throws Exception {
                registerSteps.testDeregistrationFromFollower(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
        restartFollower();
        
        runStep(new BrowserRunnable(browser, "testCheckUnregistered") {
            public void run() throws Exception {
                registerSteps.testCheckUnregistered(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
    }
    
    private void testForceUnregister() throws Exception {
        
        stopFollower();
        Utils.sleep(20000); //time for sync master with follower
        
        Browser browser = new Browser();
        
        runStep(new BrowserRunnable(browser, "testForceDeregistrationFromMaster") {
            public void run() throws Exception {
                registerSteps.testForceDeregistrationFromMaster(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
        startFollower();
        Utils.sleep(20000); //time for sync master with follower
        
        runStep(new BrowserRunnable(browser, "testForceDeregistrationFromFollower") {
            public void run() throws Exception {
                registerSteps.testForceDeregistrationFromFollower(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
        restartFollower();
        
        runStep(new BrowserRunnable(browser, "testCheckUnregistered") {
            public void run() throws Exception {
                registerSteps.testCheckUnregistered(etcUrl, etcVWUrl, vwUrl, followerApiUrl);
            }
        });
        
    }
    
    protected void restartFollower () {
        stopFollower();
        startFollower();
    }
    
    protected void stopFollower () {
        killWebview(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        killEM(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        
    }
    
    protected void startFollower () {
        startEmAndWebview(SimpleAgcTestBed.FOLLOWER_ROLE_ID);
        
    }
    
    protected void checkIfFollowerIsStandalone() {
        
    }
    
    
    private void runStep(BrowserRunnable step) throws Exception {
        step.getBrowser().open();
        try {
            step.run();
        } catch (Exception ex) {
            step.getBrowser().takeScreenshot("AGCRegisterTest", step.getMethodName(), "FAILURE");
            log.info("STEP " + step.getMethodName() + " failed!");
            throw ex;
        } finally {
            step.getBrowser().close();
        }
        log.info("STEP " + step.getMethodName() + " finished!");
    }
    
    abstract class BrowserRunnable {
        final protected Browser browser;
        final protected String methodName;
        final protected RegisterSteps registerSteps;
        
        BrowserRunnable(Browser browser, String methodName) {
            this.browser = browser;
            this.methodName = methodName;
            registerSteps = new RegisterSteps(browser);
        }
        
        public Browser getBrowser() {
            return browser;
        }

        public String getMethodName() {
            return methodName;
        }

        public RegisterSteps getRegisterSteps() {
            return registerSteps;
        }
        
        public abstract void run() throws Exception;
    }
}
