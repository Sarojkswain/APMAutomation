/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.tas.flow.tess.TessUI;
import com.ca.tas.flow.tess.TessUI.WebServerFilterConfig;
import com.ca.tas.flow.tess.reports.CorrelationalSlaReport;
import com.ca.tas.flow.tess.reports.ImpactLeadersReport;
import com.ca.tas.flow.tess.reports.StatsDataCSVReport;
import com.ca.tas.flow.tess.reports.TessReportConfiguration;
import com.ca.tas.flow.tess.reports.TimeFrame;
import com.ca.tas.flow.tess.reports.TransactionCountReport;
import com.ca.tas.flow.tess.reports.TransactionDefectReport;
import com.ca.tas.flow.tess.reports.TransactionPerformanceReport;
import com.ca.tas.flow.tess.reports.TransactionQualityReport;
import com.ca.tas.flow.tess.reports.TransactionTimeReport;
import com.ca.tas.flow.tess.reports.UserSlaPerformanceReport;
import com.ca.tas.flow.tess.reports.UserSlaQualityReport;

/**
 * Configures TESS for the FLD
 * @author keyja01
 *
 */
@Flow
public class ConfigureTessFlow extends FlowBase {
    private enum ReportType {
        ImpactLeadersReport, CorrelationalSlaReport, StatsDataCSVReport, TransactionCountReport,
        TransactionDefectReport, TransactionPerformanceReport, TransactionQualityReport,
        TransactionTimeReport, UserSlaPerformanceReport, UserSlaQualityReport
    }
    
    private class ConfigurationStep {
        private Runnable runnable;
        private String name;
        private int retryCount = 3;
        
        private ConfigurationStep(String name, Runnable runnable) {
            this.runnable = runnable;
            this.name = name;
        }
    }
    
    @SuppressWarnings("serial")
    private class ConfigurationException extends RuntimeException {

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    private static final Logger log = LoggerFactory.getLogger(ConfigureTessFlow.class);
    
    @FlowContext
    private ConfigureTessFlowContext ctx;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        //wait 60s so cluster can start
        Thread.sleep(60 * 1000);
        
        final TessUI tess = createTessUI();
        try {
            runConfig(tess);
        } finally {
            tess.close();
        }
    }

