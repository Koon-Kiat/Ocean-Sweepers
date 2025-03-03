package project.game.engine.api.logging;

import project.game.common.logging.core.LogLevel;

/**
 * Factory interface for creating log event objects.
 */
public interface ILoggerEventFactory {
    /**
     * Creates a log event.
     *
     * @param loggerName name of the logger
     * @param level      log level
     * @param message    log message
     * @param throwable  optional throwable
     * @param timestamp  time of the event
     * @param threadName name of the thread
     * @return the created log event
     */
    ILoggerEvent createEvent(String loggerName, LogLevel level, String message,
            Throwable throwable, long timestamp, String threadName);
}