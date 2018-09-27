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
package com.ca.apm.tests.flow;

import java.util.HashMap;

import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * Stores context and configuration to change properties using flow
 * Structure based on other already existing flows.
 * 
 * @author sobar03
 *
 */
/**
 * @author sobar03
 *
 */
public class ChangePropertiesFlowContext implements IFlowContext {


    /**
     * Path to properties file.
     */
    private String propertiesPath = "path";

    /**
     * Boolean that checks if properties that are not found should be added to end of file.
     * By default set to false;
     */
    private boolean addNotExisting = false;

    /**
     * Map with desired props
     */
    private HashMap<String, String> desiredProperties;

    /**
     * Constructor with required properties.
     * 
     * @param desiredProperties
     * @param propertiesPath
     */
    public ChangePropertiesFlowContext(HashMap<String, String> desiredProperties,
        String propertiesPath) {
        this.propertiesPath = propertiesPath;
        this.desiredProperties = desiredProperties;
    }


    /*
     * Setters and getters.
     */

    public void setAddNotExisting(boolean addNotExisting) {
        this.addNotExisting = addNotExisting;
    }

    public boolean isAddNotExisting() {
        return addNotExisting;
    }


    public String getPropertiesPath() {
        return propertiesPath;
    }



    public void setPropertiesPath(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }



    public HashMap<String, String> getDesiredProperties() {
        return desiredProperties;
    }



    public void setDesiredProperties(HashMap<String, String> desiredProperties) {
        this.desiredProperties = desiredProperties;
    }
}
