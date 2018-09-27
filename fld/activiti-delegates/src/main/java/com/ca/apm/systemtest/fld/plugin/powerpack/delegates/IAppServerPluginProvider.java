package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import org.activiti.engine.delegate.DelegateExecution;

/**
 * Created by shadm01 on 17-Aug-15.
 */
public interface IAppServerPluginProvider {

    public AppServerPlugin getPlugin(DelegateExecution execution);

}
