/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.List;

/**
 * FlowContext for collecting and storing logs using FluentD
 * @author shadm01
 */
public class FluentdFlowContext implements IFlowContext {
    public final static String INTROSCOPE_PATTERN = "/^(?<time>.*CES{0,1}T) \\[(?<severity>[A-Z]+)\\] \\[(?<machine>[\\S ]+)\\] \\[(?<method>[\\S]+)\\] (?<message>.*)$/";
    public final static String INTROSCOPE_DATE_PATTERN = "%m/%d/%y %I:%M:%S.%L %p %Z";

    public final static String PERFLOG_PATTERN =
            "/^(?<time>.*CEST),(?<jvm_mem_total>\\d+),(?<jvm_mem_free>\\d+),(?<jvm_time_percent>\\d+)," +
            "(?<number_workstations>\\d+),(?<harvest_duration>\\d+),(?<smartstor_duration>\\d+)," +
                    "(?<agent_connection_throttle_count>\\d+),(?<agent_num_metrics>\\d+)," +
                    "(?<num_historic_metrics>\\d+),(?<agent_numer_agents>\\d+),(?<agent_metric_data_pending>\\d+)," +
                    "(?<agent_metric_data_rate>\\d+),(?<server_number_metric_groups>\\d+)," +
                    "(?<trans_n_inserts_per_intervla>\\d+),(?<trans_n_dropped_per_interval>\\d+)," +
                    "(?<trans_n_querries_per_interval>\\d+),(?<trans_query_time_per_interval>\\d+)," +
                    "(?<trans_index_ins_time>\\d+),(?<trans_data_ins_time>\\d+),(?<trans_num_traces>\\d+)," +
                    "(?<trans_TT_queue_size>\\d+),(?<mm_queryrate>\\d+),(?<mm_queue_memory>\\d+)," +
                    "(?<mom_coll_metrics_per_interval>\\d+),(?<persist_ins_per_interval>\\d+)," +
                    "(?<number_of_map_entity_m_groups>\\d+),(?<queries_per_interval>\\d+)," +
                    "(?<queries_duration_ms>\\d+).*$/";
    public final static String PERFLOG_DATE_PATTERN = "%m/%d/%y %I:%M:%S %p %Z";

    public final static String GC_PATTERN = "/^(?<timestamp>[\\d\\.]+): \\[GC \\((?<reason>[a-zA-Z ]+)\\)  (?<memory_info>[0-9K\\(\\)\\-\\>]+), (?<duration>[0-9.]+).*$/"; //No date pattern here, since it is in milliseconds

    private String configFileContent;
    private List<String> outputFolers = new ArrayList<>();

    protected FluentdFlowContext(Builder builder) {
        this.configFileContent = builder.configFileContent;
        this.outputFolers = builder.outputFolers;
    }

    public List<String> getOutputFolers() {
        return outputFolers;
    }

    public String getConfigFileContent() {
        return configFileContent;
    }

    public static class Builder extends BuilderBase<Builder, FluentdFlowContext> {
        String configFileContent;

        private final List<String> inputs = new ArrayList<>();
        private final List<String> outputs = new ArrayList<>();
        private final List<String> outputFolers = new ArrayList<>();

        public Builder() {
        }

        @Override
        public FluentdFlowContext build() {
            FluentdFlowContext flowContext = getInstance();

            flowContext.configFileContent = createConfigFile();
            Args.notNull(flowContext.configFileContent, "FluentD config file contents");

            return flowContext;
        }

