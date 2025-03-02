package project.game.common.logging.core;

import java.util.logging.Level;

import project.game.common.logging.api.ILogger;
import project.game.common.logging.util.LogLevelUtils;

/**
 * Game-specific logger that provides convenient methods for game logging.
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
        this.logger = LogManager.getLogger("project.game." + component);
    }

    /**
     * Generic logging method that handles the common logging patterns
     */
    private void logWithLevel(LogLevel level, String message) {
        logger.log(level.getJavaLevel(), message);
    }

    private void logWithLevel(LogLevel level, String format, Object... args) {
        logger.log(level.getJavaLevel(), format, args);
    }

    private void logWithLevel(LogLevel level, String message, Throwable t) {
        logger.log(level.getJavaLevel(), message, t);
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

    // Generic logging methods
    public void log(LogLevel level, String message) {
        logWithLevel(level, message);
    }

    public void log(LogLevel level, String format, Object... args) {
        logWithLevel(level, format, args);
    }

    public void log(LogLevel level, String message, Throwable e) {
        logWithLevel(level, message, e);
    }

    public void setLevel(LogLevel level) {
        logger.setLevel(level.getJavaLevel());
    }

    public LogLevel getLevel() {
        Level level = logger.getLevel();
        return LogLevelUtils.fromJavaLevel(level);
    }
}