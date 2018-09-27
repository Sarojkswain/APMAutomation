package com.ca.apm.commons.flow;

import java.util.List;

import com.ca.apm.automation.action.flow.IFlowContext;

public class FailoverEMStartStopFlowContext  implements IFlowContext {

    
    private String dirloc;
    private String emType;

    /**
     * Static factory method to eliminate builder and give meaningfull names to constructors
     * @param logFile
     * @param keyword
     * @return
     */
    public static FailoverEMStartStopFlowContext startStopFailoverEM( String dirloc,String emType) {
        return new FailoverEMStartStopFlowContext(dirloc,emType);
    }

    

    protected FailoverEMStartStopFlowContext(String dirloc, String emType) {
        this.dirloc = dirloc;
        this.emType = emType;
    }

    public String getdir() {
        return dirloc;
    }
    
    public String getEmType() {
        return emType;
    }

  

}
