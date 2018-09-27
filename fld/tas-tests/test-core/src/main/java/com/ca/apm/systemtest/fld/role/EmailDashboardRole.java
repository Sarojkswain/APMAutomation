/**
 *
 */
package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.testbed.ITestbed;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role to send an initial FLD setup configuration via email to configured recipients.
 *
 * @author shadm01
 * 
 */
public class EmailDashboardRole extends AbstractRole implements FLDConstants, FLDLoadConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger(EmailDashboardRole.class);

    private final ITasResolver resolver;
    private final ITestbed testbed;
    private String mailHost;
    private int mailPort =-1;
    private String[] emailAddresses;

    public EmailDashboardRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.mailHost = builder.mailHost;
        if (builder.emailAddresses.size() > 0) {
            this.emailAddresses = builder.emailAddresses.toArray(new String[builder.emailAddresses.size()]);
        } //TODO - throw exception if not

        if (builder.mailPort!=0) {
            this.mailPort = builder.mailPort;
        }
        this.resolver = builder.resolver;
        this.testbed = builder.testbed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
//        runFlow(aaClient, GenericFlow.class);
        EmailRenderer renderer = new EmailRenderer(resolver);
        String emailBody = renderer.generateEmailBody();
        try {
            sendEmail(emailBody);
        } catch (EmailException ex) {
            LOGGER.error("error sending e-mail", ex);
        } catch (MalformedURLException ex) {
            LOGGER.error("error sending e-mail - malformed URL", ex);
        }
    }

    private void sendEmail(final String body) throws EmailException, MalformedURLException {
        LOGGER.info("Sending e-mail with dashboard");
        final HtmlEmail email = new HtmlEmail();

        email.setHostName(mailHost);

        if (mailPort!=-1){
            email.setSmtpPort(mailPort);
        }

        email.addTo(emailAddresses);
        email.setFrom("FLD@ca.com");

        email.setSocketTimeout(60000);
        email.setSocketConnectionTimeout(60000);

        final Date date = Calendar.getInstance().getTime();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String now = sdf.format(date);

        email.setSubject("Test configuration for '" + now + "'");

        email.setHtmlMsg(body);

        email.send();
        LOGGER.info("Sending was sent");
    }

    public static class Builder extends BuilderBase<Builder, EmailDashboardRole> {
        private String roleId;

        private ITestbed testbed;
        private String mailHost = "localhost";
        private int mailPort = 0;
        private ArrayList<String> emailAddresses = new ArrayList<>();

        private ITasResolver resolver;

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }

        public Builder testbedAPM(final ITestbed testbed) {
            this.testbed = testbed;
            return this;
        }
        
        public Builder withMailHost(final String hostname) {
            this.mailHost = hostname;
            return this;
        }

        public Builder withEmailRecieptent(final String emailAddress) {
            this.emailAddresses.add(emailAddress);
            return this;
        }

        public Builder withMailPort(final int mailPort) {
            this.mailPort = mailPort;
            return this;
        }

        @Override
        public EmailDashboardRole build() {
            EmailDashboardRole role = getInstance();
            return role;
        }

        @Override
        protected EmailDashboardRole getInstance() {
            return new EmailDashboardRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    private class MachineWithPorts {
        String machineId;
        String roleId;
        Map<String, String> ports = new TreeMap<>();

        MachineWithPorts(final String machineId, final String roleId) {
            this.machineId = machineId;
            this.roleId = roleId;
        }

        MachineWithPorts(final String machineId, final String roleId, final String... descriptionAndPort) {
            this.machineId = machineId;
            this.roleId = roleId;

            for (int z = 0; z < descriptionAndPort.length - 1; z = z + 2) {
                this.ports.put(descriptionAndPort[z], descriptionAndPort[z + 1]);
            }
        }
    }

    private class EmailRenderer {
        ITasResolver resolver;

        EmailRenderer(ITasResolver resolver) {
            this.resolver = resolver;
        }

        private String generateEmailBody() {
            StringBuilder sb = new StringBuilder();

            sb.append(createStyle());
            sb.append(createMachinesTable());

            return sb.toString();
        }

        private String createStyle() {
            return "<style type=\"text/css\">\n" +
                    "table td, table th {border: 1px solid rgb(191,191,191);}\n" +
                    "a, a:link, a:visited {text-decoration: none;} \n" +
                    "a:hover {text-decoration: underline;}\n" +
                    "h2,h2 a,h2 a:visited,h3,h3 a,h3 a:visited,h4,h5,h6,.t_cht {color:#000 !important}\n" +
                    ".ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td {line-height: 100%}\n" +
                    "</style> \n" +
                    "\n";
        }

        private String createMachinesTable() {
            StringBuilder sb = new StringBuilder();

            sb.append("<table style=\"float: left>\"" +
                    "<thead>" +
                    "<tr>" +
                    "<th>ID</th>" +
                    "<th>Host</th>" +
                    "<th>Other Links</th>" +
                    "</tr>" +
                    "</thead>");

            sb.append(renderMachinesWithCategories());

            sb.append("</table>");

            return sb.toString();
        }

        private String renderMachinesWithCategories() {
            final MachineWithPorts[] memoryMonitor = {
                    new MachineWithPorts(MEMORY_MONITOR_WEBAPP_MACHINE_ID, MEMORY_MONITOR_WEBAPP_ROLE_ID, "Memory Monitor", "8080/memory-monitor/api/memorymonitor/summary/DEMO"),
            };

            final MachineWithPorts[] mainClusterMachines = {
                    new MachineWithPorts(MOM_MACHINE_ID, EM_MOM_ROLE_ID),//, "EM", "8081"),
                    new MachineWithPorts(DATABASE_MACHINE_ID, EM_DATABASE_ROLE_ID),
                    new MachineWithPorts(WEBVIEW_MACHINE_ID, EM_WEBVIEW_ROLE_ID, "WebView", "8080/#home;tr=0"),

                    new MachineWithPorts(COLL01_MACHINE_ID, EM_COLL01_ROLE_ID),
                    new MachineWithPorts(COLL02_MACHINE_ID, EM_COLL02_ROLE_ID),
                    new MachineWithPorts(COLL03_MACHINE_ID, EM_COLL03_ROLE_ID),
                    new MachineWithPorts(COLL04_MACHINE_ID, EM_COLL04_ROLE_ID),
                    new MachineWithPorts(COLL05_MACHINE_ID, EM_COLL05_ROLE_ID),
                    new MachineWithPorts(COLL06_MACHINE_ID, EM_COLL06_ROLE_ID),
                    new MachineWithPorts(COLL07_MACHINE_ID, EM_COLL07_ROLE_ID),
                    new MachineWithPorts(COLL08_MACHINE_ID, EM_COLL08_ROLE_ID),
                    new MachineWithPorts(COLL09_MACHINE_ID, EM_COLL09_ROLE_ID),
                    new MachineWithPorts(COLL10_MACHINE_ID, EM_COLL10_ROLE_ID),

                    new MachineWithPorts(TIM01_MACHINE_ID, TIM01_ROLE_ID),
                    new MachineWithPorts(TIM02_MACHINE_ID, TIM02_ROLE_ID),
                    new MachineWithPorts(TIM03_MACHINE_ID, TIM03_ROLE_ID),
                    new MachineWithPorts(TIM04_MACHINE_ID, TIM04_ROLE_ID),
                    new MachineWithPorts(TIM05_MACHINE_ID, TIM05_ROLE_ID),
            };

            final MachineWithPorts[] secondCluster = {
                    new MachineWithPorts(MOM2_MACHINE_ID, EM_MOM2_ROLE_ID), // "EM", "8081",
                    new MachineWithPorts(WEBVIEW2_MACHINE_ID, EM_MOM2_WEBVIEW_ROLE_ID, "WebView", "8082/#home;tr=0"),
                    new MachineWithPorts(COLL21_MACHINE_ID, EM_COLL21_ROLE_ID),
                    new MachineWithPorts(COLL22_MACHINE_ID, EM_COLL22_ROLE_ID)
            };


            final MachineWithPorts[] agcMachines = {
                    new MachineWithPorts(AGC_MACHINE_ID, AGC_ROLE_ID, "WebView", "8080/#home;tr=0"), //"EM", "8081",
                    new MachineWithPorts(COLL_AGC_MACHINE_ID, AGC_COLL01_ROLE_ID),
            };

            final MachineWithPorts[] loadMachines = {
                    new MachineWithPorts(FLD_CONTROLLER_MACHINE_ID, FLD_CONTROLLER_ROLE_ID, "Load Monitor", "8080/loadmon/"),
                    new MachineWithPorts(TOMCAT_6_MACHINE_ID, TOMCAT_6_ROLE_ID),
                    new MachineWithPorts(TOMCAT_7_MACHINE_ID, TOMCAT_7_ROLE_ID),
                    new MachineWithPorts(TOMCAT_9080_MACHINE_ID, TOMCAT_9080_ROLE_ID),
                    new MachineWithPorts(TOMCAT_9081_MACHINE_ID, TOMCAT_9081_ROLE_ID),

                    new MachineWithPorts(EMLOAD_01_MACHINE_ID, EM_MOM_ROLE_ID), //HVRAgentLoadProvider
                    new MachineWithPorts(ACC_MACHINE_ID, ACC_ROLE_ID), //FLDACCLoadProvider
                    new MachineWithPorts(HAMMOND_MACHINE_ID, HAMMOND_LOAD_ROLE_ID), //FldHammondProvider
                    new MachineWithPorts(DYNAMIC_INSTR_MACHINE_ID, DYNAMIC_INSTR_ROLE_ID), //FLDAgentDynamicInstrumentationProvider
                    new MachineWithPorts(CLW_MACHINE_ID, CLW_ROLE_ID), //FLDCLWLoadProvider
                    new MachineWithPorts(FAKEWS01_MACHINE_ID, FAKEWS01_ROLE_ID), //FLDFakeWorkStationLoadProvider
                    new MachineWithPorts(FAKEWS02_MACHINE_ID, FAKEWS02_ROLE_ID), //FLDFakeWorkStationLoadProvider

                    new MachineWithPorts("APMJdbcQueryMachine", APM_JDBC_QUERY_LOAD_ROLE_ID), //FLDApmJDBCQueryLoadTestbedProvider

                    new MachineWithPorts(REAL_WORKSTATION_01_MACHINE_ID, REAL_WORKSTATION_01_ROLE_ID), //FLDRealWorkstationLoadProvider
                    new MachineWithPorts(REAL_WORKSTATION_02_MACHINE_ID, REAL_WORKSTATION_02_ROLE_ID), //..


                    new MachineWithPorts(WEBVIEW_LOAD_01_MACHINE_ID, "javaRole_" + WEBVIEW_LOAD_01_MACHINE_ID), //WebViewLoadFldTestbedProvider
                    new MachineWithPorts(WEBVIEW_LOAD_02_MACHINE_ID, "javaRole_" + WEBVIEW_LOAD_02_MACHINE_ID),
                    new MachineWithPorts(WEBVIEW_LOAD_03_MACHINE_ID, "javaRole_" + WEBVIEW_LOAD_03_MACHINE_ID),
                    new MachineWithPorts(WEBVIEW_LOAD_04_MACHINE_ID, "javaRole_" + WEBVIEW_LOAD_04_MACHINE_ID),
                    new MachineWithPorts(WEBVIEW_LOAD_05_MACHINE_ID, "javaRole_" + WEBVIEW_LOAD_05_MACHINE_ID),

                    new MachineWithPorts(FLEX_ECHO_WEBAPP_MACHINE_ID, TC_ROLE_ID), //FLDFlexLoadProvider

                    new MachineWithPorts(ENTITY_ALERT_MACHINE_ID, ENTITY_ALERT_ROLE_ID), //FLDEntityAlertLoadProvider

                    new MachineWithPorts(AGENT_SESSION_RECORDING_MACHINE_ID, AGENT_SESSION_RECORDING_ROLE_ID), //FLDAgentRecordingSessionProvider
                    new MachineWithPorts(TIM_SESSION_RECORDING_MACHINE_ID, TIM_SESSION_RECORDING_ROLE_ID), //FLDTIMRecordingSessionProvider
                    new MachineWithPorts(CEM_TESS_LOAD_MACHINE_ID, CEM_TESS_LOAD_ROLE_ID), //FLDCEMTessTestbedProvider
            };

            final MachineWithPorts[] jmeterMachines = {
                    new MachineWithPorts(JMETER_MACHINE_01_ID, JMETER_ROLE_01_ID),
                    new MachineWithPorts(JMETER_MACHINE_02_ID, JMETER_ROLE_02_ID),
                    new MachineWithPorts(JMETER_MACHINE_03_ID, JMETER_ROLE_03_ID),
                    new MachineWithPorts(JMETER_MACHINE_04_ID, JMETER_ROLE_04_ID),
                    new MachineWithPorts(JMETER_MACHINE_ID, JMETER_ROLE_AMF_ID),
            };

            final MachineWithPorts[] weblogicMachines = {
                    new MachineWithPorts(WLS_01_MACHINE_ID, WLS_01_INSTALLATION_ROLE_ID),
                    new MachineWithPorts(WLS_02_MACHINE_ID, WLS_02_INSTALLATION_ROLE_ID),
                    new MachineWithPorts(WLS03_MACHINE_ID, WLS03_ROLE_ID),
                    new MachineWithPorts(WLS04_MACHINE_ID, WLS04_ROLE_ID),
            };

            final MachineWithPorts[] webSpehereMachines = {
                    new MachineWithPorts(WEBSPHERE_01_MACHINE_ID, WEBSPHERE_01_ROLE_ID),
                    new MachineWithPorts(WEBSPHERE_02_MACHINE_ID, WEBSPHERE_02_ROLE_ID),
                    new MachineWithPorts(WEBSPHERE_03_MACHINE_ID, WEBSPHERE_03_ROLE_ID),
            };

            final MachineWithPorts[] JbossMachine = {
                    new MachineWithPorts(JBOSS_MACHINE, JBOSS6_ROLE_ID),
            };
            final MachineWithPorts[] DotNetMachine = {
                    new MachineWithPorts(DOTNET_MACHINE1, DOTNET_MACHINE1 + "_" + DOTNET_AGENT_ROLE_ID),
                    new MachineWithPorts(DOTNET_MACHINE2, DOTNET_MACHINE2 + "_" + DOTNET_AGENT_ROLE_ID),
            };
            
            final MachineWithPorts[] metricSynthMachines = {
                    new MachineWithPorts(METRICSYNTH_01_MACHINE_ID, METRICSYNTH_01_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_02_MACHINE_ID, METRICSYNTH_02_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_03_MACHINE_ID, METRICSYNTH_03_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_04_MACHINE_ID, METRICSYNTH_04_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_05_MACHINE_ID, METRICSYNTH_05_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_06_MACHINE_ID, METRICSYNTH_06_ROLE_ID),
                    new MachineWithPorts(METRICSYNTH_07_MACHINE_ID, METRICSYNTH_07_ROLE_ID),
            };

            final StringBuilder sb = new StringBuilder();

            sb.append(renderCluster("Memory Monitoring", memoryMonitor));
            sb.append(renderCluster("Main Cluster", mainClusterMachines));
            sb.append(renderCluster("Second Cluster", secondCluster));
            sb.append(renderCluster("AGC", agcMachines));
            sb.append(renderCluster("Jmeter", jmeterMachines));
            sb.append(renderCluster("Weblogic", weblogicMachines));
            sb.append(renderCluster("Websphere", webSpehereMachines));
            sb.append(renderCluster("Jboss", JbossMachine));
            sb.append(renderCluster("DotNet", DotNetMachine));
            sb.append(renderCluster("Loads", loadMachines));
            sb.append(renderCluster("Metric Synth", metricSynthMachines));
            
            sb.append(renderJMeterMappingTable("JMeters-TIM-Apps Mapping"));

            return sb.toString();
        }

        private String renderJMeterMappingTable(final String header) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<tbody>");
            sb.append("<tr><td colspan=\"4\"; bgcolor=\"#c6cacd\"><strong>").append(header).append("</strong></td></tr>");
            sb.append("<tr><td bgcolor=\"#99ffff\"><i>JMeter/Source Machine</i></td><td bgcolor=\"#99ffff\"><i>TIM/socat Machine</i></td>"
                + "<td bgcolor=\"#99ffff\"><i>App Server Machine</i></td></tr>");

            Collection<PortForwardingRole> pFwRs = testbed.getRolesByType(PortForwardingRole.class);
            
            Collection<JMeterLoadRole> jmRs = testbed.getRolesByType(JMeterLoadRole.class);
            //put together all roleIDs where mapping start
            List<String> sourceIDs = new ArrayList<String>();
            for (JMeterLoadRole jmR : jmRs) {
                sourceIDs.add(jmR.getRoleId());
            }
            sourceIDs.add(CEM_TESS_LOAD_ROLE_ID);
            sourceIDs.add(JMETER_ROLE_AMF_ID);
            sourceIDs.add(JMETER_ROLE_AMFX_ID);
            
            for (PortForwardingRole pFwR : pFwRs) {
                String roleId = pFwR.getRoleId();
                
                String hostSocatTIM = ResolveHostName(roleId);
                int portSocatTIM = pFwR.getListenPort();
                String hostTarget = pFwR.getTargetIpAddress();
                int portTarget = pFwR.getTargetPort();
                String hostSource = null;
                
                for (String s : sourceIDs) {
                    if (roleId.contains(s)) {
                        hostSource = ResolveHostName(s);
                        break;
                    }
                }
                
                sb.append("<tr>");
                if (hostSource != null) {
                    sb.append("<td>").append(hostSource).append("</td>");
                }
                sb.append("<td>").append(hostSocatTIM).append(":").append(portSocatTIM).append("</td>");
                sb.append("<td>").append(hostTarget).append(":").append(portTarget).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody>");
            return sb.toString();
        }
        
        private String renderCluster(final String header, final MachineWithPorts[] clusterMachines) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<tbody>");
            sb.append("<tr><td colspan=\"4\"; bgcolor=\"#c6cacd\"><strong>").append(header).append("</strong></td></tr>");

            for (final MachineWithPorts machine : clusterMachines) {
                final String host = ResolveHostName(machine.roleId);
                final Map<String, String> ports = machine.ports;

                sb.append("<tr>");
                sb.append("<td>").append(machine.machineId).append("</td>");

                if (!host.isEmpty()) {
                    sb.append("<td>").append(host).append("</a>").append("</td>");

                    for (final String description : ports.keySet()) {
                        String port = ports.get(description);
                        sb.append("<td>").append("<a href=\"").append("http://").append(host).append(":").append(port).append("\">").append(description).append("</a>").append("</td>");
                    }
                }

                sb.append("</tr>");
            }
            sb.append("</tbody>");
            return sb.toString();
        }

        private String ResolveHostName(String machineId) {
            try {
                return resolver.getHostnameById(machineId);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("No mapping found for machine " + machineId);
                return "";
            }

        }

    }
}
