/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.apm.siteminder.testbed;

import com.ca.apm.em.EMSamlConfigurationRole;
import com.ca.apm.siteminder.ConfigureSMFederationRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SiteMinderTestbed for testing APM with external SAML IDP.
 * Imports only EM metadata.
 */

@TestBedDefinition
public class LayeredSamlSiteminderTestbed extends SiteMinderTestbed {

    @Override
    protected ConfigureSMFederationRole getFederationConfigurationRole(ITasResolver tasResolver) {
        ConfigureSMFederationRole configureSMFederationRole =
            new ConfigureSMFederationRole.Builder("smFederation_config_role", tasResolver)
                .createEmFederation()
                .build();
        return configureSMFederationRole;
    }
    
    @Override
    protected EMSamlConfigurationRole getEmSamlConfigurationRole(ITasResolver tasResolver) {
        EMSamlConfigurationRole samlConfigurationRole =
            new EMSamlConfigurationRole.Builder("saml_config_role", tasResolver)
                .enableInternalIdp()
                .build();
        return samlConfigurationRole;
    }
}
