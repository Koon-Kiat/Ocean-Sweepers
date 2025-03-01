package project.game.common.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.common.api.ILogger;

/**
 * Adapter for Java's built-in Logger to our ILogger interface.
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
    public void log(Level level, String message) {
        logger.log(level, message);
    }

    @Override
    public void log(Level level, String message, Throwable thrown) {
        logger.log(level, message, thrown);
    }

    @Override
    public void log(Level level, String format, Object... args) {
        logger.log(level, format, args);
    }

    @Override
    public ILogger getLogger(String name) {
        return getLoggerInstance(name);
    }

    @Override
    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    @Override
    public Level getLevel() {
        return logger.getLevel();
    }

    @Override
    public void flush() {
        // Java Logger doesn't have a direct flush method,
        // handlers are flushed on close or JVM shutdown
        for (java.util.logging.Handler handler : logger.getHandlers()) {
            handler.flush();
        }
    }

    /**
     * Gets the underlying Java Logger.
     *
     * @return the Java Logger
     */
    Logger getUnderlyingLogger() {
        return logger;
    }
}