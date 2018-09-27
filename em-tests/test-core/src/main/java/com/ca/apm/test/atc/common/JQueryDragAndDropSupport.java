package com.ca.apm.test.atc.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Execute a drag and drop operation with elements of JQuery Sortable in a Selenium test.
 */
public class JQueryDragAndDropSupport {

    private static final Logger logger = Logger.getLogger(JQueryDragAndDropSupport.class);

    private static String dndJsFileContent = null;

    static {
        try {
            ClassLoader loader = FilterBy.class.getClassLoader();
            InputStream jsFileStream = loader.getResourceAsStream("dndJQuery.js");
            BufferedReader jsFileReader = new BufferedReader(new InputStreamReader(jsFileStream));
            StringBuilder jsFileContent = new StringBuilder();
            String line;

            while ((line = jsFileReader.readLine()) != null) {
                jsFileContent.append(line);
            }

            dndJsFileContent = jsFileContent.toString();
        } catch (Exception e) {
            logger.error("The file dndJQuery.js could not be read due to: " + e.toString());
        }
    }

    public static void performDragAndDrop(WebDriver driver, String dragElementCssSelector, 
        String dndHandleCssSelector, String dropElementCssSelector) throws Exception {
        if ((dndJsFileContent != null) && (driver instanceof JavascriptExecutor)) {
            ((JavascriptExecutor) driver).executeScript(dndJsFileContent 
                + "$('" + dragElementCssSelector + "').simulateDragSortable('"
                    + dndHandleCssSelector + "','" + dropElementCssSelector + "');");
        }
    }
}
