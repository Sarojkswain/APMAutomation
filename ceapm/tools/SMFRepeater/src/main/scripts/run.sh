#!/bin/sh

CLASSPATH=
for dep in `ls lib/*.jar`; do
    CLASSPATH=$dep:$CLASSPATH
done

java -cp $CLASSPATH com.ca.apm.powerpack.sysview.tools.smfrepeater.Cli $@

# vim: set tw=0:
