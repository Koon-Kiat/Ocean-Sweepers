package project.game.engine.logging;

import project.game.common.logging.core.LogLevel;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerEvent;
import project.game.engine.api.logging.ILoggerEventFactory;
import project.game.engine.api.logging.ILoggerHandler;

/**
 * Abstract base implementation of ILogger that provides common functionality.
 * This class uses the Template Method pattern to allow subclasses to focus on
 * the specific logging mechanism.
 */
public abstract class AbstractLogger implements ILogger {
    private final String name;
    protected final ILoggerEventFactory eventFactory;
    private LogLevel level = LogLevel.INFO;
    private ILoggerHandler firstHandler;

    /**
     * Creates a new AbstractLogger.
     *
     * @param name the logger name
     */
    protected AbstractLogger(String name, ILoggerEventFactory eventFactory) {
        this.name = name;
        this.eventFactory = eventFactory;
    }

    @Override
    public void log(LogLevel level, String message) {
        if (!isEnabled(level)) {
            return;
        }

        ILoggerEvent event = createLogEvent(level, message, null);
        log(event);
    }

    @Override
    public void log(LogLevel level, String message, Throwable thrown) {
        if (!isEnabled(level)) {
            return;
        }

        ILoggerEvent event = createLogEvent(level, message, thrown);
        log(event);
    }

    @Override
    public void log(LogLevel level, String format, Object... args) {
        if (!isEnabled(level)) {
            return;
        }

        String message = formatMessage(format, args);
        ILoggerEvent event = createLogEvent(level, message, null);
        log(event);
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
    public void log(ILoggerEvent event) {
        if (!isEnabled(event.getLevel())) {
            return;
        }

        dispatchLogEvent(event);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return level.getSeverity() >= this.level.getSeverity();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public void setLevel(LogLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("Log level cannot be null");
        }
        this.level = level;
    }

    /**
     * Adds a handler to this logger.
     *
     * @param handler the handler to add
     */
    public void addHandler(ILoggerHandler handler) {
        if (handler == null) {
            return;
        }

        if (firstHandler == null) {
            firstHandler = handler;
        } else {
            ILoggerHandler lastHandler = firstHandler;
            while (lastHandler.getNext() != null) {
                lastHandler = lastHandler.getNext();
            }
            lastHandler.setNext(handler);
        }
    }

    /**
     * Removes a handler from this logger.
     *
     * @param handler the handler to remove
     * @return true if the handler was removed, false if it wasn't found
     */
    public boolean removeHandler(ILoggerHandler handler) {
        if (handler == null || firstHandler == null) {
            return false;
        }

        if (firstHandler == handler) {
            firstHandler = firstHandler.getNext();
            handler.setNext(null);
            return true;
        }

        ILoggerHandler current = firstHandler;
        while (current.getNext() != null) {
            if (current.getNext() == handler) {
                current.setNext(handler.getNext());
                handler.setNext(null);
                return true;
            }
            current = current.getNext();
        }

        return false;
    }

    /**
     * Clears all handlers from this logger.
     */
    public void clearHandlers() {
        // Close all handlers first
        ILoggerHandler current = firstHandler;
        while (current != null) {
            current.close();
            current = current.getNext();
        }

        firstHandler = null;
    }

    /**
     * Gets the first handler in the chain.
     * 
     * @return the first handler, or null if none
     */
    public ILoggerHandler getFirstHandler() {
        return firstHandler;
    }

    @Override
    public void flush() {
        ILoggerHandler current = firstHandler;
        while (current != null) {
            current.flush();
            current = current.getNext();
        }
    }

    /**
     * Formats a message with the specified arguments.
     * This is a template method that can be overridden by subclasses.
     *
     * @param format the format string
     * @param args   the format arguments
     * @return the formatted message
     */
    protected String formatMessage(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return format + " [Error formatting message: " + e.getMessage() + "]";
        }
    }

    /**
     * Dispatches a log event to the handlers.
     * This is a template method that can be overridden by subclasses.
     *
     * @param event the log event to dispatch
     */
    protected void dispatchLogEvent(ILoggerEvent event) {
        if (firstHandler != null) {
            firstHandler.handle(event);
        }
    }

    /**
     * Creates a log event with the specified parameters.
     * This is a template method that can be overridden by subclasses.
     *
     * @param level     the log level
     * @param message   the log message
     * @param throwable the throwable (may be null)
     * @return the log event
     */
    protected abstract ILoggerEvent createLogEvent(LogLevel level, String message, Throwable throwable);

}