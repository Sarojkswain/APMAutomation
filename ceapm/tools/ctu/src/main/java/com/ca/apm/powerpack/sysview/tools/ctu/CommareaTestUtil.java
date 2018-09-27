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

package com.ca.apm.powerpack.sysview.tools.ctu;

import com.ibm.ctg.client.ECIRequest;
import com.ibm.ctg.client.JavaGateway;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This is a quick-and-dirty utility for manual testing of COMMAREA interactions.
 * This is intended for development testing, investigation, and the occasional manual
 * regression test.
 *
 * In the future this can be improved to support batch (non-interactive) operations.
 */
public class CommareaTestUtil implements Runnable {
    private static final String SEPARATOR =
        "+---+--------+---------+--------+----------+-------+--------->";
    private static final String CLI_PREFIX = "ctu#";
    private static final Map<String, String> DEFAULT_VALUES;
    static {
        Map<String, String> defaultValues = new HashMap<>();
        defaultValues.put("program.name", "CALLPROG");
        defaultValues.put("target.host", "localhost");
        defaultValues.put("target.port", "2006");
        defaultValues.put("target.cics", "660ECI");
        defaultValues.put("array.data", "[]");
        defaultValues.put("array.length", "16384");
        defaultValues.put("commarea.length", "0");
        defaultValues.put("outbound.length", "0");
        defaultValues.put("inbound.length", "0");
        defaultValues.put("execution.count", "1");
        defaultValues.put("verbose", "no");
        DEFAULT_VALUES = Collections.unmodifiableMap(defaultValues);
    }
    private static final Set<String> PARAMS = Collections.unmodifiableSet(DEFAULT_VALUES.keySet());

    private final PrintStream output;

    private Map<String, String> currentValues = cloneValues(DEFAULT_VALUES);

