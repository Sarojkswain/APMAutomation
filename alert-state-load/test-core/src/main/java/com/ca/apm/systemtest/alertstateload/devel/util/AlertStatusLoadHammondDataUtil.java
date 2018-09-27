package com.ca.apm.systemtest.alertstateload.devel.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

@Deprecated
public class AlertStatusLoadHammondDataUtil {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    private static final String[] ITERATION_DATES = new String[] {"2017-01-12 17:44:35,253",
            "2017-01-12 18:04:37,125", "2017-01-12 18:24:40,349", "2017-01-12 18:44:43,241",
            "2017-01-12 19:04:45,823", "2017-01-12 19:24:48,149", "2017-01-12 19:44:50,452",
            "2017-01-12 20:04:53,050", "2017-01-12 20:24:55,680", "2017-01-12 20:44:58,057",
            "2017-01-12 21:05:00,423", "2017-01-12 21:25:03,158", "2017-01-12 21:45:05,398",
            "2017-01-12 22:05:07,688", "2017-01-12 22:25:10,077", "2017-01-12 22:45:12,438",
            "2017-01-12 23:05:14,642", "2017-01-12 23:25:17,101", "2017-01-12 23:45:19,417",
            "2017-01-13 00:05:21,670", "2017-01-13 00:25:23,880", "2017-01-13 00:45:25,880",
            "2017-01-13 01:05:28,441", "2017-01-13 01:25:30,501", "2017-01-13 01:45:32,724",
            "2017-01-13 02:05:35,065", "2017-01-13 02:25:37,296", "2017-01-13 02:45:39,416",
            "2017-01-13 03:05:41,629", "2017-01-13 03:25:43,799", "2017-01-13 03:45:46,082",
            "2017-01-13 04:05:48,139", "2017-01-13 04:25:50,607", "2017-01-13 04:45:53,162",
            "2017-01-13 05:05:55,275", "2017-01-13 05:25:57,589"}; // taken from log

    private static final long[] ITERATION_TS;

    /*
     * ||: 15 min without alterts | 5 min with alerts :|| .... repeated 36x
     */
    private static final long DURATION_noAlerts = 15 * 60000L; // 15 min
    private static final long DURATION_alerts = 5 * 60000L; // 5 min
    private static final long ITERATION_DURATION = DURATION_noAlerts + DURATION_alerts; // 20 min

    static {
        try {
            ITERATION_TS = new long[ITERATION_DATES.length];
            for (int i = 0; i < ITERATION_DATES.length; i++) {
                ITERATION_TS[i] = DATE_FORMAT.parse(ITERATION_DATES[i]).getTime();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private AlertStatusLoadHammondDataUtil() {}

    public static long getRandomIterationStart() {
        //return ITERATION_TS[(new Random()).nextInt(ITERATION_TS.length)];
        //return ITERATION_TS[(new Random()).nextInt(ITERATION_TS.length - 2) + 1]; // exclude the first and last one
        return 1484240730000L;
    }

    public static long getRandomIterationStartAlerts() {
        //return getRandomIterationStart() + DURATION_noAlerts;
        return 1484241585000L;
    }

    public static long getDurationNoAlerts() {
        return DURATION_noAlerts;
    }

    public static long getDurationAlerts() {
        return DURATION_alerts;
    }

    public static long getIterationDuration() {
        return ITERATION_DURATION;
    }

    public static void main(String[] args) {
        System.out.println("AlertStatusLoadHammondDataUtil.main():: ITERATION_TS = "
            + Arrays.toString(ITERATION_TS));
    }

}
