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
package com.ca.apm.classes.from.appmap.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the vertex of any topology information. The vertex with respect to AppMap domain
 * describes the physical software component or logical element participating in communication among
 * physical components.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vertex {

    public static final String INTERNAL_PREFIX = "internal.";

    /**
     * NOTE: try to avoid any changes in attribute names as they are referenced from
     * <i>appmap-config.properties</i> configuration file!
     * 
     * @author kopji03
     *
     */
    public static enum MainAttribute {

        ATTRIBUTE_NAME_STATUS("status"),

        // location is not a gathered attribute (it's either decorated or custom)
        ATTRIBUTE_NAME_LOCATION("location", false),

        ATTRIBUTE_NAME_HOST_NAME("hostname", true, true),
        
        ATTRIBUTE_NAME_PORT("port", true, true), 
        
        ATTRIBUTE_NAME_HOST_DOMAIN("domain", true, true),

        // application name; if there are multiple instances of an app, they all share the same name
        ATTRIBUTE_NAME_APPLICATION_NAME("applicationName"),
        ATTRIBUTE_ALIAS_APPLICATION_NAME("Application"),
      
        ATTRIBUTE_NAME_NAME("name"),

        ATTRIBUTE_NAME_TYPE("type"),

        // location is not a gathered attribute (it's either decorated or custom)
        ATTRIBUTE_NAME_OWNER("owner", false),

        // location is not a gathered attribute (it's either decorated or custom)
        ATTRIBUTE_NAME_TIER("tier", false),
        
        // just a placeholder for name of the service on the BusinessTransaction.getServiceId()
        // method
        ATTRIBUTE_SERVICE_ID("serviceId"),
        // just a placeholder for name of the service on the BusinessTransaction.getServiceId()
        // method
        ATTRIBUTE_SERVICE_ID_AS_ATTRIBUTE("serviceIdAsAttribute"),
        // UI will remap serviceId to Business Service
        ATTRIBUTE_ALIAS_BUSINESS_SERVICE("Business Service"),
        
        // MOM attribute name, e.g. used in Filter
        ATTRIBUTE_MOM("Source cluster"),
        
        // the identifier of Business Transaction defined in CEM
        ATTRIBUTE_NAME_TRANSACTION_ID("transactionId"),
        // the identifier of Business Transaction defined in CEM
        ATTRIBUTE_NAME_TRANSACTION_EXTERNAL_ID("transactionExternalId"),
        // the identifier of Business Transaction defined in CEM
        ATTRIBUTE_NAME_EXPERIENCE("Experience"),
        ATTRIBUTE_NAME_IS_EXPERIENCE("IsExperience"),
        // UI will remap transactionId to Business Transaction
        // This is an alias
        ATTRIBUTE_ALIAS_NAME_BUSINESS_TRANSACTION("Business Transaction"),
        
        ATTRIBUTE_BUSINESS_TRANSACTION_NAME("transactionName"),
        // MAA related information
        ATTRIBUTE_BUSINESS_TRANSACTION_TYPE("transactionType"),
        ATTRIBUTE_DEVICE_OPERATION_SYSTEM("deviceOS", true, false),
        ATTRIBUTE_DEVICE_OPERATION_SYSTEM_VERSION("deviceOSVersion", true, true),
        ATTRIBUTE_CONNECTION_PROVIDER("connectionProvider", true, true),
        
        // Array of agents associated with this node
        ATTRIBUTE_NAME_AGENT("agent", true, false),
        ATTRIBUTE_NAME_VIRTUAL_AGENT("virtualAgent", true, false),
        ATTRIBUTE_NAME_AGENT_CONNECTED_TIME("agentConnectedTime"),
        ATTRIBUTE_NAME_AGENT_DISCONNECTED_TIME("agentDisconnectedTime"),
        ATTRIBUTE_NAME_AGENT_IS_CONNECTED("agentIsConnected"),
        
        ATTRIBUTE_NAME_COLLECTOR("collector"),
        
        // Database provider
        ATTRIBUTE_NAME_PROVIDER("provider"),

        // Servlet specific
        ATTRIBUTE_NAME_SERVLET_CLASSNAME("servletClassname"),
        ATTRIBUTE_NAME_SERVLET_METHODNAME("servletMethod"),
        
        // WS SOAP specific
        ATTRIBUTE_NAME_WS_NAMESPACE("wsNamespace"),
        ATTRIBUTE_NAME_WS_OPERATION_NAME("wsOperation"),
        ATTRIBUTE_NAME_BACKEND_NAME("backendName"),

        // Socket specific
        ATTRIBUTE_NAME_SOCKET_HOST_NAME("socketHostname", true, true),
        ATTRIBUTE_NAME_SOCKET_PORT("socketPort", true, true),
        
        // Business transaction property
        ATTRIBUTE_END_USER("endUser"),

        // Transaction sequence ID property
        ATTRIBUTE_NAME_SEQUENCE_ID("sequenceIds"),
        // Transaction sequence ID checkpoint
        ATTRIBUTE_NAME_SEQUENCE_CHECKPOINT("sequenceCheckpoint"),
        
        ATTRIBUTE_NAME_INTERNAL_IS_USER_DEFINED_BT(INTERNAL_PREFIX + "isUserDefinedBT"),
        ATTRIBUTE_NAME_AGENT_DOMAIN("agentDomain"),
        ATTRIBUTE_NAME_AGENT_PROCESS("agentProcess"),

        ATTRIBUTE_NAME_REMOTE_SERVER_NAME("remoteName", true, true),
        ATTRIBUTE_NAME_REMOTE_ADDRESS("remoteAddress", true, true),
        ATTRIBUTE_NAME_REMOTE_PORT("remotePort", true, true),

        ATTRIBUTE_NAME_LOCAL_ADDRESS("localAddress", true, true),
        
        ATTRIBUTE_NAME_IP_ADDRESS("ipAddress", true, true),
        
        ATTRIBUTE_NAME_BACKEND_NODE("backendNode"),
        ATTRIBUTE_NAME_INFERRED_BACKEND_NODE("inferredBackendNode"),
        ATTRIBUTE_NAME_SEMANTIC("semantic"),
        
        ATTRIBUTE_NAME_DOCKER_NODE("docker_node"),
        ATTRIBUTE_NAME_OPENSHIFT_NODE("ose_pod_nodename"),
        ATTRIBUTE_NAME_KUBERNETES_NODE("k8s_pod_nodename"),
        // AWS/EC2 attributes
        ATTRIBUTE_NAME_AWS_REGION("AWS_Region", false),
        ATTRIBUTE_NAME_AWS_AVAILABILITY_ZONE("AWS_Availability_Zone", false),
        ATTRIBUTE_NAME_EC2_INSTANCEID("EC2_InstanceID", false),

        // Jenkins attributes
        ATTRIBUTE_NAME_JENKINS_BUILD_NUMBER("Build Number", false),
        ATTRIBUTE_NAME_JENKINS_BUILD_STATUS("Build Status", false),

        //RDI attributes
        ATTRIBUTE_RDI_AVAILABLE_EXTENSIONS("availableExtensions"),

        // JMS attributes
        ATTRIBUTE_NAME_QUEUE_NAME("Queue Name", true, true),
        ATTRIBUTE_NAME_TOPIC_NAME("Topic Name", true, true)

        ;

        private final String name;
        private final String nameLowered;
        private final boolean gathered;
        private final boolean array;
        private static Map<String, MainAttribute> mapping = new HashMap<String, MainAttribute>();
        private static Set<String> gatheredAttributeNames;

        static {
            Set<String> ga = new HashSet<String>();
            for (MainAttribute a : values()) {
                mapping.put(a.getName(), a);
                mapping.put(a.getNameLowered(), a);
                if (a.isGathered()) {
                    ga.add(a.getName());
                }
            }
            gatheredAttributeNames = Collections.unmodifiableSet(ga);
        }
        
        private MainAttribute(String name) {
            this(name, true);
        }

        private MainAttribute(String name, boolean gathered) {
            this(name, gathered, false);
        }
        
        private MainAttribute(String name, boolean gathered, boolean array) {
            this.name = name;
            this.nameLowered = name.toLowerCase(Locale.US);
            this.gathered = gathered;
            this.array = array;
        }

        public String getName() {
            return name;
        }

        public static MainAttribute findAttribute(String name) {
            return mapping.get(name);
        }
        
        public static Set<String> getGatheredAttributeNames() {
            return gatheredAttributeNames;
        }

        public static boolean supportsArray(String name) {
            return true;
            /*MainAttribute a = mapping.get(name);
            // We want all custom attributes to support arrays
            return a == null || a.supportsArray();*/
        }
        
        public boolean isGathered() {
            return gathered;
        }

        public boolean supportsArray() {
            return true;
            //return array;
        }

        public String getNameLowered() {
            return nameLowered;
        }
    }
}
