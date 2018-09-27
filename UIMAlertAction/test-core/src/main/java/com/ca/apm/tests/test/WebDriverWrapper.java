package com.ca.apm.tests.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverWrapper extends ElementsIdentification {

    static final Logger LOGGER = LoggerFactory.getLogger(WebDriverWrapper.class);
    public FirefoxDriver fd = null;

    public FirefoxDriver fd1 = null;

    public WebDriverWait wait = null;

    public WebElement we = null;

    public Actions action = null;

    public Alert alert = null;

    public Select select = null;

    public void hitTransaction(String url) {
        
        fd1 = new FirefoxDriver();
        fd1.get(url);
        fd1.close();
        fd1.quit();
    }

    public void initBrowser() {
        fd = new FirefoxDriver();
        fd.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
        fd.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);
    }

    public void waitImplicit() {
        fd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    }

    public WebElement waitExplicitPresenceOfElement(String element) {
        wait = new WebDriverWait(fd, 120);
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(element)));
    }

    public WebElement waitExplicitPresenceOfElementByXPath(String element) {
        wait = new WebDriverWait(fd, 100);
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(element)));
    }

    public List<WebElement> waitExplicitPreseneceOfElements(String element) {
        wait = new WebDriverWait(fd, 60);
        return wait
            .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(element)));
    }

    public void rightClick() {
        action = new Actions(fd).contextClick();
        action.build().perform();
    }
    
    public void doubleClick(WebElement element){
        action = new Actions(fd);
        boolean flag = element.isDisplayed();
        if(flag){
            action.doubleClick(element).build().perform();
        }else{
           LOGGER.info("Element is not visible/displayed :" + element);
        }
      
    }

    public void mouseHover(WebElement element) {
        action = new Actions(fd).moveToElement(element);
        action.build().perform();
    }

    public void pressTab(WebElement identifier) {

        action = new Actions(fd);
        action.sendKeys(identifier, Keys.TAB).build().perform();

    }

    public void switchtoAlertandAcceptIt() {
        alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public String switchToAlertandgetText() {
        alert = wait.until(ExpectedConditions.alertIsPresent());
        return alert.getText();
    }

    public Select selectfromDropDown(WebElement element) {
        select = new Select(element);
        return select;
    }


    public void closeBrowser() {
        ArrayList<String> handleCount = new ArrayList<String>(fd.getWindowHandles());
        for (int i = 0; i < handleCount.size(); i++) {
            fd.switchTo().window(handleCount.get(i));
            fd.close();
        }
        fd.quit();
    }
}
