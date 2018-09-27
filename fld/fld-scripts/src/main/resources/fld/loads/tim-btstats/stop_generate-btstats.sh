#!/bin/bash

cd `dirname $0`

chmod u+x ./setenv_generate-btstats.sh
. ./setenv_generate-btstats.sh

if [ -f ${PID_FILE} ]; then
  read -r PID < ${PID_FILE}
  if [ "XXX" = "XXX$PID" ]; then
    echo "Not running (no PID avaliable)"
  else
    kill ${PID}
    echo "Stopped"
  fi
  rm -f ${PID_FILE}
else
  echo "Not running"
fi
