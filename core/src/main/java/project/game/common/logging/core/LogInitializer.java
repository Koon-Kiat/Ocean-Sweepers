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
        initialize(createDefaultConfig());
    }

    /**
     * Creates a default configuration with validated paths.
     */
    private static LoggerConfig createDefaultConfig() {
        return new GameLoggerConfigBuilder()
                .withGameDefaults()
                .build()
                .validate();
    }

    /**
     * Initializes the logging system with the specified configuration.
     * 
     * @param config the logger configuration to use
     */
    public static void initialize(LoggerConfig config) {
        // Validate the configuration before initializing
        config.validate();
        LogManager.initialize(config);
    }

    /**
     * Creates a development environment configuration and initializes the logging
     * system.
     */
    public static void initializeForDevelopment() {
        GameLoggerConfigBuilder builder = new GameLoggerConfigBuilder();
        builder.withDevMode();
        initialize(builder.build().validate());
    }

    /**
     * Creates a production environment configuration and initializes the logging
     * system.
     */
    public static void initializeForProduction() {
        GameLoggerConfigBuilder builder = new GameLoggerConfigBuilder();
        builder.withProdMode();
        initialize(builder.build().validate());
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