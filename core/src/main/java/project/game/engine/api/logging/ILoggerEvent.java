package project.game.engine.api.logging;

/**
 * Interface for log events.
 * Log events contain all information related to a single logging operation.
 */
public interface ILoggerEvent {
    /**
     * Gets the log level.
     *
     * @return the log level
     */
    LogLevel getLevel();

    /**
     * Gets the log message.
     *
     * @return the log message
     */
    String getMessage();

    /**
     * Gets the throwable associated with this log event, if any.
     *
     * @return the throwable, or null if none
     */
    Throwable getThrowable();

    /**
     * Gets the name of the logger that created this event.
     *
     * @return the logger name
     */
    String getLoggerName();

    /**
     * Gets the timestamp of this event in milliseconds since the epoch.
     *
     * @return the timestamp
     */
    long getTimestamp();

    /**
     * Gets the name of the thread that created this event.
     *
     * @return the thread name
     */
    String getThreadName();
}