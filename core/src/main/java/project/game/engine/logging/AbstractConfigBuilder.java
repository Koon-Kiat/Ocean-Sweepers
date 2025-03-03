package project.game.engine.logging;

import java.util.logging.Level;

/**
 * Abstract base builder class for logging configuration.
 * Provides common configuration methods with a fluent API.
 * 
 * @param <T> The concrete builder type (for method chaining)
 * @param <C> The configuration type being built
 */
public abstract class AbstractConfigBuilder<T extends AbstractConfigBuilder<T, C>, C> {

    protected final C config;

    protected AbstractConfigBuilder(C config) {
        this.config = config;
    }

    /**
     * Gets the concrete builder instance for method chaining.
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Sets the log directory path.
     */
    public abstract T withLogDirectory(String directory);

    /**
     * Sets the log file prefix.
     */
    public abstract T withLogPrefix(String prefix);

    /**
     * Sets the date/time format pattern.
     */
    public abstract T withDateFormat(String pattern);

    /**
     * Sets the maximum number of log files to keep.
     */
    public abstract T withMaxLogFiles(int maxFiles);

    /**
     * Sets the console logging level.
     */
    public abstract T withConsoleLevel(Level level);

    /**
     * Sets the file logging level.
     */
    public abstract T withFileLevel(Level level);

    /**
     * Enables/disables console logging.
     */
    public abstract T withConsoleLogging(boolean enabled);

    /**
     * Enables/disables file logging.
     */
    public abstract T withFileLogging(boolean enabled);

    /**
     * Builds and returns the final configuration.
     */
    public abstract C build();

    /**
     * Optional initialization step after building.
     * Implementations can override this to perform additional setup.
     */
    public abstract void initialize();

    /**
     * Gets the current configuration instance.
     */
    public C getConfig() {
        return config;
    }
}