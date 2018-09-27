package com.ca.apm.systemtest.fld.util.networktrafficmonitor;

import static com.ca.apm.systemtest.fld.util.networktrafficmonitor.Util.sleep;

import java.io.IOException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

public class NetworkTrafficChartGeneratorRunner {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(NetworkTrafficChartGeneratorRunner.class);

    public static final String NETWORK_TRAFFIC_CHART_GENERATOR = "NetworkTrafficChartGenerator";

    private static final int DEFAULT_ITERATION_COUNT = -1; // => infinite loop mode

    private static int iterationCount = DEFAULT_ITERATION_COUNT;

    private static ArgumentParser parser;

    private NetworkTrafficChartGeneratorRunner() {}

    public static void main(String[] args) throws IOException {
        LOGGER.info("NetworkTrafficChartGeneratorRunner.main():: entry");
        try (ClassPathXmlApplicationContext applicationContext =
            new ClassPathXmlApplicationContext("network-traffic-monitor/applicationContext.xml");) {
            Namespace namespace = parseArgs(args);
            init(namespace);
            System.out.println(NETWORK_TRAFFIC_CHART_GENERATOR);
            int iteration = 1;

            Configuration configuration =
                (Configuration) applicationContext.getBean("configuration");
            long waitInterval = configuration.getWaitInterval();

            ChartGenerationService chartGenerationService =
                (ChartGenerationService) applicationContext.getBean("chartGenerationService");

            // limited count of iterations mode
            if (iterationCount > 0) {
                LOGGER
                    .info(
                        "NetworkTrafficChartGeneratorRunner.main():: limited count of iterations mode: {}",
                        iterationCount);
                for (int i = 0; i < iterationCount; i++) {
                    LOGGER
                        .info(
                            "NetworkTrafficChartGeneratorRunner.main():: iteration {}: start chart data harvesting",
                            iteration);
                    // generate graphs
                    try {
                        chartGenerationService.generateCharts();
                    } catch (Exception e) {
                        ErrorUtils.logExceptionFmt(LOGGER, e,
                            "NetworkTrafficChartGeneratorRunner.main():: cannot create chart: {0}");
                    }

                    if ((iterationCount > 1) && (i < iterationCount - 1)) {
                        LOGGER.info(
                            "NetworkTrafficChartGeneratorRunner.main():: iteration {}: waiting",
                            iteration);
                        sleep(waitInterval);
                    }
                    iteration++;
                }
            }

            // infinite loop mode
            else {
                LOGGER.info("NetworkTrafficChartGeneratorRunner.main():: infinite loop mode");
                while (true) {
                    LOGGER
                        .info(
                            "NetworkTrafficChartGeneratorRunner.main():: iteration {}: start chart data harvesting",
                            iteration);
                    // generate graphs
                    try {
                        chartGenerationService.generateCharts();
                    } catch (Exception e) {
                        ErrorUtils.logExceptionFmt(LOGGER, e,
                            "NetworkTrafficChartGeneratorRunner.main():: cannot create chart: {0}");
                    }
                    LOGGER.info(
                        "NetworkTrafficChartGeneratorRunner.main():: iteration {}: waiting",
                        iteration);
                    sleep(waitInterval);
                    iteration++;
                }
            }
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(LOGGER, e,
                "NetworkTrafficChartGeneratorRunner.main():: exception occurred: {0}");
        } finally {
            LOGGER.info("NetworkTrafficChartGeneratorRunner.main():: exit");
        }
    }

    private static Namespace parseArgs(String[] args) {
        parser =
            ArgumentParsers
                .newArgumentParser(NetworkTrafficChartGeneratorRunner.class.getName())
                .description(
                    "NetworkTrafficChartGeneratorRunner generates graph images from a source csv file and then sends it to the NetworkTrafficMonitor webapp");

        parser.addArgument("-i", "-iterationCount").dest("iterationCount").type(Integer.class)
            .action(Arguments.store())
            .help("Count of iterations, non-positive value means infinite loop mode")
            .setDefault(DEFAULT_ITERATION_COUNT);

        Namespace namespace = parser.parseArgsOrFail(args);
        LOGGER.debug("NetworkTrafficChartGeneratorRunner.parseArgs():: namespace = {}", namespace);
        return namespace;
    }

    private static void init(Namespace namespace) {
        iterationCount = namespace.getInt("iterationCount");
    }

}
