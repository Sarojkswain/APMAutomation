package com.ca.apm.webui.test.framework.interfaces;

/**
 * <code>Constants</code> defines values that are used for the test framework.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IConstants
{

    /** log4j properties */
    public String              LOG_PROPERTIES   = "log4j.properties";

    /** browser.properties file. */
    public static final String BROWSER_FILE     = "browser.properties";

    /** launch.properties file. */
    public static final String LAUNCH_FILE      = "launch.properties";

    /** appl.properties file. */
    public static final String APPLICATION_FILE = "appl.properties";

    public final static String kFireFoxBrowser  = "Firefox";

    public final static String kIe32Browser     = "Msie32b";

    public final static String kIe64Browser     = "Msie64b";

    public final static String kChromeBrowser   = "Chrome";

    /** Application installation type. */
    public static enum INSTALL_TYPE {
        on_premise, hosted
    } // end enum

    /** Application clustering. */
    public static enum APP_MODE {
        single_node, cluster
    } // end enum

    /** Application configuration. */
    public static enum APP_CONFIG {
        local, distributed, remote
    } // end enum

    /** Status codes. */
    public static enum STATUS {
        PASS, FAIL
    } // end app-mode enum

    /** Problem type. */
    public static enum PROBLEM_TYPE {
        REGULAR, ERROR, WARNING, DURATION
    }

    /** Tree grid row position to set focus */
    public static enum ROW_FOCUS_POS {
        TOP, BOTTOM, MIDDLE
    }

    /** END DATE */
    public static enum TIME_PERIOD_TYPE {
        TODAY, YESTERDAY, LAST_WEEK, LAST_MONTH, CUSTOM
    }

    /** Problem type. */
    public static enum DATE_PICKER_CALENDAR {
        CUSTOM_START_CALENDAR, CUSTOM_END_CALENDAR, END_DATE_PICKER
    }

    /** Testcase log file. */
    public static final int      TEST_CASE_LOG              = 1;

    /** Testsuite Log File. */
    public static final int      TEST_SUITE_LOG             = 2;

    /** Testcase and Testsuite Log Files. */
    public static final int      TEST_LOGS_ALL              = 3;

    /** Logging level: No Logging. */
    public static final int      NONE                       = 0;

    /** Logging level: Errors Only. */
    public static final int      ERROR                      = 1;

    /** Logging level: Info. Includes Error. */
    public static final int      INFO                       = 3;

    /** Logging level: Debug. Includes Info, Error. */
    public static final int      DEBUG                      = 5;

    /** Logging level: Trace. Includes Debug, Info, Error. */
    public static final int      TRACE                      = 7;

    /** Comparator: EQUALS. */
    public static final int      OP_EQUALS                  = 0;

    /** Comparator: LESS THAN. */
    public static final int      OP_LESSER                  = 1;

    /** Comparator: GREATER THAN. */
    public static final int      OP_GREATER                 = 2;

    /** Comparator: NOT EQUAL. */
    public static final int      OP_NOT_EQUALS              = 3;

    /** Match Operator: EXACT MATCH. */
    public static final boolean  EXACT                      = true;

    /** Match Operator: PARTIAL MATCH. */
    public static final boolean  PARTIAL                    = false;

    /** Match Operator: KEEP matches. */
    public static final boolean  KEEP                       = true;

    /** Match Operator: DISCARD matches. */
    public static final boolean  DISCARD                    = false;

    /** Exit code: Error. */
    public static final int      EXIT_ERROR                 = 1;

    /** Exit code: Success. */
    public static final int      EXIT_SUCCESS               = 0;

    /** Used for assertions: EXISTS. */
    public static final boolean  EXISTS                     = true;

    /** Used For assertions: NOT_EXISTS. */
    public static final boolean  NOT_EXISTS                 = false;

    /** Used For assertions: MATCH. */
    public static final boolean  MATCH                      = true;

    /** Used for assertions: NO_MATCH. */
    public static final boolean  NO_MATCH                   = false;

    /** Sleep: 3 seconds. */
    public static final long     WAIT_3sec                  = 3000;

    /** Sleep: 5 seconds. */
    public static final long     WAIT_5sec                  = 5000;

    /** Sleep: 8 seconds. */
    public static final long     WAIT_8sec                  = 8000;

    /** Sleep: 13 seconds. */
    public static final long     WAIT_13sec                 = 13000;

    /** Sleep: 21 seconds. */
    public static final long     WAIT_21sec                 = 21000;

    /** Sleep: 34 seconds. */
    public static final long     WAIT_34sec                 = 34000;

    /** Sleep: 55 seconds. */
    public static final long     WAIT_55sec                 = 55000;

    /** Selenium BY_CSS. For switch statements. */
    public static final int      BY_CSS                     = 0;

    /** Selenium BY_ID. For switch statements. */
    public static final int      BY_ID                      = 1;

    /** Selenium BY_TAG. For switch statements. */
    public static final int      BY_TAG                     = 2;

    /** Selenium BY_XPATH. For switch statements. */
    public static final int      BY_XPATH                   = 3;

    /** Selenium BY_LINK. For switch statements. */
    public static final int      BY_LINK                    = 4;

    /** Selenium BY_PARTIAL_LINK. For switch statements. */
    public static final int      BY_PLINK                   = 5;

    /** Selenium BY_CLASS_NAME. For switch statements. */
    public static final int      BY_CLASS                   = 6;

    /** Selenium BY_ATTR. Custom BY value. For switch statements. */
    public static final int      BY_ATTR                    = 7;
    
    /** Required browser width for running selenium test */
    public static final int      MIN_BROWSER_WIDTH          = 1280;
    
    /** Required browser height for running selenium test */
    public static final int      MIN_BROWSER_HEIGHT         = 1024;

    /** Opposite of {@link #FAILS}. */
    public static final boolean  PASSES                     = true;

    /** Opposite of {@link #PASSES}. */
    public static final boolean  FAILS                      = false;

    /**
     * Useful for {@link String} operations, which return an index of
     * <tt>-1</tt> when an item is not found.
     */
    public static final int      NOT_FOUND                  = -1;

    /** System property: line.separator. New line character. */
    public static final String   NEW_LINE                   = System.getProperty("line.separator");

    /** Character that separates components of a file path. */
    public static final String   FILE_SEPARATOR             = System.getProperty("file.separator");

    /** Path separator character used in java.class.path. */
    public static final String   PATH_SEPARATOR             = System.getProperty("path.separator");

    /** User home directory. */
    public static final String   USER_HOME                  = System.getProperty("user.home");

    /** Operating system name. */
    public static final String   OS_NAME                    = System.getProperty("os.name");

    /** Operating system architecture. */
    public static final String   OS_ARCH                    = System.getProperty("os.arch");

    /** TAB. */
    public static final String   TAB                        = "\t";

    /** EMPTY String. */
    public static final String   EMPTY                      = "";

    public final static String[] MONTHS_OF_YEAR             = { "Jan", "Jul",
            "Feb", "Aug", "Mar", "Sep", "Apr", "Oct", "May", "Nov", "Jun",
            "Dec"                                          };

    public final static String[] MONTHS_OF_YEAR_FULL        = { "January",
            "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December" };

    public final static String[] RANGE_VALUES               = { "5 Minutes",
            "15 Minutes", "30 Minutes", "60 Minutes", "90 Minutes", "3 Hours",
            "6 Hours", "12 Hours", "24 Hours"              };

    public final static int[]    RANGE_VALUES_DURATION      = { 5, 15, 30, 60,
            90, 3, 6, 12, 24                               };

    public final static int[]    RANGE_VALUES_DURATION_TYPE = { 0, 0, 0, 0, 0,
            1, 1, 1, 1                                     };                                     // 0=Minutes,
// 1=Hours

    /** HTML Attributes */
    public static final String   kStyleAttribute            = "style";

    public static final String   kIdAttribute               = "id";

    public static final String   kOnClickAttribute          = "onclick";
    
    public static final String   kPathFillAttribute         = "fill";
    
    public static final String   kPathStrokeAttribute       = "stroke"; 

    /** HTML Style Attributes */
    public static final String   kBorderWidthStyle          = "border-width";

    public static final String   kLeftStyle                 = "left";

    public static final String   kTopStyle                  = "top";

    public static final String   kWidthStyle                = "width";

    public static final String   kHeightStyle               = "height";

    public static final String   kFontSizeStyle             = "font-size";

    public static final String   kFontWeightStyle           = "font-weight";

    public static final String   kFontStyleStyle            = "font-style";

    public static final String   kFontFamilyStyle           = "font-family";

    public static final String   kTextColorStyle            = "color";   

    public static final String   kTextAlignStyle            = "text-align";

    public static final String   kZindexStyle               = "z-index";

    public static final String   kCursorStyle               = "cursor";

    public static final String   kPointerCursorStyle        = "pointer";

} // end class
