/*
 * Copyright (c) 2000, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * @(#)ExceptionHandler.java	1.31 06/27/07
 */

package com.sun.messaging.jmq.jmsclient;

import java.util.logging.*;

import jakarta.jms.*;

import com.sun.messaging.AdministeredObject;

import com.sun.messaging.jmq.jmsclient.logging.Loggable;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

/**
 * The following are the client exception handling basic principles:
 * <p>
 * 1. Use JMS Standard Exceptions whenever appropriate as stated in the specification 1.1 chapter 7.3 "Standard
 * Exceptions". For example,
 * <p>
 * JMSSecurityException: This exception must be thrown when a provider rejects a user name/password submitted by a
 * client. It may also be thrown for any case where a security restriction prevents a method from completing.
 * <p>
 * ResourceAllocationException: This exception is thrown when a provider is unable to allocate the resources required by
 * a method. For example, this exception should be thrown when a call to createTopicConnection fails due to lack of JMS
 * provider resources.
 * <p>
 * 2. Each Exception thrown by MQ client SHOULD have its own unique error code defined in the ClientResources.java
 * whenever appropriate. Each error status returned from the broker SHOULD BE mapped to an unique client error code.
 * <p>
 * 3. Exception messages SHOULD BE as clear as possible. Each exception message defined in ClientResource.properties
 * MUST BE mapped to a unique error code defined in ClientResource.java. Information that could help user to identify
 * the cause of exceptions SHOULD BE included in the exception message.
 * <p>
 * 4. The above principles (#1-#3) are used to construct MQ exceptions. Each MQ JMS exception is identified by its
 * exception type, error code and error message. Root cause of exception is set to the linked exception of JMS
 * exception.
 *
 * <p>
 * MQ Exception handling may be divided into the following two categories:
 * <p>
 * 1. Exceptions initiated from MQ. The localized error message is obtained from AdministeredObject.cr (ClientResources
 * bundle) with errorCode as key. Additional information may also be included as part of the error message.
 * <p>
 * The appropriate JMS standard exception is constructed with "error code" and "error message" as parameters.
 * <p>
 * Each JMSException generated by JMQ has an error code defined in ClientResources.java.
 * <p>
 *
 * 2. Exceptions thrown from JVM and caught by MQ. In general, MQ converts the caught exception into JMSException type.
 * MQ choose the error code for the JMSException in two ways:
 * <p>
 * 2.1 Use generic error code ClientResources.X_CAUGHT_EXCEPTION.
 *
 * This error code is used for cases that we simply just want to use the JVM excption message in the JMSException
 * message. For example, to handle exceptions thrown during ObjectOutputStream construction.
 * <p>
 * The API handleException(Exception source, String errorCode) is used to construct the JMSException instance. For
 * example, the caller caught an IOException (ioe) would call the following API to raise a JMSException:
 * <p>
 * ExceptionHandler.handleException(ioe, ClientResources.X_CAUGHT_EXCEPTION);
 * <p>
 * The exception message with ClientResources.X_CAUGHT_EXCEPTION error code would have the error message format as
 * follows.
 * <p>
 * "[C4038]: " + RootCauseException.toString();
 *
 * <p>
 * The root cause exception is set to the JMSException as linked exception.
 *
 * <p>
 * 2.2 Use an error code defined in ClientResources.java. The associated error message is defined in
 * ClientResources.properties.
 * <p>
 * This is basically the same as above except uses an appropriate error code and message for the exception. For example,
 * <p>
 * handleException(ioe, AdministeredObject.cr.X_MESSAGE_SERIALIZE); message: "[C4016]: Serialize message failed. cause:
 * " + ioe.toString(); errorCode: AdministeredObject.cr.X_MESSAGE_SERIALIZE
 * <p>
 * handleException(ioe, AdministeredObject.cr.X_MESSAGE_READ, true); message: "[C4012]: Read message failed. cause: " +
 * ioe.toString(); errorCode: AdministeredObject.cr.X_MESSAGE_READ
 * <p>
 * The above two cases are the same. Not merged just for backward compatibility.
 * <p>
 * The Root cause exception class name and message are always included in the exception message.
 *
 *
 */
public class ExceptionHandler {

    private static final String cname = "com.sun.messaging.jmq.jmsclient.ExceptionHandler";

    public static final Logger rootLogger = Logger.getLogger(ConnectionImpl.ROOT_LOGGER_NAME);

    /**
     * All connection related exceptions are handled by connectionException() method. If the connection has set its
     * listener, it is called.
     *
     * @param source the source exception
     *
     * @exception JMSException the exception thrown
     */
    // public void
    // connectionException(JMSException source) throws JMSException {
    // connectionException(source, null);
    // }

    /**
     * All connection related exceptions are handled by connectionException() method. If the connection has set its
     * listener, it is called.
     *
     * @param source the source exception
     * @param errorCode the error code for the target exception
     *
     * @exception JMSException the target exception thrown
     */
    // public void
    // connectionException(Exception source, String errorCode) throws JMSException {
    // connectionException(source, errorCode, false);
    // }

