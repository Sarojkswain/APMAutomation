package com.ca.apm.es;
/**
 * Created by venpr05 on 2/27/2017.
 */
public class GatheringCounter {

    long comps;
    long count;
    long min;
    long max;
    long avg;

    public GatheringCounter() {
        comps = count = min = max = avg = 0;
    }

    public synchronized void record(long val) {

        if (count == 0) {
            min = max = avg = val;
        } else {

            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
            avg = (avg + val) / 2;
        }
        ++count;
    }

    public synchronized void recordComp(long comps) {

        this.comps += comps;
    }

    public long getComps() {
        return comps;
    }

    public long getCount() {
        return count;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public long getAvg() {
        return avg;
    }
}
