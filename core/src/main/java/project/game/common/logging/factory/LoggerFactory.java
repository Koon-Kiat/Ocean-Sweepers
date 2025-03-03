package project.game.common.logging.factory;

import project.game.common.logging.config.LoggerConfig;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerFactory;

/**
 * Factory interface for creating loggers.
 * This abstraction allows for different logger implementations.
 */
public interface LoggerFactory extends ILoggerFactory {
    /**
     * Gets a logger for the specified name.
     *
     * @param name the logger name
     * @return the logger instance
     */
    @Override
    ILogger getLogger(String name);

    /**
     * Gets a logger for the specified class.
     *
     * @param clazz the class
     * @return the logger instance
     */
    @Override
    ILogger getLogger(Class<?> clazz);

    /**
     * Gets the root logger.
     *
     * @return the root logger
     */
    @Override
    ILogger getRootLogger();

    /**
     * Reconfigures the logging system with new settings.
     *
     * @param config the new configuration
     */
    @Override
    void reconfigure(LoggerConfig config);

    /**
     * Shuts down the logging system.
     */
    @Override
    void shutdown();
}