package project.game.engine.logging;

import project.game.common.logging.config.LoggerConfig;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerFactory;

/**
 * Abstract base class for logger factories.
 * Provides common functionality and template methods for specific factory
 * implementations.
 */
public abstract class AbstractLoggerFactory implements ILoggerFactory {
    protected LoggerConfig config;

    /**
     * Creates a new AbstractLoggerFactory with the specified configuration.
     *
     * @param config the logger configuration
     */
    protected AbstractLoggerFactory(LoggerConfig config) {
        this.config = config;
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    public void reconfigure(LoggerConfig config) {
        if (config != null) {
            this.config = config;
            doReconfigure();
        }
    }

    /**
     * Template method for reconfiguring the logging system.
     * To be implemented by concrete factories.
     */
    protected abstract void doReconfigure();
}