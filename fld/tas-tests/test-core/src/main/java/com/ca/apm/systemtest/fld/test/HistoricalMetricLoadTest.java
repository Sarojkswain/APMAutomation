/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ca.apm.automation.utils.EnvironmentPropertyUtils;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.test.DockerFlowRunner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.util.FileCopyUtils;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.systemtest.fld.flow.agent.ReconfigureAgentFlow;
import com.ca.apm.systemtest.fld.flow.agent.ReconfigureAgentFlowContext;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.tas.role.webapp.TomcatRole;

/**
 * Used to generate historical metrics using the tesstest web application.  It first modifies the mom's loadbalancing.xml
 * config file to ensure that the metrics will be created on collector #7.
 * It generates metrics by hitting some URLs, then shuts down tomcat.  After shutting down, it renames the agent in the configuration, and restarts tomcat.  
 * Rinse, lather, and repeat until 29500000 historical metrics have been reached.   
 * 
 * @author keyja01
 *
 */
public class HistoricalMetricLoadTest extends BaseFldLoadTest {
    public static final int MAX_HISTORIC = 29500000;
    public static final String SOAP_XML = "/com/ca/apm/systemtest/fld/testbed/devel/metric-data-soap.xml";
    
    private class LoadThread implements Runnable {
        private String collector;
        private String mom;
        private int nextId = 1;
        private boolean loadbalancingXmlModified = false;

        private LoadThread(String mom, String collector) {
            this.mom = mom;
            this.collector = collector;
        }
        
        @Override
        public void run() {
            if (!loadbalancingXmlModified) {
                String configDir = envProperties.getRolePropertyById(FLDConstants.EM_MOM_ROLE_ID, "emConfigDir");
                
                String collector7 = envProperties.getMachineHostnameByRoleId(FLDConstants.EM_COLL07_ROLE_ID);
                String xml = "<agent-collector name=\"HistoricalMetrics\">"
                    + "<agent-specifier>HisoricalMetrics\\|Tomcat\\|TomcatDM.*</agent-specifier>"
                    + "<include><collector host=\"" + collector7 + "\" port=\"5001\" /></include>"
                    + "</agent-collector>";
                
                String filename = configDir;
                if (!configDir.endsWith("/") && !configDir.endsWith("\\")) {
                    filename = filename + "/";
                }
                filename = filename + "loadbalancing.xml";
                XmlModifierFlowContext ctx = new XmlModifierFlowContext.Builder(filename)
                    .createNodeByXml("/loadbalancing", xml)
                    .build();
                runFlowByMachineId(FLDConstants.MOM_MACHINE_ID, XmlModifierFlow.class, ctx, TimeUnit.MINUTES, 5);

                if (FLDConfigurationService.getConfig().isDockerMode()) {

                    DockerFlowRunner runner = new DockerFlowRunner(new EnvironmentPropertyUtils(envProperties), aaClient);
                    runner.exec(FLDConstants.EM_MOM_ROLE_ID, Collections.singletonList(Arrays.asList(
                            "/bin/cp", "-rf", "/opt/ca/custom-config/loadbalancing.xml", "/opt/ca/apm/config/loadbalancing.xml"
                    )));
                }

                loadbalancingXmlModified = true;
                
                shortWait(60000L);
            }
            
            
            int numMetrics = 0;
            while (!done && (numMetrics = checkHistoricalMetricCount()) < MAX_HISTORIC) {
                logger.info("Historical metric count: " + numMetrics);
                
                RunCommandFlowContext cmd = deserializeCommandFlowFromRole(HISTORICAL_METRICS_TOMCAT_ROLE_ID, TomcatRole.ENV_TOMCAT_START);
                logger.debug("Starting tomcat for historical metrics load");
                runCommandFlowByMachineId(envProperties.getMachineIdByRoleId(HISTORICAL_METRICS_TOMCAT_ROLE_ID), cmd);
                
                shortWait(30000L);
                logger.trace("About to fetch URLs from tesstest");
                hitUrls(envProperties.getMachineHostnameByRoleId(HISTORICAL_METRICS_TOMCAT_ROLE_ID));
                
                shortWait(60000L);
                
                logger.trace("Updating agent name");
                updateAgent();
                
                logger.trace("Stopping tomcat for historical metrics load");
                cmd = deserializeCommandFlowFromRole(HISTORICAL_METRICS_TOMCAT_ROLE_ID, TomcatRole.ENV_TOMCAT_STOP);
                runCommandFlowByMachineId(envProperties.getMachineIdByRoleId(HISTORICAL_METRICS_TOMCAT_ROLE_ID), cmd);
                
            }
        }
        
