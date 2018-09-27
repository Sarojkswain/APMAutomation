/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed.smokebeta;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * LDAP-related testbed.
 *
 * @author shadm01
 */
public class LDAPMomMachineTestbed implements FLDConstants, FldTestbedProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPMomMachineTestbed.class);

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String INSTALL_DIR = "/home/sw/em/Introscope";

    private ITestbedMachine emMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
            .templateId("w64")
            .bitness(Bitness.b64)
            .build();
        
        return Arrays.asList(emMachine);
    }

    
    private AbstractRole updateRealmsXmlFile(String realmsXmlPath, String ldapHostAndPort) {
        LOGGER.info("Updating realms.xml file with LDAP configuration information");

        XmlModifierFlowContext defaultServerBindAddressContext
                = new XmlModifierFlowContext.Builder(realmsXmlPath)
                .updateNode("/realms/realm[@id='LDAP']/property[@name='url']/value", "LDAP://" + ldapHostAndPort)
                .build();

        return UtilityRole.flow(emMachine.getMachineId() + "_UPDATE_REALMS_XML_FILE_WITH_LDAP_CONFIG",
                XmlModifierFlow.class,
                defaultServerBindAddressContext);
    }

    private AbstractRole updateDomainsXmlFile(String domainXmlFilePath) {
        LOGGER.info("Updating domains.xml file with new LDAP users");

        XmlModifierFlowContext defaultServerBindAddressContext
                = new XmlModifierFlowContext.Builder(domainXmlFilePath)
                .createNodeByXml("/domains/SuperDomain","<grant user=\"LdapAdmin\" permission=\"full\"/>")
                .createNodeByXml("/domains/SuperDomain","<grant user=\"User1\" permission=\"full\"/>")
                .createNodeByXml("/domains/SuperDomain","<grant user=\"User2\" permission=\"full\"/>")
                .createNodeByXml("/domains/SuperDomain","<grant user=\"User3\" permission=\"full\"/>")
                .build();

        return UtilityRole.flow(emMachine.getMachineId() + "_UPDATE_DOMAINS_XML_FILE_WITH_LDAP_USERS",
                XmlModifierFlow.class,
                defaultServerBindAddressContext);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        emMachine.addRole(updateRealmsXmlFile(INSTALL_DIR+"/config/realms.xml", tasResolver.getHostnameById(LDAPAdMachineTestbed.LDAP_MACHINE_ID) + ""));
        emMachine.addRole(updateDomainsXmlFile(INSTALL_DIR+"/config/domains.xml"));
    }

}
