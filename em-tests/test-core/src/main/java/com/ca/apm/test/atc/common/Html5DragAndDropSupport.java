package com.ca.apm.test.atc.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Execute a HTML5 drag and drop operation in a Selenium test. 
 */
public class Html5DragAndDropSupport {
    
    private static final Logger logger = Logger.getLogger(Html5DragAndDropSupport.class);
    
    private static String dndJsFileContent = null;
    
    static {
        try {
            ClassLoader loader = FilterBy.class.getClassLoader();
            InputStream jsFileStream = loader.getResourceAsStream("dnd.js");
            BufferedReader jsFileReader = new BufferedReader(new InputStreamReader(jsFileStream));
            StringBuilder jsFileContent = new StringBuilder();
            String line;
            
            while ((line = jsFileReader.readLine()) != null) {
                jsFileContent.append(line);
            }
            
            dndJsFileContent = jsFileContent.toString();
        } catch (Exception e) {
            logger.error("The file dnd.js could not be read due to: " + e.toString());
        }
    }
       
    public static void performDragAndDrop(WebDriver driver, String dragElementSelector, String dropElementSelector) throws Exception {
        if ((dndJsFileContent != null) && (driver instanceof JavascriptExecutor)) {
            ((JavascriptExecutor)driver).executeScript(dndJsFileContent + "$('" + dragElementSelector + "').simulateDragDrop({ dropTarget: '" + dropElementSelector + "'});");
        }
    }
}
