package com.ca.apm.webui.test.framework.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ca.apm.webui.test.framework.interfaces.IConstants;


/**
 * <code> AutomationFramework</code> provides a collection of Selenium helper
 * common methods to be used by all functional test code that uses Selenium
 * 
 * @author mccda04
 * 
 * @copyright 2014 CA Technology, All rights reserved.
 */
public abstract class AutomationFramework
    extends AbstractTestCase
{

    private static Browser fBrowser;

    private int            fScreenshotVersion; 

    /**
     * Initialize framework; reads in all properties; creates Browser object; <br>
     * sets internal WebDriver objects <br>
     * <br>
     * 
     * This method must be run before any other Framework methods
     * 
     * @param author
     *            pmfkey of author
     * @param componentName
     *            name of component being tested (used when logging messages)
     * @param methodName
     *            name of test method (used when logging messages)
     * @param screenshotFilename
     *            name of file to store screenshots taken
     */
    public void initFramework(String author,
                              String componentName,
                              String methodName,
                              String screenshotFilename)
    {
        setAuthorName(author);
        setObjectName(componentName);
        setObjectType(methodName);
        setScreenshotFilename(screenshotFilename);
        fScreenshotVersion = 1;
        
        fBrowser = BrowserFactory.getBrowser(getPropertiesObject());
        fBrowser.maximize();

        // Output current screen size and browser size to the log file 
        java.awt.Dimension screenDim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        logTestCase(INFO, "Screen size width: " + screenDim.getWidth() + " height: " + screenDim.getHeight());
        Dimension dim = fBrowser.getWd().manage().window().getSize();
        int height = dim.getHeight();
        int width = dim.getWidth();
        logTestCase(INFO, "Browser size width: " + dim.getWidth() + " height: " + height);
        // If browser height is less than 1024 or browser width is less than 1280, 
        // set browser size to 1280 X 1024 
        if ((height < IConstants.MIN_BROWSER_HEIGHT) || (width < IConstants.MIN_BROWSER_WIDTH))
        {
            logTestCase(INFO, "Attempting to set browser size to width: " + IConstants.MIN_BROWSER_WIDTH + 
                        " height: " + IConstants.MIN_BROWSER_HEIGHT);  
            if (!setBrowserSize(IConstants.MIN_BROWSER_WIDTH, IConstants.MIN_BROWSER_HEIGHT))
            {
                logTestCase(ERROR, "Failed to set browser size");
            }
        }
        
        // set internal webdriver objects
        setWebDriver(fBrowser.getWd());
        setActions(fBrowser.getActions());
        setJavascriptExecutor(fBrowser.getJavascriptExecutor());
        setWait(fBrowser.getWait());

        // set log level
        autosetLogLevel();
    }

    /**
     * Set Browser size 
     * @param width
     * @param height
     */
    public boolean setBrowserSize(int width, int height)
    {
        Window win = fBrowser.getWd().manage().window();
        Dimension dim = new Dimension (width, height);
        // Set browser size
        win.setSize(dim);  
        // Check browser size 
        dim = win.getSize();
        logTestCase(INFO, "Browser size after adjustment width: " + dim.getWidth() + " height: " + dim.getHeight());
        return ((dim.getWidth() >= IConstants.MIN_BROWSER_WIDTH) && (dim.getHeight() >= IConstants.MIN_BROWSER_HEIGHT));
    }
    
    /**
     * Exit framework; restore and stop browser; destroy base objects
     */
    public void exitFramework()
    {
        fBrowser.exitBrowser();
        assert(getTestCaseStatus().equals(STATUS.PASS));
    }

    /**
     * Setup for a Test Case
     * 
     * @param id
     *            Test case identifier
     * @param name
     *            Test case name
     * @param description
     *            Test case description
     */
    public void startTestCase(String id, String name, String description)
    {
        setTestCaseName(name);
        setTestCaseID(id);
        setTestCaseExitTextOnFail(name + ": failed validation.");
        setTestCaseDescription(description);
        logTestCaseStart();
    }

    /**
     * Set test case status and exit
     * 
     * @param ok
     *            test case status
     */
    public void endTestCase(Boolean ok)
    {
        if (ok)
        {
            setTestCaseStatusToPass();
        } else
        {
            // Take a screenshot if a test failed 
            takeScreenShot();
            setTestCaseStatusToFail();
        }
        processTestCaseExit();
    }
 
    public void setScreenshotFilename(String fileName)
    {
        setProperty("screenshot.filename", fileName);
    }

    /**
     * Take a screenshot (png) of the browser's current page. The screen shot is
     * saved in the directory specified by the property
     * <code>screenshot.directory</code>, in the launch.properties file.
     * 
     * @since QATF2.0
     */
    public void takeScreenShot()
    {
        String shotDir = getProperty("screenshot.directory");
        String fileName = getProperty("screenshot.filename")
                          + fScreenshotVersion++;
        File scrFile = ((TakesScreenshot) getWebDriver())
                .getScreenshotAs(OutputType.FILE);
        logTestCase(DEBUG, "Take screenshot: \"" + shotDir + "/" + fileName
                           + ".png.\"");
        try
        {
            FileUtils.copyFile(scrFile, new File(shotDir + "/" + fileName
                                                 + ".png"));
        } catch (IOException e)
        {
            logTestCase(ERROR, "Unable to create snapshot! [file] \"" + shotDir
                               + "/" + fileName + ".png");
            e.printStackTrace();
        } // end try..catch
    }
    
    /**
     * Return WebElement given selector; 
     * assumes root element is Web Driver
     * 
     * @param selector
     *            By selector to locate WebElement
     * @return WebElement or null
     */
    public WebElement getWebElement(By selector)
    {
        return getWebElement(getWebDriver(),selector);
    }

    /**
     * Return WebElement given rootElement and selector; rootElement can be
     * WebDriver or another WebElement
     * 
     * @param rootElement
     *            WebDriver or WebElement
     * @param selector
     *            By selector to locate WebElement
     * @return WebElement or null
     */
    public WebElement getWebElement(Object rootElement, By selector)
    {
        WebElement element = null;

        try
        {
            if (rootElement instanceof WebDriver)
            {
                element = ((WebDriver) rootElement).findElement(selector);
            } else
            {
                element = ((WebElement) rootElement).findElement(selector);
            }
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "getWebElement() Failed: no element found using [" + selector + "]\n" + e.toString()); //$NON-NLS-1$  
            takeScreenShot();
        }

        return element;
    }

    public String getInnerHTML(String id)
    {
        String innerHTML = null;
        String script = "return document.getElementById(\"" + id
                        + "\").innerHTML;";
        try
        {
            innerHTML = (String) getJavascriptExecutor().executeScript(script);
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "failed to execute javaScript (" + script + ")" + "\n" + e.toString()); //$NON-NLS-1$  
        }
        return innerHTML;
    }

    /**
     * Find List of WebElements given selector; locate elements using WebDriver
     * 
     * @param selector
     *            By selector used to locate WebElements
     * @return List of WebElements
     */
    public List<WebElement> findWebElements(Object rootElement, By selector)
    {
        List<WebElement> elements = null;

        try
        {
            if (rootElement instanceof WebDriver)
            {
                elements = ((WebDriver) rootElement).findElements(selector);
            } else
            {
                elements = ((WebElement) rootElement).findElements(selector);
            }
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "findWebElements() Failed: no elements found using selector [" + selector + "]"); //$NON-NLS-1$
            takeScreenShot();
            return null;
        }

        if (elements == null)
        {
            logTestCase(ERROR,
                        "findWebElements() Failed: no elements found using selector [" + selector + "]"); //$NON-NLS-1$
            takeScreenShot();
            return null;
        }
        return elements;
    }

    /**
     * Wait until WebElement is present given selector
     * 
     * @param selector
     *            By selector used to locate WebElement
     * @return located WebElement
     */
    public WebElement waitForWebElement(By selector)
    {
        WebElement element = null;

        try
        {
            element = getWait()
                    .until(ExpectedConditions
                                   .presenceOfElementLocated(selector));
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "waitForWebElement() Failed: Waiting failed to locate [" + selector + "]\n" + e.toString()); //$NON-NLS-1$ 
            takeScreenShot();
        }

        return element;
    }

    public boolean waitForWebElementInvisible(By selector)
    {
        Boolean status = false;

        try
        {
            status = getWait()
                    .until(ExpectedConditions
                                   .invisibilityOfElementLocated(selector));
            //	            fw.logTestCase(INFO, widgetId + " is no longer visible"); //$NON-NLS-1$                
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "Failed waiting for " + selector + " to become invisible\n" + e.toString()); //$NON-NLS-1$            
        }

        return status;
    }

    public boolean scrollWebElementIntoView(WebElement element)
    {
        Boolean status = false;
        try
        {
            ((RemoteWebElement) element).getCoordinates().onScreen();
            status = true;
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "Failed to scroll element into view\n" + e.toString()); //$NON-NLS-1$   
            status = false;
        }

        return status;
    }
    
    /**
     * Scroll down on a DIV DOM element with Javascript. Use this method when 
     * a DIV contains an item that is outside the view and in some browser, 
     * e.g. IE10, the Selenium code can't apply an action to it.    
     * @param id - DIV id
     * @param scrollValue - Use less than or equals to 1.0 to specify scroll height ratio 
     *                      Use greater than 1.0 to specify number of pixels 
     */
    public void scrollDownDIVElement(String id, double scrollValue)
    {
        String scrollValueString = Double.toString(scrollValue);
        
        
        String jsCommand = "var selBox = document.getElementById('" + id + "');" +
                     "selBox.scrollTop = ";
        
        if (scrollValue > 1.0)
        {
            // Scroll down scrollValue pixels
            jsCommand += scrollValueString; 
        }
        else 
        {
            // Scroll down scrollHeoght * scrollValue, for example, if scrollHeight 
            // (container height) is 100 pixels and the scrollValue is 0.5, it 
            // scrolls down 50 pixels. 
            jsCommand += "(selBox.scrollHeight * " + scrollValueString;
        }
        
        jsCommand += ");";
        
        this.getJavascriptExecutor().executeScript(jsCommand, "");
    }

    /**
     * Set input value for WebElement located using selector; performs clear
     * first; uses sendKeys to set input; works for html <input> selector
     * 
     * @param selector
     *            By selector used to locate WebElement
     * @param value
     *            value set in the input area
     * @return true if value set successfully
     */
    public boolean setTextAreaValue(By selector, String value)
    {
        try
        {
            WebElement we = waitForWebElement(selector);
            we.clear();
            we.sendKeys(value);
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "setTextAreaValue() Failed: input area [" + selector + "] not found\n" + e.toString()); //$NON-NLS-1$
            takeScreenShot();
            return false;
        }
        return true;
    }

    /**
     * 
     * Verifies WebElement exists and is displayed.
     * 
     * @param selector
     *            By selector used to locate WebElement
     * @param isDisplayed
     *            expected display state
     * @return true if WebELement exists and its display state matches the
     *         expected state
     */
    public boolean verifyElementDisplayState(By selector, boolean isDisplayed)
    {
        //
        WebElement element = getWebElement(getWebDriver(), selector);
        if (element == null)
        {
            takeScreenShot();
            logTestCase(ERROR,
                        "verifyElementDisplayState() failed: 'element  ["
                                + selector + "] does not exist");
            takeScreenShot();
            return false;
        }
        return testDisplayedState(element, isDisplayed);
    }

    /**
     * Returns WebElement if it exists and is displayed.
     * 
     * @param selector
     *            By selector used to locate WebElement
     * @return null or WebElement if it exists and is displayed
     */
    public WebElement getDisplayedElement(By selector)
    {
        WebElement element = getWebElement(getWebDriver(), selector);
        if (element == null)
        {
            takeScreenShot();
            logTestCase(ERROR, "getDisplayedElement() failed: 'element  ["
                               + selector + "] does not exist");
            takeScreenShot();
        } else if (!testDisplayedState(element, true))
        {
            return null;
        }
        return element;
    }

    /**
     * Tests if WebElement displayed state matches expected state.
     * 
     * @param el
     *            WebElement to test against
     * @param isDisplayed
     *            expected display state
     * @return true if WebElement displayed state matches expected state.
     */
    public boolean testDisplayedState(WebElement el, boolean isDisplayed)
    {
        if (!el.isDisplayed() && isDisplayed)
        {
            logTestCase(ERROR,
                        "testDisplayedState() failed: 'expected element [" + el
                                + "] to be displayed");
            takeScreenShot();
            return false;
        } else if (el.isDisplayed() && !isDisplayed)
        {
            logTestCase(ERROR,
                        "testDisplayedState() failed: 'expected element [" + el
                                + "] to not be displayed");
            takeScreenShot();
            return false;
        }
        return true;
    }

    /**
     * Verify if WebElement text matches expected text.
     * 
     * @param element
     *            WebElement to test against
     * @param expectedText
     *            expected text
     * @return true if expected text matches WebElement's text
     */
    public boolean verifyWebElementText(WebElement element, String expectedText)
    {
        if (!element.getText().equalsIgnoreCase(expectedText))
        {
            logTestCase(ERROR,
                        "verifyWebElementText() failed:  [" + element.getText()
                                + "] does not match expected text ["
                                + expectedText + "]");
            takeScreenShot();
            return false;
        }
        return true;
    }

    /**
     * Verify if text matches expected text.
     * 
     * @param text
     *            String to test with expected text
     * @param expectedText
     *            expected text String
     * @return true if text matches expected text (ignores case)
     */
    public boolean verifyTextMatches(String text, String expectedText)
    {
        if (!text.equalsIgnoreCase(expectedText))
        {
            logTestCase(ERROR, "verifyTextMatches() failed: [" + text
                               + "] does not match expected value ["
                               + expectedText + "]");
            takeScreenShot();
            return false;
        }
        return true;
    }

    /**
     * Get value from WebElement's 'value' attribute
     * 
     * @param selector
     *            By selector to locate WebElement
     * @return String value
     */
    public String getInputValue(By selector)
    {
        String inputValue = null;
        try
        {
            WebElement we = getWebElement(getWebDriver(), selector);
            inputValue = we.getAttribute("value");

        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "getInputValue() Failed to locate input [" + selector + "]\n" + e.toString()); //$NON-NLS-1$  
            takeScreenShot();
        }

        return inputValue;
    }

    public void showContextMenu(By elementSelector, By contextSelector)
    {
        WebElement element = getWebElement(getWebDriver(), elementSelector);
        if (element != null)
        {
            getActions().contextClick(element).perform(); // click to show
                                                          // context menu
            sleep(100);

            // locate and move to context menu to show sub-menu
            element = getWebElement(getWebDriver(), contextSelector);
            if (element != null)
            {
                getActions().moveToElement(element).perform();
                sleep(100);
            }
        }
    }

    public boolean hasPointerCursor(By selector)
    {
        boolean status = false;
        String cursorType = null;

        WebElement element = getWebElement(getWebDriver(), selector);
        if (element != null)
        {
            cursorType = (String) getStyleAttribute(element, kCursorStyle);
            if (cursorType != null)
            {
                status = cursorType.equalsIgnoreCase(kPointerCursorStyle);
            }
        }
        if (!status)
        {
            logTestCase(ERROR,
                        selector
                                + " cursor type (" + cursorType + ") is not equal to (" + kPointerCursorStyle + ")"); //$NON-NLS-1$
        }
        return status;
    }

    public String getCurrentUrl()
    {
        String url = getWebDriver().getCurrentUrl();
        String currentUrl = url.replace("%25", "%");
        return currentUrl;
    }

    public void moveFromWebElement(WebElement element, String elementId)
    {
        if (element != null)
        {
            getActions()
                    .moveToElement(element, -element.getSize().width + 2, 0)
                    .perform();

            waitForWebElementInvisible(By.id(elementId));
        }
    }

    public String getWebElementAttribute(WebElement element, String attribute)
    {
        String attributeValue = null;
        try
        {
            attributeValue = element.getAttribute(attribute);
            logTestCase(INFO,
                        "attribute (" +
                         attribute
                                + ") value (" + attributeValue + ") located successfully"); //$NON-NLS-1$
        } catch (Exception e)
        {
            logTestCase(ERROR,
                        "Failed to locate attribute " + attribute + "\n" + e.toString()); //$NON-NLS-1$  
        }

        return attributeValue;
    }

    public Object getStyleAttribute(WebElement element, String attribute)
    {
        Object value = null;
        String style = element.getAttribute(kStyleAttribute);

        if (style != null & !style.isEmpty())
        {
            String[] styleElements = style.split(";"); //$NON-NLS-1$

            for (int i = 0; i < styleElements.length; i++)
            {
                String[] styleTokens = styleElements[i].split(":"); //$NON-NLS-1$
                String styleAttribute = styleTokens[0].trim();
                String styleValue = styleTokens[1].trim();

                if (styleAttribute.equals(attribute))
                {
                    if (attribute.equals(kFontSizeStyle)
                        || attribute.equals(kBorderWidthStyle)
                        || attribute.equals(kLeftStyle)
                        || attribute.equals(kTopStyle)
                        || attribute.equals(kWidthStyle)
                        || attribute.equals(kHeightStyle))
                    {
                        value = Integer.parseInt(styleValue
                                .substring(0, styleValue.length() - 2));
                    } else if (attribute.equals(kZindexStyle))
                    {
                        value = Integer.parseInt(styleValue);
                    } else if (attribute.equals(kTextColorStyle))
                    {
                        value = rgbToHex(styleValue);
                    } else
                    {
                        value = styleValue.replaceAll("'", "");
                    }
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Dismiss active alert box.
     */
    public void dismissAlertBox()
    {
        logTestCase(TRACE, "dismissAlertBox() method entry.");
        try
        {
            getWebDriver().switchTo().alert().dismiss();
        } catch (Exception ex)
        {
            logTestCase(ERROR, "Alert box not found! Unable to dismiss.");
        } // end try..catch
        logTestCase(TRACE, "dismissAlertBox() method exit.");
    } // end method

    // FILTER LIST METHODS

    /**
     * <code>filterList</code> iterates through a list of WebElements looking
     * for a match on attrName/attrValue. For each matching WebElement,
     * depending on <code>keepMatch</code>, the element is retained or removed.
     * 
     * <pre>
     *  <i>partialMatch</i>
     *  
     *  partialMatch = true;  allow partial match on attrValue.
     *  partialMatch = false; require an exact match on attrValue.
     *  
     *  <i>keepMatch</i>
     *  
     *  keepMatch = true; return only matching elements.
     *  keepMatch = false; return only non-matching elements.
     * </pre>
     * 
     * @param wel
     *            -A WebElement List.
     * @param byType
     *            - Type of search to perform.
     * @param attrName
     *            - Target tag name or attribute name
     * @param attrValue
     *            - Target class name, xpath string, or attribute value.
     * @param partialMatch
     *            - Allow matches on partial value.
     * @param keepMatch
     *            -Return only matching elements.
     * @return A list of WebElements.
     * @since QATF2.0
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    public List<WebElement> filterByAttrValue(List<WebElement> wel,
                                              String attrName,
                                              String attrValue,
                                              boolean exact,
                                              boolean keep)
    {

        logTestCase(TRACE, "filterByAttrValue() method entry.");
        List<WebElement> results = new ArrayList<WebElement>();

        if (StringUtils.isEmpty(attrName))
        {
            return results; // null results
        } else if (StringUtils.isEmpty(attrValue))
        {
            return results; // null results
        } else
        { // iterate through each element in list

            attrName = attrName.trim();
            attrValue = attrValue.trim();
            logTestCase(DEBUG, "Filter on @" + attrName + "=\"" + attrValue
                               + "\" [exact=" + exact + "] [keep=" + keep + "]");

            for (WebElement cwe : wel)
            {
                // System.out.println("debug: " + attrName + "=" +
                // cwe.getAttribute(attrName));
                try
                {
                    if (exact)
                    { // exact match found
                        if (cwe.getAttribute(attrName).equals(attrValue))
                        {
                            this.logTestCase(DEBUG,
                                             "[filterByAttrVal] "
                                                     + "Found exact match! @"
                                                     + attrName
                                                     + "=\""
                                                     + cwe.getAttribute(attrName)
                                                     + "\"");
                            results.add(cwe);
                        } else
                        { // no exact match found
                            // do nothing
                        } // end exact if..else
                    } else
                    { // partial match found
                        if (cwe.getAttribute(attrName.trim())
                                .contains(attrValue.trim()))
                        {
                            this.logTestCase(DEBUG,
                                             "[filterByAttrVal] "
                                                     + "Partial match! @"
                                                     + attrName
                                                     + "=\""
                                                     + cwe.getAttribute(attrName)
                                                     + "\"");
                            results.add(cwe);
                        } else
                        { // partial match not found
                            // do nothing
                        } // end partial match if..else
                    } // end if..else exact vs. partial

                } catch (Exception ex)
                {
                    this.logTestCase(ERROR, ex.getMessage());
                    return results;
                } // end try..catch
            } // end for iteration

            logTestCase(DEBUG, "Results.size()=" + results.size());

            if (keep)
            { // return only the matches
                logTestCase(TRACE, "filterByAttrValue(keep-matches) "
                                   + "method exit.");
                return results;
            } else
            { // remove all the matches
                wel.removeAll(results);
                logTestCase(TRACE, "filterByAttrValue(remove-matches) "
                                   + "method exit.");
                return wel;
            } // end results return decision if..else
        } // end if..else
    } // end method

    /**
     * Filter by Class name.
     * 
     * @param wel
     * @param classValue
     * @param exact
     * @param keep
     * @return
     */
    public List<WebElement> filterByClass(List<WebElement> wel,
                                          String classValue,
                                          boolean exact,
                                          boolean keep)
    {
        List<WebElement> temp;
        logTestCase(TRACE, "filterByClass() method entry.");

        temp = filterByAttrValue(wel, "class", classValue, exact, keep);

        logTestCase(TRACE, "filterByClass() method exit.");

        return temp;

    } // end method

    /*
     * public List<WebElement> filterByAttributeName( wel, name, ) public
     * List<WebElement> filterById( wel, id public List<WebElement>
     * filterByTagName( wel, tagname public List<WebElement> filterByExactText(
     * wel, text public List<WebElement> filterByPartialText( wel, text
     */

    // FRAME METHODS

    /**
     * Switch to the the designated frame.
     * 
     * @param frameName
     * @return
     */
    public boolean switchToFrame(String frameName)
    {
        logTestCase(TRACE, "switchtoFrame() method entry.");
        try
        {
            getWebDriver().switchTo().frame(frameName);
            logTestCase(TRACE, "switchtoFrame(true) method exit.");
            return true;
        } catch (Exception e)
        {
            logTestCase(ERROR, "Unable to switch to frame! [frame] \""
                               + frameName + "\"");
            logTestCase(TRACE, "switchtoFrame(false) method exit.");
            return false;
        } // end if block
    } // end method

    // *** ASSERT METHODS ***

    /**
     * assertByClassName()
     * 
     * @param condition
     *            EXISTS or NOT_EXISTS
     * @return true false
     */
    public boolean assertByClassName(String className, boolean condition)
    {
        boolean myReturn = false;
        boolean isNull = true;

        try
        { // Look for the Element ByID
            getWebDriver().findElement(By.className(className));
            isNull = false; // element was found.
        } catch (Exception ex)
        {
            // ignore thrown error. Dealing with it in following code
        } // end try..catch

        // Element Should Exist on Page
        if (condition == NOT_EXISTS)
        {
            if (isNull)
            {
                myReturn = true;
            } else
            { // not expected AND not present
                myReturn = false;
                logTestCase(ERROR, "Class-NotExists Assertion Failed! "
                                   + "ClassName should be absent but exists. "
                                   + "[class] \"" + className + "\"");
            } // not expected but present
        } else
        { // DEFAULTS TO EXISTS
            if (isNull)
            {
                myReturn = false;
                logTestCase(ERROR, "Class-Exists Assertion Failed!"
                                   + "Class should exist but is absent! "
                                   + "[class] \"" + className + "\"");
            } // expected but not present
            else
            {
                myReturn = true;
            } // end else expected and present
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertByCSS()
     * 
     * @param idValue
     * @param condition
     *            'exists' 'not-exists'
     * @return true false
     */
    public boolean assertByCSS(String CSSValue, boolean condition)
    {

        boolean myReturn = false;
        boolean isNull = true;

        try
        {
            getWebDriver().findElement(By.cssSelector(CSSValue));
            isNull = false; // element was found since no error was thrown.
        } catch (Exception ex)
        {
            // ignore thrown error. Dealing with it in following code
        } // end try..catch

        // Element Should Exist on Page
        if (condition == NOT_EXISTS)
        {
            if (isNull)
            {
                myReturn = true;
            } else
            { // not expected AND not present
                myReturn = false;
                logTestCase(ERROR, "CSS-NotExists Assertion Failed! "
                                   + "CSS should be absent but exists. "
                                   + "[css] \"" + CSSValue + "\"");
            } // not expected but present
        } else
        { // DEFAULTS TO EXISTS: Expected but not present
            if (isNull)
            {
                myReturn = false;
                logTestCase(ERROR, "CSS-Exists Assertion Failed! "
                                   + "CSS should exist but is absent! "
                                   + "[css] \"" + CSSValue + "\"");
            } else
            {
                myReturn = true;
            } // expected and present
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertByID()
     * 
     * @param id
     * @param condition
     *            EXISTS or NOT_EXISTS
     * @return true false
     * @throws NoSuchElementException
     */
    public boolean assertByID(String id, boolean condition)
    {

        boolean myReturn = false;
        boolean isNull = true;

        try
        {
            getWebDriver().findElement(By.id(id));
            isNull = false; // element was found since no error was thrown.
        } catch (Exception ex)
        {
            // ignore thrown error. Dealing with it in following code
        } // end try..catch

        // not expected AND not present
        if (condition == NOT_EXISTS)
        {
            if (isNull)
            {
                myReturn = true;
            } else
            { // not expected but present
                myReturn = false;
                logTestCase(ERROR, "ID-NotExists Assertion Failed! "
                                   + "ID should be absent but exists. "
                                   + "[id] \"" + id + "\"");
            } // end if..else
        } else
        { // expected but not present
            if (isNull)
            {
                myReturn = false;
                logTestCase(ERROR, "ID-Exists Assertion Failed! "
                                   + "ID should exist but is absent! "
                                   + "[id] \"" + id + "\"");
            } else
            { // expected and present
                myReturn = true;
            } // end if..else
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertByName()
     * 
     * @param nameValue
     * @param condition
     *            EXISTS or NOT_EXISTS
     * @return true false
     * @throws NoSuchElementException
     */
    public boolean assertByName(String nameValue, boolean condition)
    {

        boolean myReturn = false;
        boolean isNull = true;

        try
        {
            getWebDriver().findElement(By.name(nameValue));
            isNull = false; // element was found since no error was thrown.
        } catch (Exception ex)
        {
            // ignore thrown error. Dealing with it in following code
        } // end try..catch

        // not expected AND not present
        if (condition == NOT_EXISTS)
        {
            if (isNull)
            {
                myReturn = true;
            } else
            { // not expected but present
                myReturn = false;
                logTestCase(ERROR, "Name-NotExists Assertion Failed! "
                                   + "Name should be absent but exists. "
                                   + "[name] \"" + nameValue + "\"");
            } // end if..else
        } else
        { // expected but not present
            if (isNull)
            {
                myReturn = false;
                logTestCase(ERROR, "Name-Exists Assertion Failed! "
                                   + "Name should exist but is absent! "
                                   + "[name] \"" + nameValue + "\"");
            } else
            { // expected and present
                myReturn = true;
            } // end if..else
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertByXPath()
     * 
     * @param idValue
     * @param condition
     *            EXISTS or NOT_EXISTS
     * @return true false
     * @throws NoSuchElementException
     */
    public boolean assertByXPath(String xPathValue, boolean condition)
    {
        boolean myReturn = false;
        boolean isNull = true;

        try
        {
            getWebDriver().findElement(By.xpath(xPathValue));
            isNull = false; // element was found since no error was thrown.
        } catch (Exception ex)
        {
            // ignore thrown error. Dealing with it in following code
        } // end try..catch

        // not expected AND not present
        if (condition == NOT_EXISTS)
        {
            if (isNull)
            {
                myReturn = true;
            } else
            { // not expected but present
                myReturn = false;
                logTestCase(ERROR, "XPath-NotExists Assertion Failed! "
                                   + "XPath should be absent but exists. "
                                   + "[xpath] \"" + xPathValue + "\"");
            } // end if..else
        } else
        { // expected but not present
            if (isNull)
            {
                myReturn = false;
                logTestCase(ERROR, "XPath-Exists Assertion Failed! "
                                   + "XPath should exist but is absent! "
                                   + "[xpath] \"" + xPathValue + "\"");
            } else
            { // expected and present
                myReturn = true;
            } // end if..else
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertTextOnPageSource()
     * 
     * @param idValue
     * @param condition
     *            'exists' 'notexists'
     * @return true false
     * @throws NoSuchElementException
     */
    public boolean assertTextOnPageSource(String searchString, boolean condition)
    {
        boolean myReturn = false;
        boolean isPresent = false;
        String[] pageSource = getWebDriver().getPageSource()
                .split(searchString);

        // Search for string via a split operation
        if (pageSource.length >= 2)
        { // split occurred. searchString was found.
            isPresent = true;
        } else
        { // Split did not occur. searchString not found.
            isPresent = false;
        } // end compare block

        // not expected AND not present
        if (condition == NOT_EXISTS)
        {
            if (isPresent == false)
            {
                myReturn = true;
            } else
            {
                myReturn = false;
                logTestCase(ERROR,
                            "Text-NotExists Assertion Failed! "
                                    + "Text should be absent but exists on page source. "
                                    + "[txt] \"" + searchString + "\"");
            } // end if..else
        } else
        { // expected but not present
            if (isPresent == false)
            {
                myReturn = false;
                logTestCase(ERROR,
                            "Text-Exists Assertion Failed! "
                                    + "Text should exist but is absent on page source. "
                                    + "[txt] \"" + searchString + "\"");
            } else
            { // expected and present
                myReturn = true;
            } // end if..else
        } // end if block
        return myReturn;
    } // end method

    /**
     * assertTextInWebElement
     * 
     * @param condition
     *            'exists' 'notexists'
     * @return true false
     * @throws NoSuchElementException
     */
    public boolean assertTextInWebElement(WebElement we,
                                          String text,
                                          boolean condition)
    {
        boolean myReturn = false;
        boolean isPresent = false;

        String[] pageSource = we.getText().split(text);

        // Search for string via a split operation
        if (pageSource.length >= 2)
        { // split occurred. string was found.
            isPresent = true;
        } else
        { // Split did not occur. searchString not found.
            isPresent = false;
        } // end compare block

        // not expected and not present
        if (condition == NOT_EXISTS)
        {
            if (isPresent == false)
            {
                myReturn = true;
            } else
            { // not expected but exists
                myReturn = false;
                logTestCase(ERROR,
                            "Text-NotExists Assertion Failed! "
                                    + "Text should be absent but exists in WebElement. "
                                    + "[txt] \"" + text + "\"");
            } // end if..else
        } else
        { // expected but not present
            if (isPresent == false)
            {
                myReturn = false;
                logTestCase(ERROR,
                            "Text-Exists Assertion Failed! "
                                    + "Text should exist but is absent in WebElement. "
                                    + "[txt] \"" + text + "\"");
            } else
            { // expected and present
                myReturn = true;
            } // end if..else
        } // end if block
        return myReturn;
    } // end method

    // WEB ELEMENT METHODS

    /**
     * Get the parent element of WebElement via XPath.
     */
    public WebElement getParentElement(WebElement we)
    {
        return we.findElement(By.xpath(".."));
    } // end method
    
    /**
     * click WebElement
     * first verify element is located, displayed and enabled
     * @param selector By selector to locate WebElement
     */
    public boolean clickElement(By selector)
    {     
        try {
            WebElement element = getWait()
                .until(ExpectedConditions.elementToBeClickable(selector));
            element.click();
        }
        catch (Exception e)
        {
            logTestCase(ERROR, "clickElement() failed for element (" + selector + ")");
            return false;
        }
        return true;
    }

    /**
     * Double click.
     */

    public boolean doubleClick(WebElement we)
    {
        try
        {
            getActions().doubleClick(we);
            return true;
        } catch (Exception e)
        {
            logTestCase(ERROR, "Element could not be clicked. "
                               + "Exception thrown: " + e.getLocalizedMessage());
            return false;
        } // end try..catch
    } // end method

    /**
     * Context Click.
     */
    public boolean contextClick(WebElement we)
    {
        try
        {
            getActions().contextClick(we);
            return true;
        } catch (Exception e)
        {
            logTestCase(ERROR, "Element could not be contexted-clicked. "
                               + "Exception thrown: " + e.getLocalizedMessage());
            return false;
        } // end try..catch
    } // end method

    /**
     * flashElement
     */
    public void flashWebElement(WebElement we) throws NoSuchElementException
    {

        if (!we.isDisplayed())
        {
            // do nothing
        } else
        {
            String originalStyle = we.getAttribute("style");

            for (int i = 0; i < 3; i++)
            {
                getJavascriptExecutor()
                        .executeScript("arguments[0].setAttribute('style',"
                                               + " 'background: yellow; "
                                               + "border: 2px solid red;');",
                                       we);
                sleep(250);
                getJavascriptExecutor()
                        .executeScript("arguments[0].setAttribute('style', '"
                                               + originalStyle + "');", we);
                sleep(250);
            } // end for
        } // end if..else
    } // end method

    // WAIT METHODS

    /**
     * Set default wait time
     * 
     * @param defaultWaitTimeInSeconds
     */
    public void setDefaultWaitTime(long defaultWaitTimeInSeconds)
    {
        getWait().withTimeout(defaultWaitTimeInSeconds, TimeUnit.SECONDS);
    } // end

    /**
     * Will ignore instances of NotFoundException that are encountered (thrown)
     * by default in the 'until' condition, and immediately propagate all
     * others. You can add more to the ignore list by calling
     * ignoring(exceptions to add).
     * 
     * @param timeOutInSeconds
     *            The time (seconds) in which the condition is expected to be
     *            met
     * @param condition
     *            Function expected to evaluate to true at some point
     * @return The WebElement that the expected condition was contingent on
     * @throws TimeoutException
     *             If the expected condition isn't met in the allotted time
     */
    public WebElement waitUntil(long timeOutInSeconds,
                                ExpectedCondition<WebElement> condition)
    {

        return (new WebDriverWait(getWebDriver(), timeOutInSeconds))
                .until(condition);
    } // end method

    /** waitUntilVisible() */
    public WebElement waitUntilVisible(By locator)
    {   
        WebElement element = null;
        try {
            element = getWait().until(ExpectedConditions
                                       .visibilityOfElementLocated(locator));
        }
        catch (Exception e) {}
        
        if (element == null)
        {
            logTestCase(ERROR, "waitUntilVisible() failed: Element (" + locator + ") is not visible or could not be found");
        }
        return element;
    } // end method

    /** waitUntilNotVisible() */
    public Boolean waitUntilNotVisible(By locator)
    {
        Boolean isNotVisible = false;
        
        try {
            isNotVisible = getWait().until(ExpectedConditions
                                       .invisibilityOfElementLocated(locator));
        }
        catch (Exception e) {}
        
        if ( !isNotVisible )
        {
            logTestCase(ERROR, "waitUntilVisible() failed: Element (" + locator + ") is visible but should be hidden or could not be found");
        }
        return isNotVisible;
    } 

    /** waitUntilClickable() */
    public WebElement waitUntilClickable(By locator)
    {
        return getWait()
                .until(ExpectedConditions.elementToBeClickable(locator));
    } // end method

    /** waitUntilSelected() */
    // would be nice if this returned a WebElement for chaining purposes
    public Boolean waitUntilSelected(By locator)
    {
        return getWait().until(ExpectedConditions.elementToBeSelected(locator));
    } // end method

    /** waitUntilNotSelected() */
    // would be nice if this returned a WebElement for chaining purposes
    public Boolean waitUntilNotSelected(By locator)
    {
        return getWait().until(ExpectedConditions
                                       .elementSelectionStateToBe(locator,
                                                                  false));
    } // end method

    /** waitUntilTextPresent() */
    // would be nice if this returned a WebElement for chaining purposes
    public Boolean waitUntilTextPresent(final By locator, String text)
    {
        return getWait()
                .until(ExpectedConditions.textToBePresentInElement(locator,
                                                                   text));
    } // end method

    public String rgbToHex(String rgbString)
    {
        String hexValue = "";
        int beginIndex = rgbString.indexOf('(') + 1;
        int endIndex = rgbString.indexOf(')');
        String rgbValues = rgbString.substring(beginIndex,endIndex);

        String[] tokens = rgbValues.split(","); //$NON-NLS-1$
        if ( tokens.length > 2 )
        {
            int r = Integer.parseInt(tokens[0].trim());
            int g = Integer.parseInt(tokens[1].trim());
            int b = Integer.parseInt(tokens[2].trim());
    
            hexValue = String.format("#%02x%02x%02x", r, g, b).toUpperCase(); //$NON-NLS-1$
        }
        return hexValue;
    }
    
    /** 
     * converts selenium cookie from browser to typical cookie 
     *     
     * @return CookieStore
     * */
    @SuppressWarnings("unused")
    public CookieStore seleniumCookiesToCookieStore(String url) 
    {        
        Set<Cookie> seleniumCookies = ((RemoteWebDriver) getWebDriver()).manage().getCookies();   
        CookieStore cookieStore = new BasicCookieStore();
   
        for(Cookie seleniumCookie : seleniumCookies){
            BasicClientCookie basicClientCookie =
                new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());          
            basicClientCookie.setExpiryDate(seleniumCookie.getExpiry());
            basicClientCookie.setPath(seleniumCookie.getPath());
            
            // msie driver workaround for null domain name bug
            if(seleniumCookie.getDomain() != null)
            {
               basicClientCookie.setDomain(seleniumCookie.getDomain());
            } else
            {
               String begin = url.substring(url.indexOf("//"));
               String domain = begin.substring(2, begin.lastIndexOf(":"));
               
               if(domain !=null && !domain.isEmpty())
               {
                  basicClientCookie.setDomain(domain);
               }
            }
            
            cookieStore.addCookie(basicClientCookie);
        }
     
        return cookieStore;
    }  
    
    /**
     * Test if element is stale
     * @param element WebElement
     * @return true or false
     */
    public boolean isStale(WebElement element)
    {
        if (element == null) return true;
        try {
            // Calling any method forces a staleness check
            element.isEnabled();
            return false;
          } catch (StaleElementReferenceException expected) {
            return true;
          }
    }
    
    /**
     * Hover on a <code>WebElement</code> and
     * wait for another element to appear
     * 
     * @param moveToSelector By selector used to get <code>WebElement</code> to move to
     * @param elementSelector By selector for <code>WebElement</code> that should appear
     * @return element that appears
     */
    protected WebElement hoverAndWaitForElement(By moveToSelector, By elementSelector)
    {   
        WebElement moveToElement = getWebElement(getWebDriver(), moveToSelector);
        return hoverAndWaitForElement( moveToElement, -1, -1, elementSelector, false); 
    }
    
    /**
     * Hover on a <code>WebElement</code> at
     * specific coordinates and
     * wait for another <code>WebELement</code> to appear
     * 
     * @param moveToSelector By selector used to get <code>WebElement</code> to move to
     * @param x hover at this x-coordinate offset
     * @param y hover at this y-coordinate offset
     * @param elementSelector By selector for <code>WebElement</code> that should appear
     * @return <code>WebElement</code> that appears
     */    
    protected WebElement hoverAndWaitForElement(By moveToSelector, int x, int y, By elementSelector)
    {
        WebElement moveToElement = getWebElement(getWebDriver(), moveToSelector);
        return hoverAndWaitForElement( moveToElement, x, y, elementSelector, true);  
    }
    
    protected WebElement hoverAndWaitForElement( WebElement moveToElement, int x, int y, By elementSelector)
    {                       
        return hoverAndWaitForElement( moveToElement, x, y, elementSelector, true);
    }
    
    /**
     * Hover on a <code>WebElement</code> at
     * specific coordinates if <code>useOffset is true.
     * Wait for another <code>WebELement</code> to appear
     * 
     * @param moveToSelector By selector used to get <code>WebElement</code> to move to
     * @param elementSelector By selector for <code>WebElement</code> that should appear
     * @param useOffset use offsets if true
     * @param x hover at this x-coordinate offset
     * @param y hover at this y-coordinate offset
    
     * @return <code>WebElement</code> that appears
     */    
    protected WebElement hoverAndWaitForElement(WebElement moveToElement, int x, int y, By elementSelector, boolean useOffset)
    {
        HoverForWebElement<WebElement> elementIsLocated = new HoverForWebElement<WebElement>(moveToElement, x, y, elementSelector, useOffset);
        WebDriverWait wait = new WebDriverWait(getWebDriver(), 60, 5000);
        WebElement element = wait.until(elementIsLocated);
        if ( element == null)
        {
            logTestCase(ERROR,
                        "hoverAndWaitForElement() failed: " +
                         elementSelector + 
                         " not found");
        }
        else { 
            logTestCase(INFO,
                        "hoverAndWaitForElement() success: found element hovered for (" +
                         elementSelector +
                         ")");

            getActions().moveToElement(element);
            sleep(1000);
        }
        return element; 
    }
    
    public void setImplicitWait(Long wait) {
        if (wait==null)
            wait = Long.parseLong(getProperty("implicit.wait.in.millis").trim());
        getWebDriver().manage().timeouts().implicitlyWait(wait, TimeUnit.MILLISECONDS);
    }   
    
    public void setImplicitWait()
    {
        setImplicitWait(null);        
    } 
    
    private class HoverForWebElement<T> implements ExpectedCondition<WebElement>
    {
        private WebElement fParentElement;

        private int        fXCoordinate;

        private int        fYCoordinate;

        private By         fElementSelector;

        private boolean    fUseOffset;
        
        public HoverForWebElement(WebElement parentElement, int x, int y, By elementSelector, boolean useOffset)
        {
            this.fParentElement = parentElement;
            this.fXCoordinate = x;
            this.fYCoordinate = y;
            this.fElementSelector = elementSelector;
            this.fUseOffset = useOffset;
        }
        
        public WebElement apply (WebDriver wd)
        {
            WebElement element = null;
            setImplicitWait(200L);
            
            try {
                if ( fParentElement != null ) 
                {   
                    if (fUseOffset) {
                        getActions().moveToElement(this.fParentElement, this.fXCoordinate, this.fYCoordinate).build().perform();
                    }
                    else {                    
                        getActions().moveToElement(this.fParentElement).build().perform();
                    }
                    element = getWebDriver().findElement(this.fElementSelector); 
                }
            }                     
            catch(Exception e) {
                logTestCase(INFO,
                            "hoverForWebElement() retrying: failed to find (" +
                             this.fElementSelector +
                             ") exception (" +
                             e.getMessage() +
                             ")");
                getActions().moveToElement(this.fParentElement,-1,-1).build().perform();
            }                     
            setImplicitWait();  
            return element;
        }
    }
}

