package project.game.engine.logging;

import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerConfig;
import project.game.engine.api.logging.ILoggerFactory;

/**
 * Abstract base class for logger factories.
 * Provides common functionality and template methods for specific factory
 * implementations.
 */
public abstract class AbstractLoggerFactory implements ILoggerFactory {
    protected ILoggerConfig config;

    /**
     * Creates a new AbstractLoggerFactory with the specified configuration.
     *
     * @param config the logger configuration
     */
    protected AbstractLoggerFactory(ILoggerConfig config) {
        this.config = config;
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    public void reconfigure(ILoggerConfig config) {
        if (config != null) {
            this.config = config;
            doReconfigure();
        }
    }

    protected abstract void doReconfigure();
}