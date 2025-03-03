package project.game.common.logging.builder;

import java.io.File;
import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;
import project.game.common.logging.util.LogPaths;
import project.game.engine.logging.AbstractConfigBuilder;

/**
 * Builder for game-specific logging configuration that extends the base
 * builder.
 */
public class GameLoggerConfigBuilder extends AbstractConfigBuilder<GameLoggerConfigBuilder, LoggerConfig> {

    public GameLoggerConfigBuilder() {
        super(new LoggerConfig());
    }

    @Override
    public GameLoggerConfigBuilder withLogDirectory(String directory) {
        // Always use the global log directory from LogPaths
        String globalLogDir = LogPaths.getGlobalLogDirectory();
        config.setLogDirectory(globalLogDir);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withLogPrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withDateFormat(String pattern) {
        config.setDateTimeFormat(pattern);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withMaxLogFiles(int maxFiles) {
        config.setMaxLogFiles(maxFiles);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withConsoleLevel(Level level) {
        config.setConsoleLogLevel(level);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withFileLevel(Level level) {
        config.setFileLogLevel(level);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withConsoleLogging(boolean enabled) {
        config.setUseConsoleLogging(enabled);
        return self();
    }

    @Override
    public GameLoggerConfigBuilder withFileLogging(boolean enabled) {
        config.setUseFileLogging(enabled);
        return self();
    }

    /**
     * Applies development environment configuration.
     */
    public GameLoggerConfigBuilder forDevelopment() {
        withGameDefaults();
        withConsoleLevel(Level.FINE);
        withFileLevel(Level.FINEST);
        return self();
    }

    /**
     * Applies production environment configuration.
     */
    public GameLoggerConfigBuilder forProduction() {
        withGameDefaults();
        withConsoleLevel(Level.WARNING);
        withFileLevel(Level.INFO);
        return self();
    }

    /**
     * Applies default game configuration settings.
     */
    public GameLoggerConfigBuilder withGameDefaults() {
        withLogPrefix("GameLog")
                .withLogDirectory(LogPaths.getGlobalLogDirectory())
                .withConsoleLevel(Level.INFO)
                .withFileLevel(Level.FINE)
                .withConsoleLogging(true)
                .withFileLogging(true)
                .withDateFormat("yyyy-MM-dd_HH-mm-ss")
                .withMaxLogFiles(5);
        return self();
    }

    @Override
    public LoggerConfig build() {
        // Only set the directory path, don't create it yet
        String globalLogDir = LogPaths.getGlobalLogDirectory();
        config.setLogDirectory(globalLogDir);
        return config;
    }

    @Override
    public void initialize() {
        // Create log directory only when actually initializing
        ensureLogDirectoryExists();
        LogManager.initialize(config);
        // Don't clean up logs here since it might interfere with active logging
    }

    private void ensureLogDirectoryExists() {
        File logsDir = new File(LogPaths.getGlobalLogDirectory());
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }
}