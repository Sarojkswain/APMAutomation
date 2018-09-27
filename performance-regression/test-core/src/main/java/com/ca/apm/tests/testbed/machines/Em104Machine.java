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
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.artifact.CsvToXlsTemplateVersion;
import com.ca.tas.resolver.ITasResolver;

/**
 * Machine containing Weblogic Server + StockTrader Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class Em104Machine extends EmMachine {

    static {
        // CPU
        sheetsMapping.put("cpu_tomcat8.nosi.bt.CURRENT.csv", "tomcat_cpu_nosi_bt");
        sheetsMapping.put("cpu_weblogic103.nosi.bt.CURRENT.csv", "wls_cpu_nosi_bt");
        sheetsMapping.put("cpu_weblogic103.acc.CURRENT.csv", "wls_cpu_acc");
        sheetsMapping.put("cpu_websphere85.nosi.bt.CURRENT.csv", "was_cpu_nosi_bt");
        sheetsMapping.put("cpu_iis75.nosi.bt.CURRENT.csv", "iis_cpu_nosi_bt");

        // MEMORY
        sheetsMapping.put("mem_tomcat8.nosi.bt.CURRENT.csv", "tomcat_mem_nosi_bt");
        sheetsMapping.put("mem_weblogic103.nosi.bt.CURRENT.csv", "wls_mem_nosi_bt");
        sheetsMapping.put("mem_weblogic103.acc.CURRENT.csv", "wls_mem_acc");
        sheetsMapping.put("mem_websphere85.nosi.bt.CURRENT.csv", "was_mem_nosi_bt");
        sheetsMapping.put("mem_iis75.nosi.bt.CURRENT.csv", "iis_mem_nosi_bt");

        // REQUESTS
        sheetsMapping.put("jmeter_tomcat8.nosi.bt.CURRENT.modified.csv", "tomcat_req_nosi_bt");
        sheetsMapping.put("jmeter_weblogic103.nosi.bt.CURRENT.modified.csv", "wls_req_nosi_bt");
        sheetsMapping.put("jmeter_weblogic103.acc.CURRENT.modified.csv", "wls_req_acc");
        sheetsMapping.put("jmeter_websphere85.nosi.bt.CURRENT.modified.csv", "was_req_nosi_bt");
        sheetsMapping.put("jmeter_iis75.nosi.bt.CURRENT.modified.csv", "iis_req_nosi_bt");

        // REQUESTS STATS
        sheetsMapping.put("jmeter_tomcat8.nosi.bt.CURRENT.stat.csv", "tomcat_req_stat_nosi_bt");
        sheetsMapping.put("jmeter_weblogic103.nosi.bt.CURRENT.stat.csv", "wls_req_stat_nosi_bt");
        sheetsMapping.put("jmeter_weblogic103.acc.CURRENT.stat.csv", "wls_req_stat_acc");
        sheetsMapping.put("jmeter_websphere85.nosi.bt.CURRENT.stat.csv", "was_req_stat_nosi_bt");
        sheetsMapping.put("jmeter_iis75.nosi.bt.CURRENT.stat.csv", "iis_req_stat_nosi_bt");
    }

    public Em104Machine(String machineId, ITasResolver tasResolver) {
        super(machineId, tasResolver);
    }

    public Em104Machine(String machineId, ITasResolver tasResolver, boolean deployEm, boolean predeployedEm) {
        super(machineId, tasResolver, deployEm, predeployedEm);
    }

    protected CsvToXlsTemplateVersion getCsvToXlsTemplateVersion() {
        return CsvToXlsTemplateVersion.AGENT_VER_10_5;
    }

}
