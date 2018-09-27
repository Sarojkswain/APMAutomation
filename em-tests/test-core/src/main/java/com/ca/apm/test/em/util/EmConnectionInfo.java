/*
 * Copyright (c) 2017 CA. All rights reserved.
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
package com.ca.apm.test.em.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;

public class EmConnectionInfo {

    final private String emHostname;
    final private String emUrl;
    final private String emCredential;

    public EmConnectionInfo(EmRole em, ITasResolver tasResolver) {
        this.emHostname = tasResolver.getHostnameById(em.getRoleId());
        this.emUrl = this.emHostname + ":" + Integer.toString(em.getEmPort());
        this.emCredential = null;
    }

    public EmConnectionInfo(String emUrl, String emCredential) {
        String hostname;
        try {
            hostname = new URI(emUrl).getHost();
        } catch (URISyntaxException e) {
            hostname = "localhost";
        }
        this.emHostname = hostname;
        this.emUrl = emUrl;
        this.emCredential = emCredential;
    }
    
    public Map<String, String> fillAgentProperties(Map<String, String> propertyMap) {
        propertyMap.put("agentManager.url.1", emUrl);
        if (emCredential != null) {
            propertyMap.put("agentManager.credential", emCredential);
        }
        return propertyMap;
    }
    
    public String getHostname() {
        return emHostname;
    }
}
