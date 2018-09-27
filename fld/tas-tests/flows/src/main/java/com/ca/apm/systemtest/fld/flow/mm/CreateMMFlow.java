/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import java.io.File;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.systemtest.fld.util.ZipBuilder;

/**
 * @author keyja01
 *
 */
@Flow
public class CreateMMFlow implements IAutomationFlow {
    @FlowContext
    private CreateMMFlowContext ctx;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        File f = new File(ctx.deployDir);
        if (!f.exists()) {
            f.mkdirs();
        }
        File mmFile = new File(f, ctx.filename);
        
        String xml = ctx.managementModule.toXml();
        
        ZipBuilder zip = new ZipBuilder(mmFile);
        zip.addFolder("META-INF");
        zip.addFile("META-INF/MANIFEST.MF", "".getBytes());
        zip.addFile("ManagementModule.xml", xml.getBytes());
        zip.close();
    }
}
