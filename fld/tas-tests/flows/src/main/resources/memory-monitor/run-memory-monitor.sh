#!/bin/bash

cd `dirname $0`

LOG_FILE=%%LOG_FILE%%

export JAVA_HOME=%%JAVA_HOME%%
export PATH=$JAVA_HOME/bin:$PATH
CP=%%CP%%

GC_LOG_FILE=%%GC_LOG_FILE%%
GROUP=%%GROUP%%
ROLE_NAME=%%ROLE_NAME%%
MEMORY_MONITOR_WEBAPP_HOST=%%MEMORY_MONITOR_WEBAPP_HOST%%
MEMORY_MONITOR_WEBAPP_PORT=%%MEMORY_MONITOR_WEBAPP_PORT%%
MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT=%%MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT%%
CHART_WIDTH=%%CHART_WIDTH%%
CHART_HEIGHT=%%CHART_HEIGHT%%
WAIT_INTERVAL=%%WAIT_INTERVAL%%
WORK_DIR=%%WORK_DIR%%
ITERATION_COUNT=%%ITERATION_COUNT%%

OPTS="-gcLogFile $GC_LOG_FILE -group $GROUP -roleName $ROLE_NAME -memoryMonitorWebappHost $MEMORY_MONITOR_WEBAPP_HOST"

if [ $MEMORY_MONITOR_WEBAPP_PORT ];         then OPTS="$OPTS -memoryMonitorWebappPort $MEMORY_MONITOR_WEBAPP_PORT"
fi
if [ $MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT ]; then OPTS="$OPTS -memoryMonitorWebappContextRoot $MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT"
fi
if [ $CHART_WIDTH ];                        then OPTS="$OPTS -chartWidth $CHART_WIDTH"
fi
if [ $CHART_HEIGHT ];                       then OPTS="$OPTS -chartHeight $CHART_HEIGHT"
fi
if [ $WAIT_INTERVAL ];                      then OPTS="$OPTS -waitInterval $WAIT_INTERVAL"
fi
if [ $WORK_DIR ];                           then OPTS="$OPTS -workDir $WORK_DIR"
fi
if [ $ITERATION_COUNT ];                    then OPTS="$OPTS -iterationCount $ITERATION_COUNT"
fi

exec java -cp $CP com.ca.apm.systemtest.fld.util.memorymonitor.MemoryMonitorRunner $OPTS 2>&1 | tee $LOG_FILE
