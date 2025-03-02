package project.game.common.logging.factory;

import project.game.common.logging.api.ILogger;
import project.game.common.logging.api.ILoggerFactory;
import project.game.common.logging.config.LoggerConfig;

/**
 * Abstract base class for logger factories.
 * Implements common functionality and enforces the Template Method pattern.
 */
public abstract class AbstractLoggerFactory implements ILoggerFactory {

    protected LoggerConfig config;

    /**
     * Creates a new AbstractLoggerFactory with the specified configuration.
     * 
     * @param config the logger configuration
     */
    public AbstractLoggerFactory(LoggerConfig config) {
        this.config = config;
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    public void reconfigure(LoggerConfig config) {
        this.config = config;
        doReconfigure();
    }

    /**
     * Template method to be implemented by subclasses for reconfiguration.
     */
    protected abstract void doReconfigure();

    /**
     * Gets the current logger configuration.
     * 
     * @return the current configuration
     */
    public LoggerConfig getConfig() {
        return config;
    }
}