package project.game.common.logging.core;

import project.game.engine.api.logging.ILogger;

/**
 * Game-specific logger that provides convenient methods for game logging.
 * This class serves as a high-level wrapper around the underlying logging
 * system.
 */
public class GameLogger {
    private final ILogger logger;

    /**
     * Creates a new GameLogger for the specified class.
     * 
     * @param clazz the class to log for
     */
    public GameLogger(Class<?> clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    /**
     * Creates a new GameLogger for the specified component name.
     * 
     * @param component the component name
     */
    public GameLogger(String component) {
        this.logger = LogManager.getLogger(component);
    }

    /**
     * Gets the underlying logger.
     * 
     * @return the underlying ILogger
     */
    public ILogger getUnderlyingLogger() {
        return logger;
    }

    /**
     * Generic logging method that handles the common logging patterns
     */
    private void logWithLevel(LogLevel level, String message) {
        logger.log(level, message);
    }

    private void logWithLevel(LogLevel level, String format, Object... args) {
        logger.log(level, format, args);
    }

    private void logWithLevel(LogLevel level, String message, Throwable t) {
        logger.log(level, message, t);
    }

    // Trace methods
    public void trace(String message) {
        logWithLevel(LogLevel.TRACE, message);
    }

    public void trace(String format, Object... args) {
        logWithLevel(LogLevel.TRACE, format, args);
    }

    // Debug methods
    public void debug(String message) {
        logWithLevel(LogLevel.DEBUG, message);
    }

    public void debug(String format, Object... args) {
        logWithLevel(LogLevel.DEBUG, format, args);
    }

    // Info methods
    public void info(String message) {
        logWithLevel(LogLevel.INFO, message);
    }

    public void info(String format, Object... args) {
        logWithLevel(LogLevel.INFO, format, args);
    }

    // Warn methods
    public void warn(String message) {
        logWithLevel(LogLevel.WARN, message);
    }

    public void warn(String format, Object... args) {
        logWithLevel(LogLevel.WARN, format, args);
    }

    // Error methods
    public void error(String message) {
        logWithLevel(LogLevel.ERROR, message);
    }

    public void error(String format, Object... args) {
        logWithLevel(LogLevel.ERROR, format, args);
    }

    public void error(String message, Throwable e) {
        logWithLevel(LogLevel.ERROR, message, e);
    }

    // Fatal methods
    public void fatal(String message) {
        logWithLevel(LogLevel.FATAL, message);
    }

    public void fatal(String format, Object... args) {
        logWithLevel(LogLevel.FATAL, format, args);
    }

    public void fatal(String message, Throwable e) {
        logWithLevel(LogLevel.FATAL, message, e);
    }

    /**
     * Checks if trace logging is enabled.
     * 
     * @return true if trace is enabled
     */
    public boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.TRACE);
    }

    /**
     * Checks if debug logging is enabled.
     * 
     * @return true if debug is enabled
     */
    public boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    /**
     * Checks if info logging is enabled.
     * 
     * @return true if info is enabled
     */
    public boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO);
    }

    /**
     * Checks if warn logging is enabled.
     * 
     * @return true if warn is enabled
     */
    public boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARN);
    }

    /**
     * Checks if error logging is enabled.
     * 
     * @return true if error is enabled
     */
    public boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    /**
     * Checks if fatal logging is enabled.
     * 
     * @return true if fatal is enabled
     */
    public boolean isFatalEnabled() {
        return logger.isEnabled(LogLevel.FATAL);
    }
}