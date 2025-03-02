package project.game.common.logging.builder;

import java.io.File;
import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;

/**
 * Builder for configuring the game logging system with sensible defaults.
 * Provides preset configurations for different environments (dev, prod).
 */
public class GameLoggerConfigBuilder {
    private final LoggerConfig config = new LoggerConfig();

    public GameLoggerConfigBuilder withLogPrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return this;
    }

    public GameLoggerConfigBuilder withLogDirectory(String directory) {
        config.setLogDirectory(directory);
        return this;
    }

    public GameLoggerConfigBuilder withConsoleLevel(Level level) {
        config.setConsoleLogLevel(level);
        return this;
    }

    public GameLoggerConfigBuilder withFileLevel(Level level) {
        config.setFileLogLevel(level);
        return this;
    }

    public GameLoggerConfigBuilder withConsoleLogging(boolean enabled) {
        config.setUseConsoleLogging(enabled);
        return this;
    }

    public GameLoggerConfigBuilder withFileLogging(boolean enabled) {
        config.setUseFileLogging(enabled);
        return this;
    }

    public GameLoggerConfigBuilder withDateFormat(String format) {
        config.setDateTimeFormat(format);
        return this;
    }

    public GameLoggerConfigBuilder withMaxLogFiles(int maxFiles) {
        config.setMaxLogFiles(maxFiles);
        return this;
    }

    public GameLoggerConfigBuilder withGameDefaults() {
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

    public GameLoggerConfigBuilder withDevMode() {
        // Start with game defaults
        withGameDefaults();
        // Use more verbose logging in dev mode
        config.setConsoleLogLevel(Level.FINE);
        config.setFileLogLevel(Level.FINEST);
        return this;
    }

    public GameLoggerConfigBuilder withProdMode() {
        // Start with game defaults
        withGameDefaults();
        // Use less verbose logging in production
        config.setConsoleLogLevel(Level.WARNING);
        config.setFileLogLevel(Level.INFO);
        return this;
    }

    public void initialize() {
        // Ensure the logs directory exists
        File logsDir = new File(config.getLogDirectory());
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        LogManager.initialize(config);
    }

    public LoggerConfig build() {
        return config;
    }
}