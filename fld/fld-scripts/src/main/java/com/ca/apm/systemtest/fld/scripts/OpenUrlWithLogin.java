/**
 * 
 */
package com.ca.apm.systemtest.fld.scripts;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPluginException;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumTest;

/**
 * This sample selenium test logs into a webview server, and navigates to the "Management" tab.
 * After
 * one minute, it exits and closes the browser. It can be executed as a standalone program by
 * running the main
 * method with arguments, or used by the new FLD automation.
 * 
 * @author KEYJA01, TAVPA01
 *
 */
public class OpenUrlWithLogin implements SeleniumTest {
	private static final Logger log = LoggerFactory.getLogger(OpenUrlWithLogin.class);
	private boolean stop = false;

	private static final String URL_KEY = "url";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	private static final String LOGIN_FRAME_ELEM_KEY = "login-frame-elem";
	private static final String USER_ELEM_KEY = "user-elem";
	private static final String PASS_ELEM_KEY = "pass-elem";
	private static final String LOGIN_BUTTON_ELEM_KEY = "login-button-elem";
	private static final String CLICK_ELEM_KEY = "click-elem";

	private static final String[] REQUIRED_FIELDS = {URL_KEY, USERNAME_KEY};
	private static final HashMap<String, String> DEFAULT_VALUES = new HashMap<>();
	static {
		DEFAULT_VALUES.put(PASSWORD_KEY, "");
		DEFAULT_VALUES.put(LOGIN_FRAME_ELEM_KEY, "LoginFrame");
		DEFAULT_VALUES.put(USER_ELEM_KEY, "username");
		DEFAULT_VALUES.put(PASS_ELEM_KEY, "j_passWord");
		DEFAULT_VALUES.put(LOGIN_BUTTON_ELEM_KEY, "webview-loginPage-login-button");
		DEFAULT_VALUES.put(CLICK_ELEM_KEY, "//span[text()='Management']/ancestor::a");
	}

	@Override
	public void executeSeleniumScript(final SeleniumPlugin plugin, final Map<String, String> params) throws SeleniumPluginException {
		// Check required params
		StringBuilder missing = new StringBuilder();
		String delim="";
		for (String req : REQUIRED_FIELDS) {
			if (!params.containsKey(req)) {
				missing.append(delim).append("Field " + req + " is required in params");
				delim=":: ";
			}
		}
		if (missing.length() > 0) {
			throw new SeleniumPluginException(missing.toString());
		}

		// Fill missing values
		for (String defKey : DEFAULT_VALUES.keySet()) {
			if (!params.containsKey(defKey)) {
				params.put(defKey, DEFAULT_VALUES.get(defKey));
			}
		}

		// Start test
		try {
			openUrl(plugin, params);
		} catch (InterruptedException e) {
			ErrorUtils.logExceptionFmt(log, e, "Plugin {1} interrupted. Exception: {0}", plugin);
		}
	}

	public void shouldStop() {
		stop = true;
	}

	private void openUrl(SeleniumPlugin plugin, Map<String, String> params) throws SeleniumPluginException, InterruptedException {
		WebDriver driver = null;
		String sessionId = null;

		try {
			sessionId = plugin.startSession();
			driver = plugin.webDriver(sessionId);

			boolean loggedIn = login(driver, params);
			if (!loggedIn) {
				log.info("Not logged in, exiting test");
				return;
			}

			if (stop) {
				return;
			}

			log.info("Looking for the management span");

			// yes, this is a really hacky way to find the <a> for the management tab in the
			// application
			// but the generated HTML elements don't have predictable CSS class names or ids
			// so we find the <a> link higher up in the DOM that contains the Management tab.
			WebElement managementTabLink = driver.findElement(By.xpath(params.get(CLICK_ELEM_KEY)));

			Thread.sleep(1500L);
			if (stop) {
				return;
			}

			managementTabLink.click();

			log.info("We just clicked on the management tab");

			while (!stop) {
				Thread.sleep(1000L);
			}

			log.info("Test is now concluded");

		} finally {
			if (sessionId != null) {
				plugin.closeSession(sessionId);
			}
		}
	}

	/**
	 * Attempts to log the user into a new session
	 * 
	 * @param driver
	 * @param params
	 * @return
	 * @throws InterruptedException 
	 */
	private boolean login(WebDriver driver, Map<String, String> hashParams) throws InterruptedException {
		// open up a new browser window to the webview URL
		String url = hashParams.get(URL_KEY);
		driver.get(url);

		long startTime = System.currentTimeMillis();
		long elapsed = System.currentTimeMillis() - startTime;
		String frameId = hashParams.get(LOGIN_FRAME_ELEM_KEY);
		WebElement loginFrame = null;
		if (frameId.length() > 0) {
			while (loginFrame == null && elapsed < 30000L) {
				loginFrame = driver.findElement(By.id(frameId));
				if (loginFrame == null) {
					Thread.sleep(1000L);
				}
				elapsed = System.currentTimeMillis() - startTime;
			}
			log.info("Login frame {}", loginFrame);
			if (loginFrame == null) {
				log.error("Can NOT find login frame");
				return false;
			}
		}

		// let finish loading
		Thread.sleep(5000L);

		if (loginFrame != null) {
			// switch to the iframe login page
			driver.switchTo().frame(loginFrame);
		}

		// find the login form elements
		WebElement usernameInput = findElementById(driver, hashParams.get(USER_ELEM_KEY));
		WebElement passwordInput = findElementById(driver, hashParams.get(PASS_ELEM_KEY));
		WebElement button = findElementById(driver, hashParams.get(LOGIN_BUTTON_ELEM_KEY));

		// send the username and password using the form
		usernameInput.sendKeys(hashParams.get(USERNAME_KEY));
		passwordInput.sendKeys(hashParams.get(PASSWORD_KEY));
		button.click();

		WebElement div = null;
		startTime = System.currentTimeMillis();
		elapsed = 0L;
		while (div == null && elapsed < 60000L) {
			div = findElementByClassName(driver, "webviewViewport");
			Thread.sleep(1000L);
			elapsed = System.currentTimeMillis() - startTime;
		}

		System.out.println(div);

		// if the webviewViewport div is present, then we likely logged in successfully
		return (div != null);
	}

	private WebElement findElementById(SearchContext driver, String id) {
		WebElement e = null;
		try {
			e = driver.findElement(By.id(id));
		} catch (Exception ex) {
			// ignore
		}

		return e;
	}


	private WebElement findElementByClassName(SearchContext driver, String className) {
		WebElement e = null;
		try {
			e = driver.findElement(By.className(className));
		} catch (Exception ex) {
			// ignore
		}

		return e;
	}
}
