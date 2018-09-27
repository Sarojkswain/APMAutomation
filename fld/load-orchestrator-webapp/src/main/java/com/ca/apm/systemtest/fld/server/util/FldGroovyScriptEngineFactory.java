package com.ca.apm.systemtest.fld.server.util;

import javax.script.ScriptEngine;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

/**
 * Created by haiva01 on 25.5.2015.
 */

public class FldGroovyScriptEngineFactory extends GroovyScriptEngineFactory {
    @Override
    public ScriptEngine getScriptEngine() {
        return new FldGroovyScriptEngine();
    }
}