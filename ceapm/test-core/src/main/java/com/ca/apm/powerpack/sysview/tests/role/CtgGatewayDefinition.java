/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tests.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Args;

public class CtgGatewayDefinition {
    public static final String INDENTATION = "    ";

    protected int port;
    protected Map<String,String> additionalOptions = new HashMap<String, String>();

    public CtgGatewayDefinition(int port) {
        this.port = port;
    }

    public void setOption(String option, String value) {
        Args.notNull(option, "option");
        Args.notNull(value, "value");

        additionalOptions.put(option, value);
    }

    public Collection<String> getDefinition() {
        List<String> definition = new ArrayList<String>();

        definition.add("SECTION GATEWAY");
        definition.add(INDENTATION + "protocol@tcp.handler=com.ibm.ctg.server.TCPHandler");
        definition.add(INDENTATION + "protocol@tcp.parameters=port=" + port);
        for (Map.Entry<String, String> option : additionalOptions.entrySet()) {
            definition.add(INDENTATION + option.getKey() + "=" + option.getValue() + ";");
        }
        definition.add("ENDSECTION");

        return definition;
    }
}