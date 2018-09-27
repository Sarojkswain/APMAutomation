package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import java.text.SimpleDateFormat;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * 
 * Marker Interface to be implemented by different performance test result collector classes.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class PerfTestResultCollectionConfig {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");

    @JsonTypeInfo(use = Id.CLASS, property = "@type")
    private Collection<CollectionItem> collectionItems;
    
    public PerfTestResultCollectionConfig() {
        
    }
    
    public PerfTestResultCollectionConfig(Collection<CollectionItem> collectionItems) {
        this.collectionItems = collectionItems;
    }

    /**
     * @return the collectionItems
     */
    public Collection<CollectionItem> getCollectionItems() {
        return collectionItems;
    }

    /**
     * @param collectionItems the collectionItems to set
     */
    public void setCollectionItems(Collection<CollectionItem> collectionItems) {
        this.collectionItems = collectionItems;
    }

}
