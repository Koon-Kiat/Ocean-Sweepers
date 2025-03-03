package project.game.common.logging.core;

import project.game.engine.api.logging.LogLevel;

/**
 * Configuration for the logging system.
 * This class holds all configurable parameters for logging.
 */
public class LoggerConfig {
    private final String logDirectory;
    private final int maxLogFiles;
    private final LogLevel defaultLogLevel;

    private LoggerConfig(Builder builder) {
        this.logDirectory = builder.logDirectory;
        this.maxLogFiles = builder.maxLogFiles;
        this.defaultLogLevel = builder.defaultLogLevel;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public LogLevel getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public static class Builder {
        private String logDirectory;
        private int maxLogFiles = 10; // reasonable default
        private LogLevel defaultLogLevel = LogLevel.INFO;

        public Builder withLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
            return this;
        }

        public Builder withMaxLogFiles(int maxLogFiles) {
            if (maxLogFiles > 0) {
                this.maxLogFiles = maxLogFiles;
            }
            return this;
        }

        public Builder withDefaultLogLevel(LogLevel level) {
            this.defaultLogLevel = level;
            return this;
        }

        public LoggerConfig build() {
            if (logDirectory == null || logDirectory.trim().isEmpty()) {
                throw new IllegalStateException("Log directory must be specified");
            }
            return new LoggerConfig(this);
        }
    }
}