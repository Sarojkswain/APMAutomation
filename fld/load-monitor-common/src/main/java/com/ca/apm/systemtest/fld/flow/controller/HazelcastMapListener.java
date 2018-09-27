package com.ca.apm.systemtest.fld.flow.controller;

import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

public interface HazelcastMapListener<K, V> extends EntryAddedListener<K, V>, EntryUpdatedListener<K, V> {

}
