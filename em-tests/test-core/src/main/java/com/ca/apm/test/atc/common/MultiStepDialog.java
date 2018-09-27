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

import com.ca.apm.test.atc.common.element.PageElement;

public class MultiStepDialog {

    private UI ui;

    public MultiStepDialog(UI ui) {
        this.ui = ui;
    }

    public List<PageElement> getMultistepDialogButtons() {
        return ui.findElements(By.cssSelector(".t-multistep-footer button"));
    }
    
    public PageElement getOKButtonOnMultistepDialogButtons() {
        return ui.getElementProxy(By.cssSelector(".t-multistep-footer button:nth-child(1)"));
    }
    
    public void waitForMultistepOperationIsDone() {
        Utils.sleep(200);
        ui.waitWhileVisible(By.cssSelector(".t-multistep-body .settings-wait"));
    }
    
    public boolean isSuccessMessagePresent() {
        return ui.findElements(By.cssSelector(".t-multistep-body .t-success-message")).size() > 0;
    }

    public boolean isErrorMessagePresent() {
        return ui.findElements(By.cssSelector(".t-multistep-body .settings-msg")).size() > 0;
    }

}
