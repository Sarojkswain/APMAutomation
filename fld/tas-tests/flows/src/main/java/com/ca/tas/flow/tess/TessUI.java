/**
 * 
 */
package com.ca.tas.flow.tess;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.By.ByLinkText;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.ISelect;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.apm.systemtest.fld.util.selenium.SeleniumClientBase;
import com.ca.tas.flow.tess.reports.CorrelationalSlaReport;
import com.ca.tas.flow.tess.reports.ImpactLeadersReport;
import com.ca.tas.flow.tess.reports.StatsDataCSVReport;
import com.ca.tas.flow.tess.reports.TessReportConfiguration;
import com.ca.tas.flow.tess.reports.TessReportSchedule;
import com.ca.tas.flow.tess.reports.TessReportSchedule.DayOfWeek;
import com.ca.tas.flow.tess.reports.TimeFrame;
import com.ca.tas.flow.tess.reports.TransactionCountReport;
import com.ca.tas.flow.tess.reports.TransactionDefectReport;
import com.ca.tas.flow.tess.reports.TransactionPerformanceReport;

/**
 * Provides access to programmatically configure the TESS.  Internally uses Selenium to 
 * open a browser, log in and then configure the desired stuff.
 * @author keyja01
 *
 */
public class TessUI extends SeleniumClientBase implements TessConfigurer {
    private static final Logger log = LoggerFactory.getLogger(TessUI.class);

    public static final String USER_NAME_INPUT_ELEMENT_ID = "loginForm:loginId_userName";
    public static final String PASSWORD_INPUT_ELEMENT_ID = "loginForm:loginId_passWord";
    public static final String SUBMIT_LOGIN_BUTTON_ID = "loginForm:loginId_loginButton";
    public static final String RECORDING_SESSION_URL_TEMPLATE = "http://%s:%d/wily/cem/tess/app/admin/recordingSessionList.html?pId=1";
    public static final String EMAIL_SETTINGS_URL_TEMPLATE = "http://%s:%d/wily/cem/tess/app/system/emailSettings.html?pId=1";
    public static final String CREATE_NEW_RECORDING_SESSION_ELEMENT_NAME = "new";
    public static final String FINISH_SETTING_NEW_RECORDING_SESSION_ELEMENT_NAME = "_finish";
    public static final String READ_ONLY_RECORD_TYPE_TABLE_PATH = "//div[@id='formDiv']//table[@class='formTable']//tr[1]//td[2]";
    /**
     * IP address of the client machine which loads agent.
     */
    public static final String CLIENT_IP_ADDRESS_ELEMENT_ID = "clientIpAddressString";
    /**
     * SMTP server hostname.
     */
    public static final String SMTP_SERVER_HOSTNAME_ID = "smtpServerHostname";
    /**
     * Test SMTP connection button name.
     */
    public static final String TEST_CONNECTION_ELEMENT_NAME = "test";
    /**
     * Stop recording session button name.
     */
    public static final String STOP_RECORDING_SESSION_ELEMENT_NAME = "stop";
    public static final String CLOSE_RECORDING_SESSION_ELEMENT_NAME = "done";
    /**
     * Success message for email settings test.
     */
    public static final String EMAIL_SETTINGS_TEST_SUCCESS_MESSAGE = "The Email Settings were tested successfully.";
    public static final String EMAIL_SETTINGS_SAVED_SUCCESSFULLY_MESSAGE = "The Email Settings were saved successfully.";
    
    private String hostname;
    private int port;
    private String username;
    private String password;

    /**
     *  
     * 
     */
    private TessUI(String cemHostName, int cemPort, String cemUserName, String cemUserPassword, WebDriver webDriver) {
    	super(webDriver);
        
    	this.hostname = cemHostName;
        this.port = cemPort;
        this.username = cemUserName;
        this.password = cemUserPassword;

    }

