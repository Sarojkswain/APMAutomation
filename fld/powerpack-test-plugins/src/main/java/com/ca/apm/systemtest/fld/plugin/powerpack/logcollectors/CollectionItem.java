package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;


/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public abstract class CollectionItem {

    protected AbstractPluginImpl plugin; 
    protected boolean isMove;
    protected boolean ignoreCleanupErrors;
    
    public CollectionItem() {
        this.isMove = true;
    }
    
    public CollectionItem(AbstractPluginImpl plugin) {
        this(plugin, true);
    }
    
    public CollectionItem(boolean isMove) {
        this(null, isMove);
    }
    
    public CollectionItem(AbstractPluginImpl plugin, boolean isMove) {
        this(plugin, isMove, false);
    }

    public CollectionItem(AbstractPluginImpl plugin, boolean isMove, boolean ignoreCleanupErrors) {
        this.plugin = plugin;
        this.isMove = isMove;
        this.ignoreCleanupErrors = ignoreCleanupErrors;
    }

    /**
     * @return the ignoreCleanupErrors
     */
    public boolean isIgnoreCleanupErrors() {
        return ignoreCleanupErrors;
    }

    /**
     * @param ignoreCleanupErrors the ignoreCleanupErrors to set
     */
    public void setIgnoreCleanupErrors(boolean ignoreCleanErrors) {
        this.ignoreCleanupErrors = ignoreCleanErrors;
    }

    /**
     * @return the isMove
     */
    public boolean isMove() {
        return isMove;
    }

    /**
     * @param isMove the isMove to set
     */
    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    /**
     * @return the plugin
     */
    public AbstractPluginImpl getPlugin() {
        return plugin;
    }

    /**
     * @param plugin the plugin to set
     */
    public void setPlugin(AbstractPluginImpl plugin) {
        this.plugin = plugin;
    }

    /**
     * 
     */
    public abstract void runCollection();

    protected void logInfo(String pattern, Object... arguments) {
        if (plugin != null) {
            plugin.info(pattern, arguments);
        }
    }

    protected void logError(String msg) {
        if (plugin != null) {
            plugin.error(msg);
        }
    }
    
    protected void logError(String msg, Throwable ex) {
        if (plugin != null) {
            plugin.error(msg, ex);
        }
    }

    
    protected void logWarn(Throwable ex, String pattern, Object... arguments) {
        if (plugin != null) {
            plugin.warn(ex, pattern, arguments);
        }
    }
}