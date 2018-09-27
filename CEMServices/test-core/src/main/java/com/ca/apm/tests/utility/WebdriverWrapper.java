package com.ca.apm.tests.utility;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class WebdriverWrapper {
    static int iGlobalTimeout = 20000;
    public static final Logger log = Logger.getLogger(WebdriverWrapper.class.getName());

    public WebdriverWrapper() {}

    /**
     * utility to get element
     * 
     * @param driver
     * @param elementId
     * @return WebElement
     * @throws Exception
     */
    public static WebElement getElement(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        // adding new explicit wait here to wait until page load for element to
        // be found
        try {
            /*
             * WebElement element = (new WebDriverWait(driver,30))
             * .until(new ExpectedCondition<WebElement>(){
             * 
             * findElementByType(driver, identifiertype_identifier);
             * });
             */
            // WebDriverWait wait = new WebDriverWait(driver, 10000);
            WebElement element = findElementByType(driver, identifiertype_identifier);
            // WebElement element=
            // wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(identifiertype_identifier)));
            return element;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.error("validation Check NoSuchElementException ::following object not found::"
                + identifiertype_identifier);
            return null;
        } catch (Exception e) {
            log.info("[Exception]Element not found: " + identifiertype_identifier);
            // e.printStackTrace();
            return null;
        }
    }

    public static WebElement findElementByType(WebDriver d, final String identifiertype_identifier) {
        String[] elementIdentifier = identifiertype_identifier.split("_", 2);
        log.info("Find Element Invoked:\n" + "Element Identifier is: " + elementIdentifier[0] + "_"
            + elementIdentifier[1]);
        WebDriverWait wait = new WebDriverWait(d, 60);
        // while()
        try {
            if (elementIdentifier[0].equals("name")) {
                log.info(ExpectedConditions.presenceOfElementLocated(By.name(elementIdentifier[1])));
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .name(elementIdentifier[1])));
            }

            else if (elementIdentifier[0].equals("id"))
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .id(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("xpath"))
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .xpath(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("linkText"))
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .linkText(elementIdentifier[1])));
            // return d.findElement(By.linkText("Setup"));
            else if (elementIdentifier[0].equals("partialLinkText"))
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .partialLinkText(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("tagName"))

                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .tagName(elementIdentifier[1])));


            else if (elementIdentifier[0].equals("className"))
                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .className(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("cssSelector"))

                return wait.until(ExpectedConditions.presenceOfElementLocated(By
                    .cssSelector(elementIdentifier[1])));



            else {
                log.info("[ERROR]Element not found: " + identifiertype_identifier);
                return null;
            }
        } catch (Exception e) {
            log.info("[EXCEPTION]Element not found: " + identifiertype_identifier);
            // e.printStackTrace();
            return null;
        }
    }

    public static WebElement visibiltOfElementByType(WebDriver d,
        final String identifiertype_identifier) {
        String[] elementIdentifier = identifiertype_identifier.split("_", 2);
        log.info("Find Element Invoked:\n" + "Element Identifier is: " + elementIdentifier[0]
            + " and the element to be identified is " + elementIdentifier[1]);
        WebDriverWait wait = new WebDriverWait(d, 60);


        try {
            if (elementIdentifier[0].equals("name")) {
                log.info(ExpectedConditions.visibilityOfElementLocated(By
                    .name(elementIdentifier[1])));
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .name(elementIdentifier[1])));
            } else if (elementIdentifier[0].equals("id"))
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .id(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("xpath"))
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("linkText"))
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .linkText(elementIdentifier[1])));
            // return d.findElement(By.linkText(elementIdentifier[1]));
            // return d.findElement(By.linkText("Setup"));
            else if (elementIdentifier[0].equals("partialLinkText"))
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .partialLinkText(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("tagName"))

                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .tagName(elementIdentifier[1])));


            else if (elementIdentifier[0].equals("className"))
                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .className(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("cssSelector"))

                return wait.until(ExpectedConditions.visibilityOfElementLocated(By
                    .cssSelector(elementIdentifier[1])));



            else {
                log.info("[ERROR]Element not found: " + identifiertype_identifier);
                return null;
            }
        } catch (Exception e) {
            log.info("[EXCEPTION]Element not found: " + identifiertype_identifier);
            // e.printStackTrace();
            return null;
        }
    }

    public static List<WebElement> findElementsByType(WebDriver d,
        final String identifiertype_identifier) {
        try {
            WebDriverWait wait = new WebDriverWait(d, 10000);
            String[] elementIdentifier = identifiertype_identifier.split("_", 2);
            if (elementIdentifier[0].equals("name"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .name(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("id"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .id(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("xpath"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .xpath(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("linkText"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .linkText(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("partialLinkText"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .partialLinkText(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("tagName"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .tagName(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("className"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .className(elementIdentifier[1])));
            else if (elementIdentifier[0].equals("cssSelector"))
                return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
                    .cssSelector(elementIdentifier[1])));
            else {
                log.info("[ERROR]Element not found: " + identifiertype_identifier);
                return null;
            }
        } catch (Exception e) {
            log.info("[EXCEPTION]Element not found: " + identifiertype_identifier);
            e.printStackTrace();
            return null;
        }

    }

    public static List<WebElement> getElements(WebDriver driver,
        final String identifiertype_identifier) throws Exception {

        try {
            List<WebElement> elements = findElementsByType(driver, identifiertype_identifier);
            return elements;
        } catch (Exception e) {
            log.info("[Exception]Element not found: " + identifiertype_identifier);
            e.printStackTrace();

            return null;
        }
    }

    public static int getXpathCount(WebDriver driver, final String identifiertype_identifier)
        throws Exception {
        return WebdriverWrapper.getElements(driver, identifiertype_identifier).size();
    }

    /**
     * utility to get Page title by given element id
     * 
     * @param driver
     * @param elementId
     * @return String (current name of element found)
     * @throws Exception
     */
    public static String getElementText(WebDriver driver, final String identifiertype_identifier)
        throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        return element.getText();
    }

    /**
     * utility to get Page title
     * 
     * @param driver
     * @return String
     * @throws Exception
     */
    public static String getPageTitle(WebDriver driver) throws Exception {
        return driver.getTitle();
    }

    /**
     * utility to input text at given xPath
     * 
     * @param driver
     * @param inputData
     * @param xPath
     * @throws Exception
     */
    public static void inputText(WebDriver driver, String identifiertype_identifier,
        String inputData) throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        // element.click();
        log.info("Old value: " + element.getAttribute("value") + " New value: " + inputData);
        if (element.getAttribute("value") != null) {
            element.clear();
        }

        element.sendKeys(inputData);
        waitForPageToLoad(driver, iGlobalTimeout);
        // WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
    }

    /**
     * utility to input text at given xPath
     * 
     * @param driver
     * @param inputData
     * @param xPath
     * @throws Exception
     */
    public static void inputTextToBrowseButton(WebDriver driver, String identifiertype_identifier,
        String inputData) throws Exception {
        String xpath = identifiertype_identifier.replace("xpath_", "");
        // String css = identifiertype_identifier.replace("cssSelector_", "");
        log.info("Sending keys to element: " + xpath);
        // driver.findElement(By.xpath(css)).sendKeys(inputData);
        File file = new File(inputData);
        String inputData1 = file.getAbsolutePath();
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))).sendKeys(
            inputData1);

    }


    public static void inputTextToBrowseButtonByCss(WebDriver driver,
        String identifiertype_identifier, String inputData) throws Exception {
        String css = identifiertype_identifier.replace("cssSelector_", "");
        log.info("Sending keys to element: " + css);
        File file = new File(inputData);
        String inputData1 = file.getAbsolutePath();
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css))).sendKeys(
            inputData1);
    }



    /**
     * utility to click at given xPath
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static void click(WebDriver driver, String identifiertype_identifier) throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        element.click();
        log.info("Element clicked --> " + element);
        // sleep
        waitForPageToLoad(driver, iGlobalTimeout);
        Thread.sleep(2000);

    }

    public static void clickDisable(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        element.click();
        log.info("Element clicked --> " + element);
        // sleep
        // waitForPageToLoad(driver, iGlobalTimeout);


    }


    public static void clickLinkTextWithSpan(WebDriver driver, String linkText) throws Exception {


        List<WebElement> allElements = WebdriverWrapper.getElements(driver, "cssSelector_a span");
        // wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("")));
        for (WebElement element : allElements) {
            if (element.getText().contains(linkText)) {
                element.click();

                System.out.println("Element Clicked Successfully");
                waitForPageToLoad(driver, iGlobalTimeout);
                break;

            }

        }
    }

    /**
     * utility to submit button on the page at given xPath
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static void submitButton(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        element.submit();
    }

    /**
     * utility to deselect all from combo box on the page at
     * given xPath
     * 
     * @param driver
     * @param inputData
     * @param xPath
     * @throws Exception
     */
    public static void deselectSelectBox(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        Select select = new Select(WebdriverWrapper.getElement(driver, identifiertype_identifier));
        select.deselectAll();
    }

    /**
     * utility to select combo box and select desired choice on the page at
     * given xPath
     * 
     * @param driver
     * @param inputData
     * @param xPath
     * @throws Exception
     */
    public static void selectBox(WebDriver driver, String identifiertype_identifier,
        String inputData) throws Exception {

        WebElement sample = null;
        Select select = null;
        sample = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        // sample = (new WebDriverWait(driver,
        // 60)).until(ExpectedConditions.presenceOfElementLocated((identifiertype_identifier)));
        select = new Select(sample);
        Thread.sleep(10000);
        select.selectByVisibleText(inputData);
        waitForPageToLoad(driver, iGlobalTimeout);

    }

    /**
     * utility to select list and select desired choice on the page at given
     * xPath
     * 
     * @param driver
     * @param inputData
     * @param xPath
     * @throws Exception
     */
    /*
     * public static void selectList(WebDriver driver, String identifiertype_identifier,
     * String inputData) throws Exception {
     * WebElement select = WebdriverWrapper.getElement(driver, identifiertype_identifier);
     * List<WebElement> options = select.findElements(By.tagName("option"));
     * for (WebElement option : options) {
     * if (inputData.equals(option.getText())) {
     * if(!option.isSelected())
     * option.click();
     * }
     * }
     * }
     */
    public static boolean selectList(WebDriver driver, String identifiertype_identifier,
        String inputData) {
        boolean resultantValue = false;
        try {
            WebElement select = WebdriverWrapper.getElement(driver, identifiertype_identifier);
            List<WebElement> options = select.findElements(By.tagName("option"));
            for (WebElement option : options) {
                if (inputData.equals(option.getText())) {
                    if (!option.isSelected()) option.click();
                    resultantValue = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultantValue;
    }


    public static boolean selectListByCss(WebDriver driver, String identifiertype_identifier,
        String inputData) {
        boolean resultantValue = false;
        try {
            WebElement select = WebdriverWrapper.getElement(driver, identifiertype_identifier);
            List<WebElement> options = select.findElements(By.cssSelector("option"));
            for (WebElement option : options) {
                if (inputData.equals(option.getText())) {
                    if (!option.isSelected()) option.click();
                    resultantValue = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultantValue;
    }

    public static String[] getSelectOptions(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement select = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        List<WebElement> options = select.findElements(By.tagName("option"));
        String[] options1 = new String[options.size()];
        for (WebElement option : options) {
            for (int i = 0; i < options.size(); i++) {
                options1[i] = option.getText();
            }
        }
        return options1;
    }


    public static String[] getSelectOptionsByCss(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement select = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        List<WebElement> options = select.findElements(By.cssSelector("option"));
        String[] options1 = new String[options.size()];
        for (WebElement option : options) {
            for (int i = 0; i < options.size(); i++) {
                options1[i] = option.getText();
            }
        }
        return options1;
    }

    public static String getSelectedValue(WebDriver driver, String identifiertype_identifier)
        throws Exception {

        WebElement svalue1 = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        Select select = new Select(svalue1);
        String value1 = select.getFirstSelectedOption().getText();
        return value1;
    }

    /**
     * utility to check if check box at given xPath is checked
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static boolean isElementSelected(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        if (element.isSelected())
            return true;
        else
            return false;
    }

    /**
     * utility to select check box at given xPath
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static void selectCheckBox(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement check = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        if (!isElementSelected(driver, identifiertype_identifier)) {
            check.click();
            log.info("Checkbox clicked : " + identifiertype_identifier);
        }
    }

    /**
     * utility to deselect check box at given xPath
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static void deselectCheckBox(WebDriver driver, String identifiertype_identifier) {
        try {
            WebElement check = WebdriverWrapper.getElement(driver, identifiertype_identifier);
            if (isElementSelected(driver, identifiertype_identifier)) check.click();
        } catch (Exception e) {
            log.error("DESelect CheckBox thrown Exception");
        }
    }

    /**
     * utility to choose radio button on the page at given xPath
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static void selectRadioButton(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement check = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        if (!isElementSelected(driver, identifiertype_identifier))

        check.click();


    }

    /**********************************************************************************
     * This method is useful for deleting the cookies
     * 
     * @param driver
     **********************************************************************************/
    public void deleteCookie(WebDriver driver) {
        log.info("Deleting the cookies");
        driver.manage().deleteAllCookies();

    }

    /*************************************************************************
     * This method is useful for verifying the presence of specified text
     * 
     * @param driver
     * @param xpath
     * @param(String sTargetText)
     * @throws Exception
     *************************************************************************/
    public static boolean verifyTextPresent(WebDriver driver, String identifiertype_identifier,
        String sTargetText) throws Exception {

        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        if (element.getText().contains(sTargetText)) {
            return true;
        } else {
            log.info("ERROR:: Text::  " + sTargetText + " ::   is not found ");
            return false;
        }

    }


    /************************************************************************************
     * This method is useful for getting the URL of the current browser window
     *
     * @param(driver)
     **********************************************************************************/
    public static String getURL(WebDriver driver) {

        String sURL = driver.getCurrentUrl();
        return sURL;
    }

    public boolean verifyURL(WebDriver driver, String sExpectURL) {
        String sActualURL = getURL(driver);
        if (sActualURL.equalsIgnoreCase(sExpectURL)) {
            log.info("Expected URL::" + sExpectURL + " and actual URL:: " + sActualURL
                + ":: are matching");
            return true;
        } else {
            log.info("Expected URL::" + sExpectURL + " and actual URL:: " + sActualURL
                + " are not matching");
            return false;
        }

    }

    /************************************************************************************
     * This method is used in two ways, based on the second(boolean) parameter passed.
     * 1. To check for the presence of specific text in source
     * 
     * @Param(String sTargetString String, true)
     *               2. To check for non-existence of text in source
     * @Param (String sTargetString, false)
     ************************************************************************************/
    public static void verifyTextInSource(WebDriver driver, String sTargetString,
        boolean checkForExists) {

        String sSourceData = driver.getPageSource();
        int checkpoint = sSourceData.indexOf(sTargetString);
        // Verifying the text in source
        if (checkForExists) {
            if (checkpoint != -1) {
                log.info("Checking for the text :" + sTargetString + "in source and it is found");
            } else {
                log.info("Checking for the text :" + sTargetString
                    + "in source, but it is not found");

            }
        } else {
            if (checkpoint == -1) {
                log.info("Checking for non-existence of the text :" + sTargetString
                    + "in source and it is not found");
            } else {
                log.info("Checking for non-existence of the text :" + sTargetString
                    + "in source, but it is found");
            }

        }
    }

    public static String getPageText(WebDriver driver) {
        return driver.getPageSource();
    }

    /*********************************************************************
     * This method useful to select the popOver.First we need to select the popOver/IFrame before
     * performing any section on it
     *
     * @param(String frameId)
     * @see com.thoughtworks.selenium.DefaultSelenium#selectFrame(java.lang.String)
     *********************************************************************/
    public void selectFrame(WebDriver driver, String sTargetFrameObj) {

        log.info("Selecting the frame::" + sTargetFrameObj);
        driver.switchTo().frame(sTargetFrameObj);
    }

    /*********************************************************************
     * This method is useful to select the specific window when your selenium test opens multiple
     * windows.
     * 
     * @param(String windowID)
     * @see com.thoughtworks.selenium.DefaultSelenium#selectWindow(java.lang.String)
     *****************************************************************/
    public void selectWindow(WebDriver driver, String sWindowId) {
        log.info("Selecting the window ::" + sWindowId);
        driver.switchTo().window(sWindowId);

    }

    /*********************************************************************
     * This method is useful to navigate to a desired page
     * 
     * @throws Exception
     * @param(String windowID)
     * @see com.thoughtworks.selenium.DefaultSelenium#selectWindow(java.lang.String)
     *****************************************************************/
    public static void navigateToPage(WebDriver driver, String... strings) throws Exception {

        for (int i = 0; i < strings.length; i++) {
            System.out.println("Current URL@@@@@@" + driver.getCurrentUrl());
            WebElement element = WebdriverWrapper.getElement(driver, strings[i]);
            System.out.println("Clicking on element: " + element);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            element.click();
            waitForPageToLoad(driver, iGlobalTimeout);
            /*
             * String[] s=strings[i].split("_",2);
             * List <WebElement> list = driver.findElements(By.tagName("a"));
             * System.out.println("Number of links:"+list.size());
             * for(WebElement we : list){
             * System.out.println(we.getAttribute("name"));
             * }
             */

            System.out.println("Navigated to URL: " + getURL(driver));
        }

    }

    public static void navigateToUrl(WebDriver driver, String url) {
        driver.navigate().to(url);
    }

    /**
     * utility to wait for the given object and once the object is visible click on it
     * 
     * This method is useful to click on a specified element
     * of the mouse event relative to the element returned by the locator. *
     * 
     * @throws Exception
     * @see org.openqa.selenium.interactions.Actions.moveToElement(WebElement toElement)
     */
    public static void clickAt(WebDriver driver, String srcidentifiertype_srcidentifier,
        String tgtidentifiertype_tgtidentifier) throws Exception {

        Actions builder = new Actions(driver);
        WebElement elementToClickAt =
            WebdriverWrapper.getElement(driver, srcidentifiertype_srcidentifier);
        builder.moveToElement(elementToClickAt).build().perform();
        waitForObject(driver, tgtidentifiertype_tgtidentifier);
        WebdriverWrapper.getElement(driver, tgtidentifiertype_tgtidentifier).click();
    }

    public static boolean waitForObject(WebDriver driver, String identifiertype_identifier)
        throws Exception {

        for (int i = 0; i < iGlobalTimeout / 2000; i++) {
            if (isObjectPresent(driver, identifiertype_identifier)) {
                return true;
            } else
                ;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        log.info("ERRoR:: Object: " + identifiertype_identifier
            + "not found in the given timeout value::" + iGlobalTimeout);
        return false;
    }

    /*****************************************************************
     * This method is useful for checking for presence of target object
     * 
     * @throws Exception
     * @see org.openqa.selenium.WebElement.isDisplayed()
     ******************************************************************/
    public static boolean isObjectPresent(WebDriver driver, String identifiertype_identifier) {
        boolean result = false;
        try {
            if (WebdriverWrapper.visibiltOfElementByType(driver, identifiertype_identifier) != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            log.info("Following object Not found:::::" + identifiertype_identifier);
        }

        return result;
    }

    /****************************************************************
     * This method waits until the page gets loaded
     * 
     * @see org.openqa.selenium.WebDriver.Timeouts.pageLoadTimeout(long time, TimeUnit unit)
     *****************************************************************/
    public static void waitForPageToLoad(WebDriver driver, int iGlobalTimeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(iGlobalTimeout, TimeUnit.SECONDS);
            // System.out.println("Wait For page load called....");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***************************************************************************
     * 
     * 
     ***************************************************************************/
    public void browserClose(WebDriver driver) {
        waitForPageToLoad(driver, iGlobalTimeout);
        log.info("Calling browser close method");
        driver.close();

    }

    public void browserBack(WebDriver driver) {
        log.info("Calling browser BACK method");
        waitForPageToLoad(driver, iGlobalTimeout);
        driver.navigate().back();

    }

    public static void browserRefresh(WebDriver driver) {
        log.info("Calling browser Regresh method");
        waitForPageToLoad(driver, iGlobalTimeout);
        driver.navigate().refresh();
    }

    /****************************************************************************
     * Gets the value of an input field.
     * 
     * @throws Exception
     * @param(WebDriver,String sTargerObject)
     * @see org.openqa.selenium.WebElement.getAttribute(String name)
     ****************************************************************************/
    public static String getAttribute(WebDriver driver, String sTargerObject, String attType)
        throws Exception {

        WebElement element = WebdriverWrapper.getElement(driver, sTargerObject);
        return element.getAttribute(attType);

    }

    // set the boolean option for this.....
    /*******************************************************************************
     * This method is useful to select the popUp window
     * 
     * @param(WebDriver, String Accept or Decline)
     * @see org.openqa.selenium.WebDriver.switchTo()
     ********************************************************************************/
    public static boolean selectPopUp(WebDriver driver, String actionTobeTaken) {
        try {
            Alert a = new WebDriverWait(driver, 60).until(ExpectedConditions.alertIsPresent());
            if (a != null) {
                log.info("Alert is present");
                if (actionTobeTaken.equalsIgnoreCase("accept"))
                    driver.switchTo().alert().accept();
                else
                    driver.switchTo().alert().dismiss();
                return true;
            } else {
                throw new Throwable();
            }
        } catch (Throwable e) {
            System.err.println("Alert isn't present!!");
            e.printStackTrace();
            return false;
        }
    }


    /*******************************************************************************
     * This method is to check whether a alert/confirmation box is present or not
     * 
     * @param(WebDriver)
     * @see org.openqa.selenium.WebDriver.switchTo().alert()
     ********************************************************************************/
    public static boolean isAlertPresent(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 60);
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*******************************************************************************
     * This method is to check whether a object is displayed or not
     * 
     * @throws Exception
     * @param(WebDriver,String)
     * 
     ********************************************************************************/
    public static boolean isElementDisplayed(WebDriver driver, String identifiertype_identifier)
        throws Exception {

        try {
            if (WebdriverWrapper.getElement(driver, identifiertype_identifier).isDisplayed()) {
                log.info("Object: " + identifiertype_identifier + "not found ");
                return true;

            }
        } catch (Exception e) {
            log.info("Exception :" + e.getMessage());
            return false;
            // TODO Auto-generated catch block

        }

        log.info("ERRoR:: Object: " + identifiertype_identifier
            + "not found in the given timeout value::");
        return false;
    }

    /*****************************************************************
     * This method is useful for checking for presence of target object
     * 
     * @throws Exception
     * @param(Webdriver,String)
     * @see org.openqa.selenium.WebElement.isDisplayed()
     ******************************************************************/
    public static boolean verifyTextPresent(WebDriver driver, String valueText) throws Exception {
        boolean bisElementPresent = false;
        bisElementPresent = driver.getPageSource().contains("valueText");
        return bisElementPresent;

    }

    /*****************************************************************
     * This method is useful for checking for presence of target object
     * 
     * @throws Exception
     * @param(Webdriver)
     * @see org.openqa.selenium.WebElement.isDisplayed()
     ******************************************************************/
    public static void pageRefresh(WebDriver driver) throws Exception {

        driver.navigate().refresh();


    }

    // set the boolean option for this.....
    /*******************************************************************************
     * This method is useful to select the popUp window
     * 
     * @param(WebDriver)
     * @see org.openqa.selenium.WebDriver.switchTo()
     ********************************************************************************/
    public static String getPopUpText(WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        return alert.getText();

    }


    /************************************************************************************
     * This method is used in two ways, based on the second(boolean) parameter passed.
     * 1. To check for the presence of specific text in source
     * 
     * @Param(String sTargetString String, true)
     *               2. To check for non-existence of text in source
     * @Param (String sTargetString, false)
     ************************************************************************************/
    public static boolean isTextInSource(WebDriver driver, String sTargetString) {

        String sSourceData = driver.getPageSource();
        int checkpoint = sSourceData.indexOf(sTargetString);
        // Verifying the text in source
        if (checkpoint != -1) {
            log.info("Checking for the text :" + sTargetString + " in source and it is found");
            return true;
        } else {
            log.info("Checking for the text :" + sTargetString + " in source, but it is not found");
            return false;
        }
    }

    /*******************************************************************************
     * This method is to retrive the value of the cookie
     * 
     * @param(WebDriver,cookiename)
     * 
     ********************************************************************************/
    public static String getCookieByName(WebDriver driver, String cookiename) {
        return driver.manage().getCookieNamed(cookiename).getValue();
    }

    /*******************************************************************************
     * This method is to upload the specified file.
     * 
     * @throws Exception
     * @param(WebDriver,String,String)
     * 
     ********************************************************************************/
    public static void uploadFile(WebDriver driver, String identifiertype_identifier,
        String inputData) throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        element.sendKeys(inputData);
    }

    /*******************************************************************************
     * This method is to verify the text displayed by alert
     * 
     * @param(WebDriver,String)
     * 
     ********************************************************************************/
    public static boolean isAlertTextPresent(WebDriver driver, String alertText) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 60);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            if (alert.getText().contains(alertText)) {

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteAllCookies(WebDriver driver) {
        driver.manage().deleteAllCookies();
        Util.sleep(5000);
    }

    public static void takeScreenshot(WebDriver driver, String fileLocation, String fileName) {
        File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File DestFile = new File(fileLocation + fileName);
        scr.renameTo(DestFile);
    }

    /**
     * utility to check if the given element is enabled or not
     * 
     * @param driver
     * @param xPath
     * @throws Exception
     */
    public static boolean isElementEnabled(WebDriver driver, String identifiertype_identifier)
        throws Exception {
        WebElement element = WebdriverWrapper.getElement(driver, identifiertype_identifier);
        if (element.isEnabled())
            return true;
        else
            return false;
    }

    public static boolean isSelected(WebDriver driver, String xpath) throws Exception {
        return driver.findElement(By.xpath(xpath)).isSelected();
    }

    public static boolean isSelectedByCss(WebDriver driver, String css) throws Exception {
        return driver.findElement(By.cssSelector(css)).isSelected();
    }

    public static void clickUsingXpath(WebDriver driver, String xpath) throws Exception {
        driver.findElement(By.xpath(xpath)).click();
        waitForPageToLoad(driver, iGlobalTimeout);
    }

    public static void clickUsingCss(WebDriver driver, String css) throws Exception {
        driver.findElement(By.cssSelector(css)).click();
        waitForPageToLoad(driver, iGlobalTimeout);
    }

}
