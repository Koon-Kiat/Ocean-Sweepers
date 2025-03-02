package project.game.common.logging.impl;

import java.util.logging.Level;

import project.game.common.logging.api.ContextualLogger;
import project.game.common.logging.api.ILogger;
import project.game.common.logging.context.LogMessageContext;

/**
 * Implementation of ContextualLogger that decorates an ILogger instance.
 * Uses the Decorator pattern to add contextual capabilities to any ILogger
 * implementation.
 */
public class ContextualLoggerImpl implements ContextualLogger {
    private final ILogger delegate;
    private LogMessageContext defaultContext;

    /**
     * Creates a new ContextualLoggerImpl that decorates the specified logger.
     * 
     * @param delegate the logger to delegate to
     */
    public ContextualLoggerImpl(ILogger delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new ContextualLoggerImpl with a default context.
     * 
     * @param delegate       the logger to delegate to
     * @param defaultContext the default context to use
     */
    public ContextualLoggerImpl(ILogger delegate, LogMessageContext defaultContext) {
        this.delegate = delegate;
        this.defaultContext = defaultContext;
    }

    @Override
    public void log(Level level, String message) {
        if (defaultContext != null) {
            log(level, defaultContext, message);
        } else {
            delegate.log(level, message);
        }
    }

    @Override
    public void log(Level level, String message, Throwable thrown) {
        if (defaultContext != null) {
            log(level, defaultContext, message, thrown);
        } else {
            delegate.log(level, message, thrown);
        }
    }

    @Override
    public void log(Level level, String format, Object... args) {
        if (defaultContext != null) {
            log(level, defaultContext, format, args);
        } else {
            delegate.log(level, format, args);
        }
    }

    @Override
    public void log(Level level, LogMessageContext context, String message) {
        String contextualMessage = buildContextualMessage(context, message);
        delegate.log(level, contextualMessage);
    }

    @Override
    public void log(Level level, LogMessageContext context, String message, Throwable thrown) {
        String contextualMessage = buildContextualMessage(context, message);
        delegate.log(level, contextualMessage, thrown);
    }

    @Override
    public void log(Level level, LogMessageContext context, String format, Object... args) {
        // Format the message first
        String formattedMessage = String.format(format, args);
        // Then add context
        String contextualMessage = buildContextualMessage(context, formattedMessage);
        delegate.log(level, contextualMessage);
    }

    @Override
    public ILogger getLogger(String name) {
        // Return a new contextual logger that wraps the delegate's logger
        return new ContextualLoggerImpl(delegate.getLogger(name), defaultContext);
    }

    @Override
    public ContextualLogger getContextualLogger(String name, LogMessageContext context) {
        return new ContextualLoggerImpl(delegate.getLogger(name), context);
    }

    @Override
    public void setLevel(Level level) {
        delegate.setLevel(level);
    }

    @Override
    public Level getLevel() {
        return delegate.getLevel();
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void setDefaultContext(LogMessageContext defaultContext) {
        this.defaultContext = defaultContext;
    }

    @Override
    public LogMessageContext getDefaultContext() {
        return defaultContext;
    }

    /**
     * Builds a message with context information prepended.
     * 
     * @param context the context to include
     * @param message the original message
     * @return the message with context information
     */
    private String buildContextualMessage(LogMessageContext context, String message) {
        if (context == null) {
            return message;
        }
        return context.toString() + " " + message;
    }
}