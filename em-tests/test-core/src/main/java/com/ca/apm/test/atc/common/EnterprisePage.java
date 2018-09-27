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

public class EnterprisePage {


    private UI ui;

    public static final String CSS_SELECTOR_CANCEL_REGISTRATION_LINK = "a.t-cancel-registration-link";
    public static final String CSS_SELECTOR_DEREGISTER_LINK = "a.t-deregister-link";
    public static final String CSS_SELECTOR_CANCEL_DEREGISTRATION_LINK = "a.t-cancel-deregistration-link";
    
    public EnterprisePage(UI ui) {
        this.ui = ui;
    }

    public PageElement getRegisterLink() {
        return ui.getElementProxy(By.cssSelector("a.t-register-link"));
    }

    public void waitForRegistrationDialogFadeIn() {
        ui.waitUntilVisible(getRegistrationFormSelector());
    }

    public void fillRegistrationForm(String etcUrl, String etcVWUrl, String vwUrl,
        String followerApiUrl, String agcToken) {
        fillEnterpriseATCUrl(etcUrl);
        fillEnterpriseATCWebViewUrl(etcVWUrl);
        fillWebviewUrl(vwUrl);
        fillFollowerAPIUrl(followerApiUrl);
        fillSecurityToken(agcToken);
    }

    public By getRegistrationFormSelector() {
        return By.cssSelector("form[name='agcRegisterForm']");
    }

    public void fillEnterpriseATCUrl(String url) {
        PageElement agcUrl =
            ui.getElementProxy(getRegistrationFormSelector()).findElement(
                By.cssSelector("input[name='agcUrl']"));
        agcUrl.sendKeys(url);
    }

    public void fillEnterpriseATCWebViewUrl(String url) {
        PageElement agcUrl =
            ui.getElementProxy(getRegistrationFormSelector()).findElement(
                By.cssSelector("input[name='agcWebUrl']"));
        agcUrl.sendKeys(url);
    }

    public void fillWebviewUrl(String url) {
        PageElement agcUrl =
            ui.getElementProxy(getRegistrationFormSelector()).findElement(
                By.cssSelector("input[name='webviewUrl']"));
        agcUrl.sendKeys(url);
    }

    public void fillFollowerAPIUrl(String url) {
        PageElement agcUrl =
            ui.getElementProxy(getRegistrationFormSelector()).findElement(
                By.cssSelector("input[name='followerUrl']"));
        agcUrl.sendKeys(url);
    }

    public void fillSecurityToken(String token) {
        PageElement agcUrl =
            ui.getElementProxy(getRegistrationFormSelector()).findElement(
                By.cssSelector("input[name='token']"));
        agcUrl.sendKeys(token);
    }
    
    public void clickTestConfiguration() {
        PageElement el =
            ui.getElementProxy(By.cssSelector("button.t-test-registration-btn"));
        el.click();
    }
    
    public void clickRegister() {
        PageElement el =
            ui.getElementProxy(By.cssSelector("button.t-register-btn"));
        el.click();
    }
    
    public boolean isSuccessTestRegistrationMessagePresent() {
        return ui.findElements(By.cssSelector("div.t-test-registration-success")).size() > 0;
    }
    
    public void waitForNextStep() {
        Utils.sleep(100);
        ui.waitWhileVisible(By.cssSelector(".t-registration-dialog .settings-wait"));
    }
    
    public boolean isSuccessRegistrationMessagePresent() {
        return ui.findElements(By.cssSelector("div.t-registration-success")).size() > 0;
    }
    
    public void closeDialog() {
        PageElement el =
            ui.getElementProxy(By.cssSelector("button.t-close-btn"));
        el.click();
    }
    
    public void waitReloadPageAfterSuccessRegistration() {
        ui.waitUntilVisible(By.cssSelector(CSS_SELECTOR_CANCEL_REGISTRATION_LINK));
    }
    
    public PageElement getCancelRegistrationLink() {
        return ui.getElementProxy(By.cssSelector(CSS_SELECTOR_CANCEL_REGISTRATION_LINK));
    }
    
    public boolean isDeregistrationLinkPresent() {
        return ui.findElements(By.cssSelector(CSS_SELECTOR_DEREGISTER_LINK)).size() > 0;
    }
    
    public PageElement getDeregistrationLink() {
        return ui.getElementProxy(By.cssSelector(CSS_SELECTOR_DEREGISTER_LINK));
    }
    
    public boolean isCancelDeregistrationLinkPresent() {
        return ui.findElements(By.cssSelector(CSS_SELECTOR_CANCEL_DEREGISTRATION_LINK)).size() > 0;
    }
    
    public PageElement getCancelDeregistrationLink() {
        return ui.getElementProxy(By.cssSelector(CSS_SELECTOR_CANCEL_DEREGISTRATION_LINK));
    }
    
    public boolean isStatusOnline() {
        String status = getStatus();
        return "ONLINE".equals(status);
    }
    
    public boolean isStatusTokenInvalid() {
        String status = getStatus();
        return "TOKEN INVALID".equals(status);
    }
    
    public String getStatus() {
        PageElement el = ui.getElementProxy(By.cssSelector(".t-follower-status"));
        return el.getText();
    }
    
}
