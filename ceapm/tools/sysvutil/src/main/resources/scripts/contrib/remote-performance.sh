#!/bin/sh

HOST=ca31
SYSVUTIL=/a/podja02/sysvutil

INTERVALS=10
GENER_INTERVAL=2
GENER_COMMAND=
DELAY=60
JOBPREFIX="WILYZPO*"
JOBNAMES="WILYZPO0 WILYZPO1"
COLUMNS="Jobname|CPUTime|MIPS|IORate|RealStg"

################################################################################

JOBNAMES_RE=`echo ${JOBNAMES} | tr ' ' '|'`
COMMAND="actsum current interval 1 jobname ${JOBPREFIX}"
OPTIONS="--no-header --column-include \"${COLUMNS}\" --filter-include \"Jobname=${JOBNAMES_RE}\""

read -s -p "${USER}@${HOST} password (cached): " PASS
echo

i=1
while [ ${i} -le ${INTERVALS} ]; do
    if [ ${i} -gt 1 ]; then
        sleep ${DELAY}
    fi

    echo -n -e ","

    sshpass -p ${PASS} ssh ${HOST} "cd ${SYSVUTIL} && ./run.sh ${OPTIONS} ${COMMAND}" >> all.data

    echo -n -e "\b."

    if [ ${i} -eq ${GENER_INTERVAL} -a "${GENER_COMMAND}" != "" ]; then
        ${GENER_COMMAND} >/dev/null 2>&1 &
        echo -n -e "*"
    fi

    let i+=1
done
echo

for jobname in ${JOBNAMES}; do
    grep "${jobname}" all.data | awk -v delay=${DELAY} '{print (FNR-1)*delay","$0}' >${jobname}.data
done
rm all.data

./visualize.sh $1

# vim: set expandtab:tw=0:ts=4:sw=4:
