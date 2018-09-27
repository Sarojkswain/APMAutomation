/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author keyja01
 *
 */
@Flow
public class DeployTestLoadAAAFlow extends FlowBase implements IAutomationFlow {
    
    @FlowContext
    private DeployTestLoadAAAFlowContext flowContext;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        File installDir = new File(flowContext.getInstallDir());
        if (!installDir.exists()) {
            Path p = installDir.toPath();
            Files.createDirectories(p);
        }
        PrintWriter writer = new PrintWriter(new File(installDir, "startLoadAAA.cmd"));
        writer.println("echo This load is now started");
        writer.flush();
        writer.close();
        
        writer = new PrintWriter(new File(installDir, "stopLoadAAA.cmd"));
        writer.println("echo This load is now stopped");
        writer.flush();
        writer.close();
        
    }

}
