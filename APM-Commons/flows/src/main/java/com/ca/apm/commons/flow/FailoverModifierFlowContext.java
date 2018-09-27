package com.ca.apm.commons.flow;

import java.util.List;

import com.ca.apm.automation.action.flow.IFlowContext;

public class FailoverModifierFlowContext  implements IFlowContext {

    private List<String> command;
    private String dirloc;
    /**
     * Static factory method to eliminate builder and give meaningful names to constructors
     * @param commands
     * @param dirlocation
     * @return
     */
    public static FailoverModifierFlowContext remoteMount(List<String> command, String dirloc) {
        return new FailoverModifierFlowContext(command, dirloc);
    }


    protected FailoverModifierFlowContext(List<String> command, String dirloc) {
        this.command = command;
        this.dirloc = dirloc;
    }

    public List<String> getcommand() {
        return command;
    } 
    public String getdir() {
        return dirloc;
    }
    
   }
