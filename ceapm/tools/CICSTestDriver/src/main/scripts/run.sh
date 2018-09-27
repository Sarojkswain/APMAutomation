#!/bin/sh

### Configuration
# AgentDir should be set to the absolute path of an Agent installation.
AgentDir="wily"

# AgentName is the name under which the test application will appear in EM.
AgentName="CICSTestDriver"

# CTGHost
CTGHost="usilca31"

# CTGPort
CTGPort=2008

# CICS IPIC
CICSIPIC="C660IPIC"

# Do not modify anything below this line unless you know what you're doing.
################################################################################

CTD_DIR=`dirname $0`

function listTestDefinitions {
    echo "Available test definitions:";
    pushd ${CTD_DIR}/xml >/dev/null 2>&1
    find . -name "*.xml" | grep -v "mapping.xml" | cut -b 3- | sed 's/^/    /'
    popd >/dev/null 2>&1
}

if [ -z "$AgentDir" ]; then
    echo "AgentDir has not been configured (see top of this script).";
    exit 1;
fi

AGENT="$AgentDir/Agent.jar"
CONFIG="$AgentDir/core/config/IntroscopeAgent.profile"

if [ ! -r $AGENT -o ! -r $CONFIG ]; then
    echo "The configured AgentDir is not a proper Agent installation directory: $AgentDir";
    exit 1;
fi

MAIN=com.ca.apm.powerpack.sysview.tools.cicstestdriver.CICSTestDriver

# Dynamically build a classpath based on the contents of the lib directory
CLASSPATH=
for dep in `ls ${CTD_DIR}/lib/*.jar`; do
    CLASSPATH=$dep:$CLASSPATH
done

if [ $# -lt 1 ]; then
    echo "Missing test definition.";
    listTestDefinitions;
    exit 1;
elif [ -r $1 ]; then
    XML=$1
elif [ -r ${CTD_DIR}/xml/$1 ]; then
    XML=${CTD_DIR}/xml/$1
else
    echo "Invalid test definition specified: ${$1}";
    listTestDefinitions;
    exit 1;
fi

PARMS="$2 JGATE tcp://$CTGHost jgateport=$CTGPort server=$CICSIPIC conduit commarea=D180 EBCDIC NOCOMMAREA"

java -javaagent:$AGENT -Dcom.wily.introscope.agentProfile=$CONFIG -Dcom.wily.introscope.agent.agentName=$AgentName -cp $CLASSPATH $MAIN -mapfile ${CTD_DIR}/xml/mapping.xml -xmlfile $XML $PARMS

# vim: set tw=0:
