package project.game.engine.logging.base;

import java.text.MessageFormat;

import project.game.engine.logging.api.ILogger;
import project.game.engine.logging.api.LogLevel;

/**
 * Abstract base logger that implements core logging functionality.
 * Game-specific loggers can extend this class to add custom behavior.
 */
public abstract class AbstractLogger implements ILogger {

    protected final String name;
    protected LogLevel level;

    protected AbstractLogger(String name, LogLevel initialLevel) {
        this.name = name;
        this.level = initialLevel;
    }

    @Override
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return this.level.compareSeverity(level) <= 0;
    }

    @Override
    public void log(LogLevel level, String message) {
        if (isEnabled(level)) {
            doLog(level, message, null);
        }
    }

    @Override
    public void log(LogLevel level, String message, Throwable thrown) {
        if (isEnabled(level)) {
            doLog(level, message, thrown);
        }
    }

    @Override
    public void log(LogLevel level, String format, Object... args) {
        if (isEnabled(level)) {
            String message = MessageFormat.format(format, args);
            doLog(level, message, null);
        }
    }

    @Override
    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    @Override
    public void trace(String format, Object... args) {
        log(LogLevel.TRACE, format, args);
    }

    @Override
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    @Override
    public void debug(String format, Object... args) {
        log(LogLevel.DEBUG, format, args);
    }

    @Override
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    @Override
    public void info(String format, Object... args) {
        log(LogLevel.INFO, format, args);
    }

    @Override
    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    @Override
    public void warn(String format, Object... args) {
        log(LogLevel.WARN, format, args);
    }

    @Override
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    @Override
    public void error(String format, Object... args) {
        log(LogLevel.ERROR, format, args);
    }

    @Override
    public void error(String message, Throwable thrown) {
        log(LogLevel.ERROR, message, thrown);
    }

    @Override
    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    @Override
    public void fatal(String format, Object... args) {
        log(LogLevel.FATAL, format, args);
    }

    @Override
    public void fatal(String message, Throwable thrown) {
        log(LogLevel.FATAL, message, thrown);
    }

    @Override
    public abstract ILogger getLogger(String name);

    @Override
    public abstract void flush();

    protected abstract void doLog(LogLevel level, String message, Throwable thrown);
}