    /**
     * All connection related exceptions are handled by connectionException() method. If the connection has set its
     * listener, it is called.
     *
     * @param source the source exception
     * @param errorCode the error code for the target exception
     * @param format if true, use X_CAUGHT_EXCEPTION format if false, no format
     *
     * @exception JMSException the target exception thrown
     */
    /**
     * public void connectionException(Exception source, String errorCode, boolean format) throws JMSException {
     *
     * JMSException target; if ((source instanceof JMSException) && ((JMSException)source).getErrorCode() != null) { target
     * = (JMSException)source; } else { target = getJMSException (source, errorCode, format);
     * target.setLinkedException(source); }
     *
     * //this makes readChannel exits! throw target;
     *
     * }
     **/

    /**
     * This is called when creating a connection with host and port.
     */
    public static void handleConnectException(Exception source, String host, int port) throws JMSException {

        String info = "[" + host + ":" + port + "]";

        throwConnectionException(source, info);
    }

    /**
     * This is called when creating a connection with an url.
     */
    public static void handleConnectException(Exception source, String url) throws JMSException {

        String info = "[" + url + "]";

        throwConnectionException(source, info);
    }

    /**
     * This method handles connection creation exceptions.
     *
     * @param info [host,port] or url string.
     * @throw JMSException when this method is called, it constructs a JMSException with: 1. set info and root cause as
     * exception message. 2. set X_NET_CREATE_CONNECTION as the exception error code. 3. set source exception to its
     * exception link.
     */
    private static void throwConnectionException(Exception source, String info) throws JMSException {

        /**
         * in case we caught a JMSException, just propagate.
         */
        if ((source instanceof JMSException) && ((JMSException) source).getErrorCode() != null) {
            // throw (JMSException)source;
            throwJMSException((JMSException) source);
        }

        /**
         * include connection information in the string.
         */
        String errorString0 = AdministeredObject.cr.getKString(AdministeredObject.cr.X_NET_CREATE_CONNECTION, info);

        /**
         * append root cause message.
         */
        String errorString = AdministeredObject.cr.getString(AdministeredObject.cr.X_CAUGHT_EXCEPTION, errorString0, source.toString());

        /**
         * construct jms exception with error msg and error code.
         */
        JMSException jmse = new com.sun.messaging.jms.JMSException(errorString, AdministeredObject.cr.X_NET_CREATE_CONNECTION);

        // log connection exception here so that broker host and port are logged.
        if (rootLogger.isLoggable(Level.WARNING)) {
            if (shouldLog(jmse)) {
                rootLogger.log(Level.WARNING, errorString);
            }
        }

        /**
         * set exception link.
         */
        jmse.setLinkedException(source);

        /**
         * fire the exception.
         */
        // throw jmse;
        throwJMSException(jmse);
    }

    /**
     * A general method to convert java exception to JMSException.
     *
     * @param source the source exception
     * @param errorCode the error code for the target exception
     *
     * @exception JMSException the target exception thrown
     */
    public static void handleException(Exception source, String errorCode) throws JMSException {
        handleException(source, errorCode, true);
    }

    /**
     * A general method to convert java exception to JMSException.
     *
     * @param source the source exception
     * @param errorCode the error code for the target exception
     * @param format if true, use X_CAUGHT_EXCEPTION format if false, no format
     * @exception JMSException the target exception thrown
     */
    public static void handleException(Exception source, String errorCode, boolean format) throws JMSException {

        if ((source instanceof JMSException) && ((JMSException) source).getErrorCode() != null) {
            throwJMSException((JMSException) source);
        }
        handleException(source, getJMSException(source, errorCode, format));
    }

    /**
     * This method is used to link a source exception to a JMSException
     *
     * @param source the exception source.
     * @param target the exception to be thrown.
     *
     * @exception JMSException the target JMSException thrown
     */
    public static void handleException(Exception source, JMSException target) throws JMSException {

        target.setLinkedException(source);

        if (Debug.debug) {
            printStackTrace(source);
        }

        // throw target;
        throwJMSException(target);
    }

    public static void printStackTrace(Exception e) {
        Debug.printStackTrace(e);
    }

    /**
     * Construct MQ JMSException with an unique format.
     *
     * NOTE: format is no longer in use. All exception messages use the same format.
     */
    public static JMSException getJMSException(Exception source, String errorCode, boolean format) {

        String errorString = null;

        if (source == null) {
            /**
             * This should never happen.
             */
            errorString = AdministeredObject.cr.getKString(errorCode);
        } else {
            errorString = getExceptionMessage(source, errorCode);
        }

        return new com.sun.messaging.jms.JMSException(errorString, errorCode);
    }

    /**
     * Get JMS exception message with the root cause exception and MQ error code.
     *
     * @retutn the MQ JMS Exception error string.
     */
    public static String getExceptionMessage(Exception source, String errorCode) {
        String errorString = null;

        /**
         * if no error code, use generic jvm exception message.
         */
        if (errorCode == null) {
            errorCode = AdministeredObject.cr.X_CAUGHT_EXCEPTION;
        }

        /**
         * error code and message for root cause exception. for exceptions that directly translates into JMSExceptions use this
         * error code.
         */
        if (errorCode == AdministeredObject.cr.X_CAUGHT_EXCEPTION) {
            errorString = "[" + AdministeredObject.cr.X_CAUGHT_EXCEPTION + "]: " + source.toString();
        } else {
            /**
             * for exceptions with error codes other than AdministeredObject.cr.X_CAUGHT_EXCEPTION use the following format.
             */
            String errorString0 = AdministeredObject.cr.getKString(errorCode);
            errorString = AdministeredObject.cr.getString(AdministeredObject.cr.X_CAUGHT_EXCEPTION, errorString0, source.toString());
        }

        return errorString;
    }

    /**
     * If the specified JMSRuntimeException has a wrapped exception then log the wrapped exception using the WARNING logger
     * level. Then log that we are about to throw the specified JMSRuntimeException using FINER logging. Finally throw the
     * specified JMSRuntimeException.
     *
     * @param jmsre
     * @throws JMSRuntimeException
     */
    public static void throwJMSException(JMSException jmse) throws JMSException {
        throwJMSException(Level.WARNING, jmse);
    }

    /**
     * Log the specified JMSException using the specified logger level and then rethrow the JMSException
     *
     * @param level
     * @param jmse
     * @throws JMSException
     */
    public static void throwJMSException(Level level, JMSException jmse) throws JMSException {

        try {
            // log exception
            if (shouldLog(jmse)) {

                // get linked exception
                Throwable source = jmse.getLinkedException();

                // log caught an exception
                if (source != null) {
                    logCaughtException(level, source);
                }

                // log throwing an exception
                rootLogger.log(Level.FINER, AdministeredObject.cr.I_THROW_JMS_EXCEPTION, jmse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // we are throwing this one
        throw jmse;

    }

    /**
     * Log the specified JMSRuntimeException as a WARNING message and then throw the JMSRuntimeException
     *
     * @param jmsre
     * @throws JMSRuntimeException
     */
    public static void throwJMSRuntimeException(JMSRuntimeException jmsre) {
        throwJMSRuntimeException(Level.WARNING, jmsre);
    }

    /**
     * If the specified JMSRuntimeException has a wrapped exception then log the wrapped exception using the specified
     * logger level. Then log that we are about to throw the specified JMSRuntimeException using FINER logging. Finally
     * throw the specified JMSRuntimeException.
     *
     * @param level
     * @param jmsre
     * @throws JMSRuntimeException
     */
    public static void throwJMSRuntimeException(Level level, JMSRuntimeException jmsre) {

        try {
            // log exception
            if (shouldLog(jmsre)) {

                // get linked exception
                Throwable source = jmsre.getCause();

                // log caught an exception
                if (source != null) {
                    logCaughtException(level, source);
                }

                // log throwing an exception
                rootLogger.log(Level.FINER, AdministeredObject.cr.I_THROW_JMS_EXCEPTION, jmsre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw jmsre;
    }

    /**
     * If a Exception is not Loggable, we would still want to log the exception (at least for now). The non-loggable
     * exception maybe logged more than once.
     *
     * @param e Exception the exception to be logged.
     *
     * @return boolean true if the exception should be logged.
     */
    private static boolean shouldLog(Exception e) {

        // default set to true.
        boolean isLoggable = true;

        // if it is a Loggable type, check the log state. Otherwise,
        // just log the exception.
        if (e instanceof Loggable) {

            if (((Loggable) e).getLogState()) {
                // if logged, set isLoggable to false so that it does not get
                // logged.
                isLoggable = false;
            } else {
                // set log state to true so that it only get log once.
                ((Loggable) e).setLogState(true);
            }
        }

        return isLoggable;
    }

    /**
     * Log the specified exception which was caught by the MQ client runtime as a WARNING message
     *
     * @param source
     */
    public static void logCaughtException(Throwable source) {
        logCaughtException(Level.WARNING, source);
    }

    /**
     * Log the specified exception which was caught by the MQ client runtime at the specified Logger level
     *
     * @param level
     * @param throwable
     */
    public static void logCaughtException(Level level, Throwable throwable) {

        if (throwable != null) {
            if (rootLogger.isLoggable(level)) {
                String msg = AdministeredObject.cr.getKString(ClientResources.I_CAUGHT_JVM_EXCEPTION, throwable.toString());
                rootLogger.log(level, msg);
            }
        }
    }

    public static void logError(Throwable thrown) {

        if (thrown != null) {

            if (rootLogger.isLoggable(Level.SEVERE)) {
                String msg = thrown.toString();
                // rootLogger.log(Level.SEVERE, msg, thrown);
                rootLogger.log(Level.SEVERE, msg, thrown);
            }
        }
    }

    public static void throwRemoteAcknowledgeException(JMSException source, String errorCode) throws JMSException {

        // get error string
        String errorString = AdministeredObject.cr.getKString(errorCode);

        // create exception
        JMSException newjmse = new com.sun.messaging.jms.JMSException(errorString, errorCode);

        // set exception link if any
        if (source != null) {
            newjmse.setLinkedException(source);
        }

        // throw the exception
        throwJMSException(newjmse);
    }

}
