/**
 *
 */
package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.HelpLinksTest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.landing.Tile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ca.apm.test.atc.HelpLinksConstants.*;

/**
 * E2E Selenium tests to test help doc links on the landing page.
 *
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class LandingPageHelpLinksTest extends HelpLinksTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LandingPageHelpLinksTest.class);

    @Test(groups = "failing")
    public void testTeamCenterHomePageHelpLinks() throws Exception {
        TestRunner runner = new TestRunner(getUI());
        runner.runLandingPageAppHelpLinksCheck();
    }

    @Test
    public void testSupportedLanguagesForHelpDocs() throws Exception {
        TestRunner runner = new TestRunner(getUI());
        runner.runSupportedLanguagesCheck();
    }

    @Test
    public void testDevelopmentSpace() throws Exception {
        TestRunner runner = new TestRunner(getUI());
        runner.runDevelopmentSpaceCheck();
    }

    public static void main(String[] args) throws Exception {
        TestRunner runner = new TestRunner("http://tas-cz-nc.ca.com:8082/ApmServer/#/home");
        try {
            runner.runDevelopmentSpaceCheck();
            runner.runSupportedLanguagesCheck();
            runner.runLandingPageAppHelpLinksCheck();
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
            this.ui = new UI(wd, startUrl, View.HOMEPAGE);
            init();
        }

        public void runLandingPageAppHelpLinksCheck() throws Exception {
            setUpDocOpsReleaseOverride(ui.getDriver(), UNKNOWN_RELEASE_VERSION);
            setUpAppRelease(ui.getDriver(), TEST_RELEASE_VERSION);
            setUpLanguage(ui.getDriver(), EN_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), HELP_LINK_XPATH,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

            checkATCHelpLinkHIDs(HOME_QUICK_LINKS_HELP_LINK_XPATH);
        }

        public void runSupportedLanguagesCheck() {
            checkSupportedLanguages(HOME_QUICK_LINKS_HELP_LINK_XPATH);
            checkSupportedLanguages(HELP_LINK_XPATH);
        }

        public void runDevelopmentSpaceCheck() {
            checkDevelopmentSpace(HOME_QUICK_LINKS_HELP_LINK_XPATH);
            checkDevelopmentSpace(HELP_LINK_XPATH);
        }

        private void checkDevelopmentSpace(String linkXpath) {
            setUpLanguage(ui.getDriver(), EN_LANG);
            setUpDocOpsReleaseOverride(ui.getDriver(), UNKNOWN_RELEASE_VERSION);
            setUpAppRelease(ui.getDriver(), FEATURE_BRANCH_RELEASE_VERSION);
            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, DEV_SPACE, DEFAULT_LANG);

            setUpAppRelease(ui.getDriver(), VARIABLE_PLACEHOLDER_VERSION);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, DEV_SPACE, DEFAULT_LANG);

            setUpAppRelease(ui.getDriver(), UNKNOWN_RELEASE_VERSION);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, DEV_SPACE, DEFAULT_LANG);

        }

        private void checkATCHelpLinkHIDs(String linkXpath) throws Exception {
            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);


            Tile tile = null;
            for (Tile t : ui.getLandingPage().getTiles()) {
                if (t.hasNextDrilldownLevel()) {
                    tile = t;
                    break;
                }
            }
            if (tile == null) {
                Assert.fail("There is no tile you could drill down into.");
            } else {
                tile.drillDown();

                checkHelpLinkElement(ui.getDriver(), linkXpath,
                        HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

                tile = ui.getLandingPage().getTiles().get(0);
                tile.openNotebook();

                checkHelpLinkElement(ui.getDriver(), linkXpath, HID_NOTEBOOK,
                        EXPECTED_SPACE, DEFAULT_LANG);
            }
        }

        private void checkSupportedLanguages(String linkXpath) {
            setUpDocOpsReleaseOverride(ui.getDriver(), UNKNOWN_RELEASE_VERSION);
            setUpAppRelease(ui.getDriver(), TEST_RELEASE_VERSION);
            setUpLanguage(ui.getDriver(), KO_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_KR_LANG);

            setUpLanguage(ui.getDriver(), JA_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_JP_LANG);

            setUpLanguage(ui.getDriver(), ZH_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_CN_LANG);

            setUpLanguage(ui.getDriver(), ZH_TW1_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_TW_LANG);

            setUpLanguage(ui.getDriver(), ZH_TW2_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_TW_LANG);

            setUpLanguage(ui.getDriver(), ZH_TW3_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, EXPECTED_TW_LANG);

            setUpLanguage(ui.getDriver(), EN_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

            setUpLanguage(ui.getDriver(), DEFAULT_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

            setUpLanguage(ui.getDriver(), HY_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

            setUpLanguage(ui.getDriver(), CZ_LANG);

            refreshState();

            checkHelpLinkElement(ui.getDriver(), linkXpath,
                    HID_TEAM_CENTER_HOME_PAGE, EXPECTED_SPACE, DEFAULT_LANG);

        }

        /*
         * Each time we try to execute JavaScript changes through the WebDriver
         * the changes apply only after we move to some other page. Don't understand
         * the reason of it, but this 'state refreshing' (not page refreshing!) is
         * needed for that purpose.
         */
        private void refreshState() {
            ui.getLeftNavigationPanel().goToPerspectives();
            ui.getLeftNavigationPanel().goToHomePage();
            ui.getLandingPage().waitForTilesToLoad(true);
        }

        private void init() throws Exception {
            ui.login();
            ui.getLandingPage().waitForTilesToLoad(true);
        }

        /**
         * Cleans up UI. Call this method only when running
         * in a standalone mode.
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