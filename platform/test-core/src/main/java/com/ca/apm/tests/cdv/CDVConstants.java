/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * 
 * Author : KETSW01
 */
package com.ca.apm.tests.cdv;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;



public class CDVConstants  {

    public static final String CDV_MACHINE_ID = "cdvMachine";
    public static final String EM_MACHINE_ID = "emMachine";
    public static final String MOM1_MACHINE_ID = "mom1Machine";
    public static final String MOM2_MACHINE_ID = "mom2Machine";

    public static final String CDV_ROLE_ID = "cdvRole";
    public static final String CDV1_ROLE_ID = "cdv1Role";
    public static final String CDV2_ROLE_ID = "cdv2Role";
    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String MOM1_ROLE_ID = "mom1Role";
    public static final String MOM2_ROLE_ID = "mom2Role";
    public static final String STANDALONE_EM_ROLE_ID = "standaloneEmRole";
    public static final String MOM1_COL1_ROLE_ID = "mom1Col1Role";
    public static final String MOM1_COL2_ROLE_ID = "mom1Col2Role";
    public static final String MOM2_COL1_ROLE_ID = "mom2Col1Role";
    public static final String MOM2_COL2_ROLE_ID = "mom2Col2Role";

    public static final String EM_TEMPLATE_ID_WIN = TEMPLATE_W64;
    public static final String EM_TEMPLATE_ID_LINUX = TEMPLATE_CO66;

    public static final String AGENT_MACHINE_ID = "agentMachine";
    public static final String AGENT_MACHINE_TEMPLATE_ID_WIN = TEMPLATE_W64;
    public static final String AGENT_MACHINE_TEMPLATE_ID_LINUX = TEMPLATE_CO66;

    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";

    public static final String JBOSS_ROLE_ID = "jbossRole";
    public static final String JBOSS_AGENT_ROLE_ID = "jbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJbossRole";

    public static final String CDV_HTTPS_PORT = "8444";
    public static final String MOM1_HTTPS_PORT = "8444";
    public static final String MOM2_HTTPS_PORT = "8444";
    public static final String MOM1COL1_HTTPS_PORT = "8445";
    public static final String MOM1COL2_HTTPS_PORT = "8446";
    public static final String MOM2COL1_HTTPS_PORT = "8447";
    public static final String MOM2COL2_HTTPS_PORT = "8448";

    public static final String CDV_SSL_PORT = "5443";
    public static final String MOM1_SSL_PORT = "5443";
    public static final String MOM2_SSL_PORT = "5443";
    public static final String MOM1COL1_SSL_PORT = "5444";
    public static final String MOM1COL2_SSL_PORT = "5445";
    public static final String MOM2COL1_SSL_PORT = "5446";
    public static final String MOM2COL2_SSL_PORT = "5447";

}
