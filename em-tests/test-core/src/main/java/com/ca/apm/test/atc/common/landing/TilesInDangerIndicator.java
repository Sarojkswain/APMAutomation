package com.ca.apm.test.atc.common.landing;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;

public class TilesInDangerIndicator extends ChildElementSelectorWrapper {

    public TilesInDangerIndicator(UI ui, BreadcrumbExperiences parent) {
        super(ui, parent, By.className("t-tiles-in-danger-indicator"));
    }
    
    public int getInDangerCount() {
        String elemText = getText();
        return Integer.parseInt(elemText.substring(0, elemText.indexOf('/')));
    }
}
