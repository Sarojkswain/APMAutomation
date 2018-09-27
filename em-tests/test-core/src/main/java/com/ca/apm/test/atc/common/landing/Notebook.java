package com.ca.apm.test.atc.common.landing;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;

public class Notebook extends ElementConditionWrapper {

    private ATPanel atPanel;
    private Canvas canvas;

    public Notebook(UI ui) {
        super(ui, By.cssSelector(".home-container.home-detail"));
    }
    
    public void waitForLoad() {
        ui.waitForWorkIndicator(By.cssSelector(".home-detail-map .work-indicator"));
    }

    public ATPanel getATPanel() {
        if (atPanel == null) {
            atPanel = new ATPanel(ui, this);
        }
        return atPanel;
    }

    public Canvas getCanvas() {
        if (canvas == null) {
            canvas = new Canvas(ui);
        }
        return canvas;
    }
}
