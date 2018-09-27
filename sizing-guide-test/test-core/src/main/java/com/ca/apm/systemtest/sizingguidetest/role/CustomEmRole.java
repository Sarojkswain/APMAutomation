package com.ca.apm.systemtest.sizingguidetest.role;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;

public class CustomEmRole extends EmRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEmRole.class);

    protected CustomEmRole(CustomEmRoleBuilder builder) {
        super(builder);
    }

    public static class CustomEmRoleLinuxBuilder extends EmRole.LinuxBuilder {
        private String eulaUrl = FakeEulaRole.LinuxBuilder.DEFAULT_LINUX_EULA_URL;

        public CustomEmRoleLinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected void initFlowContext() {
            assert introscopeArtifact != null : "Missing introscope artifact";
            assert eulaArtifact != null : "Missing EULA artifact";

            flowContextBuilder.introscopeVersion(introscopeArtifact.getVersion());
            flowContextBuilder.introscopeUrl(tasResolver.getArtifactUrl(introscopeArtifact));

            boolean useOsgi = shouldUseOsgi(introscopeArtifact.getVersion());

            if (useOsgi) {
                assert osgiDistributionArtifact != null : "Missing OSGI dist artifact";
                assert osgiDistPlatform != null : "Missing OSGI platform";
                flowContextBuilder.osgiDistData(osgiDistributionArtifact.getVersion(),
                    osgiDistPlatform.toString().toLowerCase());
                flowContextBuilder.osgiUrl(tasResolver.getArtifactUrl(osgiDistributionArtifact));
            } else {
                flowContextBuilder.dontUseOsgi();
            }

            flowContextBuilder.eulaVersion(eulaArtifact.getVersion());
            try {
                flowContextBuilder.eulaUrl(new URL(eulaUrl)); // set a fake eula from filesystem
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            deployFlowContext = flowContextBuilder.build();
            LOGGER
                .info(
                    "CustomEmRole.CustomEmRoleLinuxBuilder.initFlowContext():: deployFlowContext.eulaArtifactUrl = {}",
                    deployFlowContext.getEulaArtifactUrl());
            LOGGER.info(
                "CustomEmRole.CustomEmRoleLinuxBuilder.initFlowContext():: deployFlowContext = {}",
                deployFlowContext);
        }

        public Builder eulaUrl(String eulaUrl) {
            Args.notNull(eulaUrl, "eulaUrl");
            this.eulaUrl = eulaUrl;
            return builder();
        }
    }

    public static class CustomEmRoleBuilder extends EmRole.Builder {
        private String eulaUrl = FakeEulaRole.Builder.DEFAULT_WINDOWS_EULA_URL;

        public CustomEmRoleBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected void initFlowContext() {
            assert introscopeArtifact != null : "Missing introscope artifact";
            assert eulaArtifact != null : "Missing EULA artifact";

            flowContextBuilder.introscopeVersion(introscopeArtifact.getVersion());
            flowContextBuilder.introscopeUrl(tasResolver.getArtifactUrl(introscopeArtifact));

            boolean useOsgi = shouldUseOsgi(introscopeArtifact.getVersion());

            if (useOsgi) {
                assert osgiDistributionArtifact != null : "Missing OSGI dist artifact";
                assert osgiDistPlatform != null : "Missing OSGI platform";
                flowContextBuilder.osgiDistData(osgiDistributionArtifact.getVersion(),
                    osgiDistPlatform.toString().toLowerCase());
                flowContextBuilder.osgiUrl(tasResolver.getArtifactUrl(osgiDistributionArtifact));
            } else {
                flowContextBuilder.dontUseOsgi();
            }

            flowContextBuilder.eulaVersion(eulaArtifact.getVersion());
            try {
                flowContextBuilder.eulaUrl(new URL(eulaUrl)); // set a fake eula from filesystem
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            deployFlowContext = flowContextBuilder.build();
            LOGGER
                .info(
                    "CustomEmRole.CustomEmRoleBuilder.initFlowContext():: deployFlowContext.eulaArtifactUrl = {}",
                    deployFlowContext.getEulaArtifactUrl());
            LOGGER.info(
                "CustomEmRole.CustomEmRoleBuilder.initFlowContext():: deployFlowContext = {}",
                deployFlowContext);
        }

        public Builder eulaUrl(String eulaUrl) {
            Args.notNull(eulaUrl, "eulaUrl");
            this.eulaUrl = eulaUrl;
            return builder();
        }
    }

}
