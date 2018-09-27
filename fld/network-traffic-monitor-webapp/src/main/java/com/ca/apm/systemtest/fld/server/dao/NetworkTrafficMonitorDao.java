package com.ca.apm.systemtest.fld.server.dao;

import java.io.IOException;
import java.util.List;

import com.ca.apm.systemtest.fld.server.model.NetworkTrafficMonitorValue;

public interface NetworkTrafficMonitorDao<T extends NetworkTrafficMonitorValue> {

    void update(T networkTrafficMonitorValue) throws IOException;

    T find(String host, String remoteHost, String type) throws IOException;

    List<T> findAll();

}
