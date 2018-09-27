/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.apm.tests.utils;

import java.io.File;

/**
 * Class for storing EM information and directories.
 * 
 * 
 * @author KATRA03
 *
 */
public class EmConfiguration {

    private String installPath;
    private int emPort;
    private String apmSqlClientDir;

    /**
     * Default constructor.
     * 
     * @author KATRA03
     */

    public EmConfiguration(String installPath, int port,String apmSqlClientDir)

    {
        this.installPath = installPath;

        this.emPort = port;
        this.apmSqlClientDir = apmSqlClientDir;
    }

    // Setters and getters
    public int getEmPort() {
        return emPort;
    }

    public void setEmPort(int emPort) {
        this.emPort = emPort;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public String getClwPath() {
        return installPath + File.separator + "lib" + File.separator + "CLWorkstation.jar";
    }

    public String getJDBCDriverPath() {
        
        return installPath + File.separator + "lib" + File.separator + "IntroscopeJDBC.jar";
    }
    
    public String getJDBCTeiidDriverPath() {
        System.out.println(apmSqlClientDir);
        return installPath + File.separator + "APMSqlServer"+File.separator+ "client" + File.separator + "teiid-9.0.1-jdbc.jar";
        
    }

    public String getPropertiesPath() {
        return installPath + File.separator + "config" + File.separator
            + "IntroscopeEnterpriseManager.properties";
    }

    /**
     * Path to em\config\ . Always end with file separator
     */
    public String getConfigDirPath() {
        return installPath + File.separator + "config" + File.separator;
    }

    public String getLogPath() {
        return installPath + File.separator + "logs" + File.separator
            + "IntroscopeEnterpriseManager.log";
    }
}
