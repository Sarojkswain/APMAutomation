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

package com.ca.tas.agent.entities;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.jetbrains.annotations.NotNull;

/**
 * Contains data gathered from a role discovery
 */
public class RoleData {

    @XmlElementWrapper(name = "dependantComponents")
    @XmlElement(name = "component")
    private Collection<ComponentData> dependantComponents;
    private String discoveryCorrelationId;
    private String version;
    private String identifier;

    /*
        It would be nice to refactor this such that the serialisation/deserialisation is handled automatically
        during marshalling of the XML using registered XmlAdapters.
        I actually created an adapter but I couldn't use it as this class is in automation-agent-api and
        EmRoleBuilderCreator is in automation-core.   Automation-core is dependent on automation-agent-api.
     */
    //@XmlJavaTypeAdapter(EmRoleBuilderCreatorAdapter.class)
    @XmlElement(name = "emRoleBuilder")
    //private EmRoleBuilderCreator emRoleBuilderCreator;
    private String emRoleBuilderCreator;

    @XmlElementWrapper(name = "runtimeArgs")
    @XmlElement(name = "arg")
    private Collection<String> runtimeArgs;
    private String pid;
    private String installDir;
    private String type;

    @XmlElement(name = "oSName")
    private String oSName;

    @XmlElement(name = "oSVersion")
    private String oSVersion;
    
    private Integer port;

    public RoleData() {}

    public RoleData(final ComponentMetadata componentMetadata,
                    final String emRoleBuilderCreator,
                    final Collection<ComponentData> dependantComponents,
                    final ProcessData processData,
                    final String discoveryCorrelationId,
                    final String type,
                    final String version,
                    final Integer port,
                    final String oSName,
                    final String oSVersion) {
        this.dependantComponents = dependantComponents;
        this.discoveryCorrelationId = discoveryCorrelationId;
        this.version = version;
        this.identifier = componentMetadata.getComponentId();
        this.oSName = oSName;
        this.oSVersion = oSVersion;
        this.runtimeArgs = processData.getRuntimeArgs();
        this.pid = processData.getPid();
        this.installDir = componentMetadata.getInstallDir();
        this.type = type;
        this.emRoleBuilderCreator = emRoleBuilderCreator;
        this.port = port;
    }

    @NotNull
    public String getInstallDir() {
        return this.installDir;
    }

    @NotNull
    public String getPid() {
        return this.pid;
    }

    @NotNull
    public String getIdentifier() {
        return this.identifier;
    }

    @NotNull
    public String getEmRoleBuilderCreator() {
        return emRoleBuilderCreator;
    }

    @NotNull
    public Collection<String> getRuntimeArgs() {
        return this.runtimeArgs;
    }

    @NotNull
    public Collection<ComponentData> getDependantComponents() {
        return this.dependantComponents;
    }

    @NotNull
    public String getDiscoveryCorrelationId() {
        return this.discoveryCorrelationId;
    }

    @NotNull
    public String getType() {
        return this.type;
    }

    @NotNull
    public Integer getPort() {
        return this.port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    @NotNull
    public String getVersion() {
        return this.version;
    }

    public String getOSName() {
        return oSName;
    }

    public String getOSVersion() {
        return oSVersion;
    }

    public void setDependantComponents(Collection<ComponentData> dependantComponents) {
        this.dependantComponents = dependantComponents;
    }

    public void setDiscoveryCorrelationId(String discoveryCorrelationId) {
        this.discoveryCorrelationId = discoveryCorrelationId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEmRoleBuilderCreator(String emRoleBuilderCreator) {
        this.emRoleBuilderCreator = emRoleBuilderCreator;
    }

    public void setRuntimeArgs(Collection<String> runtimeArgs) {
        this.runtimeArgs = runtimeArgs;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static void main(String[] args) {

    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoleData{");
        sb.append("dependantComponents=").append(dependantComponents);
        if (dependantComponents != null) {
            sb.append(Arrays.toString(dependantComponents.toArray()));
        }
        sb.append(", discoveryCorrelationId='").append(discoveryCorrelationId).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", emRoleBuilderCreator='").append(emRoleBuilderCreator).append('\'');
        sb.append(", runtimeArgs=").append(runtimeArgs);
        sb.append(", pid='").append(pid).append('\'');
        sb.append(", installDir='").append(installDir).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", oSName='").append(oSName).append('\'');
        sb.append(", oSVersion='").append(oSVersion).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}
