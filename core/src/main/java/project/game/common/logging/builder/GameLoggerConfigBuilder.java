package project.game.common.logging.builder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;
import project.game.common.logging.util.LogPaths;

/**
 * Builder for configuring the game logging system with sensible defaults.
 * Provides preset configurations for different environments (dev, prod).
 */
public class GameLoggerConfigBuilder {
    private final LoggerConfig config = new LoggerConfig();

    /**
     * Creates a new GameLoggerConfigBuilder with default settings.
     */
    public GameLoggerConfigBuilder() {
        // Clean up any logs in invalid locations immediately
        LogPaths.cleanupInvalidLogs();
    }

    public GameLoggerConfigBuilder withLogPrefix(String prefix) {
        config.setLogFilePrefix(prefix);
        return this;
    }

    public GameLoggerConfigBuilder withLogDirectory(String directory) {
        // OVERRIDE: Always use our global log directory, ignoring the provided path
        // This ensures logs are always in the same location regardless of where the app
        // is launched
        String globalLogDir = LogPaths.getGlobalLogDirectory();
        config.setLogDirectory(globalLogDir);
        System.out.println("[INFO] Setting log directory to global path: " + globalLogDir);
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

        // ALWAYS use the global log directory
        config.setLogDirectory(LogPaths.getGlobalLogDirectory());

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
        // Force the use of the global log directory
        config.setLogDirectory(LogPaths.getGlobalLogDirectory());

        // Ensure log directory exists
        try {
            File logsDir = new File(config.getLogDirectory());
            if (!logsDir.exists() && !logsDir.mkdirs()) {
                throw new IOException("Failed to create log directory: " + logsDir.getAbsolutePath());
            }
            System.out.println("[INFO] Using log directory: " + logsDir.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Warning: " + e.getMessage());
        }

        LogManager.initialize(config);
    }

    public LoggerConfig build() {
        // Force the use of the global log directory
        config.setLogDirectory(LogPaths.getGlobalLogDirectory());
        return config;
    }
}