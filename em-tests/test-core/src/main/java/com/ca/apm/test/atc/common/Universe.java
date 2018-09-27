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

import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class Universe extends ElementConditionWrapper {

    public static final String DEFAULT_UNIVERSE = "ENTERPRISE";
    public static final String ALL_MY_UNIVERSES = "ALL MY UNIVERSES";
    
    public Universe(UI ui) {
        super(ui, By.id("universe-selector"));
    }
    
    public void expandUniverseMenu() {
        click();
    }
    
    public PageElement getUniverseSelectionCombo() {
        return ui.getElementProxy(By.id("universe-selection-combo"));
    }
    
    public PageElement getUniverseListContainer() {
        return ui.getElementProxy(By.id("universe-selection-combo-menu"));
    }
    
    public List<String> getAvailableUniversesNames() {
        PageElement universeList = getUniverseListContainer();
        List<String> universeNames = new ArrayList<>();
        
        for (PageElement listItem : universeList.findPageElements(By.cssSelector("li > a > span"))) {
            universeNames.add(listItem.getAttribute("innerText"));
        }
        
        return universeNames;
    }
    
    public void selectUniverse(String name) {
        expandUniverseMenu();
        
        PageElement universeList = getUniverseListContainer();
        
        for (PageElement universeItem : universeList.findPageElements(By.cssSelector("li > a"))) {
            if (universeItem.getText().equals(name)) {
                universeItem.click();
                break;
            }
        }
    }
    
    public String getActiveUniverseName() {
        String name = getUniverseSelectionCombo().findElement(By.tagName("span")).getAttribute("innerText");
        return name.trim();
    }
}
