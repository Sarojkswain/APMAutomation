/*
 * Copyright (c) 2017 CA. All rights reserved.
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
package com.ca.apm.systemtest.fld.hammond;

import com.ca.apm.systemtest.fld.hammond.imp.HammondImport;
import com.ca.apm.systemtest.fld.hammond.imp.HammondMetricFilter;
import com.ca.apm.systemtest.fld.hammond.imp.HammondTables;
import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.server.enterprise.entity.meta.cache.AgentMetricCache;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.util.Log;
import org.apache.commons.lang.StringUtils;

public class HammondPlayer {

    HammondAgentOrchestrator agentOrchestrator;
    private HammondTables store;

    public HammondPlayer(HammondTables store) {
        this.store = store;
        agentOrchestrator = new HammondAgentOrchestrator();
    }

    public void setupPlayback() throws Exception {
        HammondPlayerConfiguration cfg = HammondPlayerConfiguration.instance();

        // set settings

        agentOrchestrator.setHammondData(store);
        agentOrchestrator.setCollectorHost(cfg.getCollectorHost());

        if (cfg.getAgentScale() != null) {
            agentOrchestrator.setAgentScale(cfg.getAgentScale());
        }
        if (StringUtils.isNotBlank(cfg.getPrefix())) {
            agentOrchestrator.setVariant(cfg.getPrefix());
        }
        if (StringUtils.isNotBlank(cfg.getGroup())) {
            agentOrchestrator.setGroup(cfg.getGroup());
        }
        if (StringUtils.isNotBlank(cfg.getIncluded())) {
            agentOrchestrator.setIncluded(cfg.getIncluded());
        }
        if (StringUtils.isNotBlank(cfg.getExcluded())) {
            agentOrchestrator.setExcluded(cfg.getExcluded());
        }
        if (StringUtils.isNotBlank(cfg.getAgentCredential())) {
            agentOrchestrator.setAgentCredential(cfg.getAgentCredential());
        }
        if (cfg.getFiltersPath() != null) {
            HammondMetricFilter.importMetricNameFileters(cfg.getFiltersPath());
        }

        // get agent definitions and build agents
        agentOrchestrator.createAgents();
    }

    public boolean startPlayback() {
        HammondPlayerConfiguration cfg = HammondPlayerConfiguration.instance();

        Long metricsFrom = store.getRangeFrom(HammondTables.TABLE_SLICE_DATA);
        Long metricsTo = store.getRangeTo(HammondTables.TABLE_SLICE_DATA);
        Long tracesFrom = store.getRangeFrom(HammondTables.TABLE_TRACE_DATA);
        Long tracesTo = store.getRangeTo(HammondTables.TABLE_TRACE_DATA);

        if (cfg.getFrom() != null) {
            metricsFrom = tracesFrom = cfg.getFrom();
        }
        if (cfg.getTo() != null) {
            metricsTo = tracesTo = cfg.getTo();
        }

        if (metricsFrom == null || metricsTo == null || tracesFrom == null || tracesTo == null) {
            Log.out.error("Cannot replay data without valid boundaries.");
            return false;
        }

        if (metricsTo <= metricsFrom || tracesTo <= tracesFrom) {
            Log.out.error("Cannot replay data when start time is after end time.");
            return false;
        }

        return agentOrchestrator.startPlayback(metricsFrom, metricsTo, tracesFrom, tracesTo);
    }

    public static void main(String[] args) throws Exception {
        new Activator();
        Log.out = HammondPlayerConfiguration.instance().createFeedback("Hammond Player");

        HammondPlayerConfiguration cfg = HammondPlayerConfiguration.instance();
        if (!cfg.parseOptions(args)) {
            return;
        }

        HammondTables tables = new HammondTables(HammondImport.openOutputDB(cfg.getData(), true));

        final HammondPlayer sp = new HammondPlayer(tables);
        sp.setupPlayback();
        if (sp.startPlayback()) {
            long duration = 365L * 24 * 3600 * 1000;
            Thread.sleep(duration);
        }
        Runtime.getRuntime().exit(0);
    }
}
