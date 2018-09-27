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
 */
package com.ca.apm.automation.action.flow.em.config;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * FlowContext for modifying domains.xml
 * <p/>
 * Specified domains will be added above the existing domains.
 * <p/>
 * If any of the existing domains are specified, the old domain is replaced with the specifiers and
 * permissions in the specified domain
 */
public class ModifyDomainsXmlFlowContext implements IFlowContext {

    private final String domainsXMLFilePath;
    private final Map<String, Domain> domainMap;

    public ModifyDomainsXmlFlowContext(Builder builder) {
        this.domainMap = builder.domainMap;
        this.domainsXMLFilePath = builder.emBase + builder.configDirectory + builder.domainsXML;
    }

    public String getDomainsXMLFilePath() {
        return domainsXMLFilePath;
    }

    public Map<String, Domain> getDomainMap() {
        return domainMap;
    }

    public static class LinuxBuilder extends Builder {

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }


    public static class Builder extends BuilderBase<Builder, ModifyDomainsXmlFlowContext> {

        Map<String, Domain> domainMap = new LinkedHashMap<>();

        String emBase = getDeployBase() + getPathSeparator() + "em" + getPathSeparator();
        String configDirectory = "config" + getPathSeparator();
        String domainsXML = "domains.xml";

        @Override
        protected ModifyDomainsXmlFlowContext getInstance() {
            return build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ModifyDomainsXmlFlowContext build() {
            return new ModifyDomainsXmlFlowContext(this);
        }

        /**
         * Specify a domain object to be added/replaced in the domains.xml file
         *
         * @param domain
         * @return
         */
        public Builder domain(Domain domain) {
            domainMap.put(domain.getName(), domain);
            return this;
        }

        public Builder emBase(String emBaseDir) {
            if (!emBaseDir.endsWith(getPathSeparator())) {
                emBaseDir = emBaseDir + getPathSeparator();
            }
            emBase = emBaseDir;
            return this;
        }

        public Builder configDir(String configDirectory) {
            if (!configDirectory.endsWith(getPathSeparator())) {
                configDirectory = configDirectory + getPathSeparator();
            }
            this.configDirectory = configDirectory;
            return this;
        }

        public Builder domainsXMLFile(String fileName) {
            domainsXML = fileName;
            return this;
        }
    }


    public static class Domain {
        private final String name;
        private Set<String> agentSpecifiers = new HashSet<>();
        private Set<Grant> grants = new HashSet<>();

        public Domain(String name) {
            this.name = name;
        }

        public void addAgentSpecifier(String specifier) {
            agentSpecifiers.add(specifier);
        }

        public void addGrant(Grant grant) {
            grants.add(grant);
        }

        public String getName() {
            return name;
        }

        public Set<String> getAgentSpecifiers() {
            return agentSpecifiers;
        }

        public Set<Grant> getGrants() {
            return grants;
        }
    }


    public static class Grant {
        public enum Principal {
            USER, GROUP
        };

        private final Principal type;
        private final String name;
        private final String permission;

        public Grant(Principal type, String name, String permission) {
            this.type = type;
            this.name = name;
            this.permission = permission;
        }

        public Principal getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getPermission() {
            return permission;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Grant grant = (Grant) o;

            if (type != grant.type) {
                return false;
            }
            if (!name.equals(grant.name)) {
                return false;
            }
            return permission.equals(grant.permission);

        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + permission.hashCode();
            return result;
        }
    }

}
