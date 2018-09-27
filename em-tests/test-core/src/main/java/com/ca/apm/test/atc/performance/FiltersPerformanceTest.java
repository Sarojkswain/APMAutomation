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
package com.ca.apm.test.atc.performance;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.PageElement;

public class FiltersPerformanceTest extends PerformanceTest implements RepeatableTest {
    
    private FilterBy filterComboBox;

    @Test
    @Parameters({ "testIterations" })
    public void testFiltersActivation(int iterations) throws Exception {
        UI ui = getUI();
        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getRibbon().expandFilterToolbar();
        filterComboBox = ui.getFilterBy();
        
        runTest(this, iterations);
    }
    
    @Override
    public void oneLoop() throws Exception {
        List<PageElement> comboItems = filterComboBox.getFilterByMenuList(0);

        // open all filters
        for (WebElement item : comboItems) {
            filterComboBox.expandFilterByMenu(0);
            item.click();
            filterComboBox.waitForUpdate();
        }

        filterComboBox.removeCompleteFilter();
    }
}
