package project.game.common.logging.adapter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.common.logging.core.LogLevel;
import project.game.common.logging.util.LogLevelUtils;
import project.game.engine.api.logging.ILogEvent;
import project.game.engine.api.logging.ILogger;

/**
 * Adapter for Java's built-in Logger to our ILogger interface.
 * Implements the Adapter pattern to adapt Java's Logger to our ILogger
 * interface.
 */
public class JavaLoggerAdapter implements ILogger {
    private final Logger logger;
    private static final Map<String, JavaLoggerAdapter> loggerCache = new HashMap<>();

    /**
     * Creates a new JavaLoggerAdapter for the specified logger.
     *
     * @param logger the Java Logger to adapt
     */
    private JavaLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    /**
     * Gets or creates a JavaLoggerAdapter for the specified name.
     *
     * @param name the logger name
     * @return the JavaLoggerAdapter instance
     */
    public static JavaLoggerAdapter getLoggerInstance(String name) {
        return loggerCache.computeIfAbsent(name, n -> new JavaLoggerAdapter(Logger.getLogger(n)));
    }

    @Override
    public void log(LogLevel level, String message) {
        logger.log(level.getJavaLevel(), message);
    }

    @Override
    public void log(LogLevel level, String message, Throwable thrown) {
        logger.log(level.getJavaLevel(), message, thrown);
    }

    @Override
    public void log(LogLevel level, String format, Object... args) {
        // FIXED: Support both String.format()-style and MessageFormat-style
        // placeholders
        try {
            // Check if this is a MessageFormat-style message with {0}, {1}, etc.
            if (format != null && format.contains("{0")) {
                // Use MessageFormat for {0}, {1}, etc. style placeholders
                String formattedMessage = MessageFormat.format(format, args);
                logger.log(level.getJavaLevel(), formattedMessage);
            } else {
                // Use standard String.format for %s, %d, etc. style placeholders
                String formattedMessage = String.format(format, args);
                logger.log(level.getJavaLevel(), formattedMessage);
            }
        } catch (Exception e) {
            // Fallback in case of formatting error
            logger.log(level.getJavaLevel(), format);
            logger.log(level.getJavaLevel(), "Error formatting message: " + e.getMessage(), e);
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
    public ILogger getLogger(String name) {
        String childName = name;
        if (!name.contains(".")) {
            childName = logger.getName() + "." + name;
        }
        return getLoggerInstance(childName);
    }

    @Override
    public void setLevel(LogLevel level) {
        logger.setLevel(level.getJavaLevel());
    }

    @Override
    public LogLevel getLevel() {
        Level level = logger.getLevel();
        return LogLevelUtils.fromJavaLevel(level);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return logger.isLoggable(level.getJavaLevel());
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void flush() {
        // Java Logger doesn't have a direct flush method,
        // handlers are flushed on close or JVM shutdown
        for (java.util.logging.Handler handler : logger.getHandlers()) {
            handler.flush();
        }
    }

    @Override
    public void log(ILogEvent event) {
        Level javaLevel = event.getLevel().getJavaLevel();

        // Check if this level is enabled
        if (!logger.isLoggable(javaLevel)) {
            return;
        }

        // Create a LogRecord with the event details
        java.util.logging.LogRecord record = new java.util.logging.LogRecord(javaLevel, event.getMessage());
        record.setLoggerName(event.getLoggerName());
        record.setMillis(event.getTimestamp());
        record.setThrown(event.getThrowable());
        record.setSourceClassName(null); // Unknown
        record.setSourceMethodName(null); // Unknown

        // Set thread information if available
        if (event.getThreadName() != null) {
            // Java's LogRecord doesn't have a direct way to set thread name,
            // but we can use parameters or source info as a workaround
            record.setParameters(new Object[] { event.getThreadName() });
        }

        // Log the record
        logger.log(record);
    }

    /**
     * Gets the underlying Java Logger.
     *
     * @return the Java Logger
     */
    Logger getUnderlyingLogger() {
        return logger;
    }

    /**
     * Clears the logger cache. Useful for testing and preventing memory leaks.
     */
    public static void clearCache() {
        loggerCache.clear();
    }
}