    private void runConfig(final TessUI tess) throws Exception {
        ArrayList<ConfigurationStep> steps = new ArrayList<>();
        
        //delays less than 3000 milliseconds are somewhat not sufficient for "Transaction Performance" 
        //report setup 
        tess.setDelay(2000L);
        tess.login();
        
        if (ctx.removeOldWebServerFilters) {
            steps.add(new ConfigurationStep("Delete all web server filters", new Runnable() {
                @Override
                public void run() {
                    tess.deleteAllWebServerFilters();
                }
            }));
        }
        
        if (ctx.removeOldTims) {
            steps.add(new ConfigurationStep("Delete all web server filters", new Runnable() {
                @Override
                public void run() {
                    tess.deleteAllWebServerFilters();
                }
            }));
            steps.add(new ConfigurationStep("Delete all tims", new Runnable() {
                @Override
                public void run() {
                    tess.deleteAllTims();
                }
            }));
        }
        
        for (String tim: ctx.tims) {
            final String name = tim;
            steps.add(new ConfigurationStep("Configure new TIM: " + name, new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress addr = InetAddress.getByName(name);
                        tess.configureNewTim(addr.getCanonicalHostName(), false);
                    } catch (Exception e) {
                        throw new ConfigurationException("Unable to configure new TIM: " + name, e);
                    }
                }
            }));
        }
        
        for (Entry<TessService, String> entry: ctx.tessServiceMap.entrySet()) {
            final TessService svc = entry.getKey();
            final String emHost = entry.getValue();
            steps.add(new ConfigurationStep("Configure TESS service: " + svc + " --> " + emHost, new Runnable() {
                @Override
                public void run() {
                    try {
                        tess.configureTessService(svc, emHost);
                    } catch (IOException e) {
                        throw new ConfigurationException("Unable to configure TESS service: " + svc + ", host: " + emHost, e);
                    }
                }
            }));
        }
        
        for (Entry<String, TessWebServerFilterConfig> entry: ctx.tessWebServerFilters.entrySet()) {
            final TessWebServerFilterConfig tessCfg = entry.getValue();
            final String serviceName = entry.getKey();
            final WebServerFilterConfig cfg = new WebServerFilterConfig();
            steps.add(new ConfigurationStep("Configure web server filter", new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress addr = InetAddress.getByName(tessCfg.getAppServerHostname());
                        cfg.fromIpAddress = addr.getHostAddress();
                        cfg.toIpAddress = addr.getHostAddress();
                        cfg.port = tessCfg.getAppServerPort();
                        cfg.serviceName = serviceName;
                        cfg.timName = fdqn(tessCfg.getTimName());
                        tess.configureWebServerFilter(cfg);
                    } catch (IOException e) {
                        throw new ConfigurationException("Unable to configure web server filter: " + cfg, e);
                    }
                }
            }));
        }
        
        for (String tim: ctx.tims) {
            final String name = tim;
            steps.add(new ConfigurationStep("Enable tim: " + name, new Runnable() {
                @Override
                public void run() {
                    try {
                        String fdqnTim = fdqn(name);
                        log.info("Enabling TIM " + fdqnTim);
                        tess.enableTim(fdqnTim);
                    } catch (IOException e) {
                        throw new ConfigurationException("Unable to enable TIM: " + name, e);
                    }
                }
            }));
        }

        if (ctx.smtpHost != null) {
	        steps.add(new ConfigurationStep("Configure SMTP host", new Runnable () {
	        	@Override
	        	public void run() {
	        		try {
	        			tess.configureSMTP(ctx.smtpHost);
	        		} catch (Exception e) {
	        			throw new ConfigurationException("Unable to set SMTP host to: " + ctx.smtpHost, e);
	        		}
	        	}
	        }));
        }        
        steps.add(new ConfigurationStep("Delete all reports", new Runnable() {
            @Override
            public void run() {
                tess.deleteAllReports();
            }
        }));
        for (ReportType t: ReportType.values()) {
            final ReportType type = t;
            steps.add(new ConfigurationStep("Configure report " + t, new Runnable() {
                @Override
                public void run() {
                    log.info("Configuring report of type " + type);
                    TessReportConfiguration r = report(type);
                    log.info("Configuring report: " + r);
                    tess.configureReport(r);
                }
            }));
        }
        
        log.debug("Executing TESS configuration steps");
        for (ConfigurationStep step: steps) {
            while (step.retryCount > 0) {
                step.retryCount--;
                try {
                    log.info(step.name);
                    step.runnable.run();
                    break;
                } catch (Exception e) {
                    log.warn("Exception executing step, " + step.retryCount + " retries remaining", e);
                    Thread.sleep(10000L);
                }
            }
        }
        
    }

    private TessReportConfiguration report(ReportType type) {
        TessReportConfiguration cfg = null;
        switch (type) {
            case CorrelationalSlaReport:
                cfg = new CorrelationalSlaReport("SLA Report", "Correlational SLA Report", TimeFrame.Yesterday);
                break;
            case ImpactLeadersReport:
                cfg = new ImpactLeadersReport("Impact Leaders Report", "Schedule Impact Leaders", TimeFrame.Yesterday);
                break;
            case StatsDataCSVReport:
                cfg = new StatsDataCSVReport("Stats Data", "Stats Data Report");
                break;
            case TransactionCountReport:
                cfg = new TransactionCountReport("Transaction Count", "Transaction Count Report", TimeFrame.Yesterday);
                break;
            case TransactionDefectReport:
                cfg = new TransactionDefectReport("Transaction Defect", "Transaction Defect Report", TimeFrame.Yesterday);
                break;
            case TransactionPerformanceReport:
                cfg = new TransactionPerformanceReport("Transaction Performance", "Transaction Performance Report", TimeFrame.Yesterday);
                break;
            case TransactionQualityReport:
                cfg = new TransactionQualityReport("Transaction Quality", "Transaction Quality Report", TimeFrame.LastWeek);
                break;
            case TransactionTimeReport:
                cfg = new TransactionTimeReport("Transaction Time", "Transaction Time Report", TimeFrame.LastWeek);
                break;
            case UserSlaPerformanceReport:
                cfg = new UserSlaPerformanceReport("User SLA Performance", "User SLA Performance Report", TimeFrame.LastWeek);
                break;
            case UserSlaQualityReport:
                cfg = new UserSlaQualityReport("User SLA Quality", "User SLA Quality Report", TimeFrame.Yesterday);
                break;
        }
        cfg.schedule.fromAddress = "fld@ca.com";
        cfg.schedule.toAddress = ctx.reportEmail;
        return cfg;
    }

    private TessUI createTessUI() throws MalformedURLException {
    	TessUI tessUI = null;
    	if (ctx.seleniumGridHubHostAndPort != null) {
    		switch (ctx.preferredBrowser) {
    			default:	
    			case Firefox:
    				tessUI = TessUI.createTessUIForFirefoxRemoteWebDriver(ctx.tessHostname, ctx.tessPort, 
    						ctx.tessUser, ctx.tessPassword, ctx.seleniumGridHubHostAndPort);
    				break;
    			case Chrome:
    				tessUI = TessUI.createTessUIForChromeRemoteWebDriver(ctx.tessHostname, ctx.tessPort,
    						ctx.tessUser, ctx.tessPassword, ctx.seleniumGridHubHostAndPort);
    				break;
    			case IE:
    				tessUI = TessUI.createTessUIForIERemoteWebDriver(ctx.tessHostname, ctx.tessPort,
    						ctx.tessUser, ctx.tessPassword, ctx.seleniumGridHubHostAndPort);
    				break;
    		}
    		return tessUI;
    	}
    	
		switch (ctx.preferredBrowser) {
			default:	
			case Firefox:
				tessUI = TessUI.createTessUIForFirefoxWebDriver(ctx.tessHostname, ctx.tessPort, 
						ctx.tessUser, ctx.tessPassword);
				break;
			case Chrome:
				tessUI = TessUI.createTessUIForChromeWebDriver(ctx.tessHostname, ctx.tessPort,
						ctx.tessUser, ctx.tessPassword);
				break;
			case IE:
				tessUI = TessUI.createTessUIForIEWebDriver(ctx.tessHostname, ctx.tessPort,
						ctx.tessUser, ctx.tessPassword);
				break;

		}
	
		return tessUI;
    }
    
    private String fdqn(String hostname) throws IOException {
        InetAddress addr = InetAddress.getByName(hostname);
        return addr.getCanonicalHostName();
    }

}
