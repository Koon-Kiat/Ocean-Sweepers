package project.game.common.logging.core;

import java.io.File;
import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;

/**
 * Utility class for initializing the logging system with game-specific
 * defaults.
 */
public final class LogInitializer {

    private LogInitializer() {
        // Utility class, no instantiation
    }

    /**
     * Initializes the logging system with default settings.
     */
    public static void initialize() {
        initialize(new LoggerConfig());
    }

    /**
     * Initializes the logging system with the specified configuration.
     * 
     * @param config the logger configuration to use
     */
    public static void initialize(LoggerConfig config) {
        LogManager.initialize(config);
    }

    /**
     * Creates a builder for configuring the logging system.
     * 
     * @return a new logger config builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Shuts down the logging system.
     */
    public static void shutdown() {
        LogManager.shutdown();
    }

    /**
     * Builder for configuring the logging system with sensible defaults for games.
     */
    public static class Builder {
        private final LoggerConfig config = new LoggerConfig();

        /**
         * Sets the log file prefix.
         * 
         * @param prefix the prefix for log files
         * @return this builder instance
         */
        public Builder withLogPrefix(String prefix) {
            config.setLogFilePrefix(prefix);
            return this;
        }

        /**
         * Sets the log directory within the project root.
         * 
         * @param directory the log directory
         * @return this builder instance
         */
        public Builder withLogDirectory(String directory) {
            config.setLogDirectory(directory);
            return this;
        }

        /**
         * Sets the console logging level.
         * 
         * @param level the console logging level
         * @return this builder instance
         */
        public Builder withConsoleLevel(Level level) {
            config.setConsoleLogLevel(level);
            return this;
        }

        /**
         * Sets the file logging level.
         * 
         * @param level the file logging level
         * @return this builder instance
         */
        public Builder withFileLevel(Level level) {
            config.setFileLogLevel(level);
            return this;
        }

        /**
         * Enables or disables console logging.
         * 
         * @param enabled true to enable console logging
         * @return this builder instance
         */
        public Builder withConsoleLogging(boolean enabled) {
            config.setUseConsoleLogging(enabled);
            return this;
        }

        /**
         * Enables or disables file logging.
         * 
         * @param enabled true to enable file logging
         * @return this builder instance
         */
        public Builder withFileLogging(boolean enabled) {
            config.setUseFileLogging(enabled);
            return this;
        }

        /**
         * Sets the date-time format for log timestamps.
         * 
         * @param format the date format pattern
         * @return this builder instance
         */
        public Builder withDateFormat(String format) {
            config.setDateTimeFormat(format);
            return this;
        }

        /**
         * Sets the maximum number of log files to keep.
         * 
         * @param maxFiles the maximum number of log files
         * @return this builder instance
         */
        public Builder withMaxLogFiles(int maxFiles) {
            config.setMaxLogFiles(maxFiles);
            return this;
        }

        /**
         * Sets sensible defaults for game development.
         * 
         * @return this builder instance
         */
        public Builder withGameDefaults() {
            // Use a game-specific log file prefix
            config.setLogFilePrefix("GameLog");

            // Store logs in the root/logs directory
            config.setLogDirectory("logs");

            // Use INFO level for console and file logging
            config.setConsoleLogLevel(Level.INFO);
            config.setFileLogLevel(Level.FINE);

            // Enable both console and file logging
            config.setUseConsoleLogging(true);
            config.setUseFileLogging(true);

            // Use a safe date format that works on all OS
            config.setDateTimeFormat("yyyy-MM-dd_HH-mm-ss");

            // Keep 10 log files by default
            config.setMaxLogFiles(10);

            return this;
        }

        /**
         * Sets development mode defaults.
         * 
         * @return this builder instance
         */
        public Builder withDevMode() {
            // Start with game defaults
            withGameDefaults();

            // Use more verbose logging in dev mode
            config.setConsoleLogLevel(Level.FINE);
            config.setFileLogLevel(Level.FINEST);

            return this;
        }

        /**
         * Sets production mode defaults.
         * 
         * @return this builder instance
         */
        public Builder withProdMode() {
            // Start with game defaults
            withGameDefaults();

            // Use less verbose logging in production
            config.setConsoleLogLevel(Level.WARNING);
            config.setFileLogLevel(Level.INFO);

            return this;
        }

        /**
         * Builds the logger configuration and initializes the logging system.
         */
        public void initialize() {
            // Ensure the logs directory exists
            File logsDir = new File(config.getLogDirectory());
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            LogManager.initialize(config);
        }
    }
}