    /**
     * Constructor.
     *
     * @param output Stream to use for output.
     */
    public CommareaTestUtil(PrintStream output) {
        Validate.notNull(output);
        this.output = output;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            // This is implemented as a simple REPL-like utility.

            output.println("('help' for list of commands)");
            boolean run = true;
            while (run) {
                output.print(CLI_PREFIX + " ");
                output.flush();

                final String command = scanner.nextLine().trim();
                final String[] words = command.split("\\s+", 3);

                if (command.isEmpty() || words.length < 1) {
                    continue;
                }

                final String operation = words[0];

                switch (operation) {
                    case "exit":
                    case "quit":
                        run = false;
                        break;

                    case "help":
                        printHelp();
                        break;

                    case "get":
                        if (words.length >= 2) {
                            final String param = words[1];
                            if (!PARAMS.contains(param)) {
                                output.println("Unknown parameter: '" + param + "'");
                                break;
                            }

                            printParamValue(param);
                        } else {
                            PARAMS.forEach(this::printParamValue);
                        }

                        break;

                    case "set":
                        if (words.length < 3) {
                            output.println("Bad command syntax: '" + command + "'");
                            break;
                        }

                        final String param = words[1];
                        final String value = words[2];

                        if (!PARAMS.contains(param)) {
                            output.println("Unknown parameter: '" + param + "'");
                            break;
                        }

                        setParamValue(param, value);
                        printParamValue(param);
                        break;

                    case "send":
                        try {
                            final String host = currentValues.get("target.host");
                            final int port = Integer.valueOf(currentValues.get("target.port"));
                            final String cicsServer = currentValues.get("target.cics");
                            final String program = currentValues.get("program.name");
                            final int commareaLength = Integer.valueOf(currentValues.get("commarea.length"));
                            final int outboundLength = Integer.valueOf(currentValues.get("outbound.length"));
                            final int inboundLength = Integer.valueOf(currentValues.get("inbound.length"));
                            final int arrayLength = Integer.valueOf(currentValues.get("array.length"));
                            String dataTemplate = currentValues.get("array.data");
                            final int executionCount = Integer.valueOf(currentValues.get("execution.count"));
                            final boolean verbose = currentValues.get("verbose").equals("yes");
                            byte[] commarea;

                            JavaGateway gateway = new JavaGateway(host, port);

                            ECIRequest request = new ECIRequest(cicsServer, null, null, null,
                                (byte[])null, ECIRequest.ECI_EXTENDED, ECIRequest.ECI_LUW_NEW);
                            request.Cics_Rc = 0;

                            request.Program = program;
                            request.Commarea_Length = commareaLength;
                            if (outboundLength > 0) {
                                request.setCommareaOutboundLength(true);
                                request.setCommareaOutboundLength(outboundLength);
                            }
                            if (inboundLength > 0) {
                                request.setCommareaInboundLength(true);
                                request.setCommareaInboundLength(inboundLength);
                            }

                            // Program execution(s)
                            for (int i = 0; i < executionCount; ++i) {
                                commarea = Arrays.copyOf(
                                    dataTemplate.replaceAll("%%%", String.valueOf(i)).getBytes(),
                                    arrayLength);
                                request.Commarea = commarea;
                                request.Extend_Mode = ECIRequest.ECI_EXTENDED;
                                if (verbose) {
                                    printRequest(request, '>', true, false);
                                }
                                request.Cics_Rc = 0;
                                gateway.flow(request);
                                System.out.println("Executed " + program + ": " + request.Cics_Rc);
                                if (verbose) {
                                    printCommArea(commarea, '*', true);
                                    printRequest(request, '<', false, true);
                                }
                            }

                            // Commit
                            request.Extend_Mode = ECIRequest.ECI_COMMIT;
                            request.Cics_Rc = 0;
                            gateway.flow(request);
                            System.out.println("Commit: " + request.Cics_Rc);
                        } catch (Exception e) {
                            output.println(
                                "Caught exception during send" + System.lineSeparator() + "[" + e.getClass().getName() + "] "
                                    + System.lineSeparator() + e.getLocalizedMessage());
                        }
                        break;

                    default:
                        output.println("Unknown operation: '" + operation + "'");
                        break;
                }
            }
        }
    }

    /**
     * Get a raw parameter value.
     *
     * @param param Parameter to query.
     * @return Raw parameter value
     */
    private String getParamValue(String param) {
        assert param != null;
        assert PARAMS.contains(param);

        return currentValues.get(param);
    }

    /**
     * Set a raw parameter value.
     *
     * @param param Parameter to set.
     * @param value Value to set.
     * @return The new parameter value.
     */
    private String setParamValue(String param, String value) {
        assert param != null;
        assert PARAMS.contains(param);
        assert value != null;

        return currentValues.put(param, value);
    }

    /**
     * Clones a set of known parameter values.
     *
     * @param source Source parameter value set.
     * @return Cloned set.
     */
    private Map<String, String> cloneValues(Map<String, String> source) {
        Validate.notNull(source);
        Validate.isTrue(source.keySet().containsAll(PARAMS));

        Map<String, String> clonedValues = new HashMap<>(PARAMS.size());
        for (final String param : PARAMS) {
            clonedValues.put(param, source.get(param));
        }

        return clonedValues;
    }

    /**
     * Prints a parameter values to the output stream.
     *
     * @param param Parameter to print.
     */
    private void printParamValue(String param) {
        assert param != null;
        assert PARAMS.contains(param);

        output.println(param + " = " + getParamValue(param));
    }

    /**
     * Prints the state of a commarea buffer to the output stream.
     *
     * @param data Commarea buffer.
     * @param prefix Prefix character.
     * @param includeFooter Whether to include a footer separator line.
     */
    private void printCommArea(byte[] data, char prefix, boolean includeFooter) {
        final String ascii = new String(data, Charset.forName("US-ASCII"));
        final String ebcdic = new String(data, Charset.forName("Cp1047"));

        output.println(SEPARATOR);

        output.println("| " + prefix + " |                  "
            + "  ASCII  | "
            + StringUtils.center(Integer.toHexString(data.hashCode()), 8) + " | "
            + StringUtils.center(String.valueOf(ascii.length()), 5) + " | "
            + ascii);
        output.println("| " + prefix + " |                  "
            + "  EBCDIC | "
            + StringUtils.center(Integer.toHexString(data.hashCode()), 8) + " | "
            + StringUtils.center(String.valueOf(ebcdic.length()), 5) + " | "
            + ebcdic);

        if (includeFooter) {
            output.println(SEPARATOR);
        }
    }

    /**
     * Prints the state of a {@link ECIRequest} object to the output stream.
     *
     * @param request Request object.
     * @param prefix Prefix character.
     * @param includeHeader Whether to include a table header.
     * @param includeFooter Whether to include a footer separator line.
     */
    private void printRequest(ECIRequest request, char prefix, boolean includeHeader,
                                    boolean includeFooter) {
        if (includeHeader) {
            output.println(SEPARATOR);
            output.println("| - | ca.len | out.len | in.len |  a.hash  | a.len | a.data");
            output.println(SEPARATOR);
        }

        output.println("| "
            + prefix + " | "
            + StringUtils.center(String.valueOf(request.Commarea_Length), 6) + " | "
            + StringUtils.center(String.valueOf(request.getCommareaOutboundLength()), 7) + " | "
            + StringUtils.center(String.valueOf(request.getCommareaInboundLength()), 6) + " | "
            + StringUtils.center("", 8) + " | "
            + StringUtils.center("", 5) + " | ");
        printCommArea(request.Commarea, prefix, includeFooter);
    }

    /**
     * Prints basic (syntax) help for the available commands.
     */
    private void printHelp() {
        output.println(StringUtils.join(Arrays.asList("exit", "quit", "help", "get [<param>]",
            "set <param> <value>", "send"), System.lineSeparator()));
    }
}
