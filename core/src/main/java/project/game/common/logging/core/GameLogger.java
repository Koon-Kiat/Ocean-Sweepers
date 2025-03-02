package project.game.common.logging.core;

import java.util.logging.Level;

import project.game.common.logging.api.ILogger;

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

    public void trace(String message) {
        logger.log(LogLevel.TRACE.getJavaLevel(), message);
    }

    public void trace(String format, Object... args) {
        logger.log(LogLevel.TRACE.getJavaLevel(), format, args);
    }

    public void debug(String message) {
        logger.log(LogLevel.DEBUG.getJavaLevel(), message);
    }

    public void debug(String format, Object... args) {
        logger.log(LogLevel.DEBUG.getJavaLevel(), format, args);
    }

    public void info(String message) {
        logger.log(LogLevel.INFO.getJavaLevel(), message);
    }

    public void info(String format, Object... args) {
        logger.log(LogLevel.INFO.getJavaLevel(), format, args);
    }

    public void warn(String message) {
        logger.log(LogLevel.WARN.getJavaLevel(), message);
    }

    public void warn(String format, Object... args) {
        logger.log(LogLevel.WARN.getJavaLevel(), format, args);
    }

    public void error(String message) {
        logger.log(LogLevel.ERROR.getJavaLevel(), message);
    }

    public void error(String format, Object... args) {
        logger.log(LogLevel.ERROR.getJavaLevel(), format, args);
    }

    public void error(String message, Throwable e) {
        logger.log(LogLevel.ERROR.getJavaLevel(), message, e);
    }

    public void fatal(String message) {
        logger.log(LogLevel.FATAL.getJavaLevel(), message);
    }

    public void fatal(String format, Object... args) {
        logger.log(LogLevel.FATAL.getJavaLevel(), format, args);
    }

    public void fatal(String message, Throwable e) {
        logger.log(LogLevel.FATAL.getJavaLevel(), message, e);
    }

    public void log(LogLevel level, String message) {
        logger.log(level.getJavaLevel(), message);
    }

    public void log(LogLevel level, String format, Object... args) {
        logger.log(level.getJavaLevel(), format, args);
    }

    public void log(LogLevel level, String message, Throwable e) {
        logger.log(level.getJavaLevel(), message, e);
    }

    public void setLevel(LogLevel level) {
        logger.setLevel(level.getJavaLevel());
    }

    public LogLevel getLevel() {
        Level level = logger.getLevel();
        return LogLevel.fromJavaLevel(level);
    }
}