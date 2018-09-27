package com.ca.apm.systemtest.fld.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Windows performance counters monitoring utilities.
 * <p>
 * <p>
 * Created by haiva01 on 3.9.2015.
 */
public class WindowsPerfMonitorUtils {
    public static final DateFormat DATE_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final Logger log = LoggerFactory.getLogger(WindowsPerfMonitorUtils.class);

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * This function starts given metrics monitoring.
     *
     * @param metrics list of metrics names
     * @return handle
     * @throws Exception
     */
    public static PerfMonitorHandle startMonitoring(Collection<String> metrics) throws Exception {
        PerfMonitorHandle handle = new TypePerfHandle(metrics);
        handle.startMonitoring();
        return handle;
    }

    /**
     * This function ends monitoring for given handle.
     *
     * @param handle handle
     * @return handle
     * @throws Exception
     */
    public static PerfMonitorHandle endMonitoring(PerfMonitorHandle handle) throws Exception {
        handle.endMonitoring();
        return handle;
    }


    public interface Sample {
        String getValue();

        void setValue(String value);

        @JsonSerialize(using = CustomDateSerializer.class)
        @JsonDeserialize(using = CustomDateDeserializer.class)
        Date getDate();

        void setDate(Date date);
    }


    public interface PerfMonitorHandle extends AutoCloseable {
        Collection<String> getRequestedMetricNames();

        Collection<String> getRecordedMetricNames();

        Map<String, Collection<Sample>> getSamples();

        List<String> getHeaders();

        List<List<String>> getDataRows();

        void startMonitoring() throws Exception;

        void endMonitoring() throws Exception;
    }

    protected static class SampleImpl implements Sample {
        @JsonIgnore
        public String metricValue;
        @JsonIgnore
        public Date date;

        public SampleImpl(String metricValue, Date date) {
            this.metricValue = metricValue;
            this.date = date;
        }

        @Override
        public String getValue() {
            return metricValue;
        }

        @Override
        public void setValue(String value) {
            metricValue = value;
        }

        @Override
        public Date getDate() {
            return date;
        }

