package project.game.common.logging.core;

import project.game.common.logging.builder.GameLoggerConfigBuilder;
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
     * Creates a builder for configuring the game logging system.
     * 
     * @return a new game logger config builder
     */
    public static GameLoggerConfigBuilder builder() {
        return new GameLoggerConfigBuilder();
    }

    /**
     * Shuts down the logging system.
     */
    public static void shutdown() {
        LogManager.shutdown();
    }
}