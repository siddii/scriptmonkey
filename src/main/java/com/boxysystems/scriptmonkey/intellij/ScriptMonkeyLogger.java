package com.boxysystems.scriptmonkey.intellij;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Siddique Hameed
 * Date: Apr 7, 2011
 * Time: 11:29:22 AM
 */
public final class ScriptMonkeyLogger {
    private static final Logger logger = Logger.getLogger(ScriptMonkeyLogger.class);

    public static Logger getLogger(String name) {return Logger.getLogger(name);}

    public static void setAdditivity(boolean additive) {logger.setAdditivity(additive);}

    public static Logger getLogger(String name, LoggerFactory factory) {return Logger.getLogger(name, factory);}

    public static boolean isTraceEnabled() {return logger.isTraceEnabled();}

    public static Logger getRootLogger() {return Logger.getRootLogger();}

    public static void warn(Object message) {logger.warn(message);}

    public static Category getParent() {return logger.getParent();}

    public static Enumeration getAllAppenders() {return logger.getAllAppenders();}

    public static boolean isInfoEnabled() {return logger.isInfoEnabled();}

    public static void log(Priority priority, Object message) {logger.log(priority, message);}

    public static Appender getAppender(String name) {return logger.getAppender(name);}

    public static void warn(Object message, Throwable t) {logger.warn(message, t);}

    public static void setResourceBundle(ResourceBundle bundle) {logger.setResourceBundle(bundle);}

    public static Level getLevel() {return logger.getLevel();}

    public static void error(Object message) {logger.error(message);}

    //public static Category getInstance(Class clazz) {return Category.getInstance(clazz);}

    public static void log(String callerFQCN, Priority level, Object message, Throwable t) {logger.log(callerFQCN, level, message, t);}

    public static void debug(Object message, Throwable t) {logger.debug(message, t);}

    public static void l7dlog(Priority priority, String key, Throwable t) {logger.l7dlog(priority, key, t);}

    //public static LoggerRepository getDefaultHierarchy() {return Category.getDefaultHierarchy();}

    public static boolean isAttached(Appender appender) {return logger.isAttached(appender);}

    public static void l7dlog(Priority priority, String key, Object[] params, Throwable t) {logger.l7dlog(priority, key, params, t);}

    public static void fatal(Object message, Throwable t) {logger.fatal(message, t);}

    public static void removeAppender(Appender appender) {logger.removeAppender(appender);}

    //public static Level getPriority() {return logger.getPriority();}

    public static void log(Priority priority, Object message, Throwable t) {logger.log(priority, message, t);}

    public static void info(Object message) {logger.info(message);}

    public static void error(Object message, Throwable t) {logger.error(message, t);}

    public static void trace(Object message) {logger.trace(message);}

    public static void addAppender(Appender newAppender) {logger.addAppender(newAppender);}

    public static void setLevel(Level level) {logger.setLevel(level);}

    public static void info(Object message, Throwable t) {logger.info(message, t);}

    //public static void setPriority(Priority priority) {logger.setPriority(priority);}

    //public static Enumeration getCurrentCategories() {return Category.getCurrentCategories();}

    public static void removeAppender(String name) {logger.removeAppender(name);}

    //public static Logger exists(String name) {return Category.exists(name);}

    public static boolean isDebugEnabled() {return logger.isDebugEnabled();}

    public static boolean getAdditivity() {return logger.getAdditivity();}

    //public static void shutdown() {Category.shutdown();}

    //public static Category getInstance(String name) {return Category.getInstance(name);}

    //public static LoggerRepository getHierarchy() {return logger.getHierarchy();}

    public static LoggerRepository getLoggerRepository() {return logger.getLoggerRepository();}

    public static void trace(Object message, Throwable t) {logger.trace(message, t);}

    public static void callAppenders(LoggingEvent event) {logger.callAppenders(event);}

    public static void assertLog(boolean assertion, String msg) {logger.assertLog(assertion, msg);}

    public static String getName() {return logger.getName();}

    public static void removeAllAppenders() {logger.removeAllAppenders();}

    public static void debug(Object message) {logger.debug(message);}

    public static ResourceBundle getResourceBundle() {return logger.getResourceBundle();}

    public static Level getEffectiveLevel() {return logger.getEffectiveLevel();}

    //public static Priority getChainedPriority() {return logger.getChainedPriority();}

    //public static Category getRoot() {return Category.getRoot();}

    public static Logger getLogger(Class clazz) {return Logger.getLogger(clazz);}

    public static void fatal(Object message) {logger.fatal(message);}

    public static boolean isEnabledFor(Priority level) {return logger.isEnabledFor(level);}

    //public static void info(String message) {
    //    logger.info(message);
    //}
    //
    //public static void debug(String message) {
    //    logger.debug(message);
    //}
    //
    //public static void error(String message, Throwable t) {
    //    if (t != null) {
    //        logger.error(message, t);
    //    } else {
    //        logger.error(message);
    //    }
    //}
    //
    //public static void warn(String message) {
    //    logger.warn(message);
    //}
}
