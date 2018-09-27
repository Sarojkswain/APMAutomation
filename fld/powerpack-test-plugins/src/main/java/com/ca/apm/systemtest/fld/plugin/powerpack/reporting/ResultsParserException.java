package com.ca.apm.systemtest.fld.plugin.powerpack.reporting;

import com.ca.apm.systemtest.fld.plugin.RemoteCallException;

public class ResultsParserException extends RemoteCallException {

    public static final String ERR_GNUPLOT_LOG_PARSING_CONFIG_IS_INVALID = "ERR_GNUPLOT_LOG_PARSING_CONFIG_IS_INVALID";
    public static final String ERR_GNUPLOT_LOG_PARSING_FAILED = "ERR_GNUPLOT_LOG_PARSING_FAILED";

    /**
     *
     */
    private static final long serialVersionUID = 3445246161115554417L;

    public ResultsParserException() {
        super();
    }

    public ResultsParserException(String msg) {
        super(msg);
    }

    public ResultsParserException(String msg, String errorCode) {
        super(msg, errorCode);
    }

    public ResultsParserException(String errorCode, String pattern, Object... arguments) {
        super(errorCode, pattern, arguments);
    }

    public ResultsParserException(Throwable ex, String errorCode, String pattern, Object... arguments) {
        super(ex, errorCode, pattern, arguments);
    }

    public ResultsParserException(Throwable ex) {
        super(ex);
    }

    public ResultsParserException(Throwable ex, String errorCode) {
        super(ex, errorCode);
    }

    public ResultsParserException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public ResultsParserException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
    }

}
