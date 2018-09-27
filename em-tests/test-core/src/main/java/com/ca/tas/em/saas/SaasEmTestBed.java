/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

package com.ca.tas.em.saas;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.artifact.built.docker.ApmAllInDockerImage;
import com.ca.tas.artifact.built.docker.ApmSaasDockerImage;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.role.revert.RevertRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.tests.annotations.TestBedDynamicField;
import com.google.common.net.HostAndPort;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * SaasEmTestBed
 *
 * @author Jan Pojer (pojja01@ca.com)
 */
@TestBedDefinition
public class SaasEmTestBed implements ITestbedFactory {
    public static final String EM_MACHINE_ID = "SaasEmMachine";
    public static final String APM_ROLE_ID = "apm-saas-role";
    public static final String DEFAULT_AUTH_USER = "hanz";
    public static final String DEFAULT_AUTH_PASS = "pass";
    
    private static final String EM_MACHINE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO7;

    private static final String PARAM_MEMORY_LIMIT_EM = "memLimitEm";
    private static final String PARAM_MEMORY_LIMIT_TRADESERVICE = "memLimitTradeService";
    private static final String PARAM_MEMMORY_LIMIT_JMETER = "memLimitJmeter";
    private static final String PARAM_AUTH_USER = "authUser";
    private static final String PARAM_AUTH_PASS = "authPass";
    private static final String PARAM_MAX_HEAP = "maxHeapEm";
    private static final String PARAM_ACC_HOST = "accHost";
    private static final String PARAM_ACC_TOKEN = "accToken";
    private static final String PARAM_APM_TOKEN = "apmToken";
    private static final String PARAM_APM_AGENT_TOKEN = "apmAgentToken";
    private static final String PARAM_APM_DEBUG = "apmDebug";
    private static final String PARAM_DOCKER_HOSTNAME = "hostname";
    private static final String PARAM_DOCKER_COMMAND_PROPERTIES = "commandProperties";
    private static final String PARAM_APM_IMAGE = "apmImage";
    private static final String PARAM_APM_WEBVIEW_MONITOR_IP = "agentWebViewMonitorIp";
    private static final String PARAM_APM_WEBVIEW_MONITOR_PORT = "agentWebViewMonitorPort";

    @TestBedDynamicField(PARAM_MEMORY_LIMIT_EM)
    private String memLimitEm = "2000";
    @TestBedDynamicField(PARAM_MEMORY_LIMIT_TRADESERVICE)
    private String memLimitTradeService = "1000";
    @TestBedDynamicField(PARAM_MEMMORY_LIMIT_JMETER)
    private String memLimitJmeter = "300";
    @TestBedDynamicField(PARAM_AUTH_USER)
    private String user = DEFAULT_AUTH_USER;
    @TestBedDynamicField(PARAM_MAX_HEAP)
    private String maxHeapEm = "2048";
    @TestBedDynamicField(PARAM_AUTH_PASS)
    private String pass = DEFAULT_AUTH_PASS;
    @TestBedDynamicField(PARAM_DOCKER_HOSTNAME)
    private String hostname = StringUtils.EMPTY;
    @TestBedDynamicField(PARAM_DOCKER_COMMAND_PROPERTIES)
    private Set<String> commands = new HashSet<>(Collections.singletonList(
            "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast=30"
    ));
    @TestBedDynamicField(PARAM_ACC_HOST)
    private String accHost = StringUtils.EMPTY;
    @TestBedDynamicField(PARAM_ACC_TOKEN)
    private String accToken = StringUtils.EMPTY;
    @TestBedDynamicField(PARAM_APM_TOKEN)
    private String apmToken = StringUtils.EMPTY;
    @TestBedDynamicField(PARAM_APM_AGENT_TOKEN)
    private String apmAgentToken = StringUtils.EMPTY;
    @TestBedDynamicField(PARAM_APM_IMAGE)
    private String apmImage = "apm-saas";
    private boolean apmSaas = true;

    @TestBedDynamicField(PARAM_APM_WEBVIEW_MONITOR_IP)
    private String agentWebViewMonitorIp = "dummy";

    @TestBedDynamicField(PARAM_APM_WEBVIEW_MONITOR_PORT)
    private Integer agentWebViewMonitorPort = 8888;

    @TestBedDynamicField(PARAM_APM_DEBUG)
    private boolean apmDebug = false;

    private boolean doRevert = true;
    
    private Collection<String> customConfigData = null;

    public SaasEmTestBed() {
    }

    public SaasEmTestBed(String elasticHost) {
        apmToken = RoleUtility.ADMIN_AUX_TOKEN;
        apmSaas = false;
        doRevert = false;
        if (elasticHost != null) {
            customConfigData = Arrays.asList(
                "com.ca.apm.ttstore=es",
                "ca.apm.ttstore.elastic.url=http://" + elasticHost + ":9200",
                "ca.apm.ttstore.elastic.index.init=true");
        }
    }

