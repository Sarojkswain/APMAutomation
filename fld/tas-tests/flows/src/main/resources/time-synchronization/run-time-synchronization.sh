#!/bin/bash

log()
{
  echo "[$PID] [`whoami`] [`date`] - ${*}" | tee -a $LOG_FILE
}

cd `dirname $0`

LOG_FILE=%%LOG_FILE%%

mkdir -p ./logs
touch $LOG_FILE

PID=$$
log "Current PID = $PID"

export JAVA_HOME=%%JAVA_HOME%%
export PATH=$JAVA_HOME/bin:$PATH
CP=%%CP%%
LOG4J_CONFIG=%%LOG4J_CONFIG%%

WAIT_INTERVAL=%%WAIT_INTERVAL%%
WORK_DIR=%%WORK_DIR%%

OPTS=%%OPTS%%

if [ $WAIT_INTERVAL ]; then OPTS="$OPTS -waitInterval $WAIT_INTERVAL"
fi
if [ $WORK_DIR ];      then OPTS="$OPTS -workDir $WORK_DIR"
fi

TO_KILL=`ps aux | fgrep -v 'grep' | fgrep -v $PID | fgrep $(basename $BASH_SOURCE)`
log "TO_KILL = $TO_KILL"
kill `ps aux | fgrep -v 'grep' | fgrep -v $PID | fgrep $(basename $BASH_SOURCE) | awk '{print $2;}'` 2>/dev/null
log "KILL_EXIT_CODE = $?"

TO_KILL=`ps aux | fgrep -v 'grep' | fgrep -v $PID | fgrep 'TimeSynchronizationRunner'`
log "TO_KILL = $TO_KILL"
kill `ps aux | fgrep -v 'grep' | fgrep -v $PID | fgrep 'TimeSynchronizationRunner' | awk '{print $2;}'` 2>/dev/null
log "KILL_EXIT_CODE = $?"

while :
do
  log "Running java com.ca.apm.systemtest.fld.util.timesync.TimeSynchronizationRunner"
  java -cp $CP -Dlog4j.configuration=$LOG4J_CONFIG com.ca.apm.systemtest.fld.util.timesync.TimeSynchronizationRunner $OPTS 2>&1
  JAVA_EXIT_CODE=$?
  log "JAVA_EXIT_CODE = $JAVA_EXIT_CODE"
  if [ $JAVA_EXIT_CODE -eq 0 ]; then
    log "Bye.."
    break
  else
    log "Time synchronization failed, restarting.."
  fi
  sleep 2
done

log "Exit"
