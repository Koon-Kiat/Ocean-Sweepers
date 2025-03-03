package project.game.common.logging.builder;

import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;
import project.game.engine.api.logging.ILogger;
import project.game.engine.logging.AbstractConfigBuilder;

/**
 * Builder for configuring and creating generic loggers using a fluent API.
 */
public class LoggerBuilder extends AbstractConfigBuilder<LoggerBuilder, LoggerConfig> {
    private String name;
    private Class<?> clazz;
    private boolean isInitialized = false;

    public LoggerBuilder() {
        super(new LoggerConfig());
    }

    @Override
    public LoggerBuilder withLogDirectory(String directory) {
        config.setLogDirectory(directory);
        return self();
    }

    @Override
    public LoggerBuilder withLogPrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return self();
    }

    @Override
    public LoggerBuilder withDateFormat(String pattern) {
        config.setDateTimeFormat(pattern);
        return self();
    }

    @Override
    public LoggerBuilder withMaxLogFiles(int maxFiles) {
        config.setMaxLogFiles(maxFiles);
        return self();
    }

    @Override
    public LoggerBuilder withConsoleLevel(Level level) {
        config.setConsoleLogLevel(level);
        return self();
    }

    @Override
    public LoggerBuilder withFileLevel(Level level) {
        config.setFileLogLevel(level);
        return self();
    }

    @Override
    public LoggerBuilder withConsoleLogging(boolean enabled) {
        config.setUseConsoleLogging(enabled);
        return self();
    }

    @Override
    public LoggerBuilder withFileLogging(boolean enabled) {
        config.setUseFileLogging(enabled);
        return self();
    }

    /**
     * Sets the logger name.
     */
    public LoggerBuilder withName(String name) {
        this.name = name;
        return self();
    }

    /**
     * Sets the logger class.
     */
    public LoggerBuilder withClass(Class<?> clazz) {
        this.clazz = clazz;
        return self();
    }

    @Override
    public LoggerConfig build() {
        return config;
    }

    @Override
    public void initialize() {
        if (!isInitialized) {
            LogManager.initialize(config);
            isInitialized = true;
        }
    }

    /**
     * Builds and returns a logger with the configured settings.
     */
    public ILogger buildLogger() {
        if (clazz != null) {
            return getLogger(clazz);
        } else if (name != null) {
            return getLogger(name);
        }
        return getRootLogger();
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
        if (!isInitialized) {
            initialize();
        }
    }
}