        @Override
        protected FluentdFlowContext getInstance() {
            return new FluentdFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder addInputFromTail(String tag, String path) {
            inputs.add(new TailInput(tag, path).toString());
            return this;
        }

        public Builder addInputFromTail(String tag, String path, String format, String time_format) {
            inputs.add(new TailInput(tag, path, format, time_format).toString());
            return this;
        }

        public Builder addInputFromScoket(String port) {
            inputs.add(new SocketInput(port).toString());
            return this;
        }

        public Builder addPlainConfig(String config) {
            inputs.add(config);
            return this;
        }


        public Builder addOutputToSocket(String tag, String host, String port) {
            outputs.add(new SocketOutput(tag, host, port).toString());
            return this;
        }

        public Builder addOutputToFile(String tag, String filePath, boolean compress) {
            outputs.add(new FileOutput(tag, filePath, compress).toString());
            outputFolers.add(filePath);
            return this;
        }

        public Builder addElasticSearchOutput(String tag, String host, String port) { //TODO - remove <match> folders until we know that there are not two of same log types
            outputs.add(new ElasticSearchOutput(tag, host, port).toString());
            return this;
        }


        public Builder addMultipleOutputs(String tag, OutputPlugin... plugins) {
            StringBuilder sb = new StringBuilder();
            sb.append("<match ").append(tag).append(">").append("\n");
            sb.append("@type copy\n");

            //TODO - DM - Ensure that you are using same tag in all instances!
            for (OutputPlugin plugin : plugins) {
                sb.append(plugin.toCopyString());
            }

            sb.append("</match>\n");
            sb.append("\n");

            outputs.add(sb.toString());

            return this;
        }

        private String createConfigFile() {
            StringBuilder sb = new StringBuilder();
            for (String input : inputs) {
                sb.append(input).append("\n");
            }

            for (String output : outputs) {
                sb.append(output).append("\n");
            }

            return sb.toString();
        }
    }

    public static abstract class OutputPlugin {
        String tag;

        protected abstract String toPrintableString();

        final public String toCopyString() {
            return "<store>\n" +
                    toPrintableString() +
                    "</store>\n";
        }

        public String toStandaloneString() {
            return String.format(
                    "<match %s>\n" +
                            toPrintableString() +
                            "</match>\n",
                    this.tag
            );
        }

        public String toString() {
            return toStandaloneString();
        }
    }

    private static class TailInput {
        String tag;
        String path;
        String format;
        String time_format = "";

        public TailInput(String tag, String path) {
            this.tag = tag;
            this.path = path;
            this.format = "none";
        }

        public TailInput(String tag, String path, String format, String time_format) {
            this.tag = tag;
            this.path = path;
            this.format = format;
            this.time_format = time_format;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<source>\n");
            sb.append("  @type tail\n");
            sb.append("  format ").append(this.format).append("\n");

            if (!time_format.isEmpty()) {
                sb.append("  time_format ").append(this.time_format).append("\n");
            }

            sb.append("  path ").append(this.path).append("\n");
            sb.append("  tag ").append(this.tag).append("\n");
            sb.append("</source>\n");

            return sb.toString();
        }
    }

    private static class SocketInput {
        String port;

        public SocketInput(String port) {

            this.port = port;
        }

        @Override
        public String toString() {
            return String.format(
                    "<source>\n" +
                            "  type forward\n" +
                            "  port %s\n" +
                            "</source>\n",
                    this.port
            );
        }
    }

    public static class SocketOutput extends OutputPlugin {
        String host;
        String port;

        public SocketOutput(String tag, String host, String port) {
            this.tag = tag;
            this.host = host;
            this.port = port;
        }

        @Override
        protected String toPrintableString() {
            return
                    "  @type forward\n" +
                            "  send_timeout 60s\n" +
                            "  recover_wait 10s\n" +
                            "  heartbeat_interval 1s\n" +
                            "  phi_threshold 16\n" +
                            "  hard_timeout 60s\n" +
                            "\n" +
                            "  <server>\n" +
                            "    host "+this.host+"\n" +
                            "    port "+this.port+"\n" +
                            "  </server>\n";
        }
    }

    public static class FileOutput extends OutputPlugin {
        String path;
        Boolean compress;

        public FileOutput(String tag, String path, boolean compress) {
            this.tag = tag;
            this.path = path;
            this.compress = compress;
        }

        @Override
        public String toPrintableString() {
            StringBuilder sb = new StringBuilder();

            sb.append("@type file").append("\n");
            sb.append("path ").append(this.path).append("\n");

            if (compress) {
                sb.append("compress gzip").append("\n");
            }

            return sb.toString();
        }
    }

    public static class ElasticSearchOutput extends OutputPlugin {
        String hostname;
        String port;

        public ElasticSearchOutput(String tag, String hostname, String port) {
            this.tag = tag;
            this.hostname = hostname;
            this.port = port;
        }

        @Override
        public String toPrintableString() {
            return "@type elasticsearch" + "\n" +
                    "host " + hostname + "\n" +
                    "port " + port + "\n" +
                    "include_tag_key true" + "\n" +
                    "tag_key @log_name" + "\n" +
                    "logstash_format true" + "\n" + //TODO - DM - maybe not ?
                    "flush_interval 10s" + "\n";
        }
    }
}
