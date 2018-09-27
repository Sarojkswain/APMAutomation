/*
 * Copyright (c) 2015 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.test.atc.common;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Actions for zooming on the map
 * You need to click on an element in the map first
 * so the 'document.activeElement' is set.
 */

public class Zoom {

    WebDriver driver;
    
    public Zoom(WebDriver driver){
        this.driver = driver;
    }
    
    /* script to be executed in the browser */
    public static final String WHEEL_SCRIPT =
      "window.BrowserWheel = function(zoom) {" +
      "  var wheel = zoom > -1 ? 120 : -120;" +
      "  var view = document.activeElement;" +
      "  var repeat = Math.abs(zoom);" +
      "  for (var i = 0; i < repeat; i++) {" +
      "    var evt = document.createEvent(\"MouseEvents\");" +
      "    evt.initMouseEvent(" +
      "        \"DOMMouseScroll\"," +
      "        true," +
      "        true," +
      "        window," +
      "        wheel," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        0," +
      "        null" +
      "    );" +
      "    view.dispatchEvent(evt);" +
      "  };" +
      "};";

    public void zoomIt (int zoom) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript(WHEEL_SCRIPT);
        jse.executeScript("BrowserWheel(" + zoom + ");");
    
    };



    /**
     * Zoom in the map
     * 
     * @param {number} [level] - number of levels to zoom in
     * @throws InterruptedException
     */
    public void zoomIn(Integer level) {
        if (level == null) {
            level = 1;
        }
        zoomIt(level * -6);
        Utils.sleep(1000);
    }

    /**
     * Zoom out the map
     * 
     * @param {number} [level] - number of levels to zoom out
     * @throws InterruptedException
     */
    public void zoomOut(Integer level) {
        if (level == null) {
            level = 1;
        }
        zoomIt(level * 6);
        Utils.sleep(1000);
    }
    
}
