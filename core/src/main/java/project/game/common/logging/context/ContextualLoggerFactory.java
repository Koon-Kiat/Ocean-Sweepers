package project.game.common.logging.context;

import project.game.common.logging.LogManager;
import project.game.common.logging.api.ContextualLogger;
import project.game.common.logging.api.ILogger;
import project.game.common.logging.impl.ContextualLoggerImpl;

/**
 * Factory for creating contextual loggers.
 * This factory makes it easy to obtain ContextualLogger instances.
 */
public final class ContextualLoggerFactory {

    private ContextualLoggerFactory() {
        // Utility class, no instantiation
    }

    /**
     * Gets a contextual logger for the specified class.
     * 
     * @param clazz the class
     * @return a new contextual logger
     */
    public static ContextualLogger getLogger(Class<?> clazz) {
        ILogger baseLogger = LogManager.getLogger(clazz);
        return new ContextualLoggerImpl(baseLogger);
    }

    /**
     * Gets a contextual logger for the specified name.
     * 
     * @param name the logger name
     * @return a new contextual logger
     */
    public static ContextualLogger getLogger(String name) {
        ILogger baseLogger = LogManager.getLogger(name);
        return new ContextualLoggerImpl(baseLogger);
    }

    /**
     * Gets a contextual logger with the specified context.
     * 
     * @param clazz   the class
     * @param context the default context for the logger
     * @return a new contextual logger with the specified context
     */
    public static ContextualLogger getLogger(Class<?> clazz, LogMessageContext context) {
        ILogger baseLogger = LogManager.getLogger(clazz);
        return new ContextualLoggerImpl(baseLogger, context);
    }

    /**
     * Gets a contextual logger with the specified context.
     * 
     * @param name    the logger name
     * @param context the default context for the logger
     * @return a new contextual logger with the specified context
     */
    public static ContextualLogger getLogger(String name, LogMessageContext context) {
        ILogger baseLogger = LogManager.getLogger(name);
        return new ContextualLoggerImpl(baseLogger, context);
    }

    /**
     * Creates a new context builder for building log contexts.
     * 
     * @return a new context builder
     */
    public static LogMessageContextBuilder createContext() {
        return new LogMessageContextBuilder();
    }

    /**
     * Creates a new context builder with the specified operation name.
     * 
     * @param operationName the operation name
     * @return a new context builder
     */
    public static LogMessageContextBuilder createContext(String operationName) {
        return new LogMessageContextBuilder().operation(operationName);
    }
}