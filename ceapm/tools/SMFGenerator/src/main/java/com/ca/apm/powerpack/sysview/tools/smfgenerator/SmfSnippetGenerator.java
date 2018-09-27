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

package com.ca.apm.powerpack.sysview.tools.smfgenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deterministically random generator of SMF snippets used for SMF selection algorithm testing.
 *
 * None of the distribution definitions are thread safe.
 */
public class SmfSnippetGenerator {
    static final Logger log = LoggerFactory.getLogger(SmfSnippetGenerator.class);
    public static final Random masterRand = new Random(1); // deterministic random

    public static final int SAMPLING_INTERVAL = 10 * 1000; // [ms]
    /** DB2 subsystem IDs used for CICS nodes */
    private static final int DEF_DB2_NODES = 10;
    /** Maximal job name length (defines CICS/IMS node name) */
    static final int MAX_JOB_NAME = 8;
    private static final int SHORTEN_LENGHT = 150;

    protected final SmfRecordGenerator smfGenerator;
    private final int runLength; // [ms]

    public SmfSnippetGenerator(SmfRecordGenerator smfGenerator, int runLength) {
        this.smfGenerator = smfGenerator;
        this.runLength = runLength;
    }

    private class Runner implements Runnable {
        private final long runLength; // [ns]

        private final DistributionSequence distribution;
        private final Environment environment;
        private final ConcurrentLinkedQueue<SmfSnippet> consumer = new ConcurrentLinkedQueue<>();
        /** Sample counter. */
        private int sample = 0;

        private TreeMap<Integer, List<Signature>> dist;

        /*
        SmfRecordGenerator cicsOut;
        SmfRecordGenerator imsOut;
        */

        /**
         * Constructor.
         *
         * @param runLength Run length [ms].
         * @param environment Environment.
         * @param distribution Distribution sequence.
         */
        private Runner(int runLength, Environment environment, DistributionSequence distribution) {
            this.runLength = runLength * 1_000_000L;
            this.environment = environment;
            this.distribution = distribution;
        }

