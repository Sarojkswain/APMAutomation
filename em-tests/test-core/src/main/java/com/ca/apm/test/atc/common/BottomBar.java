package com.ca.apm.test.atc.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.ca.apm.test.atc.common.element.PageElement;

public class BottomBar {
    
    private static final Logger logger = Logger.getLogger(BottomBar.class);
    
    private final UI ui;

    public BottomBar(UI ui) {
        this.ui = ui;
    }
    
    public int getActiveFilterCount() {
        PageElement el = ui.getElementProxy(By.id("bottom-bar-active-filters-count"));
        logger.info("Bottom bar - active filter count: " + el.getText());
        return Integer.valueOf(el.getText());
    }
    
    public String getActivePerspectiveName() {
        PageElement el = ui.getElementProxy(By.id("bottom-bar-active-perspective"));
        logger.info("Bottom bar - active perspective: " + el.getText());
        return el.getText();
    }

    public boolean isEventsActive() {
        PageElement el = ui.getElementProxy(By.id("bottom-bar-events-active"));
        String val = el.getText();
        logger.info("Bottom bar - events active: " + val);
        
        if ("ON".equalsIgnoreCase(val)) {
            return true;
        } else if ("OFF".equalsIgnoreCase(val)) {
            return false;
        } else {
            throw new IllegalArgumentException("Should be either 'ON' or 'OFF' while it was '" + val + "'");
        }
    }
    
    public boolean isHighlightingActive() {
        PageElement el = ui.getElementProxy(By.id("bottom-bar-highlighting-enabled"));
        String val = el.getText();
        logger.info("Bottom bar - highlighting active: " + val);
        
        if ("ON".equalsIgnoreCase(val)) {
            return true;
        } else if ("OFF".equalsIgnoreCase(val)) {
            return false;
        } else {
            throw new IllegalArgumentException("Should be either 'ON' or 'OFF' while it was '" + val + "'");
        }
    }

    private String[] getActivePerspectiveAttributes(boolean boldOnly) {
        By selector;
        if (boldOnly) {
            selector = By.cssSelector("#bottom-bar-active-perspective-info > span strong");
        } else {
            selector = By.cssSelector("#bottom-bar-active-perspective-info > span");
        }
        List<PageElement> els = ui.findElements(selector);
        String[] attributes = new String[els.size()];
        for (int i = 0; i < els.size(); i++) {
            String attr = els.get(i).getText().trim();
            if (attr.endsWith(",")) {
                attr = attr.substring(0, attr.length() - 1);
            }
            attributes[i] = attr;
        }
        return attributes;
    }

    public String[] getActivePerspectiveAttributes() {
        return getActivePerspectiveAttributes(false);
    }

    public String[] getActivePerspectiveAttributesInBold() {
        return getActivePerspectiveAttributes(true);
    }

    public boolean isCopyrightStringPresent() {
        try {
            String copyrightString = ui.getElementProxy(By.cssSelector(".bottomBar .copyrightString")).getText();
            return copyrightString.toLowerCase().contains("copyright");
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
