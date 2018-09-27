/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.PrintWriter;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * Configures a socat port forwarding tunnel on CentOS/RHEL 6.x machines.  WILL NOT WORK ON WINDOWS OR OTHER
 * FLAVORS OF LINUX!
 * @author keyja01
 *
 */
@Flow
public class ConfigurePortForwardingFlow extends FlowBase {
    @FlowContext
    private ConfigurePortForwardingFlowContext ctx;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        installSocat();
        startForwarding();
    }
    
    
    private void startForwarding() throws Exception {
        String logFile = logFileName();
        logger.info("Logging socat tunnel to " + logFile);
        
        /*
#!/bin/bash
nohup socat TCP-LISTEN:8080,fork TCP:172.20.20.79:8080 > tunnel1.log &
         */
        File workDir = new File(ctx.workDir);
        File script = new File(workDir, tunnelScriptFileName());
        
        workDir.mkdirs();
        
        PrintWriter writer = new PrintWriter(script);
        writer.println("#!/bin/bash");
        writer.format("nohup socat TCP-LISTEN:%d,fork TCP:%s:%d > %s &", ctx.listenPort, ctx.targetIpAddress, ctx.targetPort, logFile);
        writer.println();
        writer.flush();
        writer.close();
        
        new Execution.Builder("chmod", logger)
            .args(new String[] {"u+x", script.getPath()})
            .build()
            .go();
        
        // nohup socat TCP-LISTEN:8080,fork TCP:172.20.20.79:8080
        new Execution.Builder(script, logger)
            .workDir(new File(ctx.workDir))
            .build()
            .go();
    }
    
    
    private String logFileName() {
        String s = "socat_" + ctx.listenPort + "-" + ctx.targetIpAddress + "_" + ctx.targetPort + ".log";
        
        return s;
    }
    
    
    private String tunnelScriptFileName() {
        String s = "socat_" + ctx.listenPort + "-" + ctx.targetIpAddress + "_" + ctx.targetPort + ".sh";
        
        return s;
    }


    private void installSocat() throws Exception {
        String[] args = new String[] {"-y", "install", "socat"};
        Execution execution = new Execution.Builder("yum", logger)
            .args(args)
            .build();
        execution.go();
    }

}
