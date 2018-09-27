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
package com.ca.apm.test.atc.common;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import com.ca.apm.test.atc.common.element.PageElement;

public class WebView {

    private final UI ui;

    public enum TimeRange {
        LIVE("Live"),
        CUSTOM_RANGE("Custom Range");
        
        private String label;
        
        private TimeRange(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public WebView(UI ui) {
        this.ui = ui;
    }

    public PageElement getHeaderCaLogo() {
        return ui.getElementProxy(By.cssSelector(".apmIvsTitle"));
    }
    
    public PageElement getTimeController() {
        return ui.getElementProxy(By.id("webview-timecontroller-timewindow-combobox-input"));
    }
   
    public String getTimeControllerSelectedValue() {
        return getTimeController().getAttribute("value");
    }
    
    public List<PageElement> getTimeControllerValueElements() {
        // Make sure all combos are collapsed
        getHeaderCaLogo().click();
        Utils.sleep(150);
        
        // Open the Time Window combo
        getTimeController().click();
        Utils.sleep(150);
        
        return ui.findElements(By.xpath("/html/body/div/div/div/div/span[@class='webview-Common-ListItem']"));
    }
    
    public List<String> getTimeControllerValues() {
        List<String> values = new ArrayList<String>();
        for (PageElement el : getTimeControllerValueElements()) {
           values.add(el.getText()); 
        }
        return values;
    }
    
    public PageElement getTimeResolution() {
        return ui.getElementProxy(By.id("webview-timecontroller-resolution-combobox-input"));
    }
    
    public String getTimeResolutionSelectedValue() {
        return getTimeResolution().getAttribute("value");
    }
    
    public List<PageElement> getTimeResolutionValueElements() {
        // Make sure all combos are collapsed
        getHeaderCaLogo().click();
        Utils.sleep(150);
        
        // Open the Time Window combo
        getTimeResolution().click();
        Utils.sleep(150);
        
        return ui.findElements(By.xpath("/html/body/div/div/div/div/span[@class='webview-Common-ListItem']"));
    }
    
    public List<String> getTimeResolutionValues() {
        List<String> values = new ArrayList<String>();
        for (PageElement el : getTimeResolutionValueElements()) {
           values.add(el.getText()); 
        }
        return values;
    }
    
    public String getMetricNameInMetricBrowserUnderGraph(){
        return ui.getElementProxy(By.xpath("//div[@id=\"webViewInvestigatorView-TabPanel\"]//div[@id=\"webview-investigator-linechartlegend-grid\"]/div[2]//table//tr[1]/td[3]//span[1]")).getText();
    }
    
    public void waitForUpdateLegendGrid() {
        ui.waitUntilVisible(By.id("webview-investigator-linechartlegend-grid"));
    }
    
    public PageElement getConsoleTabMenuItem() {
    	return ui.getElementProxy(By.xpath("//div[@id='webview-TabPanel']//a/em/span/span[text()=\"Console\"]"));
    }
    
    public void clickConsoleTabItem() {
    	getConsoleTabMenuItem().click();
    	Utils.sleep(2000);
    }
    
    public List<PageElement> getDashboardLinks() {
    	return ui.waitUntilElementsVisible(By.xpath("//div[contains(@id, 'dashboard-hyperlink-')]//a"));
    }


    public PageElement getLiTab(String title) {
        return ui.getElementProxy(By.xpath("//li[.='" + title + "']"));
    }


    /**
     * 
     */
    public void expandTreeItem(String itemText) {
        PageElement element = ui.getElementProxy(By.xpath("//span[.='" + itemText + "']"));
        Actions action = new Actions(ui.getDriver());
        action.doubleClick(element).perform();
    }

    /**
     * @param string
     * @return
     */
    public int countTableOccurences(String elemText) {
        List<PageElement> elementsByClass = ui.waitUntilElementsVisible(By.className("webview-Common-Column-cell"));
        int count = 0;
        for (PageElement e : elementsByClass) {
            if (e.getText().contains(elemText)) {
                count++;
            }
        }
        return count;
    }

}
