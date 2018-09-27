/**
 * 
 */
package com.ca.apm.test.atc;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static com.ca.apm.test.atc.HelpLinksConstants.*;

/**
 * E2E Selenium tests to test help doc links in the app map.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class AppMapHelpLinksTest extends HelpLinksTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMapHelpLinksTest.class);

    public static final String TEST_UNIVERSE = "HelpDocLinks_Test_Universe";

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL, owner = "sinal04")
    @Test(groups = "failing")
    public void testAppMapHelpLinks() throws Exception {
        TestRunner runner = new TestRunner(getUI());
        runner.runAppMapHelpLinksCheck();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL, owner = "sinal04")
    @Test
    public void testWebViewHelpLink() throws Exception {
        TestRunner runner = new TestRunner(getUI());
        runner.runWebViewHelpLinkCheck();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TestRunner runner = new TestRunner("http://tas-cz-nc.ca.com:8082/ApmServer/#/dashboard");
        try {
            runner.runAppMapHelpLinksCheck();
            runner.runWebViewHelpLinkCheck();
        } finally {
            runner.cleanup();
        }
    }

    private static class TestRunner {
        private UI ui;

        public TestRunner(UI ui) throws Exception {
            this.ui = ui;
            init();
        }

        /**
         * Constructor to create and run the test outside TAS and TestNg.
         * 
         * @param startUrl
         * @throws Exception
         */
        public TestRunner(String startUrl) throws Exception {
            RemoteWebDriver wd = createWebDriver();
            this.ui = new UI(wd, startUrl, View.DASHBOARD);
            init();
        }

        /**
         * Checks Help link on the top of the WebView page and Default Dashboard
         * links "How to Use Management Modules" and "How to Create Dashboards".
         * 
         * @throws Exception
         */
        public void runWebViewHelpLinkCheck() throws Exception {
            WebElement wvLink = ui.getTopNavigationPanel().getAnyWebviewLinkElement();
            wvLink.click();

            ui.switchToWebView();


            checkHelpLinkElementBy(ui.getDriver(), By.id("webview-introscope-help-help-link"),
                HelpLinksConstants.WEBVIEW_HELP_LINK_PATTERN, HID_BLUE_BOX_PAGE, DEV_SPACE,
                DEFAULT_LANG);

            ui.getWebView().clickConsoleTabItem();
            
            Utils.sleep(2000);
            
            List<PageElement> dashboardLinks = ui.getWebView().getDashboardLinks();
            Assert.assertNotNull(dashboardLinks);

            LOGGER.info("Found the following dashboard links: ");
            for (PageElement dashbLinkElem : dashboardLinks) {
                LOGGER.info("{}: {}", dashbLinkElem.getText(), dashbLinkElem.getAttribute("href"));
            }

            Assert.assertEquals(dashboardLinks.size(), 2);

            for (PageElement anchor : dashboardLinks) {
                String href = anchor.getAttribute("href");
                Assert.assertNotNull(href);

                /*
                 * Webview uses GWT's Hyperlink class for external dashboard links as well.
                 * Hyperlink prepends URLs with # sign.
                 */
                int ind = href.indexOf("#https");
                if (ind != -1) {
                    href = href.substring(ind + 1);
                }
                if (href.contains("dashboards")) {
                    checkHelpDocLink(HelpLinksConstants.WEBVIEW_HELP_LINK_PATTERN, href,
                        HID_CREATE_AND_EDIT_DASHBOARDS, DEV_SPACE, DEFAULT_LANG);
                } else if (href.toLowerCase().contains("management_modules")) {
                    checkHelpDocLink(HelpLinksConstants.WEBVIEW_HELP_LINK_PATTERN, href,
                        HID_CREATE_AND_USE_MANAGEMENT_MODULES, DEV_SPACE, DEFAULT_LANG);
                } else {
                    Assert.fail("Unexpected dashboard link found: " + href);
                }
            }

        }

        public void runAppMapHelpLinksCheck() throws Exception {
            setUpDocOpsReleaseOverride(ui.getDriver(), UNKNOWN_RELEASE_VERSION);
            setUpAppRelease(ui.getDriver(), TEST_RELEASE_VERSION);
            setUpLanguage(ui.getDriver(), DEFAULT_LANG);

            ui.getLeftNavigationPanel().goToMapViewPage();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HID_MAP, EXPECTED_SPACE,
                DEFAULT_LANG);

            ui.getDashboardPage().go();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HID_DASHBOARD, EXPECTED_SPACE,
                DEFAULT_LANG);

            ui.getLeftNavigationPanel().goToPerspectives();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HID_PERSPECTIVES, EXPECTED_SPACE,
                DEFAULT_LANG);

            ui.getPerspectiveSettings().displayAddPerspectiveDialog();

            checkHelpLinkElement(ui.getDriver(), PERSPECTIVES_HELP_LINK_XPATH, HID_PERSPECTIVES,
                EXPECTED_SPACE, DEFAULT_LANG);

            ui.getPerspectiveSettings().closeModalDialog();

            ui.getLeftNavigationPanel().goToUniverses();

            // TODO: change HID_Start_with_Team_Center to
            // HID_Configure_Universes when this change
            // gets pushed
            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HID_CONFIGURE_UNIVERSES,
                EXPECTED_SPACE, DEFAULT_LANG);

            if (ui.getUniverseSettings().isUniversePresent(TEST_UNIVERSE)) {
                ui.getUniverseSettings().deleteUniverse(TEST_UNIVERSE);
            }
            ui.getUniverseSettings().createUniverse(TEST_UNIVERSE);
            ui.getUniverseSettings().openUsersDialog(TEST_UNIVERSE);

            checkHelpLinkElement(ui.getDriver(), CONFIGURE_UNIVERSES_HELP_LINK_XPATH,
                HID_CONFIGURE_UNIVERSES, EXPECTED_SPACE, DEFAULT_LANG);

            ui.getUniverseSettings().closeUsersDialog();

            ui.getLeftNavigationPanel().goToDecorationPolicies();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HelpLinksConstants.HID_ATTRIBUTE_RULES,
                EXPECTED_SPACE, DEFAULT_LANG);

            try {
                ui.getLeftNavigationPanel().goToEnterprise();

                checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HelpLinksConstants.HID_TEAM_CENTER_HOME_PAGE,
                    EXPECTED_SPACE, DEFAULT_LANG);

                // Check two help links in text mode
                checkHelpLinkElement(ui.getDriver(), ENTERPRISE_TEAM_CENTER_HELP_LINK_XPATH,
                    HID_ENTERPRISE_TEAM_CENTER, EXPECTED_SPACE, DEFAULT_LANG);
                checkHelpLinkElement(ui.getDriver(),
                    CONFIGURE_ENTERPRISE_TEAM_CENTER_HELP_LINK_XPATH,
                    HID_CONFIGURE_ENTERPRISE_TEAM_CENTER, EXPECTED_SPACE, DEFAULT_LANG);
            } catch (WebDriverException e) {
                // We might be testing against Master EM, in such case there
                // should be Providers
                // instead of Enterprise tab. If the Providers tab is not found
                // either, then let it fail.
                LOGGER.warn("No 'Enterprise' tab found, trying to find 'Providers'..");
                ui.getLeftNavigationPanel().goToFollowers();

                checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HelpLinksConstants.HID_TEAM_CENTER,
                    EXPECTED_SPACE, DEFAULT_LANG);
            }

            ui.getLeftNavigationPanel().goToSecurity();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH, HelpLinksConstants.HID_CONFIGURE_SECURITY,
                EXPECTED_SPACE, DEFAULT_LANG);
        }

        private void init() throws Exception {
            ui.login();
        }

        /**
         * Cleans up UI. Call this method only when running in a standalone
         * mode.
         */
        private void cleanup() {
            if (ui != null) {
                ui.cleanup();
                try {
                    ui.getDriver().quit();
                } catch (Exception e) {
                    LOGGER.error("Error closing driver", e);
                }
            }
        }

    }
}