        @Override
        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "SampleImpl{"
                + ", " + metricValue + '\''
                + ", " + DATE_FORMAT.format(date) + '}';
        }
    }


    public static class MonitoringNotRunning extends Exception {
        public MonitoringNotRunning(String message) {
            super(message);
        }
    }


    /**
     * This class uses the <code>typeperf</code> command to read Windows performance counter.
     */
    protected static class TypePerfHandle implements PerfMonitorHandle {
        private static final String TYPEPERF_EXE = "typeperf.exe";
        private static final DateFormat CSV_DATE_FORMAT = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss.SSS", Locale.ENGLISH);

        private static CSVFormat TYPEPERF_CSV_FORMAT = CSVFormat.newFormat(',')
            .withHeader().withQuote('"').withIgnoreEmptyLines().withIgnoreSurroundingSpaces();

        private File csvFile;
        private File metricsListFile;
        private StartedProcess process;
        private Set<String> metrics = new TreeSet<>();
        private Set<String> recordedMetrics;
        private Map<String, Collection<Sample>> samples;
        private List<String> headers;
        private List<List<String>> dataRows;

        public TypePerfHandle(Collection<String> metrics) {
            this.metrics.addAll(metrics);
        }

        @Override
        public Collection<String> getRequestedMetricNames() {
            return new TreeSet<>(metrics);
        }

        @Override
        public Collection<String> getRecordedMetricNames() {
            return new TreeSet<>(recordedMetrics);
        }

        @Override
        public Map<String, Collection<Sample>> getSamples() {
            return samples;
        }

        @Override
        public List<String> getHeaders() {
            return headers;
        }

        @Override
        public List<List<String>> getDataRows() {
            return dataRows;
        }

        @Override
        public void startMonitoring() throws IOException {
            log.debug("Starting monitoring.");
            final File tempDir = ACFileUtils.createTemporaryDirectory("typeperf");
            csvFile = ACFileUtils.generateTemporaryFile("typeperf", ".csv", tempDir);
            csvFile.deleteOnExit();
            metricsListFile = ACFileUtils.generateTemporaryFile("typeperf-metrics", ".txt", tempDir);
            metricsListFile.deleteOnExit();
            FileUtils.writeLines(metricsListFile, metrics, "\r\n");
            tempDir.deleteOnExit();

            List<String> cmd = new ArrayList<>(10);
            cmd.add(TYPEPERF_EXE);
            // "Yes" to all questions.
            cmd.add("-y");
            // Use 1 second sampling intervals.
            cmd.add("-si");
            cmd.add("1");
            //cmd.add("-f");
            //cmd.add("CSV");
            // Output file.
            cmd.add("-o");
            cmd.add(csvFile.getAbsolutePath());
            // Metrics to gather.
            cmd.add("-cf");
            cmd.add(metricsListFile.getAbsolutePath());

            // Remove CLASSPATH from environment. It is not needed by typeperf.exe and it eats into
            // the limit of command line length.

            Map<String, String> env = new TreeMap<>(System.getenv());
            env.put("CLASSPATH", null);

            // Run typperf.exe.

            ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
                .command(cmd)
                .environment(env);
            process = pe.start();
        }

        @Override
        public void endMonitoring() throws Exception {
            log.debug("Stopping monitoring.");
            if (!(process != null && process.getProcess() != null)) {
                log.error("Monitoring is not running. Cannot end it.");
                return;
            }

            process.getProcess().destroy();
            process = null;

            CSVParser csvParser;
            try {
                csvParser = CSVParser.parse(csvFile, StandardCharsets.ISO_8859_1,
                    TYPEPERF_CSV_FORMAT);
            } catch (Exception ex) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                    "Error parsing CSV from typeperf. Exception: {0}");
            }

            // Process CSV headers.

            recordedMetrics = new TreeSet<>();
            final Collection<Map.Entry<String, Integer>> columns = new ArrayList<>(10);
            int maxIndex = 0;
            for (Map.Entry<String, Integer> entry : csvParser.getHeaderMap().entrySet()) {
                int index = entry.getValue();
                if (index == 0) {
                    // Skip first column. It is the timestamp field.
                    continue;
                }
                columns.add(entry);
                recordedMetrics.add(entry.getKey());
                maxIndex = Math.max(maxIndex, entry.getValue());
            }

            headers = new ArrayList<>(csvParser.getHeaderMap().keySet());

            // Populate samples collections list with empty collections of samples.

            @SuppressWarnings("unchecked")
            Collection<Sample>[] collectionsArray = (Collection<Sample>[]) Array.newInstance(
                Collection.class, maxIndex + 1);
            for (int i = 0; i != maxIndex + 1; ++i) {
                collectionsArray[i] = new ArrayList<>(10);
            }
            final List<Collection<Sample>> collections = Arrays.asList(collectionsArray);

            // Populate samples map with header names and empty collections.

            samples = new TreeMap<>();
            for (Map.Entry<String, Integer> entry : csvParser.getHeaderMap().entrySet()) {
                samples.put(entry.getKey(), collections.get(entry.getValue()));
            }

            // Parse the data records.

            dataRows = new ArrayList<>(10);
            Map<String, String> valueStrings = new HashMap<>(10);
            try {
                for (CSVRecord rec : csvParser) {
                    final List<String> dataRow = new ArrayList<>(headers.size());
                    dataRows.add(dataRow);

                    final Date timeStamp = CSV_DATE_FORMAT.parse(rec.get(0));
                    dataRow.add(DATE_FORMAT.format(timeStamp));

                    for (Map.Entry<String, Integer> colIndex : columns) {
                        int index = colIndex.getValue();
                        final String valueString = rec.get(index);
                        String metricValue = valueStrings.get(valueString);
                        if (metricValue == null) {
                            valueStrings.put(valueString, valueString);
                            metricValue = valueString;
                        }

                        dataRow.add(metricValue);
                        if (StringUtils.isNotBlank(valueString)) {
                            final Sample sample = new SampleImpl(metricValue, timeStamp);
                            collections.get(index).add(sample);
                        }
                    }
                }
            } catch (Exception ex) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                    "Error parsing CSV from typeperf. Exception: {0}");
            }

            csvFile = null;

            if (log.isTraceEnabled()) {
                log.trace("Samples parsed:\n{}", samples);
            }
        }

        /**
         * Closes this resource, relinquishing any underlying resources.
         * This method is invoked automatically on objects managed by the
         * {@code try}-with-resources statement.
         *
         * @throws Exception if this resource cannot be closed
         */
        @Override
        public void close() throws Exception {
            if (process != null && process.getProcess() != null) {
                endMonitoring();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if (process != null) {
                try {
                    process.getProcess().destroy();
                } catch (Exception ex) {
                    // Just eat it.
                }
            }
            super.finalize();
        }
    }

    public static class CustomDateSerializer extends JsonSerializer<Date> {
        public CustomDateSerializer() {
        }

        @Override
        public void serialize(Date value, JsonGenerator gen,
            SerializerProvider arg2) throws IOException {

            gen.writeString(DATE_FORMAT.format(value));
        }
    }

    public static class CustomDateDeserializer extends JsonDeserializer<Date> {
        public CustomDateDeserializer() {
        }

        @Override
        public Date deserialize(JsonParser p,
            DeserializationContext ctxt) throws IOException {
            final String text = p.getText();
            Date date;
            try {
                date = DATE_FORMAT.parse(text);
            } catch (ParseException e) {
                String msg = ErrorUtils.logExceptionFmt(log, e,
                    "Error parsing date from string {1}. Exception: {0}", text);
                throw new DateParsingError(msg, e);
            }
            return date;
        }

        private static class DateParsingError extends JsonProcessingException {
            public DateParsingError(String msg, Throwable rootCause) {
                super(msg, rootCause);
            }
        }

    }
}
