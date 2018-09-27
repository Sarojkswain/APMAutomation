set term png small size 1600,800
set datafile separator ","

set grid x y
set xlabel "time [m]"
set xtics 10

set yrange [0:]

# Time,RealStg,IORate,MIPS,CPUTime

set output 'RealStg.png'
set ylabel "[MB]"
plot _in1 using ($1/60):3 title _t1 with lines, _in2 using ($1/60):3 title _t2 with lines

set output 'IORate.png'
set ylabel "[IO/s]"
plot _in1 using ($1/60):4 title _t1 with lines, _in2 using ($1/60):4 title _t2 with lines

set output 'MIPS.png'
set ylabel "[MIPS]"
plot _in1 using ($1/60):5 title _t1 with lines, _in2 using ($1/60):5 title _t2 with lines

set output 'CPUTime.png'
set ylabel "[s]"
plot _in1 using ($1/60):6 title _t1 with lines, _in2 using ($1/60):6 title _t2 with lines

# vim: set tw=0:
