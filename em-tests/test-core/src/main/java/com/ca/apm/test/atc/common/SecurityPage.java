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

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class SecurityPage {

    UI ui;
    
    public static final String CSS_SELECTOR_GENERATE_NEW_TOKEN = "button.t-generate-token-link";
    
    public SecurityPage(UI ui) {
        this.ui = ui;
    }
    
    public void clickOnGenerateNewToken() {
        ui.getElementProxy(By.cssSelector(CSS_SELECTOR_GENERATE_NEW_TOKEN)).click();
    }
    
    public void fillLabel(String label) {
        PageElement el = ui.getElementProxy(By.cssSelector("input[name='label']"));
        el.sendKeys(label);
    }
    
    public void selectSystemToken() {
        PageElement el = ui.getElementProxy(By.id("generate-key-agc-label"));
        el.click();
    }
    
    public void selectPublicToken() {
        PageElement el = ui.getElementProxy(By.id("generate-key-public-label"));
        el.click();
    }

    public void submitForm() {
        PageElement el = ui.getElementProxy(By.cssSelector("button.t-token-generate-btn"));
        el.click();
    }
    
    public void waitForNextStep() {
        Utils.sleep(100);
        ui.waitWhileVisible(By.cssSelector(".t-generating-token-dialog .settings-wait"));
    }
    
    public boolean isGeneratedTokenPresent() {
        return ui.findElements(By.cssSelector("table.t-generated-token-dialog .t-token")).size() > 0;
    }
    
    public String getGeneratedToken() {
        //WebElement el = driver.findElement(By.xpath("/html/body/div[6]/div/div/div[2]/div/table/tbody/tr[4]/td/h4"));
        PageElement el = ui.getElementProxy(By.cssSelector("table.t-generated-token-dialog .t-token"));
        return el.getText();
    }
    
    public void clickOnClose() {
        PageElement el = ui.getElementProxy(By.id("btn-close"));
        el.click();
    }
    
    
    
}
