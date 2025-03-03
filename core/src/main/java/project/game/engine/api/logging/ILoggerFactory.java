package project.game.engine.api.logging;

import project.game.common.logging.config.LoggerConfig;

/**
 * Factory interface for creating loggers.
 * Follows the Factory Method pattern for creating logger instances.
 */
public interface ILoggerFactory {
    /**
     * Gets a logger for the specified name.
     *
     * @param name the logger name
     * @return the logger instance
     */
    ILogger getLogger(String name);

    /**
     * Gets a logger for the specified class.
     *
     * @param clazz the class
     * @return the logger instance
     */
    ILogger getLogger(Class<?> clazz);

    /**
     * Gets the root logger.
     *
     * @return the root logger
     */
    ILogger getRootLogger();

    /**
     * Reconfigures the logging system with the specified configuration.
     *
     * @param config the new configuration
     */
    void reconfigure(LoggerConfig config);

    /**
     * Shuts down the logging system.
     */
    void shutdown();
}