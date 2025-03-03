package project.game.common.logging.core;

import project.game.engine.api.logging.ILogEvent;

/**
 * Implementation of ILogEvent that stores log event information.
 * Provides a builder for easy creation of log events.
 */
public class LogEvent implements ILogEvent {
    private final LogLevel level;
    private final String message;
    private final Throwable throwable;
    private final String loggerName;
    private final long timestamp;
    private final String threadName;

    private LogEvent(Builder builder) {
        this.level = builder.level;
        this.message = builder.message;
        this.throwable = builder.throwable;
        this.loggerName = builder.loggerName;
        this.timestamp = builder.timestamp;
        this.threadName = builder.threadName;
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    /**
     * Creates a new builder for constructing LogEvent objects.
     * 
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating LogEvent objects.
     */
    public static class Builder {
        private LogLevel level = LogLevel.INFO;
        private String message = "";
        private Throwable throwable = null;
        private String loggerName = "root";
        private long timestamp = System.currentTimeMillis();
        private String threadName = Thread.currentThread().getName();

        /**
         * Sets the log level.
         * 
         * @param level the log level
         * @return this builder instance
         */
        public Builder level(LogLevel level) {
            if (level != null) {
                this.level = level;
            }
            return this;
        }

        /**
         * Sets the log message.
         * 
         * @param message the message
         * @return this builder instance
         */
        public Builder message(String message) {
            if (message != null) {
                this.message = message;
            }
            return this;
        }

        /**
         * Sets the throwable.
         * 
         * @param throwable the throwable
         * @return this builder instance
         */
        public Builder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        /**
         * Sets the logger name.
         * 
         * @param loggerName the logger name
         * @return this builder instance
         */
        public Builder loggerName(String loggerName) {
            if (loggerName != null) {
                this.loggerName = loggerName;
            }
            return this;
        }

        /**
         * Sets the timestamp.
         * 
         * @param timestamp the timestamp
         * @return this builder instance
         */
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the thread name.
         * 
         * @param threadName the thread name
         * @return this builder instance
         */
        public Builder threadName(String threadName) {
            if (threadName != null) {
                this.threadName = threadName;
            }
            return this;
        }

        /**
         * Builds a LogEvent with the configured properties.
         * 
         * @return a new LogEvent
         */
        public LogEvent build() {
            return new LogEvent(this);
        }
    }
}