    /**
     * Creates a TessUI that will use a remote web driver to connect to Selenium Grid Hub's IE web driver.
     * 
     * @param cemHostName                     CEM hostname
     * @param cemPort                         CEM port
     * @param cemUserName                     CEM user name
     * @param cemPassword                     CEM user password
     * @param seleniumServerHostAndPort       Selenium Grid Hub's URL in form of protocol://host:port
     * @return                                ready to use TessUI object
     * @throws MalformedURLException          Selenium Grid Hub's URL in a bad format
     */
    public static TessUI createTessUIForIERemoteWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword, 
    		String seleniumServerHostAndPort) throws MalformedURLException {
    	return createTessUIForRemoteWebDriver(cemHostName, cemPort, cemUserName, cemPassword, 
    			seleniumServerHostAndPort, DesiredCapabilities.internetExplorer());
    }    

    /**
     * Creates a TessUI that will use a remote web driver to connect to Selenium Grid Hub's Firefox web driver.
     * 
     * @param cemHostName                     CEM hostname
     * @param cemPort                         CEM port
     * @param cemUserName                     CEM user name
     * @param cemPassword                     CEM user password
     * @param seleniumServerHostAndPort       Selenium Grid Hub's URL in form of protocol://host:port
     * @return                                ready to use TessUI object
     * @throws MalformedURLException          Selenium Grid Hub's URL in a bad format
     */
    public static TessUI createTessUIForFirefoxRemoteWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword, 
    		String seleniumServerHostAndPort) throws MalformedURLException {
        DesiredCapabilities cap = DesiredCapabilities.firefox();
        cap.setCapability("marionette", false);
    	return createTessUIForRemoteWebDriver(cemHostName, cemPort, cemUserName, cemPassword, 
    			seleniumServerHostAndPort, cap);
    }    

    /**
     * Creates a TessUI that will use a remote web driver to connect to Selenium Grid Hub's Chrome web driver.
     * 
     * @param cemHostName                     CEM hostname
     * @param cemPort                         CEM port
     * @param cemUserName                     CEM user name
     * @param cemPassword                     CEM user password
     * @param seleniumServerHostAndPort       Selenium Grid Hub's URL in form of protocol://host:port
     * @return                                ready to use TessUI object
     * @throws MalformedURLException          Selenium Grid Hub's URL in a bad format
     */
    public static TessUI createTessUIForChromeRemoteWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword, 
    		String seleniumServerHostAndPort) throws MalformedURLException {
    	return createTessUIForRemoteWebDriver(cemHostName, cemPort, cemUserName, cemPassword, 
    			seleniumServerHostAndPort, DesiredCapabilities.chrome());
    }    

    /**
     * Creates a TessUI that will use a remote web driver to connect to a Selenium Grid Hub's web driver specified by 
     * its <code>capabilities</code>. Use this method when you need more control on the web driver settings.
     * 
     * @param cemHostName                     CEM hostname
     * @param cemPort                         CEM port
     * @param cemUserName                     CEM user name
     * @param cemPassword                     CEM user password
     * @param seleniumServerHostAndPort       Selenium Grid Hub's URL in form of protocol://host:port
     * @param capabilities                    driver capabilities
     * @return                                ready to use TessUI object
     * @throws MalformedURLException          Selenium Grid Hub's URL in a bad format
     */

    public static TessUI createTessUIForRemoteWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword, 
    		String seleniumServerHostAndPort, DesiredCapabilities capabilities) throws MalformedURLException {
    	WebDriver driver = new RemoteWebDriver(new URL(seleniumServerHostAndPort), capabilities);
    	return new TessUI(cemHostName, cemPort, cemUserName, cemPassword, driver);
    }

    /**
     * Creates a TessUI that will use a local Firefox web driver. 
     * 
     * @param cemHostName    CEM hostname
     * @param cemPort        CEM port
     * @param cemUserName    CEM user name
     * @param cemPassword    CEM user password
     * @return               ready to use TessUI object
     */
    public static TessUI createTessUIForFirefoxWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword) {
        DesiredCapabilities cap = DesiredCapabilities.firefox();
        cap.setCapability("marionette", false);
    	return new TessUI(cemHostName, cemPort, cemUserName, cemPassword, new FirefoxDriver(cap));
    }

    /**
     * Creates a TessUI that will use a local Chrome web driver. 
     * 
     * @param cemHostName    CEM hostname
     * @param cemPort        CEM port
     * @param cemUserName    CEM user name
     * @param cemPassword    CEM user password
     * @return               ready to use TessUI object
     */
    public static TessUI createTessUIForChromeWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword) {
    	return new TessUI(cemHostName, cemPort, cemUserName, cemPassword, new ChromeDriver());
    }

    /**
     * Creates a TessUI that will use a local IE web driver. 
     * 
     * @param cemHostName    CEM hostname
     * @param cemPort        CEM port
     * @param cemUserName    CEM user name
     * @param cemPassword    CEM user password
     * @return               ready to use TessUI object
     */
    public static TessUI createTessUIForIEWebDriver(String cemHostName, int cemPort, String cemUserName, String cemPassword) {
    	return new TessUI(cemHostName, cemPort, cemUserName, cemPassword, new InternetExplorerDriver());
    }
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#close()
     */
    @Override
    public void close() {
        try {
            super.close();
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to close Selenium driver. Exception: {0}");
        }
    }
    
    
    private void sendKeys(WebElement element, String keys) {
        element.sendKeys(keys);
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#login()
     */
    @Override
    public void login() {
        getUrl("http://" + hostname + ":" + port + "/wily/cem/tess/app/login.html");
        runLogin();
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#listReports()
     */
    public List<TessReportConfiguration> listReports() {
        List<TessReportConfiguration> list = new ArrayList<>();
        clickWithDelay(driver.findElement(ByIdOrName.name("cem")));
        WebElement link = null;
        
        try {
            link = driver.findElement(ByLinkText.linkText("My Reports"));
        } catch (Exception e) {
            log.info("Cannot find \"CEM -> My Reports\": " + e.getMessage());
        }
        if (link == null) {
            return list;
        }
        clickWithDelay(link);
        
        
        List<WebElement> rows = null;
        
        try {
            rows = driver.findElements(By.xpath("//table[@id='reportDef']//tr"));
        } catch (Exception e) {
            log.info("Cannot find table with reports: " + e.getMessage());
        }
        if (rows == null || rows.size() == 0) {
            return list;
        }
        
        for (WebElement row: rows) {
            List<WebElement> tds = row.findElements(By.tagName("td"));
            if (tds.size() >= 8) {
                list.add(extractReportConfig(tds));
            }
        }
        
        //Now, if we have Monthly or Weekly schedules we need to go into Edit mode 
        //for each of them to grab either correct dayOfMonth or dayOfWeek respectively.
        for (TessReportConfiguration cfg : list) {
        	if (cfg.schedule != null) {
        		switch (cfg.schedule.period) {
        		case Weekly:
        			cfg.schedule.dayOfWeek = getDayOfWeek(cfg);
        			break;
        		case Monthly:
        			cfg.schedule.dayOfMonth = getDayOfMonth(cfg);
        			break;
        		case Daily:
        		default:
        				break;
        		}
        	}
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#deleteAllReports()
     */
    @Override
    public void deleteAllReports() {
        List<TessReportConfiguration> reports = listReports();
        if (reports.size() == 0) {
            log.info("No reports configured - nothing to delete");
        }
        
        // select all of the reports
        for (TessReportConfiguration cfg: reports) {
            clickWithDelay(cfg.clickBox);
        }
        
        // and click delete
        WebElement deleteButton = driver.findElement(By.name("delete"));
        clickWithDelay(deleteButton);
        
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#configureReport(com.ca.tas.flow.tess.TessReportConfiguration)
     */
    @Override
    public final void configureReport(TessReportConfiguration report) {
        // getting the list will navigate to the reports page as a side effect, which we want
        listReports();
        
        WebElement newButton = driver.findElement(By.name("new"));
        clickWithDelay(newButton);
        
        report.configureOnCEM(this, driver);
        report.scheduleOnCEM(this, driver);
        
        clickWithDelay(driver.findElement(By.id("_finish")));
    }
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#enableTim(java.lang.String)
     */
    @Override
    public void enableTim(String tim) throws IOException {
        InetAddress addr = InetAddress.getByName(tim);
        String ipAddr = addr.getHostAddress();
        
        List<TimConfig> list = readTimConfigs();
        for (TimConfig cfg: list) {
            if (ipAddr.equals(cfg.ipAddress)) {
                if (cfg.enabled) {
                    log.info("TIM " + tim + " already enabled.");
                    return;
                }
                cfg.checkbox.click();
                
                WebElement enableButton = driver.findElement(By.name("enable"));
                clickWithDelay(enableButton);
                
                try {
                	/* 
                	 * After fresh install of EM+CEM there is a pop-up at this point.
                	 * After accepting it once it never appears again.
                	 */
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                } catch (NoAlertPresentException nape) {
                	//
                }
                
                try {
                    if (checkSuccessMessage("Successfully enabled the selected monitor(s).")) {
                        log.info("TIM " + tim + " enabled");
                    } else {
                        log.warn("TIM " + tim + " was NOT enabled!");
                    }
                } catch (Exception e) {
                    log.warn("An exception occured while checking if TIM " + tim + " was enabled", e);
                }
                return;
            }
        }
    }
    
    private boolean checkSuccessMessage(String tgt) {
        WebElement messagesDiv = driver.findElement(By.xpath("//div[@id='caMessagesDiv']"));
        List<WebElement> spans = messagesDiv.findElements(By.tagName("span"));
        for (WebElement span: spans) {
        	String spanTxt = span.getText();
            if (spanTxt != null && (tgt.endsWith(spanTxt) || spanTxt.contains(tgt))) {
                return true;
            }
        }
        
        return false;
    }
    
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#configureNewTim(java.lang.String, boolean)
     */
    @Override
    public void configureNewTim(String name, boolean enableMTP) throws IOException {
        InetAddress addr = InetAddress.getByName(name);
        String ipAddress = addr.getHostAddress();
        name = addr.getCanonicalHostName();
        
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(ByLinkText.linkText("Monitors"));
        if (link == null) {
            log.warn("Unable to find Monitors tab in setup page");
            return;
        }
        link.click();
        
        WebElement newButton = driver.findElement(By.name("new"));
        clickWithDelay(newButton);
       
        WebElement nameInput = driver.findElement(By.id("name"));
        WebElement ipAddressInput = driver.findElement(By.id("ipAddressAsString"));
        WebElement checkbox = driver.findElement(By.name("MTPEnabled"));
        
        sendKeys(nameInput, name);
        sendKeys(ipAddressInput, ipAddress);
        if (enableMTP) {
            checkbox.click();
        }
        
        WebElement saveButton = driver.findElement(By.name("_finish"));
        clickWithDelay(saveButton);
    }
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#readTessServiceConfigs()
     */
    @Override
    public List<TessServiceConfig> readTessServiceConfigs() {
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(ByLinkText.linkText("Services"));
        if (link == null) {
            return Collections.emptyList();
        }
        clickWithDelay(link);
        
        List<TessServiceConfig> retval = new ArrayList<>();
        List<WebElement> list = driver.findElements(By.xpath("//table[@id='servicesDef']//tr"));
        for (WebElement we: list) {
            List<WebElement> tds = we.findElements(By.tagName("td"));
            if (tds.size() == 2) {
                TessServiceConfig cfg = extractTessServiceConfig(tds);
                retval.add(cfg);
            }
        }
        
        return retval;
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#configureTessService(com.ca.tas.flow.tess.ConfigureTessFlowContext.TessService, java.lang.String)
     */
    @Override
    public void configureTessService(TessService svc, String emHost) throws IOException {
        InetAddress addr = InetAddress.getByName(emHost);
        emHost = addr.getCanonicalHostName();
        
        List<TessServiceConfig> list = readTessServiceConfigs();
        for (TessServiceConfig cfg: list) {
            if (cfg.service.equals(svc)) {
                if (cfg.em.equals(emHost)) {
                    log.info("Service " + svc + " alread configured on " + emHost);
                } else {
                    configureTessService(cfg, emHost);
                }
            }
        }
    }
    
    
    /**
     * Internal method that actually clicks on the UI using selenium to configure the service
     * @param cfg
     * @param emHost
     */
    private void configureTessService(TessServiceConfig cfg, String emHost) {
        log.info("Configuring TESS service " + cfg.service + " on collector " + emHost);
        clickWithDelay(cfg.link);

        TessServiceDetailConfig detailConfig = null;
        
        List<WebElement> list = driver.findElements(By.xpath("//table[@id='entity']//tr"));
        for (WebElement we: list) {
            List<WebElement> tds = we.findElements(By.tagName("td"));
            if (tds.size() == 6) {
                detailConfig = extractTessServiceDetailConfig(tds);
                if (detailConfig.em.equals(emHost)) {
                    break;
                }
                detailConfig = null;
            }
        }
        
        if (detailConfig != null) {
            detailConfig.checkbox.click();
            WebElement saveButton = driver.findElement(By.name("save"));
            clickWithDelay(saveButton);
            // and clear the popup alert dialog
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } else {
            WebElement cancelButton = driver.findElement(By.name("cancel"));
            clickWithDelay(cancelButton);
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#configureWebServerFilter(com.ca.tas.flow.tess.TessUI.WebServerFilterConfig)
     */
    @Override
    public void configureWebServerFilter(WebServerFilterConfig cfg) {
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(ByLinkText.linkText("Web Server Filters"));
        if (link == null) {
            log.warn("Unable to find the Web Server Filters link");
            return;
        }
        clickWithDelay(link);

        WebElement newButton = driver.findElement(By.name("new"));
        clickWithDelay(newButton);

        WebElement nameInput = driver.findElement(By.name("name"));
        WebElement timSelect = driver.findElement(By.id("monitor"));
        WebElement fromIpInput = driver.findElement(By.id("fromIpAddressAsString"));
        WebElement toIpInput = driver.findElement(By.id("toIpAddressAsString"));
        WebElement portInput = driver.findElement(By.id("port"));

        List<WebElement> options = timSelect.findElements(By.tagName("option"));
        boolean timSelected = false;
        for (WebElement option: options) {
            String timName = option.getText();
            if (cfg.timName.equals(timName)) {
                option.click();
                timSelected = true;
                log.debug("Found requested TIM " + cfg.timName + " in monitor drop down list");
                break;
            }
        }
        if (!timSelected) {
            log.warn("Did not find specified TIM (" + cfg.timName + ") in monitor drop down list.");
        }

        nameInput.clear();
        sendKeys(nameInput, cfg.serviceName);
        fromIpInput.clear();
        sendKeys(fromIpInput, cfg.fromIpAddress);
        toIpInput.clear();
        sendKeys(toIpInput, cfg.toIpAddress);
        portInput.clear();
        sendKeys(portInput, Integer.toString(cfg.port));

        WebElement saveButton = driver.findElement(By.name("_finish"));
        clickWithDelay(saveButton);
    }

    /**
     * Configures RTTM configuration on the TESS
     */
    @Override
    public void configureRttm() {
        rttmGlobalConfiguration();

        for (int i = 1; i <= 9; ++i) {
            rttmCreateUserGroup(i);
        }
    }

    private void rttmGlobalConfiguration() {
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(By.linkText("RTTM Configuration"));
        clickWithDelay(link);

        WebElement enableUserGroupCheckbox = driver.findElement(By.name("enableUserGroup"));
        if (! enableUserGroupCheckbox.isSelected()) {
            clickWithDelay(enableUserGroupCheckbox);
        }

        WebElement maxStatsPerTim = driver.findElement(By.name("maxStatsPerTim"));
        maxStatsPerTim.clear();
        maxStatsPerTim.click();
        maxStatsPerTim.sendKeys("600");

        WebElement enableGeoLocation = driver.findElement(By.name("enableGeoLocation"));
        if (enableGeoLocation.isSelected()) {
            clickWithDelay(enableGeoLocation);
        }

        WebElement saveButton = driver.findElement(By.name("save"));
        clickWithDelay(saveButton);

        WebElement messageDiv = waitFor(
            ExpectedConditions.visibilityOfElementLocated(By.id("caMessagesDiv")));
        messageDiv.findElement(
            By.xpath(".//span[.='Successfully saved RTTM global configuration']"));
    }

    private void rttmCreateUserGroup(int num) {
        log.info("Creating RTTM user group {}", num);
        WebElement newGroupButton = driver.findElement(By.name("new"));
        clickWithDelay(newGroupButton);

        WebElement matcherComboBox = waitFor(
            ExpectedConditions.visibilityOfElementLocated(By.name("matcherType")));
        ISelect matcherSelect = new Select(matcherComboBox);
        matcherSelect.selectByVisibleText("User Group");

        WebElement aliasInput = driver.findElement(By.name("alias"));
        aliasInput.clear();
        aliasInput.click();
        aliasInput.sendKeys("Group_" + num);

        WebElement patternInput = driver.findElement(By.name("pattern"));
        patternInput.click();
        patternInput.click();
        patternInput.sendKeys("created-usergroup-[0-9^-]+-" + num + ".*");

        WebElement typeComboBox = driver.findElement(By.name("type"));
        ISelect typeSelect = new Select(typeComboBox);
        typeSelect.selectByVisibleText("Regular Expression Match");

        WebElement finishButton = driver.findElement(By.name("_finish"));
        clickWithDelay(finishButton);
        waitFor(ExpectedConditions.visibilityOfElementLocated(By.name("new")));
    }

    /* (non-Javadoc)
         * @see com.ca.tas.flow.tess.TessConfigurer#readWebServerFilterConfigs()
         */
    @Override
    public List<WebServerFilterConfig> readWebServerFilterConfigs() {
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(ByLinkText.linkText("Web Server Filters"));
        if (link == null) {
            return Collections.emptyList();
        }
        clickWithDelay(link);
        
        ArrayList<WebServerFilterConfig> retval = new ArrayList<>();
        
        List<WebElement> list = driver.findElements(By.xpath("//table[@id='webserverfilter']//tr"));
        for (WebElement we: list) {
            List<WebElement> tds = we.findElements(By.tagName("td"));
            if (tds.size() == 4) {
                WebServerFilterConfig cfg = extractWebServiceConfig(tds);
                retval.add(cfg);
            }
        }
        
        return retval;
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#deleteAllTims()
     */
    @Override
    public void deleteAllTims() {
        List<TimConfig> list = readTimConfigs();
        if (list.size() == 0) {
            return;
        }
        // first disable any that are enabled
        for (TimConfig cfg: list) {
            if (cfg.enabled) {
                disableTim(cfg);
            }
        }
        
        // and now delete them one by one
        int size = list.size() + 1;
        while ((list = readTimConfigs()).size() > 0) {
            if (size != list.size() + 1) {
                throw new IllegalStateException("While deleting TIMs, expected " + (size - 1) + 
                    " TIMs, found " + list.size());
            }
            TimConfig cfg = list.get(0);
            deleteTim(cfg);
            size = list.size();
        }
    }

    
    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#readTimConfigs()
     */
    @Override
    public List<TimConfig> readTimConfigs() {
        driver.findElement(ByIdOrName.name("setupMenu")).click();
        WebElement link = driver.findElement(ByLinkText.linkText("Monitors"));
        if (link == null) {
            return Collections.emptyList();
        }
        clickWithDelay(link);
        
        ArrayList<TimConfig> retval = new ArrayList<>();
        List<WebElement> list = driver.findElements(By.xpath("//table[@id='monitor']//tr"));
        for (WebElement we: list) {
            List<WebElement> tds = we.findElements(By.tagName("td"));
            if (tds.size() == 8) {
                TimConfig cfg = extractTimConfig(tds);
                retval.add(cfg);
            }
        }
        return retval;
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.flow.tess.TessConfigurer#deleteAllWebServerFilters()
     */
    @Override
    public void deleteAllWebServerFilters() {
        List<WebServerFilterConfig> list = readWebServerFilterConfigs();
        if (list.size() == 0) {
            return;
        }
        for (WebServerFilterConfig cfg: list) {
            cfg.checkbox.click();
        }
        
        WebElement deleteButton = driver.findElement(By.name("delete"));
        clickWithDelay(deleteButton);
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

	@Override
	public String startRecordingSession(String clientIP, RecordType type) {
		String url = String.format(RECORDING_SESSION_URL_TEMPLATE, hostname, port);
		
		getUrl(url);
		String windowId = driver.getWindowHandle();
		
		runLogin();
		
		log.info("Creating new recording session, clicking on '{}'", 
				CREATE_NEW_RECORDING_SESSION_ELEMENT_NAME);
		
        By newLink = By.name(CREATE_NEW_RECORDING_SESSION_ELEMENT_NAME);
        WebElement element = driver.findElement(newLink);
        clickWithDelay(element);
        
    	/* 
    	 * Provide Agent or TIM record type.  
    	 */
        By selectedMonitorTypeLink = By.id(type.getHtmlRecordTypeRadioButtonId());
        try {
        	element = driver.findElement(selectedMonitorTypeLink);	
            log.info("Selecting '{}' record type by clicking on '{}'", type.name(), 
            		type.getHtmlRecordTypeRadioButtonId());
        	clickWithDelay(element);
        } catch (NoSuchElementException e) {
        	//Just check we're going to monitor the right thing
        	log.info("No record type selecting options found. Most probably there is only one option available. Checking it matches the asked one..");
        	WebElement monitorTypeReadOnlyElement = driver.findElement(By.xpath(READ_ONLY_RECORD_TYPE_TABLE_PATH));
        	if (monitorTypeReadOnlyElement == null) {
        		String msg = "No read-only record type found!";
        		log.error(msg);
        		throw new RuntimeException(msg);
        	}

        	String recordType = monitorTypeReadOnlyElement.getText();
        	if (recordType == null) {
        		String msg = "Failed to determine record type!"; 
        		log.error(msg);
        		throw new IllegalStateException(msg);
        	}
        	
        	log.info("Found the only record type: {}", recordType);
        	
        	String expectType = type.name();
        	if (!recordType.toLowerCase().contains(expectType.toLowerCase())) {
        		String msg = String.format("Expected record type '%s', found '%s'", 
        				expectType, recordType);
        		log.error(msg);
        		throw new IllegalStateException(msg);
        	}
        }
        
        if (clientIP != null) {
        	log.info("Client IP provided. Searching for corresponding input field by id '{}'", 
        			CLIENT_IP_ADDRESS_ELEMENT_ID);
            By clientIpAddressStringElem = By.id(CLIENT_IP_ADDRESS_ELEMENT_ID);
            element = driver.findElement(clientIpAddressStringElem);
            log.info("Setting Client IP to '{}'", clientIP);
            element.clear();
            element.sendKeys(clientIP);
        }

        log.info("Finishing new recording configuration, clicking on '{}'", 
        		FINISH_SETTING_NEW_RECORDING_SESSION_ELEMENT_NAME);
        By finishLink = By.name(FINISH_SETTING_NEW_RECORDING_SESSION_ELEMENT_NAME);
        element = driver.findElement(finishLink);
        clickWithDelay(element);
        return windowId;
	}

	@Override
	public void stopRecordingSession(String windowId) {
		if (windowId != null) {
			driver.switchTo().window(windowId);	
		}

		log.info("Stopping recording session by clicking on '{}'", 
				STOP_RECORDING_SESSION_ELEMENT_NAME);
		By stopLink = By.name(STOP_RECORDING_SESSION_ELEMENT_NAME);
        WebElement element = driver.findElement(stopLink);
        clickWithDelay(element);

        log.info("Closing recording session by clicking on '{}'", 
        		CLOSE_RECORDING_SESSION_ELEMENT_NAME);
		By doneLink = By.name(CLOSE_RECORDING_SESSION_ELEMENT_NAME);
        element = driver.findElement(doneLink);
        clickWithDelay(element);
	}

	@Override
	public String getSMTPHost() {
		String url = String.format(EMAIL_SETTINGS_URL_TEMPLATE, this.hostname, port);
		getUrl(url);
		
		runLogin();
		
        By smtpHostnameElem = By.id(SMTP_SERVER_HOSTNAME_ID);
        WebElement element = driver.findElement(smtpHostnameElem);
        return element.getAttribute("value");
	}

	@Override
	public void configureSMTP(String smtpHostname) {
		String url = String.format(EMAIL_SETTINGS_URL_TEMPLATE, this.hostname, port);
		getUrl(url);
		
		runLogin();
		
        By smtpHostnameElem = By.id(SMTP_SERVER_HOSTNAME_ID);
        WebElement element = driver.findElement(smtpHostnameElem);
        String existingSMTPHost = element.getAttribute("value");
        if (existingSMTPHost != null) {
        	log.info("Found existing SMTP host setting: {}", existingSMTPHost);
        	if (existingSMTPHost.equals(smtpHostname)) {
        		log.info("Old and new SMTP host values are the same, exiting.");
        		return;
        	}
        }

        log.info("Setting SMTP hostname to '{}'", smtpHostname);
        element.clear();
        element.sendKeys(smtpHostname);

		log.info("Testing connection by clicking on '{}'", 
				TEST_CONNECTION_ELEMENT_NAME);
		By testConnectionLink = By.name(TEST_CONNECTION_ELEMENT_NAME);
        element = driver.findElement(testConnectionLink);
        clickWithDelay(element);
        
        if (checkSuccessMessage(EMAIL_SETTINGS_TEST_SUCCESS_MESSAGE)) {
        	log.info("Successfully tested new email settings.");
        } else {
        	log.warn("Email settings test failed.");
        }
        
        WebElement saveButton = driver.findElement(By.name("save"));
        clickWithDelay(saveButton);
        
        if (checkSuccessMessage(EMAIL_SETTINGS_SAVED_SUCCESSFULLY_MESSAGE)) {
        	log.info("Successfully saved new email settings.");
        } else {
        	log.warn("Failed to save email settings.");
        }
	}
	
	private void runLogin() {
		WebElement usernameInput = null;

		try {
			usernameInput = driver.findElement(ById.id(USER_NAME_INPUT_ELEMENT_ID));
		} catch (NoSuchElementException e) {
			//already logged in, ignore
			log.info("Seems we are already logged in CEM");
			log.debug("No username input field found!", e);
			return;
		}
		
		log.info("Logging in CEM..");
		WebElement passwordInput = driver.findElement(ById.id(PASSWORD_INPUT_ELEMENT_ID));
		WebElement submit = driver.findElement(ById.id(SUBMIT_LOGIN_BUTTON_ID));
        
        sendKeys(usernameInput, username);
        sendKeys(passwordInput, password);
        submit(submit);
	}

    private WebServerFilterConfig extractWebServiceConfig(List<WebElement> tds) {
        WebServerFilterConfig cfg = new WebServerFilterConfig();

        cfg.checkbox = tds.get(0).findElement(By.tagName("input"));
        cfg.link = tds.get(1).findElement(By.tagName("a"));
        cfg.timName = tds.get(2).getText();
        String ipRange = tds.get(3).getText();
        parseTimIpAddr(ipRange, cfg);
        return cfg;
    }
    
    private TimConfig extractTimConfig(List<WebElement> tds) {
        TimConfig cfg = new TimConfig();
        
        cfg.checkbox = tds.get(0).findElement(By.tagName("input"));
        cfg.link = tds.get(1).findElement(By.tagName("a"));
        cfg.name = cfg.link.getText();
        cfg.ipAddress = tds.get(6).findElement(By.tagName("a")).getText();
        cfg.timLink = tds.get(6).findElement(By.tagName("a"));
        cfg.enabled = tds.get(2).getText().equals("Enabled");
        
        return cfg;
    }

    private TessReportConfiguration extractReportConfig(List<WebElement> tds) {
        TessReportConfiguration cfg = TessReportConfiguration.newConfig();
        cfg.clickBox = tds.get(0).findElement(By.tagName("input"));
        cfg.name = tds.get(1).getText();
        WebElement elem = tds.get(7).findElement(By.tagName("a"));
        cfg.editLinkHref = elem.getAttribute("href");
        cfg.schedule = extractReportSchedule(tds, cfg);
        return cfg;
    }
    
    private TessReportSchedule extractReportSchedule(List<WebElement> tds, TessReportConfiguration cfg) {
    	String frequency = tds.get(4).getText();
    	String time = tds.get(5).getText();
    	String recipients = tds.get(6).getText();
    	String[] hourMinutes = time.split(":");
    	int hour = Integer.parseInt(hourMinutes[0]);
    	int minute = Integer.parseInt(hourMinutes[1]);
    	
    	TessReportSchedule schedule = null;
    	switch (frequency) {
    	case "Daily":
    		schedule = TessReportSchedule.daily(hour, minute);
    		break;
    	case "Weekly":
    		//set it to unknown for now
    		schedule = TessReportSchedule.weekly(DayOfWeek.Unknown, hour, minute);
    		break;
    	case "Monthly":
    		//set it to 1 for now
    		schedule = TessReportSchedule.monthly(1, hour, minute);
    		break;
    	default: 
    		throw new IllegalStateException("Unexpected report schedule found!");
    	}
    	schedule.toAddress = recipients;
    	return schedule;
    }
    
    private DayOfWeek getDayOfWeek(TessReportConfiguration cfg) {
    	getUrl(cfg.editLinkHref);
    	
    	Select dayOfWeekSelect = new Select(driver.findElement(By.name("dayOfWeek")));
    	String dayOfWeekStr = dayOfWeekSelect.getFirstSelectedOption().getText();
    	DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr);
    	
    	delay();
    	driver.navigate().back();
    	delay();
    	return dayOfWeek;
    }
    
    private int getDayOfMonth(TessReportConfiguration cfg) {
    	getUrl(cfg.editLinkHref);
    	
    	Select dayOfMonthSelect = new Select(driver.findElement(By.name("dayOfMonth")));
    	String dayOfMonthStr = dayOfMonthSelect.getFirstSelectedOption().getText();
    	int dayOfMonth = Integer.parseInt(dayOfMonthStr);
    	
    	delay();
    	driver.navigate().back();
    	delay();
    	return dayOfMonth;
    }
    
    private TessServiceConfig extractTessServiceConfig(List<WebElement> tds) {
        TessServiceConfig cfg = new TessServiceConfig();
        
        cfg.link = tds.get(0).findElement(By.tagName("a"));
        cfg.service = TessService.forServiceName(cfg.link.getText());
        cfg.em = tds.get(1).getText();
        
        return cfg;
    }
    
    private TessServiceDetailConfig extractTessServiceDetailConfig(List<WebElement> tds) {
        TessServiceDetailConfig cfg = new TessServiceDetailConfig();
        cfg.checkbox = tds.get(0).findElement(By.tagName("input"));
        cfg.em = tds.get(1).getText();
        cfg.ipAddress = tds.get(2).getText();
        cfg.emType = tds.get(3).getText();
        cfg.running = "Up".equals(tds.get(4).getText());
        cfg.description = tds.get(5).getText();
        
        return cfg;
    }
    
    private void disableTim(TimConfig cfg) {
        cfg.checkbox.click();
        
        WebElement deleteButton = driver.findElement(By.name("disable"));
        clickWithDelay(deleteButton);
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }
    
    private void deleteTim(TimConfig cfg) {
        cfg.link.click();
        
        WebElement deleteButton = driver.findElement(By.name("delete"));
        clickWithDelay(deleteButton);
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }



    private void parseTimIpAddr(String ipRange, WebServerFilterConfig cfg) {
        // expect "130.119.68.106 - 130.119.68.106:9090"
        Pattern p = Pattern.compile("((([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])) \\- ((([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5]))\\:(\\d+)");
        Matcher m = p.matcher(ipRange);
        if (m.matches()) {
            cfg.fromIpAddress = m.group(1);
            cfg.toIpAddress = m.group(5);
            cfg.port = Integer.parseInt(m.group(9));
        } else {
            throw new IllegalArgumentException("Could not parse txt for web service filter ip addresses and port");
        }
    }


    public static class WebServerFilterConfig {
        public WebElement checkbox;
        public WebElement link;
        public String serviceName;
        public String timName;
        public String fromIpAddress;
        public String toIpAddress;
        public int port;
        
        @Override
        public String toString() {
            return "WebServiceConfig[" + serviceName + "," + timName + "," + fromIpAddress + "," + toIpAddress + "," + port + "]";
        }
    }
    
    
    public static class TimConfig {
        public boolean enabled;
        public WebElement timLink;
        public WebElement checkbox;
        public WebElement link;
        public String name;
        public String ipAddress;
        
        @Override
        public String toString() {
            return "TimConfig[" + name + "," + ipAddress + "]";
        }
    }

    public static class  TessServiceConfig {
        public WebElement link;
        public TessService service;
        public String em;
        
        @Override
        public String toString() {
            return "TessServiceConfig[" + service + "," + em + "]";
        }
    }
    
    public static class TessServiceDetailConfig {
        public WebElement checkbox;
        public String em;
        public String ipAddress;
        public String emType;
        public boolean running;
        public String description;
        
        @Override
        public String toString() {
            return "TessServiceDetailConfig[" + em + "," + ipAddress + "," + emType + "," + 
                running + "," + description + "]";
        }
    }
    
    public static void main(String[] args) throws Exception {
        TessConfigurer tessUI = TessUI.createTessUIForFirefoxWebDriver("fldmom01c.ca.com", 8081, "cemadmin", "quality");
        try {
            tessUI.login();
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
            }
            tessUI.configureReport(new ImpactLeadersReport("Impact Leaders Report", "Schedule Impact Leaders", TimeFrame.Yesterday));
        } finally {
            Thread.sleep(15000L);
            tessUI.close();
        }
    }

    public static void testListReports() throws Exception {
        TessConfigurer tessUI = TessUI.createTessUIForChromeRemoteWebDriver("tas-czfld-n4c.ca.com", 8081, 
        		"cemadmin", "quality", "http://localhost:9515");
        try {
            tessUI.login();
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
            	e.printStackTrace();
            }
            List<TessReportConfiguration> reportConfigs = tessUI.listReports();
            System.out.println(reportConfigs);
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            Thread.sleep(15000L);
            tessUI.close();
        }
    	
    }
    
    public static void testConfigureReport() throws Exception {
        TessUI tessUI = TessUI.createTessUIForFirefoxWebDriver("tas-czfld-n8.ca.com", 8081, "cemadmin", "quality");
        try {
        	tessUI.setDelay(1500L);
            tessUI.login();
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
            }
            
            tessUI.configureReport(initCEMReport(new ImpactLeadersReport("Impact Leaders Report", "Schedule Impact Leaders", TimeFrame.Yesterday)));
            tessUI.configureReport(initCEMReport(new CorrelationalSlaReport("SLA Report", "Schedule Correlational SLA Report", TimeFrame.Yesterday)));
            tessUI.configureReport(initCEMReport(new StatsDataCSVReport("Stats Data", "Stats Data Report")));
            tessUI.configureReport(initCEMReport(new TransactionCountReport("Transaction Count", "Transaction Count Report", TimeFrame.Yesterday)));
            tessUI.configureReport(initCEMReport(new TransactionDefectReport("Transaction Defect", "Transaction Defect Report", TimeFrame.Yesterday)));
            tessUI.configureReport(initCEMReport(new TransactionPerformanceReport("Transaction Performance", "Transaction Performance Report", TimeFrame.Yesterday)));
        } finally {
        	System.out.println("Finishing....");
            Thread.sleep(60000L);
            tessUI.close();
        }
    }

    public static void testSMTPHostSetting() throws Exception {
        TessUI tessUI = TessUI.createTessUIForFirefoxWebDriver("tas-czfld-n8.ca.com", 8081, "cemadmin", "quality");
        try {
        	tessUI.setDelay(1500L);
        	tessUI.login();
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
            	e.printStackTrace();
            }
            tessUI.configureSMTP("mail.ca.com");
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            Thread.sleep(15000L);
            tessUI.close();
        }
    }
    
    public static void testAgentRecordingSession() throws Exception {
    	testRecordingSession("tas-cz-fld-nae", 8081, "cemadmin", "quality", 
    			RecordType.Agent);
    }

    public static void testTIMRecordingSession() throws Exception {
    	testRecordingSession("tas-cz-fld-nae", 8081, "cemadmin", "quality", 
    			RecordType.TIM);
    }

    public static void testRecordingSession(String host, int port, String login, String password, RecordType type) throws Exception {
    	TessUI tessUI = null;
    	try {
    		tessUI = TessUI.createTessUIForFirefoxWebDriver("tas-cz-fld-nf1", 8081, "cemadmin", "quality");
    		tessUI.setDelay(1500L);
    		tessUI.login();
    		List<TessReportConfiguration> list = tessUI.listReports();
    		for (TessReportConfiguration cfg: list) {
    		    System.out.println(cfg);
    		}
    		tessUI.deleteAllReports();
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
            }
            tessUI.configureReport(new ImpactLeadersReport("Impact Leaders Report", "Schedule Impact Leaders", TimeFrame.Yesterday));
    	} finally {
    		if (tessUI != null) {
        		tessUI.close();
    		}
    	}
    }
    
    private static TessReportConfiguration initCEMReport(TessReportConfiguration cfg) {
    	cfg.schedule.fromAddress = TessReportSchedule.DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS;
    	cfg.schedule.toAddress = "babayaga@ca.com";
    	return cfg;
    }

    /**
     * Wait until WebElement is present given selector
     *
     * @param expectedCondition expected condition
     * @param waitTime          wait time amount
     * @param unit              wait time amount unit
     * @return located WebElement
     */
    public <ConditionResultT> ConditionResultT waitFor(
        ExpectedCondition<ConditionResultT> expectedCondition, long waitTime, TimeUnit unit) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, unit.toSeconds(waitTime));
            log.info("Waiting for [{}]", expectedCondition);
            return wait.until(expectedCondition);
        } catch (Throwable e) {
            log.error("waitFor() Failed: Waiting failed to locate [{}]", expectedCondition, e);
            throw new RuntimeException(e);
        }
    }

    public <ConditionResultT> ConditionResultT waitFor(
        ExpectedCondition<ConditionResultT> expectedCondition) {
        return waitFor(expectedCondition, 1, TimeUnit.MINUTES);
    }
}
