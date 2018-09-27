#!/bin/bash

cd `dirname $0`

chmod u+x ./setenv_generate-btstats.sh
. ./setenv_generate-btstats.sh

if [ -f ${PID_FILE} ]; then
  echo "Already running with pid $(cat ${PID_FILE})"
else
  nohup ${PYTHON} ${SCRIPT_FILE} $* &
  echo $! > ${PID_FILE}
  chmod 400 ${PID_FILE}
  echo "Started with pid $(cat ${PID_FILE})"
fi
