#!/bin/sh

# Be more Bourne compatible
DUALCASE=1; export DUALCASE # for MKS sh
if test -n "${ZSH_VERSION+set}" && (emulate sh) >/dev/null 2>&1; then :
  emulate sh
  # Pre-4.2 versions of Zsh do word splitting on ${1+"$@"}, which
  # is contrary to our usage.  Disable this feature.
  alias -g '${1+"$@"}'='"$@"'
  setopt NO_GLOB_SUBST
else
  case `(set -o) 2>/dev/null` in #(
  *posix*) :
    set -o posix ;; #(
  *) :
     ;;
esac
fi

find_apm_agent_jar() {
    find . -name 'agent-apm-agent-*-jar-with-dependencies.jar' -print | {
        while read line ; do
	    echo $line
	    return
        done
    }
}

# ******************************************************************************
# Start agent for Unix
# ******************************************************************************
# Use RESTART_LEVEL 1 when you would like to restart also in case of runtime
# exception otherwise use RESTART_LEVEL 50
RESTART_LEVEL=1

cd "`dirname $0`"
THIS_DIR="`pwd`"

unset exitCode
while [ ${exitCode:-100} -ge $RESTART_LEVEL ]; do
    if [ -z "$exitCode" ]; then
        echo "--------------------------------------------------------------------------------"
        echo "Starting Agent on Unix"
    else
        echo "------------------------------"
        echo "Agent RELOAD (exit code $exitCode)"
    fi

    # Create link if missing
    if [ ! -L 'agent-current' ]; then
        ln -s . agent-current
    fi

    # Setup classpath
    CLASSPATH="agent-current/lib/*"
    CLASSPATH=".:${CLASSPATH}"
    if [ ! -z "$PRE_CLASSPATH" ] ; then
        CLASSPATH="$PRE_CLASSPATH:$CLASSPATH"
    fi
    export CLASSPATH
    echo CLASSPATH=$CLASSPATH

    AGENT_APM_AGENT_JAR=`find_apm_agent_jar`
    if [ ! -z "$AGENT_APM_AGENT_JAR" \
	-a -r "$AGENT_APM_AGENT_JAR" ] ; then
	APM_AGENT="-javaagent:$AGENT_APM_AGENT_JAR"
	if [ ! -z "$INTROSCOPEAGENT_PROFILE" \
	    -a -r "$INTROSCOPEAGENT_PROFILE" ] ; then
	    # Do nothing. We will use the $INTROSCOPEAGENT_PROFILE variable.
	    :
	fi
	if [ -z "$INTROSCOPEAGENT_PROFILE" \
	    -a -r "$THIS_DIR/IntroscopeAgent.profile" ] ; then
	    INTROSCOPEAGENT_PROFILE="$THIS_DIR/IntroscopeAgent.profile"
	fi
	if [ -z "$INTROSCOPEAGENT_PROFILE" \
	    -a -r "$THIS_DIR/wily/core/config/IntroscopeAgent.profile" ] ; then
	    INTROSCOPEAGENT_PROFILE="$THIS_DIR/wily/core/config/IntroscopeAgent.profile"
	fi
	if [ ! -z "$INTROSCOPEAGENT_PROFILE" \
	    -a -r "$INTROSCOPEAGENT_PROFILE" ] ; then
	    APM_AGENT+=" -Dcom.wily.introscope.agentProfile="
	    APM_AGENT+="$INTROSCOPEAGENT_PROFILE"
	else
	    APM_AGENT+=" -Dcom.wily.introscope.agent.agentName=FLDAgent"
	    APM_AGENT+=" -Dintroscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=aqpp-em01"
	fi
    else
	APM_AGENT=
    fi

    JAVA_XX_FLAGS="-XX:MaxHeapFreeRatio=30 -XX:MinHeapFreeRatio=10"

    # Trap SIGINT / CTRL-C so that this cript does not exit
    # imediately with the Java process being interrupted.
    trap ':' INT
    {
        set -x
        java \
	    $JAVA_XX_FLAGS \
            "-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n" \
	    $APM_AGENT \
            "-Dfld.agent.property.file=fld-agent-production.properties" \
            "-Dfld.agent.node.name=`hostname`" \
            "-Dlog4j.configuration=file:$PWD/log4j.properties" \
            com.ca.apm.systemtest.fld.agent.Agent \
            fldagent-test-context.xml
    }
    exitCode=$?
    # Restore SIGINT / CTRL-C handling so that this script
    # can be interrupted as well.
    trap - INT
    # Sleep to avoid too busy loops on errors.
    sleep 2
done

echo "------------"
echo "Bye"
popd
