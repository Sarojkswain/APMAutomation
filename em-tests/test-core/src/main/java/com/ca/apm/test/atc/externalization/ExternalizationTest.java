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
package com.ca.apm.test.atc.externalization;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.PageElement;

import static org.junit.Assert.assertNotEquals;

public class ExternalizationTest extends UITest {

    @Test
    public void testExternalization() throws Exception {

        JapanUITest japanUItest = new JapanUITest();
        UI enUI = getUI();
        enUI.login();
        enUI.getLeftNavigationPanel().goToMapViewPage();

        japanUItest.before(
            ExternalizationTest.class.getMethod("testExternalization", new Class<?>[0]));
        UI jaUI = japanUItest.getUI();
        jaUI.login();
        jaUI.getLeftNavigationPanel().goToMapViewPage();

        // check timeline toolbar expander
        WebElement enElem = enUI.getTimeline().getTimelineToggleControl();
        WebElement jaElem = jaUI.getTimeline().getTimelineToggleControl();
        assertNotEquals(enElem.getText(), jaElem.getText());

        // check 'Alert Status' text from 'Alerts Sumary' in Details panel
        final String NODE_NAME = "TradeOptions|service";

        String xpath
            = "//*[@id=\"alert-list-container\"]//a[contains(text(), \"StatusTestMM/TradeOptionsAlert\")]";
        boolean enAlertPresent = false;
        boolean jaAlertPresent = false;

        enUI.getPerspectivesControl().expand();
        List<PageElement> list = enUI.getPerspectivesControl().getListOfPerspectives();
        list.get(list.size() - 1).click();
        enUI.getCanvas().waitForUpdate();
        enUI.getCanvas().getNodeByName(NODE_NAME).click();

        try {
            enUI.waitUntilVisible(By.xpath(xpath));
            enAlertPresent = true;
        } catch (TimeoutException te) {
            logger.warn("Could not find EN status events, skipping test.");
        }

        jaUI.getPerspectivesControl().expand();
        list = jaUI.getPerspectivesControl().getListOfPerspectives();
        list.get(list.size() - 1).click();
        jaUI.getCanvas().waitForUpdate();
        jaUI.getCanvas().getNodeByName(NODE_NAME).click();

        try {
            jaUI.waitUntilVisible(By.xpath(xpath));
            jaAlertPresent = true;
        } catch (TimeoutException te) {
            logger.warn("Could not find JA status events, skipping test.");
        }

        xpath = "//*[@id=\"alert-summary-container\"]/div/div[2]/table/tbody/tr[1]/td[1]/span";

        if (enAlertPresent && jaAlertPresent) {
            enElem = enUI.getDriver().findElement(By.xpath(xpath));
            jaElem = jaUI.getDriver().findElement(By.xpath(xpath));
            assertNotEquals(enElem.getText(), jaElem.getText());
        }

        jaUI.cleanup();
        japanUItest.after(true);
    }
}
