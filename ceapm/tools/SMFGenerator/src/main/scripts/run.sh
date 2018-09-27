#!/bin/sh

CLASSPATH=
for dep in `ls lib/*.jar`; do
    CLASSPATH=$dep:$CLASSPATH
done
BASE="java -cp $CLASSPATH com.ca.apm.powerpack.sysview.tools.smfgenerator.Cli"

# AGENT
AGENT="--host usilca31.ca.com --port 11111"

# TEST DEFINITIONS
TCOMMON="--run-length 120"
CONST_1="${TCOMMON}  --test-const --nodes   100 --tps    10"
CONST_2="${TCOMMON}  --test-const --nodes   100 --tps   100"
CONST_3="${TCOMMON}  --test-const --nodes   100 --tps  1000"
CONST_4="${TCOMMON}  --test-const --nodes   100 --tps 10000"
CONST_5="${TCOMMON}  --test-const --nodes  1000 --tps  1000"
CONST_6="${TCOMMON} --test-const --nodes  1000 --tps 10000"
CONST_7="${TCOMMON}  --test-const --nodes 10000 --tps 10000"
CONST_8="${TCOMMON}  --test-const --nodes 40000 --tps 10000"

PEAK_C="--peak-period 50000 --normal-nodes 999 --normal-base-tps 20 --peak-nodes 1 --peak-low-tps 0 --peak-high-tps 10"
PEAK_0="${TCOMMON} --test-peak ${PEAK_C} --peak-duration  250"
PEAK_1="${TCOMMON} --test-peak ${PEAK_C} --peak-duration  500"
PEAK_2="${TCOMMON} --test-peak ${PEAK_C} --peak-duration 5000"

DIST_C="--nodes 1000 --tps 100"
DIST_1="${TCOMMON} --test-distribution ${DIST_C} --extra-distribution   5000"
DIST_2="${TCOMMON} --test-distribution ${DIST_C} --extra-distribution  20000"
DIST_3="${TCOMMON} --test-distribution ${DIST_C} --extra-distribution 500000"
DIST_4="${TCOMMON} --test-distribution ${DIST_C} --extra-distribution 950000"

DIST_C="--nodes 1000 --extra-distribution 100 500000"
DIST_5="${TCOMMON} --test-distribution ${DIST_C} --tps   20"
DIST_6="${TCOMMON} --test-distribution ${DIST_C} --tps 1000"

${BASE} ${AGENT} ${CONST_2}

# vim: set tw=0:
