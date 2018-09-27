package com.ca.apm.saas.test.helpers;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import java.text.MessageFormat;

/**
 * @author haiva01
 */
public final class ErrorReport {
    private static String formatExceptionMessage(Throwable ex) {
        String exceptionMessage;
        return (exceptionMessage = ex.getMessage()) != null
            ? exceptionMessage : ex.getClass().getName();
    }


    /**
     * This function logs an exception into given logger. The log message
     * is formatted using MessageFormat's format and trailing arguments.
     * The {0} placeholder always expands to exception's message.
     *
     * @param log     logger
     * @param ex      exception to be logged and wrapped
     * @param pattern MessageFormat's pattern
     * @param args    arguments for MessageFormat's pattern
     * @return formatted message
     */
    public static String logExceptionFmt(final Logger log, final Throwable ex, final String pattern,
        Object... args) {
        if (args == null) {
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final Object[] formatArgs = new Object[args.length + 1 /* ex.getMessage() */];
        formatArgs[0] = formatExceptionMessage(ex);
        if (args.length > 0) {
            System.arraycopy(args, 0, formatArgs, 1, args.length);
        }

        final String msg = MessageFormat.format(pattern, formatArgs);
        log.error(msg, ex);
        return msg;
    }


    /**
     * This function logs an exception into given logger and rethrows
     * wrapped the exception wrapped in RuntimeException. The log message
     * is formatted using MessageFormat's format and trailing arguments.
     * The {0} is always exception message.
     *
     * @param log     logger
     * @param ex      exception to be logged and wrapped
     * @param pattern MessageFormat's pattern
     * @param args    arguments for MessageFormat's pattern
     * @throws RuntimeException new instance of RuntimeException with given message wrapped
     *                          exception
     */
    public static RuntimeException logExceptionAndWrapFmt(final Logger log, final Throwable ex,
        final String pattern, final Object... args) {
        return new RuntimeException(logExceptionFmt(log, ex, pattern, args), ex);
    }
}
