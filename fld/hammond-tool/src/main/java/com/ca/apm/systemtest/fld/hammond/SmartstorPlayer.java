package com.ca.apm.systemtest.fld.hammond;

import java.util.Timer;

import com.ca.apm.systemtest.fld.hammond.data.AppmapData;
import com.ca.apm.systemtest.fld.hammond.data.SmartstorData;
import com.ca.apm.systemtest.fld.hammond.data.TransactionsData;
import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.util.Log;

public class SmartstorPlayer {

    SmartstorData store;
    TransactionsData transactionsData;
    AppmapData appmapData;
    AgentOrchestrator agentOrchestrator;

    Timer timer;
    //boolean stopped;
    long timeOffset;
    public SmartstorPlayer(SmartstorData store, TransactionsData transactionsData, AppmapData appmapData) {
        this.store = store;
        this.transactionsData = transactionsData;
        this.appmapData = appmapData;
        agentOrchestrator = new AgentOrchestrator();
        timer = new Timer();
    }

    public void setupPlayback() throws Exception {
        Configuration cfg = Configuration.instance();

        // set settings
        agentOrchestrator.setAgentScale(cfg.getScaleRatio());
        agentOrchestrator.setAgentCredential(cfg.getAgentCredential());
        agentOrchestrator.setCollectorHost(cfg.getCollectorHost());
        agentOrchestrator.setPrefix(cfg.getPrefix());
        agentOrchestrator.setIncluded(cfg.getIncluded());
        agentOrchestrator.setExcluded(cfg.getExcluded());

        // get agent definitions and build agents
        agentOrchestrator.createAgents(store);
    }

    public boolean startPlayback() {
        if (store.getSlicesRangeFrom() == null || store.getSlicesRangeTo() == null) {
            return false;
        }

        return agentOrchestrator.startPlayback(store, transactionsData, appmapData);
    }

    public static void main(String[] args) throws Exception {
        new Activator();
        Log.out = Configuration.instance().createFeedback("Smartstor Player");

        Configuration cfg = Configuration.instance();
        if (!cfg.parsePlayerOptions(args)) {
            return;
        }

        SmartstorData store = new SmartstorData(cfg.getDataFolder());
        store.load();
        
        TransactionsData transactions = new TransactionsData(cfg.getDataFolder()); 
        transactions.load();

        if (cfg.getFrom() != null && cfg.getTo() != null) {
            store.setSlicesRange(cfg.getFrom(), cfg.getTo());
            transactions.setSlicesRange(cfg.getFrom(), cfg.getTo());
        }
        
        AppmapData appmapData = new AppmapData(cfg.getDataFolder());
        appmapData.load();

        final SmartstorPlayer sp = new SmartstorPlayer(store, transactions, appmapData);
        sp.setupPlayback();
        if (sp.startPlayback()) {
            long duration = cfg.isPlayOnce()
                    ? store.getSlicesRangeTo() - store.getSlicesRangeFrom() + Agent.HARVEST_PERIOD
                    : cfg.getDuration();
            Thread.sleep(duration);
        }
        Runtime.getRuntime().exit(0);
    }
}
