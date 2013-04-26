package com.boxysystems.scriptmonkey.intellij;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Siddique Hameed
 * Date: Apr 7, 2011
 * Time: 11:29:22 AM
 */
public final class ScriptMonkeyLogger {

    private static final Logger logger = Logger.getLogger(ScriptMonkeyLogger.class);

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void error(String message, Throwable t) {
        if (t != null) {
            logger.error(message, t);
        } else {
            logger.error(message);
        }
    }

    public static void warn(String message) {
        logger.warn(message);
    }
}
