package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

/**
 * Universal FLD plugin to collect different types of resources into specified target location(s).
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@PluginAnnotationComponent(pluginType = DefaultPerfResultsCollectorPluginImpl.PLUGIN)
public class DefaultPerfResultsCollectorPluginImpl extends AbstractPluginImpl implements PerformanceTestResultsCollectorPlugin  {
    public static final String PLUGIN = "defaultPerfResultsCollectorPlugin";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPerfResultsCollectorPluginImpl.class);

    @Override
    @ExposeMethod(description = "Collects different kinds of resources into specified location or locations.")
    public void collect(PerfTestResultCollectionConfig config) {
        if (config == null) {
            String msg = "Result log collection configuration object must not be null!";
            error(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        if (config.getCollectionItems() == null) {
            String msg = "Result collection items must not be null!";
            error(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        if (config.getCollectionItems().isEmpty()) {
            String msg = "Result collection items must not be empty!";
            error(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        Collection<CollectionItem> collectionItems = config.getCollectionItems();
        for (CollectionItem collectionItem : collectionItems) {
            if (collectionItem.getPlugin() == null) {
                collectionItem.setPlugin(this);
            }
            collectionItem.runCollection();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
