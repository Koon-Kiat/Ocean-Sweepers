package project.game.common.logging.core;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.factory.JavaLoggerFactory;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerFactory;

/**
 * Central management class for the logging system.
 * This class acts as a facade to the logging system, providing simple access
 * points.
 */
public final class LogManager {
    private static LoggerConfig currentConfig;
    private static ILoggerFactory factory;
    private static boolean initialized = false;

    // Private constructor to prevent instantiation
    private LogManager() {
        // Utility class, no instantiation
    }

    /**
     * Initializes the logging system with default configuration.
     */
    public static synchronized void initialize() {
        if (!initialized) {
            initialize(new LoggerConfig().validate());
        }
    }

    /**
     * Initializes the logging system with the provided configuration.
     * 
     * @param config the logger configuration
     */
    public static synchronized void initialize(LoggerConfig config) {
        // Validate configuration before using it
        currentConfig = config.validate();

        // Create the factory based on the configuration
        factory = new JavaLoggerFactory(currentConfig);

        initialized = true;
    }

    /**
     * Gets a logger for the specified name.
     * 
     * @param name the logger name
     * @return the logger instance
     */
    public static ILogger getLogger(String name) {
        ensureInitialized();
        return factory.getLogger(name);
    }

    /**
     * Gets a logger for the specified class.
     * 
     * @param clazz the class
     * @return the logger instance
     */
    public static ILogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Gets the root logger.
     * 
     * @return the root logger
     */
    public static ILogger getRootLogger() {
        ensureInitialized();
        return factory.getRootLogger();
    }

    /**
     * Reconfigures the logging system with new settings.
     * 
     * @param config the new configuration
     */
    public static void reconfigure(LoggerConfig config) {
        ensureInitialized();
        currentConfig = config.validate();
        factory.reconfigure(currentConfig);
    }

    /**
     * Sets a different logger factory implementation.
     *
     * @param newFactory the factory to use
     */
    public static synchronized void setLoggerFactory(ILoggerFactory newFactory) {
        if (newFactory == null) {
            throw new IllegalArgumentException("Logger factory cannot be null");
        }

        // Shutdown the current factory
        if (factory != null) {
            factory.shutdown();
        }

        factory = newFactory;

        // Initialize the factory with current config if already initialized
        if (initialized && currentConfig != null) {
            currentConfig = currentConfig.validate();
            factory.reconfigure(currentConfig);
        }
    }

    /**
     * Gets the current configuration.
     * 
     * @return the current configuration
     */
    public static LoggerConfig getConfiguration() {
        ensureInitialized();
        return currentConfig;
    }

    /**
     * Shuts down the logging system, closing all handlers.
     * Should be called on application exit.
     */
    public static synchronized void shutdown() {
        if (initialized && factory != null) {
            factory.shutdown();
            factory = null;
            initialized = false;
        }
    }

    /**
     * Ensures that the logging system is initialized.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
