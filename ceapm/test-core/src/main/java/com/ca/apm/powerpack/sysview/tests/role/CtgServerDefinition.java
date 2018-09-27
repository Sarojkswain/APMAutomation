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

public class CtgServerDefinition {
    public static final String INDENTATION = "    ";

    private enum DefinitionType {
        IPIC, ECI
    }

    protected DefinitionType type;

    protected String id;
    protected String host;
    protected int port;
    protected String description;
    protected Map<String,String> additionalOptions = new HashMap<String, String>();

    protected CtgServerDefinition(DefinitionType type, String id, String host, int port) {
        Args.notEmpty(id, "id");
        Args.notEmpty(host, "Host");
        Args.check(port >= 0 && port <= 65535, "Port value out of range");

        this.id = id;
        this.type = type;
        this.host = host;
        this.port = port;
        this.description = type + " on " + host + ":" + port;
    }

    public static CtgServerDefinition getIpicDefinition(String id, String host, int port) {
        return new CtgServerDefinition(DefinitionType.IPIC, id, host, port);
    }

    public static CtgServerDefinition getEciDefinition(String id, String host, int port) {
        return new CtgServerDefinition(DefinitionType.ECI, id, host, port);
    }

    public void setDescription(String description) {
        Args.notEmpty(description, "description");

        this.description = description;
    }

    public void setOption(String option, String value) {
        Args.notNull(option, "option");
        Args.notNull(value, "value");

        additionalOptions.put(option, value);
    }

    public Collection<String> getDefinition() {
        List<String> definition = new ArrayList<String>();
        definition.add("");
        switch (type) {
            case ECI:
                definition.add("SECTION SERVER=" + id);
                definition.add(INDENTATION + "description=" + description);
                definition.add(INDENTATION + "netname=" + host);
                definition.add(INDENTATION + "port=" + port);
                definition.add(INDENTATION + "protocol=TCPIP");
                break;

            case IPIC:
                definition.add("SECTION IPICSERVER=" + id);
                definition.add(INDENTATION + "description=" + description);
                definition.add(INDENTATION + "hostname=" + host);
                definition.add(INDENTATION + "port=" + port);
                break;
        }

        for (Map.Entry<String, String> option : additionalOptions.entrySet()) {
            definition.add(INDENTATION + option.getKey() + "=" + option.getValue());
        }

        definition.add("ENDSECTION");

        return definition;
    }
}