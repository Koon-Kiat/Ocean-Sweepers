package project.game.common.logging.util;

import project.game.common.logging.builder.GameLoggerConfigBuilder;
import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogManager;

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
        initialize(createDefaultConfig());
    }

    /**
     * Creates a default configuration with validated paths.
     */
    private static LoggerConfig createDefaultConfig() {
        return new GameLoggerConfigBuilder()
                .withGameDefaults()
                .build();
    }

    /**
     * Initializes the logging system with the specified configuration.
     */
    public static void initialize(LoggerConfig config) {
        LogManager.initialize(config);
    }

    /**
     * Creates a development environment configuration and initializes the logging
     * system.
     */
    public static void initializeForDevelopment() {
        GameLoggerConfigBuilder builder = new GameLoggerConfigBuilder();
        builder.forDevelopment();
        initialize(builder.build());
    }

    /**
     * Creates a production environment configuration and initializes the logging
     * system.
     */
    public static void initializeForProduction() {
        GameLoggerConfigBuilder builder = new GameLoggerConfigBuilder();
        builder.forProduction();
        initialize(builder.build());
    }

    /**
     * Creates a builder for configuring the game logging system.
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