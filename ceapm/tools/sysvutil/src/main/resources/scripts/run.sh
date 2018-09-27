#!/bin/sh

JAVA_HOME_8=/sys/java31bt/v8r0m0/usr/lpp/java/J8.0

CLASSPATH=
for dep in `ls lib/*.jar`; do
    CLASSPATH=$dep:$CLASSPATH
done

${JAVA_HOME_8}/bin/java -cp ${CLASSPATH} com.ca.apm.powerpack.sysview.tools.sysvutil.Cli $@
