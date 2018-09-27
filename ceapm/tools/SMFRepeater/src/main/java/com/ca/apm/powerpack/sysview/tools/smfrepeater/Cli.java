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

package com.ca.apm.powerpack.sysview.tools.smfrepeater;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.ParameterException;
import com.ca.apm.automation.utils.smf.SmfData;
import com.ca.apm.automation.utils.smf.SmfSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Entry point for a command line interface for the SMF Repeater tool.
 */
public class Cli {
    private static final Logger log = LoggerFactory.getLogger(Cli.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        // Parse command-line arguments
        Parms parms = new Parms();
        try {
            JCommander jc = new JCommander(parms, args);
            if (parms.help) {
                jc.usage();
                return;
            }
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            return;
        }

        SmfSender sender = new SmfSender(parms.host, parms.ports);

        // Process all input files and convert them to SmfData instances to be sent out later.
        List<SmfData> records = new ArrayList<>();
        for (String file : parms.files) {
            records.add(SmfData.fromFile(FileSystems.getDefault().getPath(file)));
        }

        // Send SmfData instances to Agents.
        for (int i = 0; i < parms.repeats; ++i) {
            if (i != 0) {
                Thread.sleep(parms.delay);
            }

            log.info("Send #{}", i);
            for (SmfData smf : records) {
                sender.send(smf);
            }
        }

        sender.disconnect();
        log.info("Disconnected");
    }
}

/**
 * Class that defines/holds the command line arguments used by the {@link Cli} class.
 */
class Parms {
    @Parameter(names = { "--help" }, help = true, description = "Print command help")
    public boolean help;

    @Parameter(names = { "-h", "--host" }, description = "Agent host", required = true)
    public String host;

    @Parameter(names = { "-p", "--port" }, description = "Agent port", required = true)
    public List<Integer> ports;

    @Parameter(names = { "-f", "--file" }, description = "Data file", required = true)
    public List<String> files;

    @Parameter(names = { "-r", "--repeats" }, description = "Repeats")
    public int repeats = 1;

    @Parameter(names = { "-d", "--delay" }, description = "Delay between repeats")
    public int delay = 2_000;
}
