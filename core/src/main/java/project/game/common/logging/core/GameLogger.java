package project.game.common.logging.core;

import project.game.common.logging.api.ContextualLogger;
import project.game.common.logging.context.ContextualLoggerFactory;
import project.game.common.logging.context.LogMessageContext;

/**
 * Game-specific logger that provides convenient methods for game logging.
 * This class adds game-specific context and logging levels to the core logging
 * system.
 */
public class GameLogger {
    private final ContextualLogger logger;
    private final String component;

    /**
     * Creates a new GameLogger for the specified class.
     * 
     * @param clazz the class to log for
     */
    public GameLogger(Class<?> clazz) {
        this.logger = ContextualLoggerFactory.getLogger(clazz);
        this.component = clazz.getSimpleName();
    }

    /**
     * Creates a new GameLogger for the specified component name.
     * 
     * @param component the component name
     */
    public GameLogger(String component) {
        this.logger = ContextualLoggerFactory.getLogger("project.game." + component);
        this.component = component;
    }

    /**
     * Logs a trace message.
     * 
     * @param message the message to log
     */
    public void trace(String message) {
        logger.log(LogLevel.TRACE.getJavaLevel(), message);
    }

    /**
     * Logs a trace message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void trace(String format, Object... args) {
        logger.log(LogLevel.TRACE.getJavaLevel(), format, args);
    }

    /**
     * Logs a debug message.
     * 
     * @param message the message to log
     */
    public void debug(String message) {
        logger.log(LogLevel.DEBUG.getJavaLevel(), message);
    }

    /**
     * Logs a debug message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void debug(String format, Object... args) {
        logger.log(LogLevel.DEBUG.getJavaLevel(), format, args);
    }

    /**
     * Logs an info message.
     * 
     * @param message the message to log
     */
    public void info(String message) {
        logger.log(LogLevel.INFO.getJavaLevel(), message);
    }

    /**
     * Logs an info message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void info(String format, Object... args) {
        logger.log(LogLevel.INFO.getJavaLevel(), format, args);
    }

    /**
     * Logs a warning message.
     * 
     * @param message the message to log
     */
    public void warn(String message) {
        logger.log(LogLevel.WARN.getJavaLevel(), message);
    }

    /**
     * Logs a warning message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void warn(String format, Object... args) {
        logger.log(LogLevel.WARN.getJavaLevel(), format, args);
    }

    /**
     * Logs a warning message with an exception.
     * 
     * @param message the message to log
     * @param e       the exception to log
     */
    public void warn(String message, Throwable e) {
        logger.log(LogLevel.WARN.getJavaLevel(), message, e);
    }

    /**
     * Logs an error message.
     * 
     * @param message the message to log
     */
    public void error(String message) {
        logger.log(LogLevel.ERROR.getJavaLevel(), message);
    }

    /**
     * Logs an error message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void error(String format, Object... args) {
        logger.log(LogLevel.ERROR.getJavaLevel(), format, args);
    }

    /**
     * Logs an error message with an exception.
     * 
     * @param message the message to log
     * @param e       the exception to log
     */
    public void error(String message, Throwable e) {
        logger.log(LogLevel.ERROR.getJavaLevel(), message, e);
    }

    /**
     * Logs a fatal error message.
     * 
     * @param message the message to log
     */
    public void fatal(String message) {
        logger.log(LogLevel.FATAL.getJavaLevel(), message);
    }

    /**
     * Logs a fatal error message with formatting.
     * 
     * @param format the format string
     * @param args   the format arguments
     */
    public void fatal(String format, Object... args) {
        logger.log(LogLevel.FATAL.getJavaLevel(), format, args);
    }

    /**
     * Logs a fatal error message with an exception.
     * 
     * @param message the message to log
     * @param e       the exception to log
     */
    public void fatal(String message, Throwable e) {
        logger.log(LogLevel.FATAL.getJavaLevel(), message, e);
    }

    /**
     * Logs a message at the specified level.
     * 
     * @param level   the logging level
     * @param message the message to log
     */
    public void log(LogLevel level, String message) {
        logger.log(level.getJavaLevel(), message);
    }

    /**
     * Logs a message with formatting at the specified level.
     * 
     * @param level  the logging level
     * @param format the format string
     * @param args   the format arguments
     */
    public void log(LogLevel level, String format, Object... args) {
        logger.log(level.getJavaLevel(), format, args);
    }

    /**
     * Logs a message with an exception at the specified level.
     * 
     * @param level   the logging level
     * @param message the message to log
     * @param e       the exception to log
     */
    public void log(LogLevel level, String message, Throwable e) {
        logger.log(level.getJavaLevel(), message, e);
    }

    /**
     * Logs a game event with context.
     * 
     * @param eventType the type of event
     * @param message   the event message
     */
    public void gameEvent(String eventType, String message) {
        LogMessageContext context = ContextualLoggerFactory.createContext()
                .operation(eventType)
                .with("component", component)
                .build();

        logger.log(LogLevel.INFO.getJavaLevel(), context, message);
    }

    /**
     * Logs a game event with context and formatting.
     * 
     * @param eventType the type of event
     * @param format    the format string
     * @param args      the format arguments
     */
    public void gameEvent(String eventType, String format, Object... args) {
        LogMessageContext context = ContextualLoggerFactory.createContext()
                .operation(eventType)
                .with("component", component)
                .build();

        logger.log(LogLevel.INFO.getJavaLevel(), context, format, args);
    }

    /**
     * Logs a gameplay metric with context.
     * 
     * @param metricName the name of the metric
     * @param value      the metric value
     */
    public void metric(String metricName, Object value) {
        LogMessageContext context = ContextualLoggerFactory.createContext()
                .operation("Metric")
                .with("component", component)
                .with("metric", metricName)
                .build();

        logger.log(LogLevel.DEBUG.getJavaLevel(), context, "Metric recorded: {0}={1}", metricName, value);
    }

    /**
     * Gets the underlying ContextualLogger.
     * 
     * @return the ContextualLogger
     */
    public ContextualLogger getContextualLogger() {
        return logger;
    }

    /**
     * Sets the logging level for this logger.
     * 
     * @param level the level to set
     */
    public void setLevel(LogLevel level) {
        logger.setLevel(level.getJavaLevel());
    }

    /**
     * Gets the current logging level for this logger.
     * 
     * @return the current level
     */
    public LogLevel getLevel() {
        return LogLevel.fromJavaLevel(logger.getLevel());
    }

    /**
     * Gets the component name for this logger.
     * 
     * @return the component name
     */
    public String getComponent() {
        return component;
    }
}