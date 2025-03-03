package project.game.engine.api.logging;

import project.game.common.logging.core.LogLevel;

/**
 * Interface for loggers.
 * Provides methods for logging messages at different levels.
 */
public interface ILogger {
    /**
     * Logs a message at the specified level.
     *
     * @param level   the log level
     * @param message the message to log
     */
    void log(LogLevel level, String message);

    /**
     * Logs a message with an exception at the specified level.
     *
     * @param level   the log level
     * @param message the message to log
     * @param thrown  the exception to log
     */
    void log(LogLevel level, String message, Throwable thrown);

    /**
     * Logs a formatted message at the specified level.
     *
     * @param level  the log level
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void log(LogLevel level, String format, Object... args);

    /**
     * Logs a log event directly.
     * 
     * @param event the log event to log
     */
    void log(ILoggerEvent event);

    /**
     * Logs a message at TRACE level.
     *
     * @param message the message to log
     */
    void trace(String message);

    /**
     * Logs a formatted message at TRACE level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void trace(String format, Object... args);

    /**
     * Logs a message at DEBUG level.
     *
     * @param message the message to log
     */
    void debug(String message);

    /**
     * Logs a formatted message at DEBUG level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void debug(String format, Object... args);

    /**
     * Logs a message at INFO level.
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a formatted message at INFO level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void info(String format, Object... args);

    /**
     * Logs a message at WARN level.
     *
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Logs a formatted message at WARN level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void warn(String format, Object... args);

    /**
     * Logs a message at ERROR level.
     *
     * @param message the message to log
     */
    void error(String message);

    /**
     * Logs a formatted message at ERROR level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void error(String format, Object... args);

    /**
     * Logs a message with an exception at ERROR level.
     *
     * @param message the message to log
     * @param thrown  the exception to log
     */
    void error(String message, Throwable thrown);

    /**
     * Logs a message at FATAL level.
     *
     * @param message the message to log
     */
    void fatal(String message);

    /**
     * Logs a formatted message at FATAL level.
     *
     * @param format the format string
     * @param args   the arguments to the format string
     */
    void fatal(String format, Object... args);

    /**
     * Logs a message with an exception at FATAL level.
     *
     * @param message the message to log
     * @param thrown  the exception to log
     */
    void fatal(String message, Throwable thrown);

    /**
     * Gets a child logger with the specified name.
     *
     * @param name the child logger name
     * @return the child logger
     */
    ILogger getLogger(String name);

    /**
     * Sets the log level for this logger.
     *
     * @param level the log level
     */
    void setLevel(LogLevel level);

    /**
     * Gets the log level for this logger.
     *
     * @return the log level
     */
    LogLevel getLevel();

    /**
     * Checks if logging is enabled for the specified level.
     *
     * @param level the log level
     * @return true if logging is enabled for the level
     */
    boolean isEnabled(LogLevel level);

    /**
     * Gets the name of this logger.
     *
     * @return the logger name
     */
    String getName();

    /**
     * Flushes any buffered log messages.
     */
    void flush();
}