        private void updateAgent() {
            String id = String.format("TomcatDM-%05d", ++nextId);
            String tomcatDir = envProperties.getRolePropertyById(HISTORICAL_METRICS_TOMCAT_ROLE_ID, "tomcatInstallDir");
            String wilyDir = tomcatDir + "\\wily";
            String confDir = wilyDir + "\\core\\config";
            String introscopeProps = confDir + "\\IntroscopeAgent.profile";
            
            Map<String, String> replacePairs = new HashMap<String, String>();
            replacePairs.put("introscope.agent.agentName", id);
            
            
            ReconfigureAgentFlowContext ctx = ReconfigureAgentFlowContext.getBuilder()
                .agentConfigFile(introscopeProps)
                .agentName(id)
                .build();
            runFlowByMachineId(ACC_MACHINE_ID, ReconfigureAgentFlow.class, ctx);
            
        }
        
        private void hitUrls(String hostname) {
            for (int i = 0; i < 1500; i++) {
                String id = String.format("%03d", i);
                String url = "http://" + hostname + ":8080/tesstest/webapp/foo?option=buy&user=andy" + id 
                    + "&callback=http://" + hostname + ":8080/tesstest/webapp/bar/" + id;
                logger.trace("Fetching url: " + url);
                fetchUrl(url);
            }
        }
        
        private void fetchUrl(String url) {
            CloseableHttpClient hc = null;
            HttpGet req = new HttpGet(url);
            
            try {
                hc = HttpClients.createDefault();
                CloseableHttpResponse resp = hc.execute(req);
                OutputStream out = new ByteArrayOutputStream();
                resp.getEntity().writeTo(out);
                // System.out.println(out.toString());
                resp.close();
                hc.close();
            } catch (Exception e) {
                logger.error("Exception while fetching URL: " + e.getMessage());
            }
        }
        
        private void shortWait(long ms) {
            synchronized (this) {
                try {
                    this.wait(ms);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

        private int checkHistoricalMetricCount() {
            try {
                CloseableHttpClient hc = null;
                HttpPost post = new HttpPost("http://" + mom + ":8081/introscope-web-services/services/MetricsDataService");
                String xml = FileCopyUtils.copyToString(new InputStreamReader(HistoricalMetricLoadTest.class.getResourceAsStream(SOAP_XML)));
                String coll = collector;
                if (coll.contains(".")) {
                    coll = coll.substring(0, coll.indexOf("."));
                }
                xml = xml.replace("%%AGENT_REGEXP%%", ".*" + coll + ".*");
                xml = xml.replace("%%METRIC%%", "Enterprise Manager\\|Connections:Number of Historical Metrics");
                System.out.println("Using xml:\n" + xml);
                
                CredentialsProvider cp = new BasicCredentialsProvider();
                AuthScope authscope = new AuthScope(mom, 8081);
                Credentials credentials = new UsernamePasswordCredentials("cemadmin", "quality");
                cp.setCredentials(authscope, credentials);
                
                StringEntity entity = new StringEntity(xml, ContentType.TEXT_XML);
                post.setEntity(entity);
                hc = HttpClients.createDefault();
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                HttpHost host = new HttpHost(mom, 8081);
                authCache.put(host, basicAuth);
                
                HttpClientContext httpCtx = HttpClientContext.create();
                httpCtx.setAuthCache(authCache);
                httpCtx.setCredentialsProvider(cp);;
                
                post.addHeader("SOAPAction", "\"\"");
                
                CloseableHttpResponse resp = hc.execute(host, post, httpCtx);
                if (resp.getStatusLine().getStatusCode() == 200) {
                    HttpEntity respEntity = resp.getEntity();
                    SAXBuilder saxBuilder = new SAXBuilder();
                    Document doc = saxBuilder.build(respEntity.getContent());
                    Element root = doc.getRootElement();
                    
                    XPathFactory xpathFact = XPathFactory.instance();
                    Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    Namespace apm = Namespace.getNamespace("apm", "urn:ca.wily.introscope.webservices");
                    XPathExpression<Element> xp = xpathFact.compile("//multiRef", Filters.element(), null, xsi, apm);
                    List<Element> list = xp.evaluate(root);
                    for (Element e: list) {
                        String val = e.getAttributeValue("type", xsi);
                        if (val.endsWith(":MetricData")) {
                            Element mvElement = e.getChild("metricValue");
                            int mv = Integer.parseInt(mvElement.getValue());
                            return mv;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception while checking historical metric count", e);
                e.printStackTrace();
            }
            
            return 0;
        }

    }
    
    private boolean done = false;

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        return "historical-metrics";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        done = false;
        String mom = envProperties.getMachineHostnameByRoleId(FLDConstants.EM_MOM_ROLE_ID);
        String collector = envProperties.getMachineHostnameByRoleId(FLDConstants.EM_COLL07_ROLE_ID);
        Thread thread = new Thread(new LoadThread(mom, collector));
        thread.start();
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        done = true;
    }

}
