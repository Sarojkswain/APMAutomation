#!/bin/sh

gnuplot -e "_in1='WILYZPO0.data'; _t1='new'; _in2='WILYZPO1.data'; _t2='control'" performance.plot

if [ $# -ge 1 ]; then
    mkdir -p results/$1
    mv *.png results/$1
    mv *.data results/$1
fi

# vim: set tw=0:
