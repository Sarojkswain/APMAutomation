#!/bin/sh

AgentDir="wily"
AgentName="ctu"

################################################################################

AGENT="$AgentDir/Agent.jar"
CONFIG="$AgentDir/core/config/IntroscopeAgent.profile"

AGENT="-javaagent:${AgentDir}/Agent.jar -Dcom.wily.introscope.agentProfile=${AgentDir}/core/config/IntroscopeAgent.profile -Dcom.wily.introscope.agent.agentName=${AgentName}"

CLASSPATH=
for dep in `ls lib/*.jar`; do
    CLASSPATH=${dep}:${CLASSPATH}
done
MAIN=com.ca.apm.powerpack.sysview.tools.ctu.Cli

${JAVA_HOME_8}/bin/java ${AGENT} -cp ${CLASSPATH} ${MAIN} $*

# vim: set tw=0:
