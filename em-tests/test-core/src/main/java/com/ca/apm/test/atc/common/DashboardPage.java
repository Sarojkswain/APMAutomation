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

public class DashboardPage extends Tab {

    
    private UI ui;

    public DashboardPage(UI ui) {
        this.ui = ui;
    }

    /**
     * Check if Card Tab (Dashboard) is selected
     */
    @Override
    public boolean isSelected() {
        return ui.getLeftNavigationPanel().isViewActive(View.DASHBOARD);
    }

    /**
     * Switch to Card View
     */
    @Override
    public void go() {
        ui.getLeftNavigationPanel().selectView(View.DASHBOARD);
    }

    public void waitForReload() {
        ui.waitForWorkIndicator();
        ui.waitUntilVisible(By.cssSelector(".trend-card-panel"));
    }
}
