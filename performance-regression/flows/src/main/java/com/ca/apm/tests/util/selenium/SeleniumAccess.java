/**
 * 
 */
package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.WebElement;

/**
 * @author keyja01
 *
 */
public interface SeleniumAccess {

    /**
     * Click on a {@link WebElement} from a returned configuration class
     * @param element
     */
    void clickWithDelay(WebElement element);

    void selectOptionByValue(WebElement element, String value);
    
    void selectOptionByIndex(WebElement element, int index);
    
    void selectOptionByVisibleText(WebElement element, String name);
    
    void setInputTextByName(String name, String text);
    
    void setInputTextById(String id, String text);
}
