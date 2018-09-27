package com.ca.apm.nextgen.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * @author bocto01
 */
public class ConnectivityTest extends BaseWebViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityTest.class);

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(332571)
    // (Login.BadPassword) NEG - 016 - Login with bad password (Automated)
    public void loginBadPasswordTest() {
        try (WebViewUi ui = getWvUi()) {

            ui.login(getWvUrl(), "Admin", "x", false);

            ui.getWebDriver().switchTo().frame("LoginFrame");

            WebElement loginerror = ui.waitForWebElement(By.id("loginerror"));
            assertEquals(loginerror.getText(), "A log in error occurred.");

            ui.getWebDriver().switchTo().defaultContent();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(332562)
    // (Login.Login_AsAdmin) 007 - Login as Admin (Automated)
    public void loginAsAdminTest() {
        try (WebViewUi ui = getWvUi()) {

            boolean login = ui.login(getWvUrl(), "Admin", "", true);
            assertTrue(login);

            boolean logout = ui.logout(true);
            assertTrue(logout);

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(332563)
    // (Login.Login_AsGuest) 008 - Login as Guest (Automated)
    public void loginLoginAsGuestTest() {
        try (WebViewUi ui = getWvUi()) {

            boolean login = ui.login(getWvUrl(), "Guest", "", true);
            assertTrue(login);

            boolean logout = ui.logout(true);
            assertTrue(logout);

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(357264)
    // (Logout.Basic) WebView Logout Link
    public void logoutBasicTest() {
        try (WebViewUi ui = getWvUi()) {

            String user = "Test1";
            String logoutLinkText = user + " (Logout)";

            boolean login = ui.login(getWvUrl(), user, "", true);
            assertTrue(login);

            WebElement logoutLink = ui.waitForLogoutLink(logoutLinkText);
            assertEquals(logoutLink.getText(), logoutLinkText);

            ui.getActions().moveToElement(logoutLink).perform();

            boolean logout = ui.logout(true);
            assertTrue(logout);

            login = ui.login(getWvUrl(), user, "", true);
            assertTrue(login);

            logout = ui.logout(true);
            assertTrue(logout);

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(357267)
    // (Logout.Guest) Login as Guest
    public void logoutGuestTest() {
        try (WebViewUi ui = getWvUi()) {

            String user = "Guest";
            String logoutLinkText = user + " (Logout)";

            boolean login = ui.login(getWvUrl(), user, "", true);
            assertTrue(login);

            WebElement logoutLink = ui.waitForLogoutLink(logoutLinkText);
            assertEquals(logoutLink.getText(), logoutLinkText);

            login = ui.login(getWvLoginUrl(), user, "", false, false, true);
            assertTrue(login);

            logoutLink = ui.waitForLogoutLink(logoutLinkText);
            assertEquals(logoutLink.getText(), logoutLinkText);

            boolean logout = ui.logout(true);
            assertTrue(logout);

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

}