        /**
         * Send generated SMF snippet through the smf generator.
         *
         * @param smf SMF snippet.
         */
        private void send(SmfSnippet smf) {
            try {
                smfGenerator.send(smf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            long start = System.nanoTime();
            long endTime = start + runLength;

            do {
                long sleep = distribution.getSleepHint(); // [ms]
                if (sleep > 10) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    Thread.yield();
                }

                if (Thread.interrupted()) {
                    log.info("interrupted");
                    break;
                }

                List<Integer> idxs = distribution.getNewTransactions(System.nanoTime() - start);
                for (Integer e : idxs) {
                    SmfSnippet snippet = environment.generateTransactionSnip(e);
                    send(snippet);

                    // In some situations the distribution can return a large number of items to be
                    // sent and, depending on the environment, sends can be/get slow. Therefore we
                    // check the time-based end condition after each send.
                    if (endTime - System.nanoTime() <= 0) {
                        break;
                    }
                }
                idxs.clear();
                //if (now - start >= (sample + 1L) * SAMPLING_INTERVAL * 1_000_000L) {
                //    sample++;
                //    //log.info("{} {}", bs.getPerformance(), ((now - start) / 1_000_000_000));
                //    //log.info("# selection-items:"); bs.dumpSelectionSignatures();
                //    //sample(now - start);
                //}
            } while (endTime - System.nanoTime() > 0);
            //sample(now - start);

            dist = new TreeMap<>();
            List<Integer> l = new ArrayList<>();
            int sum = 0;
            for (Signature ut : environment.uniqs) {
                l.add(ut.getGenerated());
                sum += ut.getGenerated();
                List<Signature> list = dist.get(ut.getGenerated());
                if (list == null) {
                    list = new ArrayList<>();
                    dist.put(ut.getGenerated(), list);
                }
                list.add(ut);
            }

            if (log.isDebugEnabled()) {
                StringBuilder sb =
                    new StringBuilder("Distribution of subsystems per transaction sent:");
                for (Entry<Integer, List<Signature>> e : dist.entrySet()) {
                    sb.append("\n");
                    sb.append(e.getKey());
                    sb.append(" : ");
                    sb.append(e.getValue().size());
                    sb.append(" ");
                    sb.append(shorten(e.getValue()));
                }
                log.debug(sb.toString());
                Collections.sort(l);
                log.debug("sum {} median {} hash {}", sum, l.get(l.size() / 2),
                    environment.uniqs.hashCode());
            }
        }

        public TreeMap<Integer, List<Signature>> getDist() {
            return dist;
        }

        /**
         * Analyze the generated and filtered snippets and log statistics.
         *
         * @param offset time since start [ns]
         */
        private void sample(long offset) {

            //log.info("Sample: {}s", round((double) offset / 1_000_000_000, 2));
            Set<Signature> found = new HashSet<>();

            long sumOutLifetime = 0;

            for (SmfSnippet snippet : consumer) {
                found.add(new Signature(snippet.getType(), snippet.getJobName(), snippet
                    .getDb2Ssid(), null));
                sumOutLifetime += snippet.getLifetime();
            }

            /*
            log.info("# Unique items in output:");
            for (Signature s : found) {
                log.info("#  {}", s.toString());
            }
            */

            // for input, subsums are already created per signature
            int sumIn = 0;
            long sumInLifetime = 0;
            int sumInUniq = 0;
            for (Signature sig : environment.uniqs) {
                sumInLifetime += sig.sumLifetime;
                sumIn += sig.generated;
                if (sig.generated != 0) {
                    sumInUniq++;
                }
            }

            double avgInLifetime = (double) sumInLifetime / sumIn / 1_000_000d; // [ms]
            double avgOutLifetime = (double) sumOutLifetime / consumer.size() / 1_000_000d; // [ms]

            //log.info("# total {}->{} ({})", sumIn, consumer.size(), round((double) consumer.size() / sumIn, 4));
            //log.info("# uniq {}->{} ({})", sumInUniq, found.size(), round((double) found.size() / sumInUniq, 4));
            //log.info("# average lifetime {}->{} ({})", round(avgInLifetime, 2), round(avgOutLifetime, 2), round(avgOutLifetime / avgInLifetime, 4));
            log.info("{} {} {} {}", // <unique> <lifetime> <total> <time>
                round((double) found.size() / sumInUniq, 4),
                round(avgOutLifetime / avgInLifetime, 4),
                round((double) consumer.size() / sumIn, 4),
                round((double) offset / 1_000_000_000, 2));
        }
    }

    /**
     * Return string representation of object, shortened. For debugging outputs.
     *
     * @param o Object
     * @return Shortened string representation of object.
     */
    private String shorten(Object o) {
        assert o != null;
        String ret = o.toString();
        if (ret.length() < SHORTEN_LENGHT) {
            return ret;
        }
        return ret.substring(0, SHORTEN_LENGHT) + "...";
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal decimal = new BigDecimal(value);
        decimal = decimal.setScale(places, RoundingMode.HALF_UP);
        return decimal.doubleValue();
    }

    /**
     * Create default environment with equal amount of CICS and IMS nodes and default DB2 nodes.
     *
     * @param nodes total number of nodes
     * @return generated environment
     */
    public Environment defaultEnvironment(int nodes) {
        return new Environment(nodes / 2, nodes - nodes / 2, DEF_DB2_NODES, masterRand.nextLong());
    }

    /**
     * Create distribution that spans whole run with unchanging TPS, by default all signatures use
     * equal share, optionally with some transactions having uneven share.
     *
     * @param nodes Number of nodes.
     * @param tps Generated transactions per second.
     * @param exceptionPpms Optional PPM for each exceptional transaction with uneven share.
     * @return generated distribution
     */
    private DistributionSequence constantDistribution(int nodes, int tps, int... exceptionPpms) {
        return new DistributionSequence(new Distribution(runLength,
            new ConstantTransactionRate(tps), new WeightedSignatureDistribution(nodes,
                exceptionPpms)));
    }

    // Various testing patterns execution follows.
    public void executeConst(int nodes, int tps) {
        _executeConst(nodes, tps);
    }

