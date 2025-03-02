package project.game.common.logging.builder;

import java.util.logging.Level;

import project.game.common.logging.api.ILogger;
import project.game.common.logging.api.ProjectPathStrategy;
import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;
import project.game.common.logging.impl.DefaultProjectPathStrategy;
import project.game.common.logging.impl.JavaLoggerFactory;

/**
 * Builder class for configuring and creating loggers using a fluent API.
 * Follows the Builder pattern to allow step-by-step configuration.
 */
public class LoggerBuilder {
    private final LoggerConfig config;
    private ProjectPathStrategy pathStrategy;
    private String name;
    private Class<?> clazz;

    /**
     * Creates a new LoggerBuilder with default configuration.
     */
    public LoggerBuilder() {
        this.config = new LoggerConfig();
        this.pathStrategy = new DefaultProjectPathStrategy();
    }

    /**
     * Sets the logger name.
     * 
     * @param name the name for the logger
     * @return this builder instance
     */
    public LoggerBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the logger class.
     * 
     * @param clazz the class for the logger
     * @return this builder instance
     */
    public LoggerBuilder withClass(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * Sets the path strategy.
     * 
     * @param pathStrategy the strategy for determining project paths
     * @return this builder instance
     */
    public LoggerBuilder withPathStrategy(ProjectPathStrategy pathStrategy) {
        this.pathStrategy = pathStrategy;
        return this;
    }

    /**
     * Sets the log directory.
     * 
     * @param directory the log directory
     * @return this builder instance
     */
    public LoggerBuilder withLogDirectory(String directory) {
        config.setLogDirectory(directory);
        return this;
    }

    /**
     * Sets the log file prefix.
     * 
     * @param prefix the log file prefix
     * @return this builder instance
     */
    public LoggerBuilder withLogFilePrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return this;
    }

    /**
     * Sets the file logging level.
     * 
     * @param level the level for file logging
     * @return this builder instance
     */
    public LoggerBuilder withFileLevel(Level level) {
        config.setFileLogLevel(level);
        return this;
    }

    /**
     * Sets the console logging level.
     * 
     * @param level the level for console logging
     * @return this builder instance
     */
    public LoggerBuilder withConsoleLevel(Level level) {
        config.setConsoleLogLevel(level);
        return this;
    }

    /**
     * Enables or disables file logging.
     * 
     * @param enabled true to enable file logging
     * @return this builder instance
     */
    public LoggerBuilder withFileLogging(boolean enabled) {
        config.setUseFileLogging(enabled);
        return this;
    }

    /**
     * Enables or disables console logging.
     * 
     * @param enabled true to enable console logging
     * @return this builder instance
     */
    public LoggerBuilder withConsoleLogging(boolean enabled) {
        config.setUseConsoleLogging(enabled);
        return this;
    }

    /**
     * Sets the maximum number of log files to keep.
     * 
     * @param maxFiles the maximum number of log files
     * @return this builder instance
     */
    public LoggerBuilder withMaxLogFiles(int maxFiles) {
        config.setMaxLogFiles(maxFiles);
        return this;
    }

    /**
     * Sets the formatter class name.
     * 
     * @param formatterClassName the name of the formatter class
     * @return this builder instance
     */
    public LoggerBuilder withFormatter(String formatterClassName) {
        config.setLoggerFormatter(formatterClassName);
        return this;
    }

    /**
     * Sets the date format pattern.
     * 
     * @param pattern the date format pattern
     * @return this builder instance
     */
    public LoggerBuilder withDateFormat(String pattern) {
        config.setDateTimeFormat(pattern);
        return this;
    }

    /**
     * Builds and initializes the logging system with the configured settings.
     * 
     * @return the configured LogManager
     */
    public LogManager initialize() {
        LogManager.initialize(config);
        return null; // Return null since LogManager is a static utility class
    }

    /**
     * Builds and returns a logger with the configured settings.
     * 
     * @return the configured logger
     */
    public ILogger build() {
        // If name and class are both null or both set, prioritize the class
        if (clazz != null) {
            return getLogger(clazz);
        } else if (name != null) {
            return getLogger(name);
        } else {
            // No name or class specified, return the root logger
            return getRootLogger();
        }
    }

    private ILogger getLogger(String name) {
        ensureInitialized();
        return LogManager.getLogger(name);
    }

    private ILogger getLogger(Class<?> clazz) {
        ensureInitialized();
        return LogManager.getLogger(clazz);
    }

    private ILogger getRootLogger() {
        ensureInitialized();
        return LogManager.getRootLogger();
    }

    private void ensureInitialized() {
        // Use a custom factory if path strategy was specified
        if (pathStrategy != null && !(pathStrategy instanceof DefaultProjectPathStrategy)) {
            LogManager.setFactory(new JavaLoggerFactory(config, pathStrategy));
        } else {
            LogManager.initialize(config);
        }
    }
}