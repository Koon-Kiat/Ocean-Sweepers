package project.game.common.logging.core;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.factory.SimpleLoggerFactory;
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
    private static boolean initializing = false;
    private static final Object LOCK = new Object();

    // Private constructor to prevent instantiation
    private LogManager() {
        // Utility class, no instantiation
    }

    /**
     * Initializes the logging system with default configuration.
     */
    public static void initialize() {
        if (!initialized) {
            initialize(new LoggerConfig().validate());
        }
    }

    /**
     * Initializes the logging system with the provided configuration.
     * 
     * @param config the logger configuration
     */
    public static void initialize(LoggerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        synchronized (LOCK) {
            // If already initialized with the same config, skip
            if (initialized && currentConfig == config) {
                return;
            }

            // Prevent recursive initialization
            if (initializing) {
                return;
            }

            try {
                initializing = true;

                // Shutdown existing factory if any
                if (factory != null) {
                    factory.shutdown();
                    factory = null;
                }

                // Validate and apply new configuration
                currentConfig = config.validate();
                factory = new SimpleLoggerFactory(currentConfig);
                initialized = true;

            } finally {
                initializing = false;
            }
        }
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
        synchronized (LOCK) {
            currentConfig = config.validate();
            factory.reconfigure(currentConfig);
        }
    }

    /**
     * Sets a different logger factory implementation.
     *
     * @param newFactory the factory to use
     */
    public static void setLoggerFactory(ILoggerFactory newFactory) {
        if (newFactory == null) {
            throw new IllegalArgumentException("Logger factory cannot be null");
        }

        synchronized (LOCK) {
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
    public static void shutdown() {
        synchronized (LOCK) {
            if (initialized && factory != null) {
                factory.shutdown();
                factory = null;
                initialized = false;
                currentConfig = null;
            }
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
