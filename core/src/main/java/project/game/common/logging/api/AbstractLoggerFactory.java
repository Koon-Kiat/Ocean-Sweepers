package project.game.common.logging.api;

import project.game.common.logging.config.LoggerConfig;

/**
 * Abstract base class for logger factories.
 * Implements common functionality and enforces the Template Method pattern.
 */
public abstract class AbstractLoggerFactory implements LoggerFactory {

    protected LoggerConfig config;
    protected ProjectPathStrategy pathStrategy;

    /**
     * Creates a new AbstractLoggerFactory with the specified configuration.
     * 
     * @param config the logger configuration
     */
    public AbstractLoggerFactory(LoggerConfig config) {
        this.config = config;
    }

    /**
     * Creates a new AbstractLoggerFactory with the specified configuration and path
     * strategy.
     * 
     * @param config       the logger configuration
     * @param pathStrategy the path strategy
     */
    public AbstractLoggerFactory(LoggerConfig config, ProjectPathStrategy pathStrategy) {
        this.config = config;
        this.pathStrategy = pathStrategy;
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

    /**
     * Sets the path strategy for this factory.
     * 
     * @param pathStrategy the path strategy to use
     */
    public void setPathStrategy(ProjectPathStrategy pathStrategy) {
        this.pathStrategy = pathStrategy;
    }

    /**
     * Gets the current path strategy.
     * 
     * @return the current path strategy
     */
    public ProjectPathStrategy getPathStrategy() {
        return pathStrategy;
    }
}