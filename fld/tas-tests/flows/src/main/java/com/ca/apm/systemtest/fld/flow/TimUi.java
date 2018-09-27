package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.systemtest.fld.util.selenium.SeleniumClientBase;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Locale;

/**
 * This is utility class that handles TIM web application UI through Selenium and allows
 * its configuration.
 *
 * @author haiva01
 */
public class TimUi extends SeleniumClientBase {
    public static final String INTERFACES_LINK_XPATH = "//A[starts-with(@HREF,'interfaces')]";
    public static final String IF_CHECKBOX_XPATH_FMT = "//td/input[@name='if' and @value='%s']";
    public static final String TIM_WEBAPP_INDEX_URL = "http://%s:%s@%s:%d/cgi-bin/ca/apm/tim/index";
    public static final String SET_BUTTON_XPATH = "//input[@value='Set']";
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public TimUi(String hostname, int port, String username, String password) {
        super(new FirefoxDriver());
        this.hostname = hostname;
        this.port = port;
        this.username = StringUtils.defaultString(username, "admin");
        this.password = StringUtils.defaultString(password, "quality");
    }

    private static String formatIfXpath(String ifName) {
        return String.format(IF_CHECKBOX_XPATH_FMT, ifName);
    }

    public void visitTimWebApp() {
        String urlStr = String.format(Locale.US, TIM_WEBAPP_INDEX_URL,
            username, password, hostname, port);
        //driver.navigate().to(urlStr);
        //delay();
        getUrl(urlStr);
    }

    public void clickInterfacesLink() {
        clickWithDelay(driver.findElement(By.xpath(INTERFACES_LINK_XPATH)));
    }
    
    
    public void configureProperty(String key, String value) {
        visitTimWebApp();
        String expr = "//A[starts-with(@HREF,'config/index')]";
        clickWithDelay(driver.findElement(By.xpath(expr)));
        expr = "//A[starts-with(@HREF,'editnew')]";
        clickWithDelay(driver.findElement(By.xpath(expr)));
        
        WebElement nameInput = driver.findElement(By.name("name"));
        WebElement valueInput = driver.findElement(By.name("value"));
        nameInput.sendKeys(key);
        valueInput.sendKeys(value);
        
        WebElement submit = driver.findElement(By.xpath("//input[@value='ADD']"));
        clickWithDelay(submit);
    }
    

    public void uncheckIf(String ifName) {
        final String xpath = formatIfXpath(ifName);
        final By selector = By.xpath(xpath);
        if (driver.findElement(selector).isSelected()) {
            driver.findElement(selector).click();
            delay();
        }
    }

    public void checkIf(String ifName) {
        final String xpath = formatIfXpath(ifName);
        final By selector = By.xpath(xpath);
        if (!driver.findElement(selector).isSelected()) {
            driver.findElement(selector).click();
            delay();
        }
    }

    public void clickSet() {
        clickWithDelay(driver.findElement(By.xpath(SET_BUTTON_XPATH)));
    }
}
