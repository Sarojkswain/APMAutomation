/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */
package com.ca.apm.siteminder;


/**
 * @author surma04
 *
 */
public enum ServletExecVersion {
    v60ax32win("6.0");

    private String version;
    private String extension = "exe";
    private static final String SERVLET_EXEC = "ServletExec_AS";
    private static final String SERVLET_FOLDER = "servletexec";

    /**
     * 
     */
    private ServletExecVersion(final String version) {
        this.version = version;
    }

    /**
     * @return
     */
    public String getFilename() {
        // ServletExec_AS_60a
        return String.format("%s_%sa.%s", SERVLET_EXEC, this.version.replaceAll("\\.", ""), this.extension);
    }
    
    public String getFolderName() {
        // TODO win only
        return String.format("%s\\%s-%s\\%s\\", SERVLET_FOLDER, SERVLET_FOLDER, this.version, "win32");
    }
}
