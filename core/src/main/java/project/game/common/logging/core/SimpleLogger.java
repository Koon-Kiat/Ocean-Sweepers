package project.game.common.logging.core;

import project.game.common.logging.factory.SimpleLoggerFactory;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerEvent;
import project.game.engine.api.logging.ILoggerEventFactory;
import project.game.engine.logging.AbstractLogger;

/**
 * A simple implementation of the logging system that extends AbstractLogger.
 */
public class SimpleLogger extends AbstractLogger {
    private final SimpleLoggerFactory factory;

    public SimpleLogger(String name, SimpleLoggerFactory factory) {
        super(name, new DefaultLogEventFactory());
        this.factory = factory;
    }

    @Override
    protected ILoggerEvent createLogEvent(LogLevel level, String message, Throwable throwable) {
        return eventFactory.createEvent(
                getName(),
                level,
                formatMessage(message, level, message, throwable),
                throwable,
                System.currentTimeMillis(),
                Thread.currentThread().getName());
    }

    @Override
    protected String formatMessage(String message, Object... args) {
        if (message == null || args == null || args.length == 0) {
            return message;
        }

        // Replace {n} placeholders with actual values
        String result = message;
        for (int i = 0; i < args.length; i++) {
            String placeholder = "{" + i + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(args[i]));
            }
        }
        return result;
    }

    @Override
    protected void dispatchLogEvent(ILoggerEvent event) {
        super.dispatchLogEvent(event);
    }

    @Override
    public ILogger getLogger(String name) {
        return factory.getLogger(name);
    }

    /**
     * Default implementation of ILogEventFactory that creates LogEvent instances.
     */
    private static class DefaultLogEventFactory implements ILoggerEventFactory {
        @Override
        public ILoggerEvent createEvent(String loggerName, LogLevel level, String message,
                Throwable throwable, long timestamp, String threadName) {
            return LogEvent.builder()
                    .level(level)
                    .message(message)
                    .throwable(throwable)
                    .loggerName(loggerName)
                    .timestamp(timestamp)
                    .threadName(threadName)
                    .build();
        }
    }
}