package com.ca.apm.systemtest.fld.plugin.windows.perfmon;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils;
import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.PerfMonitorHandle;
import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.Sample;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * Created by haiva01 on 7.9.2015.
 */
public class WindowsPerfmonPluginImpl extends AbstractPluginImpl implements WindowsPerfmonPlugin {
    private static final Logger log = LoggerFactory.getLogger(WindowsPerfmonPluginImpl.class);

    private final Map<Integer, PerfMonitorHandle> perfMonHandles
        = new TreeMap<>();
    private int handleSerialNumber = 0;

    public WindowsPerfmonPluginImpl() {
    }

    protected int addHandle(PerfMonitorHandle handle) {
        int index;

        synchronized (perfMonHandles) {
            index = ++handleSerialNumber;
            perfMonHandles.put(index, handle);
        }

        return index;
    }

    protected PerfMonitorHandle getHandle(int index) {
        synchronized (perfMonHandles) {
            return perfMonHandles.get(index);
        }
    }

    protected PerfMonitorHandle removeHandle(int index) {
        synchronized (perfMonHandles) {
            PerfMonitorHandle handle = perfMonHandles.get(index);
            perfMonHandles.remove(index);
            return handle;
        }
    }

    @Override
    public int monitor(Collection<String> metricsNames) throws Exception {
        log.info("Starting monitoring of {}", metricsNames);
        PerfMonitorHandle handle = WindowsPerfMonitorUtils.startMonitoring(metricsNames);
        int intHandle = addHandle(handle);
        log.info("Returning int handle {}", intHandle);
        return intHandle;
    }

    @Override
    public void stopMonitoring(int intHandle) throws Exception {
        log.info("Stopping monitoring for handle {}", intHandle);
        PerfMonitorHandle handle = getHandle(intHandle);
        handle.endMonitoring();
        Map<String, Collection<Sample>> samples = handle.getSamples();
        if (log.isDebugEnabled()) {
            log.debug("Samples: {}", samples);
        }
    }

    @Override
    public Map<String, Collection<Sample>> getSamples(int intHandle) throws Exception {
        PerfMonitorHandle handle = getHandle(intHandle);
        return handle.getSamples();
    }

    @Override
    public List<List<String>> getRawSamples(int intHandle) throws Exception {
        PerfMonitorHandle handle = getHandle(intHandle);
        return handle.getDataRows();

    }

    @Override
    public List<String> getHeaders(int intHandle) {
        PerfMonitorHandle handle = getHandle(intHandle);
        return handle.getHeaders();
    }

    @Override
    public void closeHandle(int intHandle) throws Exception {
        removeHandle(intHandle).close();
    }
}
