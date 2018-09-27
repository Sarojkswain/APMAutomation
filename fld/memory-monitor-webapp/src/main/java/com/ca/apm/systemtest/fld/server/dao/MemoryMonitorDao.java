package com.ca.apm.systemtest.fld.server.dao;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface MemoryMonitorDao<T extends Serializable> {

    void update(T value) throws IOException;

    T find(String id) throws IOException;

    List<T> findAll();

}
