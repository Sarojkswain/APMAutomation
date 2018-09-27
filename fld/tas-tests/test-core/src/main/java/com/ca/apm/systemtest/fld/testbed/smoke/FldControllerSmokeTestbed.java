/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ca.apm.systemtest.fld.testbed.loads.FldControllerLoadProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.LogMonitorConfigurationSource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class FldControllerSmokeTestbed implements ITestbedFactory {

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed tb = new Testbed("FldControllerSmokeTestbed");
        
        FldControllerLoadProvider fldController = new FldControllerLoadProvider(getConig());
        tb.addMachines(fldController.initMachines());
        fldController.initTestbed(tb, tasResolver);
        
        return tb;
    }

    private FLDConfiguration getConig() {
        return new FLDConfiguration() {
            
            @Override
            public boolean isSkipBackup() {
                return false;
            }
            
            @Override
            public boolean isDockerMode() {
                return false;
            }
            
            @Override
            public LogMonitorConfigurationSource getWebViewLogMonitorConfiguration() {
                return null;
            }
            
            @Override
            public String getTessSmtpHost() {
                return null;
            }
            
            @Override
            public String getReportEmail() {
                return null;
            }
            
            @Override
            public LogMonitorConfigurationSource getMomLogMonitorConfiguration() {
                return null;
            }
            
            @Override
            public Map<String, String> getMachineTemplateOverrides() {
                return null;
            }
            
            @Override
            public String[] getLogMonitorEmail() {
                return null;
            }
            
            @Override
            public String getFldConfigSmtpHost() {
                return null;
            }
            
            @Override
            public String getEmVersion() {
                return null;
            }
            
            @Override
            public String getDomainConfigVersion() {
                return null;
            }
            
            @Override
            public String getDbTargetReleaseVersion() {
                return null;
            }
            
            @Override
            public LogMonitorConfigurationSource getCollectorLogMonitorConfiguration(String collector) {
                return null;
            }
            
            @Override
            public String getBackupUser() {
                return null;
            }
            
            @Override
            public String getBackupPassword() {
                return null;
            }
            
            @Override
            public String getBackupHost() {
                return null;
            }

            @Override
            public String getAgent2EmConnectionType() {
                return null;
            }

            @Override
            public boolean isEmNeedClientAuth() {
                return false;
            }

            @Override
            public boolean isWeblogicDockerDeploy() {
                return false;
            }

            @Override
            public boolean isWebsphereDockerDeploy() {
                return false;
            }

            @Override
            public boolean isJBossDockerDeploy() {
                return false;
            }

            @Override
            public boolean isTomcatDockerDeploy() {
                return false;
            }

            @Override
            public int getRunDuration(TimeUnit unit) {
                return (int) unit.convert(14, TimeUnit.DAYS);
            }

            @Override
            public String getApiVersion() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isOracleMode() {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }

}
