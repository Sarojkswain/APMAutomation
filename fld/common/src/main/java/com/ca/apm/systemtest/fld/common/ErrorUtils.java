package com.ca.apm.systemtest.fld.common;

/**
 * Utilities for handling errors and exceptions.
 * Created by haiva01 on 18.11.2014.
 */

import org.slf4j.Logger;

import java.text.MessageFormat;

public final class ErrorUtils {
    public static void throwRuntimeException(final String pattern, final Object... args)
        throws RuntimeException {
        final String msg = MessageFormat.format(pattern, args);
        throw new RuntimeException(msg);
    }

    public static void throwRuntimeException(final String msg)
        throws RuntimeException {
        throw new RuntimeException(msg);
    }


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
        final Object arg1, final Object... args) {
        Object[] formatArgs = new Object[args.length + 1 /* arg1 */ + 1 /* ex.getMessage() */];
        formatArgs[0] = formatExceptionMessage(ex);
        formatArgs[1] = arg1;
        System.arraycopy(args, 0, formatArgs, 2, args.length);

        final String msg = MessageFormat.format(pattern, formatArgs);
        log.error(msg, ex);
        return msg;
    }


    /**
     * This variant is the like the one above except that it does not require additional argument
     * when the only printed element is "{0}" for exception message.
     *
     * @param log     logger
     * @param ex      exception to be logged and wrapped
     * @param pattern MessageFormat's pattern
     * @return formatted message
     */
    public static String logExceptionFmt(final Logger log, final Throwable ex,
        final String pattern) {
        final String msg = MessageFormat.format(pattern, formatExceptionMessage(ex));
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
     * @throws RuntimeException
     */
    public static RuntimeException logExceptionAndWrapFmt(final Logger log, final Throwable ex,
        final String pattern, final Object arg1, final Object... args) {
        return new RuntimeException(logExceptionFmt(log, ex, pattern, arg1, args), ex);
    }


    /**
     * This variant is the like the one above except that it does not require additional argument
     * when the only printed element is "{0}" for exception message.
     *
     * @param log     logger
     * @param ex      exception to be logged and wrapped
     * @param pattern MessageFormat's pattern
     * @return RuntimeException
     */
    public static RuntimeException logExceptionAndWrapFmt(final Logger log, final Throwable ex,
        final String pattern) {
        return new RuntimeException(logExceptionFmt(log, ex, pattern), ex);
    }


    /**
     * Use logExceptionAndWrapFmt() instead.
     *
     * @param log
     * @param ex
     * @param pattern
     * @param args
     * @return
     */
    @Deprecated
    public static RuntimeException logExceptionAndWrap(final Logger log, final Exception ex,
        final String pattern, final Object... args) {
        return logExceptionAndWrapFmt(log, ex, pattern, args);
    }


    public static RuntimeException logExceptionAndWrap(final Logger log, final Exception ex,
        final String msg) {
        log.error(msg, ex);
        if (!(ex instanceof RuntimeException)) {
            return new RuntimeException(msg, ex);
        }
        return (RuntimeException) ex;
    }


    /**
     * This function logs string into given logger and returns RuntimeException with the same
     * message.
     *
     * @param log     logger
     * @param pattern MessageFormat pattern
     * @param args    MessageFormat pattern arguments
     * @return RuntimeException
     */
    public static RuntimeException logErrorAndReturnException(final Logger log, final String
        pattern, final Object... args) {
        final String msg = MessageFormat.format(pattern, args);
        log.error(msg);
        return new RuntimeException(msg);
    }

    /**
     * This function logs string into given logger and returns RuntimeException with the same
     * message.
     *
     * @param log logger
     * @param msg message to log
     * @return RuntimeException
     */
    public static RuntimeException logErrorAndReturnException(final Logger log, final String msg) {
        log.error(msg);
        return new RuntimeException(msg);
    }

    public static RuntimeException logErrorAndThrowException(final Logger log, final String
        pattern, final Object... args) {
        throw logErrorAndReturnException(log, pattern, args);
    }

}
