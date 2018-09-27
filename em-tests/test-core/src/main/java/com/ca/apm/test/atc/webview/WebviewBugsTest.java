package com.ca.apm.test.atc.webview;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.tas.test.em.transactiontrace.appmap.StandAloneWithNowhereBankBTWindowsTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test of F9270 APM - Time resolution control in WebView (Limited Version).
 * 
 * @author strma15
 */
public class WebviewBugsTest extends UITest {

    private UI ui;
    private String wvLiveUrl;

    private void init() throws Exception {
        ui = getUI();

        ui.login();
    }

    @Tas(testBeds = {@TestBed(name = StandAloneWithNowhereBankBTWindowsTestBed.class, executeOn = StandAloneWithNowhereBankBTWindowsTestBed.MACHINE_ID)}, owner = "surma04", size = SizeType.MEDIUM)
    @Test(groups = {"transactionTrace","Securability","bugs"})
    public void testStoredXSSinTransactionTracerModule() throws Exception{
        init();
        try{
            WebElement wvLink = ui.getTopNavigationPanel().getAnyWebviewLinkElement();

            wvLiveUrl = wvLink.getAttribute("href").replace("home", "tools"); 
            ui.getDriver().navigate().to(wvLiveUrl);
            
            //start TT session
            ui.waitUntilVisible(By.id("webview-tt-start-trace-session-link"));  
            Alert alert = ui.getDriver().switchTo().alert();
            alert.accept();
            
            //start TT session with HTML script in description
            ui.waitUntilVisible(By.id("webview-tt-start-trace-session-link"));  
            ui.getDriver().switchTo().alert();
            //uncheck minimum transaction duration checkbox
            WebElement cBox = ui.getDriver().findElement(By.id("x-auto-58"));
            if(cBox.isSelected()){
                cBox.click();
            }
            //enable USERID checkbox
            cBox = ui.getDriver().findElement(By.id("x-auto-61"));
            if(cBox.isSelected()){
                cBox.click();
            }
            Select dropdown = new Select(ui.getDriver().findElement(By.id("webview-tt-ttStartDialog-ComparisonType-Combobox-input")));
            dropdown.selectByVisibleText("does not equal");
            //input userId description
            WebElement userId = ui.getDriver().findElement(By.id("webview-tt-ttStartDialog-ParameterValue-Field-input"));
            userId.sendKeys("");
            // insert script for XSS

            ui.getDriver().findElement(By.id("webview-tt-ttStartDialog-OK-Button")).click();

            //TT is started. Wait for some time for traces to get generated.
            Utils.sleep(5000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        WebElement switchTrace = ui.getDriver().findElement(By.id("webview-tt-TransactionTrace-SwitchButton"));
        switchTrace.click();
        Assert.assertTrue(ui.getDriver().findElements(By.linkText("")).size()==0);       

    }

    @Tas(testBeds = {@TestBed(name = StandAloneWithNowhereBankBTWindowsTestBed.class, executeOn = StandAloneWithNowhereBankBTWindowsTestBed.MACHINE_ID)}, owner = "surma04", size = SizeType.MEDIUM)
    @Test(groups = {"transactionTrace","full","Securability","bugs"})
    public void testSample() throws Exception{
        
        init();
        Assert.assertTrue(true);
    }
}
