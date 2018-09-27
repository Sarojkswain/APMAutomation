/**
 * 
 */
package com.ca.apm.systemtest.fld.test.loads;

import java.util.HashSet;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;

/**
 * @author keyja01
 *
 */
public class MetricSynthLoadTest extends BaseFldLoadTest implements FLDConstants {
    private static class LoadConfig {
        private String msRole;
        private String emRole;
        private String city;
        
        /**
         * @param emRole
         * @param city
         */
        public LoadConfig(String msRole, String emRole, String city) {
            this.msRole = msRole;
            this.emRole = emRole;
            this.city = city;
        }
    }
    
    private LoadConfig[][] configs = new LoadConfig[][] {
        new LoadConfig[] {
                          new LoadConfig(METRICSYNTH_01_ROLE_ID, EM_COLL01_ROLE_ID, "praha"), 
                          new LoadConfig(METRICSYNTH_01_ROLE_ID, EM_COLL01_ROLE_ID, "amsterdam"), 
                          new LoadConfig(METRICSYNTH_01_ROLE_ID, EM_COLL02_ROLE_ID, "copenhagen"), 
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_02_ROLE_ID, EM_COLL02_ROLE_ID, "tallinn"),
                             new LoadConfig(METRICSYNTH_02_ROLE_ID, EM_COLL03_ROLE_ID, "paris"), 
                             new LoadConfig(METRICSYNTH_02_ROLE_ID, EM_COLL03_ROLE_ID, "helsinki"), 
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_03_ROLE_ID, EM_COLL04_ROLE_ID, "tbilisi"),
                             new LoadConfig(METRICSYNTH_03_ROLE_ID, EM_COLL04_ROLE_ID, "athens"), 
                             new LoadConfig(METRICSYNTH_03_ROLE_ID, EM_COLL05_ROLE_ID, "berlin"), 
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_04_ROLE_ID, EM_COLL05_ROLE_ID, "budapest"), 
                             new LoadConfig(METRICSYNTH_04_ROLE_ID, EM_COLL06_ROLE_ID, "bucharest"), 
                             new LoadConfig(METRICSYNTH_04_ROLE_ID, EM_COLL06_ROLE_ID, "vilnius"), 
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_05_ROLE_ID, EM_COLL07_ROLE_ID, "warsaw"), 
                             new LoadConfig(METRICSYNTH_05_ROLE_ID, EM_COLL08_ROLE_ID, "monaco"), 
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_06_ROLE_ID, EM_COLL08_ROLE_ID, "baku"),
                             new LoadConfig(METRICSYNTH_06_ROLE_ID, EM_COLL09_ROLE_ID, "bratislava"), 
                             new LoadConfig(METRICSYNTH_06_ROLE_ID, EM_COLL09_ROLE_ID, "vaduz"),
        }, new LoadConfig[] {
                             new LoadConfig(METRICSYNTH_07_ROLE_ID, EM_COLL10_ROLE_ID, "belgrade"), 
                             new LoadConfig(METRICSYNTH_07_ROLE_ID, EM_COLL10_ROLE_ID, "bern")
        }
    };
    
    private boolean shouldStop;

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        return "metric-synth";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        
        for (LoadConfig[] list: configs) {
            final LoadConfig[] cfgList = list;
            Thread th = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    HashSet<String> msHosts = new HashSet<>();
                    for (LoadConfig cfg: cfgList) {
                        if (shouldStop) {
                            return;
                        }
                        logger.info("Starting metric synth load for " + cfg.city);
                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        String postUrlFmt = "http://%s:8080/loadScenario?scenarioId=basic&emHost=%s&params=city:%s";
                        String emHost = envProperties.getMachineHostnameByRoleId(cfg.emRole);
                        String msHost = envProperties.getMachineHostnameByRoleId(cfg.msRole);
                        msHosts.add(msHost);
                        String postUrl = String.format(postUrlFmt, msHost, emHost, cfg.city);
                        HttpPost post = new HttpPost(postUrl);
                        CloseableHttpResponse resp = null;
                        
                        try {
//                            resp = httpClient.execute(post);
                            logger.info("Successfully started metric synth load for " + cfg.city);
                        } catch (Exception e) {
                            logger.warn("Unable to start metric synth load for " + cfg.city, e);
                        } finally {
                            try {
                                resp.close();
                            } catch (Exception e) {
                            }
                        }
                        
                    }
                    
                    for (String msHost: msHosts) {
                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        String postUrlFmt = "http://%s:8080/agent/start?delay=2000";
                        String postUrl = String.format(postUrlFmt, msHost);
                        HttpPost post = new HttpPost(postUrl);
                        CloseableHttpResponse resp = null;
                        
                        try {
                            resp = httpClient.execute(post);
                        } catch (Exception e) {
                            System.out.println("Unable to start agents on metric synth host: " + msHost);
                            e.printStackTrace();
                        } finally {
                            try {
                                resp.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            });
            th.start();
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        shouldStop = true;
        
        //TODO - as soon as the stop functionality is 
    }

}
