package project.game.common.logging;

import java.util.logging.Level;

/**
 * Interface for logging operations. Abstracts the actual logging
 * implementation.
 */
public interface ILogger {
    /**
     * Logs a message at the specified level.
     *
     * @param level   the logging level
     * @param message the message to log
     */
    void log(Level level, String message);

    /**
     * Logs a message with an exception at the specified level.
     *
     * @param level   the logging level
     * @param message the message to log
     * @param thrown  the exception to log
     */
    void log(Level level, String message, Throwable thrown);

    /**
     * Logs a formatted message at the specified level.
     *
     * @param level  the logging level
     * @param format the message format
     * @param args   the arguments for the format
     */
    void log(Level level, String format, Object... args);

    /**
     * Gets the logger for a specific class or name.
     *
     * @param name the logger name
     * @return the logger instance
     */
    ILogger getLogger(String name);

    /**
     * Sets the logging level.
     *
     * @param level the level to set
     */
    void setLevel(Level level);

    /**
     * Gets the current logging level.
     *
     * @return the current level
     */
    Level getLevel();

    /**
     * Flushes any buffered logs.
     */
    void flush();
}