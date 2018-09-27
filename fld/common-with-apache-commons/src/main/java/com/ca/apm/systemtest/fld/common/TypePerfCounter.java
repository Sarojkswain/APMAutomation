package com.ca.apm.systemtest.fld.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

/**
 * TypePerf counter object contains information on a performance counter for Windows typeperf utility. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TypePerfCounter {
    //Perf counter name templates
    public static final String PROCESS_ID_COUNTER_TEMPLATE                    = "\\Process({0})\\ID Process";
    public static final String PROCESS_PRIVATE_BYTES_COUNTER_TEMPLATE         = "\\Process({0})\\Private Bytes";
    public static final String PROCESS_WORKING_SET_PRIVATE_COUNTER_TEMPLATE   = "\\Process({0})\\Working Set - Private";
    public static final String PROCESS_WORKING_SET_COUNTER_TEMPLATE           = "\\Process({0})\\Working Set";
    public static final String PROCESS_HANDLE_COUNT_COUNTER_TEMPLATE          = "\\Process({0})\\Handle Count";
    public static final String PROCESS_THREAD_COUNT_COUNTER_TEMPLATE          = "\\Process({0})\\Thread Count";
    public static final String PROCESS_CPU_TIME_COUNTER_TEMPLATE              = "\\Process({0})\\% Processor Time";
    
    //Perf counter names
    public static final String PROCESSOR_TOTAL_CPU_TIME_COUNTER               = "\\Processor(_Total)\\% Processor Time";

    private String performanceCounterTemplate;
    private String instanceName;
    private String performanceCounter;

    private TypePerfCounter(String performanceCounterTemplate, String instanceName,
        String performanceCounter) {
        this.performanceCounterTemplate = performanceCounterTemplate;
        this.instanceName = instanceName;
        this.performanceCounter = performanceCounter;
    }

    private TypePerfCounter(String performanceCounter) {
        this(null, null, performanceCounter);
    }

    /**
     * @return the performanceCounterTemplate
     */
    public String getPerformanceCounterTemplate() {
        return performanceCounterTemplate;
    }

    /**
     * @return the instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * @return the performanceCounter
     */
    public String getPerformanceCounter() {
        return performanceCounter;
    }
    
    @Override
    public String toString() {
        return performanceCounter;
    }

    /**
     * Parses performance counters from string.
     * 
     * <code>perfCountersStr</code> is considered to list performance counter names if <code>instanceName</code> is <code>null</code>; 
     * otherwise if <code>instanceName</code> is not <code>null</code> it is considered to list performance counter name patterns in the 
     * format of {@link MessageFormat}, i.e. containing <code>{0}</code> placeholders to be replaced by the instance name.  
     * 
     * @param perfCountersStr   performance counter names or patterns, delimited by comma
     * @param instanceName      instance name 
     * @return                  collection of perf counter objects
     */
    public static Collection<TypePerfCounter> parsePerfCounters(String perfCountersStr, String instanceName) {
        String[] perfCounterPatterns = StringUtils.split(perfCountersStr, ',');
        ArrayList<TypePerfCounter> perfCounters = new ArrayList<>(perfCounterPatterns != null ? perfCounterPatterns.length : 0);
        if (perfCounterPatterns != null) {
            for (String perfCounterPattern : perfCounterPatterns) {
                if (instanceName != null) {
                    perfCounters.add(createPerfCounter(perfCounterPattern, instanceName));
                } else {
                    perfCounters.add(createPerfCounter(perfCounterPattern));
                }
            }
        }
        return perfCounters;
    }
    
    /**
     * Creates a typeperf counter object given a counter template and instance name.
     * 
     * @param perfCounterNameTemplate   performance counter name template; should follow format of {@link MessageFormat}'s pattern
     * @param instanceName              instance name; can be a full name or a regexp acceptable by the typeperf tool
     * @return
     */
    public static TypePerfCounter createPerfCounter(String perfCounterNameTemplate, String instanceName) {
        String perfCounter = MessageFormat.format(perfCounterNameTemplate, instanceName);
        return new TypePerfCounter(perfCounterNameTemplate, instanceName, perfCounter);
    }
    
    public static TypePerfCounter createPerfCounter(String perfCounter) {
        return new TypePerfCounter(perfCounter);
    }

}