    /**
     * Constant flow of transactions generated equally from all nodes.
     *
     * @param nodes Number of nodes.
     * @param tps Transactions per second total.
     */
    public TreeMap<Integer, List<Signature>> _executeConst(int nodes, int tps) {
        //String description = "Constant time distribution, evenly distributed unique transactions.";
        Environment environment = defaultEnvironment(nodes);
        DistributionSequence distributions = constantDistribution(environment.getCount(), tps);
        Runner runner = new Runner(runLength, environment, distributions);
        //log.info("# test: [const] nodes={} tps={}", nodes, tps);
        runner.run();
        return runner.getDist();
    }

    public void executePeak(int peakDuration) {
        int normalTps = 20;
        int normalNodes = 9;
        // single node that goes from lowTps to highTps
        int extraNodes = 1;
        int lowTps = 0;
        int highTps = 1000;
        // scenario periodically repeats [ms]
        int period = 25000;

        _executePeak(peakDuration, normalTps, normalNodes, extraNodes, lowTps, highTps, period);
    }

    /**
     * Constant "background" transactions, a single exceptional transaction generates substantial
     * traffic only during peak.
     *
     * @param peakDuration duration of peak [ms]
     */
    public TreeMap<Integer, List<Signature>> _executePeak(int peakDuration, int normalTps, int normalNodes, int extraNodes, int lowTps, int highTps, int period) {
        //String description = "Deviation in distribution of transactions in time.";
        int nodes = normalNodes + extraNodes;
        int lowPpm = lowTps * 1_000_000 / (lowTps + normalNodes * normalTps);
        int highPpm = highTps * 1_000_000 / (highTps + normalNodes * normalTps);
        Environment environment = defaultEnvironment(nodes);
        Distribution outsidePeak =
            new Distribution(period - peakDuration, new ConstantTransactionRate(normalNodes
                * normalTps + extraNodes * lowTps), new WeightedSignatureDistribution(
                environment.getCount(), lowPpm));
        Distribution peak =
            new Distribution(peakDuration, new ConstantTransactionRate(normalNodes * normalTps
                + extraNodes * highTps), new WeightedSignatureDistribution(environment.getCount(),
                highPpm));
        DistributionSequence distributions = new DistributionSequence(outsidePeak, peak);
        Runner runner = new Runner(runLength, environment, distributions);
        log.info("# test: [peak] peak.duration={} peak.period={} normal.nodes={} normal.tps={} extra.nodes={} extra.low={} extra.high={}",
            peakDuration, period, normalNodes, normalTps, extraNodes, lowTps, highTps);
        //log.info("Start: {} peak={}", description, peakDuration);
        runner.run();
        return runner.getDist();
        //log.info("End: {} peak={}", description, peakDuration);
    }

    public void executeExc(int exceptionPpm) {
        //String description = "Deviation in distribution of unique transactions.";
        int tps = 100;
        int nodes = 10;
        _executeDist(tps, nodes, exceptionPpm);
    }

    public void executeExc2(int tps) {
        //String description = "Rare and abundant transaction.";
        int nodes = 10;
        int[] exceptionPpms = {10_000, 500_000};
        _executeDist(tps, nodes, exceptionPpms);
    }

    public TreeMap<Integer, List<Signature>> _executeDist(int tps, int nodes, int... exceptionPpms) {
        //String description = "Deviation in distribution of unique transactions.";
        Environment environment = defaultEnvironment(nodes);
        DistributionSequence distributions =
            constantDistribution(environment.getCount(), tps, exceptionPpms);
        Runner runner = new Runner(runLength, environment, distributions);

        // don't ask
        String dists = "";
        for (Integer d : exceptionPpms) {
            dists += String.valueOf(d) + " ";
        }
        dists = StringUtils.strip(dists);

        log.info("# test: [dist] nodes={} tps={} distributions={}", nodes, tps, dists);
        //log.info("Start: {} tps={} nodes={} dist=[{}]", description, tps, nodes, dists);
        runner.run();
        return runner.getDist();
        //log.info("End: {} tps={} nodes={} dist=[{}]", description, tps, nodes, dists);
    }
}
