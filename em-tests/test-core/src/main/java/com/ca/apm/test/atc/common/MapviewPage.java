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

import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.element.PageElement;

public class MapviewPage extends Tab {
    
    private final UI ui;

    public MapviewPage(UI ui) {
        this.ui = ui;
    }

    /**
     * Check if Map Tab is selected
     */
    @Override
    public boolean isSelected() {
        return ui.getLeftNavigationPanel().isViewActive(View.MAPVIEW);
    }
    
    @Override
    public void go() {
        ui.getLeftNavigationPanel().selectView(View.MAPVIEW);
    }
    
    public void waitForReload() {
        ui.waitForWorkIndicator(By.id("appMapWorkIndicator"));
    }
    
    public PageElement getErrorDialog() {
        return ui.getElementProxy(By.cssSelector(".content .error-dialog"));
    }
    
    public String getErrorDialogTitle() {
        return getErrorDialog().findElement(By.cssSelector(".dialog-title")).getText();
    }

    public String getErrorDialogText() {
        return getErrorDialog().findElement(By.cssSelector(".error-dialog-text")).getText();
    }
}
