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

package com.ca.apm.automation.action.flow.mainframe.sysview;

import com.ca.apm.automation.action.flow.IFlowContext;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Context for the {@link SysviewPortlistFlow} flow.
 */
public class SysviewPortlistFlowContext implements IFlowContext {
    private final String loadlib;
    private final SysviewPortlistFlow.Operation operation;
    private Collection<String> groups = SysviewPortlistFlow.DEFAULT_GROUPS;
    private final Collection<Integer> ports = new ArrayList<>(1);

    public SysviewPortlistFlowContext(String loadlib, SysviewPortlistFlow.Operation operation,
                                      Collection<Integer> ports) {
        this.loadlib = loadlib;
        this.operation = operation;
        addPorts(ports);
    }

    public SysviewPortlistFlowContext addPort(int port) {
        Validate.inclusiveBetween(1, 65535, port);

        ports.add(port);
        return this;
    }

    public SysviewPortlistFlowContext addPorts(Collection<Integer> ports) {
        for (int port : ports) {
            Validate.inclusiveBetween(1, 65535, port);
        }

        this.ports.addAll(ports);
        return this;
    }

    public SysviewPortlistFlowContext usingGroups(Collection<String> groups) {
        Validate.notEmpty(groups);

        this.groups = groups;
        return this;
    }

    public String getLoadlib() {
        return loadlib;
    }

    SysviewPortlistFlow.Operation getOperation() {
        return operation;
    }

    Collection<String> getGroups() {
        return groups;
    }

    Collection<Integer> getPorts() {
        return ports;
    }
}
