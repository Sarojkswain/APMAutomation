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

package com.ca.apm.powerpack.sysview.tools.smfgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Cli {
    private static final Logger log = LoggerFactory.getLogger(Cli.class);

    public static void main(String[] args) throws IOException {
        Parms parms = new Parms();
        /*JCommander jc = */new JCommander(parms, args);
        
        // Generator initialization (runLength: second -> milliseconds)
        SmfRecordGenerator srg = new SmfRecordGenerator(parms.host, parms.ports);
        SmfSnippetGenerator ssg = new SmfSnippetGenerator(srg, parms.runLength * 1_000);
        
        // Test execution
        if (parms.testConst) {
            ssg._executeConst(parms.nodes, parms.tps);
        }
        else if (parms.testPeak) {
            ssg._executePeak(parms.peakDuration, parms.normalBaseTps, parms.normalNodes,
                parms.peakNodes, parms.peakLowTps, parms.peakHighTps, parms.peakPeriod);
        }
        else if (parms.testDistribution) {
            int[] intDistribution = parms.extraDistribution.stream()
                .map(s -> s.replace("_", ""))
                .mapToInt(s -> Integer.valueOf(s))
                .toArray();
            ssg._executeDist(parms.tps, parms.nodes, intDistribution);
        }
        else {
            log.error("No test was selected");
        }

        srg.disconnect();
    }
}

//@Parameters(separators = "=")
class Parms {
    // === General =====================================================================================================
    @Parameter(names = { "-h", "--help" }, help = true, description = "Print command help")
    private boolean help;
    
    @Parameter(names = { "--host" }, description = "Agent host")
    public String host;
    
    @Parameter(names = { "--port" }, description = "Agent port")
    public List<Integer> ports;

    // === Test selection options ======================================================================================
    @Parameter(names = { "-tc", "--test-const" }, description = "Execute const test")
    public boolean testConst = false;
    
    @Parameter(names = { "-tp", "--test-peak" }, description = "Execute peak test")
    public boolean testPeak = false;
    
    @Parameter(names = { "-td", "--test-distribution" }, description = "Execute uneven distribution test")
    public boolean testDistribution = false;
    
    // === Const (common) test options =================================================================================
    @Parameter(names = { "-rl", "--run-length" }, description = "Test run length [s]")
    public int runLength = 5 * 60;

    @Parameter(names = { "-n", "--nodes" }, description = "Number of unique nodes (transactions)")
    public int nodes = 100;
    
    @Parameter(names = { "-tps", "--tps" }, description = "Transactions per second")
    public int tps = 20;

    // === Peak test  options ==========================================================================================
    @Parameter(names = { "-pd", "--peak-duration" }, description = "Duration of the peak [ms]")
    public int peakDuration = 500;
    
    @Parameter(names = { "-pp", "--peak-period" }, description = "Period between peaks [ms]")
    public int peakPeriod = 25_000;
    
    @Parameter(names = { "-nn", "--normal-nodes" }, description = "Number of normal (non-peak) nodes")
    public int normalNodes = 9;
    
    @Parameter(names = { "-nbt", "--normal-base-tps" }, description = "Base tps of normal nodes")
    public int normalBaseTps = 20;
    
    @Parameter(names = { "-pn", "--peak-nodes" }, description = "Number of peak nodes")
    public int peakNodes = 1;
    
    @Parameter(names = { "-plt", "--peak-low-tps" }, description = "Low tps of peak nodes")
    public int peakLowTps = 0;
    
    @Parameter(names = { "-pht", "--peak-high-tps" }, description = "High tps of peak nodes")
    public int peakHighTps = 1000;
    
    // === Distribution test options ===================================================================================
    @Parameter(names = { "-ed", "--extra-distribution" }, variableArity = true, description = "Distribution of abnormal nodes in transactions in million")
    public List<String> extraDistribution = new ArrayList<>(Arrays.asList("10_000", "500_000"));
}