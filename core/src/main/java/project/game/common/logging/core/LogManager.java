package project.game.common.logging.core;

import project.game.common.logging.api.ILogger;
import project.game.common.logging.api.ILoggerFactory;
import project.game.common.logging.builder.LoggerBuilder;
import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.factory.JavaLoggerFactory;

/**
 * Central management class for the logging system.
 * This class acts as a facade to the logging system, providing simple access
 * points.
 */
public final class LogManager {
    private static ILoggerFactory factory;

    // Private constructor to prevent instantiation
    private LogManager() {
        // Utility class, no instantiation
    }

    /**
     * Initializes the logging system with default configuration.
     */
    public static synchronized void initialize() {
        if (factory == null) {
            factory = new JavaLoggerFactory(new LoggerConfig());
        }
    }

    /**
     * Initializes the logging system with the provided configuration.
     * 
     * @param config the logger configuration
     */
    public static synchronized void initialize(LoggerConfig config) {
        if (factory == null) {
            factory = new JavaLoggerFactory(config);
        } else {
            factory.reconfigure(config);
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
        factory.reconfigure(config);
    }

    /**
     * Sets a custom logger factory implementation.
     * 
     * @param customFactory the custom factory to use
     */
    public static synchronized void setFactory(ILoggerFactory customFactory) {
        factory = customFactory;
    }

    /**
     * Creates a new logger builder for fluent configuration.
     * 
     * @return a new logger builder instance
     */
    public static LoggerBuilder builder() {
        return new LoggerBuilder();
    }

    /**
     * Shuts down the logging system, closing all handlers.
     * Should be called on application exit.
     */
    public static void shutdown() {
        if (factory != null) {
            factory.shutdown();
            factory = null;
        }
    }

    private static void ensureInitialized() {
        if (factory == null) {
            initialize();
        }
    }
}
