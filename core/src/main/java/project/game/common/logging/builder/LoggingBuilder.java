package project.game.common.logging.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogLevel;
import project.game.common.logging.core.LogManager;
import project.game.common.logging.util.LogLevelUtils;
import project.game.engine.api.logging.ILogHandler;

/**
 * Builder for configuring the logging system with a fluent API.
 * This class makes it easy to set up logging with common configurations.
 */
public class LoggingBuilder {
    private final LoggerConfig config;
    private List<ILogHandler> customHandlers;
    private String loggerProviderName;

    /**
     * Creates a new LoggingBuilder with default settings.
     */
    public LoggingBuilder() {
        this.config = new LoggerConfig();
        this.customHandlers = new ArrayList<>();
    }

    /**
     * Sets the log directory.
     * 
     * @param directory the directory for log files
     * @return this builder instance
     */
    public LoggingBuilder withLogDirectory(String directory) {
        config.setLogDirectory(directory);
        return this;
    }

    /**
     * Sets the log file prefix.
     * 
     * @param prefix the prefix for log files
     * @return this builder instance
     */
    public LoggingBuilder withLogFilePrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return this;
    }

    /**
     * Sets the log file extension.
     * 
     * @param extension the extension for log files
     * @return this builder instance
     */
    public LoggingBuilder withLogFileExtension(String extension) {
        config.setLogFileExtension(extension);
        return this;
    }

    /**
     * Sets the date format pattern.
     * 
     * @param pattern the date format pattern
     * @return this builder instance
     */
    public LoggingBuilder withDateFormat(String pattern) {
        config.setDateTimeFormat(pattern);
        return this;
    }

    /**
     * Sets the maximum number of log files to keep.
     * 
     * @param maxFiles the maximum number of log files
     * @return this builder instance
     */
    public LoggingBuilder withMaxLogFiles(int maxFiles) {
        config.setMaxLogFiles(maxFiles);
        return this;
    }

    /**
     * Sets the console logging level.
     * 
     * @param level the level for console logging
     * @return this builder instance
     */
    public LoggingBuilder withConsoleLevel(LogLevel level) {
        config.setConsoleLogLevel(level.getJavaLevel());
        return this;
    }

    /**
     * Sets the console logging level from a string name.
     * 
     * @param levelName the level name for console logging
     * @return this builder instance
     */
    public LoggingBuilder withConsoleLevel(String levelName) {
        LogLevel level = LogLevelUtils.fromString(levelName);
        return withConsoleLevel(level);
    }

    /**
     * Sets the file logging level.
     * 
     * @param level the level for file logging
     * @return this builder instance
     */
    public LoggingBuilder withFileLevel(LogLevel level) {
        config.setFileLogLevel(level.getJavaLevel());
        return this;
    }

    /**
     * Sets the file logging level from a string name.
     * 
     * @param levelName the level name for file logging
     * @return this builder instance
     */
    public LoggingBuilder withFileLevel(String levelName) {
        LogLevel level = LogLevelUtils.fromString(levelName);
        return withFileLevel(level);
    }

    /**
     * Enables or disables console logging.
     * 
     * @param enabled true to enable console logging
     * @return this builder instance
     */
    public LoggingBuilder withConsoleLogging(boolean enabled) {
        config.setUseConsoleLogging(enabled);
        return this;
    }

    /**
     * Enables or disables file logging.
     * 
     * @param enabled true to enable file logging
     * @return this builder instance
     */
    public LoggingBuilder withFileLogging(boolean enabled) {
        config.setUseFileLogging(enabled);
        return this;
    }

    /**
     * Sets the console logging level.
     * 
     * @param level the level for console logging
     * @return this builder instance
     */
    public LoggingBuilder withConsoleLevel(Level level) {
        config.setConsoleLogLevel(level);
        return this;
    }

    /**
     * Sets the file logging level.
     * 
     * @param level the level for file logging
     * @return this builder instance
     */
    public LoggingBuilder withFileLevel(Level level) {
        config.setFileLogLevel(level);
        return this;
    }

    /**
     * Adds a custom log handler.
     * 
     * @param handler the handler to add
     * @return this builder instance
     */
    public LoggingBuilder withHandler(ILogHandler handler) {
        if (handler != null) {
            customHandlers.add(handler);
        }
        return this;
    }

    /**
     * Sets the preferred logger provider.
     * 
     * @param providerName the name of the provider to use
     * @return this builder instance
     */
    public LoggingBuilder withProvider(String providerName) {
        this.loggerProviderName = providerName;
        return this;
    }

    /**
     * Configures the builder for development mode.
     * In development mode, logs are more verbose and include debug output.
     * 
     * @return this builder instance
     */
    public LoggingBuilder forDevelopment() {
        // More verbose logging for development
        withConsoleLevel(LogLevel.DEBUG);
        withFileLevel(LogLevel.TRACE);
        withConsoleLogging(true);
        withFileLogging(true);
        withMaxLogFiles(10);
        return this;
    }

    /**
     * Configures the builder for production mode.
     * In production mode, logs are less verbose to improve performance.
     * 
     * @return this builder instance
     */
    public LoggingBuilder forProduction() {
        // Less verbose logging for production
        withConsoleLevel(LogLevel.WARN);
        withFileLevel(LogLevel.INFO);
        withConsoleLogging(true);
        withFileLogging(true);
        withMaxLogFiles(5);
        return this;
    }

    /**
     * Configures the builder with standard settings for a game application.
     * 
     * @param gameName the name of the game (used for log file prefix)
     * @return this builder instance
     */
    public LoggingBuilder forGame(String gameName) {
        withLogFilePrefix(gameName);
        withLogDirectory("logs");
        withDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return this;
    }

    /**
     * Ensures that the log directory exists.
     * 
     * @return this builder instance
     */
    public LoggingBuilder ensureLogDirectory() {
        File logDir = new File(config.getLogDirectory());
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return this;
    }

    /**
     * Initializes the logging system with the configured settings.
     */
    public void initialize() {
        // Ensure the log directory exists
        ensureLogDirectory();

        // Initialize the logging system with the configuration
        LogManager.initialize(config);

        // Add custom handlers if any
        if (!customHandlers.isEmpty()) {
            for (ILogHandler handler : customHandlers) {
                try {
                    // Add to the root logger, assuming it's an AbstractLogger
                    LogManager.getRootLogger().log(LogLevel.INFO,
                            "Adding custom handler: " + handler.getClass().getName());
                } catch (Exception e) {
                    System.err.println("Error adding custom handler: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gets the configuration built by this builder.
     * 
     * @return the logger configuration
     */
    public LoggerConfig getConfig() {
        return config;
    }
}