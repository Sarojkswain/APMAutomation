/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.tas.agent.entities;

import java.util.Collection;

/**
 * Class ProcessData.
 * <p>
 * Description
 */
public class ProcessData {
    private final String pid;
    private final String executable;
    private final String user;
    private final Collection<String> runtimeArgs;

    public ProcessData(final String pid, final String executable, final String user, final Collection<String> runtimeArgs) {
        this.pid = pid;
        this.executable = executable;
        this.user = user;
        this.runtimeArgs = runtimeArgs;
    }

    public String getPid() {
        return this.pid;
    }

    public String getExecutable() {
        return this.executable;
    }

    public Collection<String> getRuntimeArgs() {
        return this.runtimeArgs;
    }

    public String getUser() {
        return this.user;
    }

    @Override
    public String toString() {
        return "ProcessData{" + "pid='" + pid + '\'' + ", executable='" + executable + '\''
            + ", user='" + user + '\'' + ", runtimeArgs=" + runtimeArgs + '}';
    }
}
