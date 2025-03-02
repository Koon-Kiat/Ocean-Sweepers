package project.game.common.logging.api;

import java.util.logging.Level;

import project.game.common.logging.context.LogMessageContext;

/**
 * An extension of ILogger that supports contextual logging.
 * This interface allows keeping the core logging abstraction (ILogger) free
 * from context-specific dependencies while adding contextual capabilities
 * through extension.
 */
public interface ContextualLogger extends ILogger {

    /**
     * Logs a message with context at the specified level.
     * 
     * @param level   the logging level
     * @param context the log context
     * @param message the message to log
     */
    void log(Level level, LogMessageContext context, String message);

    /**
     * Logs a message with exception and context at the specified level.
     * 
     * @param level   the logging level
     * @param context the log context
     * @param message the message to log
     * @param thrown  the exception to log
     */
    void log(Level level, LogMessageContext context, String message, Throwable thrown);

    /**
     * Logs a formatted message with context at the specified level.
     * 
     * @param level   the logging level
     * @param context the log context
     * @param format  the message format
     * @param args    the arguments for the format
     */
    void log(Level level, LogMessageContext context, String format, Object... args);

    /**
     * Sets a default context to use for all logs when a context is not explicitly
     * provided.
     * 
     * @param defaultContext the default context
     */
    void setDefaultContext(LogMessageContext defaultContext);

    /**
     * Gets the default context.
     * 
     * @return the default context or null if not set
     */
    LogMessageContext getDefaultContext();

    /**
     * Creates a child logger with a specified context.
     * 
     * @param name    the logger name
     * @param context the context for the new logger
     * @return a new contextual logger instance
     */
    ContextualLogger getContextualLogger(String name, LogMessageContext context);
}