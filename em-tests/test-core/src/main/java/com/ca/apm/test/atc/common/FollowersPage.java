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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import com.ca.apm.test.atc.common.element.PageElement;
import com.google.common.base.Function;

public class FollowersPage {

    private final UI ui;

    public FollowersPage(UI ui) {
        this.ui = ui;
    }

    public PageElement getTable() {
        return ui.getElementProxy(By.cssSelector("div.followers-table"));
    }

    public Integer getRowByFollowerAPIURL(final String follower) {
        FluentWait<WebDriver> wait = Utils.getFluentWait(ui.getDriver(), Utils.DEFAULT_WAIT_DURATION);
        return wait.until(new Function<WebDriver, Integer>() {
            @Override
            public Integer apply(WebDriver driver) {
                List<PageElement> els = getTable().findPageElements(By.cssSelector(".t-cell-api-url .ui-grid-cell-contents"));
                for (int i = 0; i < els.size(); i++) {
                    PageElement el = els.get(i);
                    if (follower.equals(el.getText())) {
                        return i;
                    }
                }
                
                return null;
             }
        });
    }
    
    /**
     * Wait for follower when will be online 
     * 
     * @param follower
     * @param timeoutInSec
     * @return
     */
    public Integer waitUntilFollowerIsOnline(final String follower, int timeoutInSec) {
        FluentWait<WebDriver> wait = Utils.getFluentWait(ui.getDriver(), timeoutInSec);
        
        return wait.until(new Function<WebDriver, Integer>() {
            @Override
            public Integer apply(WebDriver driver) {
                List<PageElement> els = getTable().findPageElements(By.cssSelector(".t-cell-api-url .ui-grid-cell-contents"));
                for (int i = 0; i < els.size(); i++) {
                    PageElement el = els.get(i);
                    if (follower.equals(el.getText())) {
                        if (isStatusOnline(i)) {
                            return i;    
                        }
                    }
                }
                
                return null;
             }
        });
    }

    public PageElement getCancelRegistrationLink(int row) {
        return getTable().findElement(By.cssSelector(".t-action-cancel-registration"));
    }
    
    public boolean isCancelRegistrationLinkPresent(int row) {
        return getTable().findPageElements(By.cssSelector(".t-action-cancel-registration")).size() > 0;
    }
        
    public PageElement getCancelDeregistrationLink(int row) {
        return getTable().findElement(By.cssSelector(".t-action-cancel-deregistration"));
    }
    
    public boolean isCancelDeregistrationLinkPresent(int row) {
        return getTable().findPageElements(By.cssSelector(".t-action-cancel-deregistration")).size() > 0;
    }
        
    public boolean isDeregisterLinkPresent(int row) {
        return getTable().findPageElements(By.cssSelector(".t-action-deregister")).size() > 0;
    }
    
    public PageElement getDeregisterLink(int row) {
        return getTable().findElement(By.cssSelector(".t-action-deregister"));
    }
    
    public boolean isStatusRegistering(int row) {
        String status = getStatus(row);
        return status != null && status.startsWith("REGISTERING ");
    }
    
    public boolean isStatusDeregistering(int row) {
        String status = getStatus(row);
        return status != null && status.startsWith("DEREGISTERING ");
    }
    
    public boolean isStatusOnline(int row) {
        String status = getStatus(row);
        return status != null && status.startsWith("ONLINE ");
    }
    
    public boolean isStatusNotResponding(int row) {
        String status = getStatus(row);
        return status != null && status.startsWith("NOT RESPONDING ");
    }
    
    public boolean isStatusUnregister(int row) {
        String status = getStatus(row);
        return status != null && status.startsWith("UNREGISTERED ");
    }
    
    public String getStatus(int row) {
        List<PageElement> els = getTable().findPageElements(By.cssSelector(".t-cell-status"));
        return row >= els.size() ? null : els.get(row).getText();
    }
}
