/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.em.appmap;

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.Browser;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.test.em.util.RestUtility;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.em.appmap.AppMapTradingServiceTestBed;
import com.ca.tas.test.em.appmap.PhantomJSTest;
import com.ca.tas.test.utils.LocalStorage;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Contains the end-to-end testing scenarios to verify the topological information provided by map
 * view capability of AppMap feature.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
public class AppMapTopologySystemTest extends WebViewTestNgTest {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final EnvironmentPropertyContext envProp;
    private LocalStorage localStorage;
    private RestUtility utility = new RestUtility();
    
    public AppMapTopologySystemTest() throws Exception {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }
    
    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        localStorage = new LocalStorage(testMethod);
    }
    
    /**
     * Verifies APM UI displaying a particular <b>Business Transaction</b> monitored in the testing
     * environment.
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>test bed with provisioned <i>Trade Service</i> testing web application</li>
     * <li>testing web application is monitored by CA Agent</li>
     * <li>definition of Business Service and match rules for Business Transactions is propagated to
     * CA Agent</li>
     * <li>Business Transactions have been executed against testing web application</li>
     * <li>you can see the topology information of monitored applications in <i>By Frontend</i> and
     * <i>By Business Service</i> views of original Triage Map WebView's feature</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>go to the trend view of Business Services in APM UI</li>
     * <li>select the <i>Trading Service</i> Business Service in trend view</li>
     * <li>display the map view by choosing <i>Go To Map</i> button</li>
     * <li>is <i>Balances</i> Business Transaction present?</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTING RESULTS</h5>
     * <p>
     * We are able to locate <i>Balances</i> Business Transaction in map view of <i>Trading
     * Service</i> Business Service.
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * The test scenario depends on 4 types of REST resources:
     * <ul>
     * <li>/api/service: If you are missing any expected Business Service the appropriate controller
     * is not providing that.</li>
     * <li>/api/transaction: If you are missing any expected Business Transaction the appropriate
     * controller is not providing that.</li>
     * <li>/api/application: If you are missing any expected Application the appropriate controller
     * is not providing that.</li>
     * <li>/api/graph: If you are missing any expected element in map view the appropriate
     * controller is not providing that.</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = @TestBed(name = AppMapTradingServiceTestBed.class, executeOn = "endUserMachine"), owner = "pospa02", size = SizeType.SMALL)
    @Test(groups = {"appmap", "smoke"})
    public void testExistenceOfMonitoredBusinessTransactionOnMap() throws Exception {
        localStorage.fetchResource(envProp.getRolePropertiesById("trade-service").getProperty(
                "tbexports.default.url"));
        PhantomJSTest.execute("importBT_TradingService.js", envProp,
                localStorage.getFileLocations("*bt-exports*.zip"));

        /* generate data by trade-service */
        //PhantomJSTest.execute("/com/ca/tas/test/em/appmap/trade.js", envProp);
        startTTs();
        String tomcatHost = getFQDN(envProperties.getMachineHostnameByRoleId("tomcat60"));
        String tomcatPort = envProperties.getRolePropertyById("tomcat60", "tomcatCatalinaPort");
        String args[] = { "http://" + tomcatHost + ":" + tomcatPort + "/" };
        for (int j = 0; j < 3; ++j) {
            main(args);
        }
        
        log.info("Going to sleep for 1 minute to be sure the information from APM monitoring facility is propagated to EM");
        TimeUnit.MINUTES.sleep(1);
        
        checkWebview("introscope");

        configureDecorationPolicy();
        
        assertTrue(PhantomJSTest.execute("/com/ca/tas/test/em/appmap/maptest.js", envProp));
    }
    
    private void startTTs() {
        String command = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
        ClwRunner standaloneClwRunner = utilities.createClwUtils("introscope").getClwRunner();
        
        standaloneClwRunner.runClw(command);
    }

    private void configureDecorationPolicy() throws Exception {
        String emHost = envProperties.getMachineHostnameByRoleId("introscope");
        utility.addGroup(emHost, "company");
        List<List<String>> ids = utility.getBTVertices(emHost,
                Arrays.asList("Options Trading", "Place Order", "Balances", "Login"));
        utility.assignAttributeToVertices(emHost, ids.get(0), "company", "Pepsi");
        utility.assignAttributeToVertices(emHost, ids.get(1), "company", "Pepsi");
        utility.assignAttributeToVertices(emHost, ids.get(2), "company", "Pepsi");
        utility.assignAttributeToVertices(emHost, ids.get(3), "company", "Lego");
    }
    
    public static void main(String[] args) {
        String baseUrl = args[0];
        String localHrefs[] =
            {"TradeService" /* to obtain COOKIES */, "TradeService" /* click with COOKIES */,
                    "AuthenticationService/ServletA6", "TradeService/PlaceOrder",
                    "TradeService/TradeOptions", "TradeService/ViewOrders",
                    "ReportingService/ServletA6"};

        try {
            Browser browser = new Browser();
            browser.openDefault();
            try {
                for (int i = 0; i < localHrefs.length; ++i) {
                    browser.getDriver().get(baseUrl + localHrefs[i]);
                    Thread.sleep(50);
                }
            } catch (Exception ex) {
                browser.takeScreenshot("AppMapTopologySystemTest", "TradeService", "FAILURE");
            } finally {
                browser.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