    @Override
    public ITestbed create(final ITasResolver tasResolver) {

        final Collection<IRole> extraRoles = new ArrayList<IRole>();
        
        final DockerRole.LinuxBuilder emRoleBuilder = new DockerRole.LinuxBuilder(APM_ROLE_ID)
                .registry(detectArtifactory(tasResolver))
                .image(apmSaas ? ApmSaasDockerImage.fromDefaultVersion(tasResolver)
                               : ApmAllInDockerImage.fromDefaultVersion(tasResolver))
                .commands(new ArrayList<>(this.commands))
                //.property(DockerProperty.MEMORY, this.memLimitEm)
                .hostname(this.hostname.isEmpty() ? StringUtils.EMPTY : this.hostname)
                .env("HEAP_XMX_EM", this.maxHeapEm)
                .env("APM_USER_NAME", this.user)
                .env("APM_USER_PASSWORD", this.pass)
                .env("ACC_TOKEN", this.accToken)
                .env("ACC_HOST_IP", this.accHost)
                .env("APM_TOKEN", this.apmToken.isEmpty() ? null : this.apmToken)
                .env("APM_AGENT_TOKEN", this.apmAgentToken.isEmpty() ? null : this.apmAgentToken)
                .env("DEBUG_ENABLED", this.apmDebug ? "true" : null)
                .env("APM_LOG_LEVEL_EM", "DEBUG")
                .port(5001, 5001)
                .port(8081, 8081)
                .port(8082, 8080)
                .volume("/root/logs", "/opt/ca/logs");

        if (customConfigData != null) {
            emRoleBuilder.volume("/root/custom-config",
                apmSaas ? "/home/1010/custom-config" : "/home/apm/custom-config");
            
        }
        if (this.apmDebug) {
            emRoleBuilder
                    .env("DEBUG_ENABLED", this.apmDebug ? "true" : null)
                    .port(9009, 9009);
        }

        final DockerRole emRole = emRoleBuilder.build();

        if (customConfigData != null) {
            FileCreatorFlowContext customConfixContext = new FileCreatorFlowContext.Builder()
                    .fromData(customConfigData)
                    .destinationPath("/root/custom-config/IntroscopeEnterpriseManager.properties")
                    .build();
            IRole customConfigRole = new UniversalRole.Builder("custom_config", tasResolver)
                    .runFlow(FileCreatorFlow.class, customConfixContext).build();
            customConfigRole.before(emRole);
            extraRoles.add(customConfigRole);
        }

        final DockerRole tradeServicesRole = new DockerRole.LinuxBuilder("tradeservices-role")
                .registry(detectArtifactory(tasResolver))
                .image("apm-tradeservice-tomcat8" + (apmSaas ? "-saas" : ""))
                .version(tasResolver.getDefaultVersion())
                .hostname("tradeservice-app")
                //.property(DockerProperty.MEMORY, this.memLimitTradeService)
                .env("AGENT_TOKEN", this.apmAgentToken.isEmpty() ? null : this.apmAgentToken)
                .env("BROWSER_AGENT_DISABLED", "true")
                .env("AGENT_NAME", "CA APM Demo Agent - Tomcat")
                .port(7080, 7080)
                .link(emRole, "docker-em")
                .build();

        final DockerRole jMeterRole = new DockerRole.LinuxBuilder("jmeter-role")
                .registry(detectArtifactory(tasResolver))
                .image("jmeter-client" + (apmSaas ? "-saas" : ""))
                .version(tasResolver.getDefaultVersion())
                .link(tradeServicesRole, "tradeservice-app")
                //.property(DockerProperty.MEMORY, this.memLimitJmeter)
                .build();

        final ITestbedMachine emMachine = TestBedUtils.createLinuxMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID)
                .addRole(emRole, tradeServicesRole, jMeterRole).addRoles(extraRoles);

        final ITestbed testbed = new Testbed(SaasEmTestBed.class.getSimpleName())
                .addMachine(emMachine);

        //revert machine
        if (doRevert) {
            new RevertRole.Builder(testbed, tasResolver).build();
        }

        return testbed;
    }

    private HostAndPort detectArtifactory(final ITasResolver tasResolver) {
        final String regionalArtifactory = tasResolver.getRegionalArtifactory().toString();

        return !StringUtils.isEmpty(regionalArtifactory) && regionalArtifactory.contains("oerth-scx") ? HostAndPort.fromParts("oerth-scx.ca.com", 4443) : HostAndPort.fromParts(
                "artifactory-emea-cz.ca.com", 4443);
    }
}
