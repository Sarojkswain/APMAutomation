package com.ca.apm.test.atc.common.element;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.wily.util.StringUtils;

public class ATCDropDownOpener extends WebElementWrapper {

    protected final Logger logger = Logger.getLogger(getClass());
    
    private static int MAX_ATTEMPTS = 10;
    
    public ATCDropDownOpener(WebElement element, UI ui) {
        super(element, ui);
    }

    public ATCDropDownOpener(PageElement element) {
        super(element);
    }

    private boolean isDropdownOpened(By dropdownMenuSelector) {
        List<PageElement> el = ui.findElements(dropdownMenuSelector);
        if (el.size() > 0) {
            return el.get(0).isDisplayed();
        } else {
            return false;
        }
    }
    
    private void open(By dropdownMenuSelector) {
        int attempts = 0;
        while (!isDropdownOpened(dropdownMenuSelector) && attempts < MAX_ATTEMPTS) {
            logger.info("Clicking to open the drop down menu '" + dropdownMenuSelector.toString() + "', attempt #" + attempts);
            click();
            Utils.sleep(250);
            attempts++;
        }
    }
    
    private void close(By dropdownMenuSelector) {
        int attempts = 0;
        while (isDropdownOpened(dropdownMenuSelector) && attempts < MAX_ATTEMPTS) {
            logger.info("Clicking to close the drop down menu '" + dropdownMenuSelector.toString() + "', attempt #" + attempts);
            click();
            Utils.sleep(250);
            attempts++;
        }
    }
    
    public void selectFromDropdown(By dropdownMenuSelector, String label) {
        open(dropdownMenuSelector);
        
        boolean labelFound = false;
        PageElement dropdownMenu = ui.getElementProxy(dropdownMenuSelector);
        List<PageElement> menuItems = dropdownMenu.findPageElements(By.tagName("li"));
        for (PageElement item : menuItems) {
            String itemLabel = null;
            try {
                itemLabel = item.findElement(By.tagName("a")).getAttribute("data-name");
            } catch (Exception e) {
            }
            if (itemLabel == null) {
                itemLabel = item.getText();
            }
            if (item.isDisplayed() && itemLabel.equals(label.trim())) {
                item.click();
                labelFound = true;
                break;
            }
        }
        
        if (!labelFound) {
            throw new IllegalArgumentException(
                "Specified label '" + label + "' does not exist in the dropdown menu");
        }
    }
    
    public List<String> getDropdownOptions(By dropdownMenuSelector) {
        return getDropdownOptions(dropdownMenuSelector, null, null);
    }
    
    public List<String> getDropdownOptions(By dropdownMenuSelector, String cssClassOfOptionsToInclude) {
        return getDropdownOptions(dropdownMenuSelector, cssClassOfOptionsToInclude, null);
    }
    
    public List<String> getDropdownOptions(By dropdownMenuSelector, String cssClassOfOptionsToInclude, String attributeName) {
        List<String> values = new ArrayList<String>();
        
        open(dropdownMenuSelector);
        
        List<PageElement> menuItems = ui.getElementProxy(dropdownMenuSelector).findPageElements(By.cssSelector("li a"));
        for (PageElement item : menuItems) {
            Utils.runAgainOnStaleReferenceException(new Runnable() {
                @Override
                public void run() {
                    if (item.isDisplayed() && ((cssClassOfOptionsToInclude == null) || (item.getAttribute("class").contains(cssClassOfOptionsToInclude)))) {
                        if (attributeName == null) {
                            values.add(item.getText());
                        } else {
                            values.add(item.getAttribute(attributeName));
                        }
                    }
                };
            });
        }
        
        close(dropdownMenuSelector);
        
        return values;
    }
    
    public List<PageElement> getDropdownLinkElements(By dropdownMenuSelector, String cssClassOfOptionsToInclude) {
        List<PageElement> elements = new ArrayList<PageElement>();
        
        open(dropdownMenuSelector);
        
        List<PageElement> menuItems = ui.getElementProxy(dropdownMenuSelector).findPageElements(By.cssSelector("li a"));
        for (PageElement item : menuItems) {
            if (item.isDisplayed() && ((cssClassOfOptionsToInclude == null) || (item.getAttribute("class").contains(cssClassOfOptionsToInclude)))) {
                elements.add(item);
            }
        }
        
        return elements;
    }
    
    public String getSelectedOption() {
        return getSelectedOption(null);
    }
    
    public String getSelectedOption(String stripPrefix) {
        String text = getText(); 
        if (!StringUtils.isEmpty(stripPrefix)) {
            if (text.startsWith(stripPrefix)) {
                text = text.substring(stripPrefix.length());
            }
        }
        return text.trim();
    }
}
