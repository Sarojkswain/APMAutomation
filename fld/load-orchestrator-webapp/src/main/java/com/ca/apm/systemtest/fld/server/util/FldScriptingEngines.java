package com.ca.apm.systemtest.fld.server.util;

import javax.script.ScriptEngineManager;

import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;

/**
 * Created by haiva01 on 25.5.2015.
 */
public class FldScriptingEngines extends ScriptingEngines {
    public FldScriptingEngines(
        ScriptBindingsFactory scriptBindingsFactory) {
        super(scriptBindingsFactory);
        cachedEngines.put("groovy", new FldGroovyScriptEngine());
    }

    public FldScriptingEngines(ScriptEngineManager scriptEngineManager) {
        super(scriptEngineManager);
    }
}
