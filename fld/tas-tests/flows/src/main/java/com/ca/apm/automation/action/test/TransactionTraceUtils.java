package com.ca.apm.automation.action.test;

import java.util.List;

/**
 * Transaction Tracer utility class which uses {@link ClwRunner} as the driver.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TransactionTraceUtils {

    public static final String CLW_TRACE_TRANSACTIONS_COMMAND_TEMPLATE = "trace transactions exceeding %d ms in agents matching (%s) for %d s";
    
    private final ClwRunner2 clw;

    public TransactionTraceUtils(ClwRunner2 clw) {
        this.clw = clw;
    }

    /**
     * Trace transactions for the <code>forSec</code> seconds given the agent name regular expression <code>agentNameRegex</code> and 
     * transaction minimal length in milliseconds <code>exceedsMillis</code>.
     * 
     * <p/>
     * This methods corresponds to CLWorkstation.jar's "trace transactions" command.
     * 
     * @param agentNameRegex  agent name regular expression
     * @param exceedsMillis   minimal transaction length in milliseconds   
     * @param forSecs         tracing session length in seconds
     * @return                CLWorkstation's output
     */
    public List<String> traceTransactions(String agentNameRegex, int exceedsMillis, int forSecs) {
        final String command = String.format(CLW_TRACE_TRANSACTIONS_COMMAND_TEMPLATE, exceedsMillis, agentNameRegex, forSecs);
        return clw.runClw(command);
    }

}
