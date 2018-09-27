package com.ca.apm.test.atc.common.element;

import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;

public class ATCCheckBox extends WebElementWrapper {

    public ATCCheckBox(WebElement element, UI ui) {
        super(element, ui);
    }

    public ATCCheckBox(PageElement element) {
        super(element);
    }

    /**
     * Check if the menu checkbox is checked
     * 
     * @param el TODO
     * @param {object} el
     */
    public boolean isCheckboxChecked() {
        String[] splitted = getAttribute("class").split(" ");
        for (String s : splitted) {
            if (s.indexOf("custom-checkbox-checked") != -1) {
                return true;
            }
        }
        return false;
    }
}
