package project.game.context.factory;

import project.game.context.core.ConfigurableGameConstants;
import project.game.context.core.IGameConstants;

/**
 * Factory class for creating game constants implementations.
 * Provides access to the configurable constants system.
 */
public class GameConstantsFactory {
    private static IGameConstants instance;

    /**
     * Initialize the constants system with a configuration file.
     * Must be called before getConstants().
     * 
     * @param configFile Path to the configuration file to load
     * @return The initialized constants instance
     */
    public static synchronized IGameConstants initialize(String configFile) {
        instance = ConfigurableGameConstants.init(configFile);
        return instance;
    }

    /**
     * Get the current constants instance.
     * 
     * @return The current constants instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static IGameConstants getConstants() {
        if (instance == null) {
            throw new IllegalStateException("GameConstantsFactory not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Reset the factory to create a new instance next time.
     * Mainly used for testing or when switching configurations.
     */
    public static void reset() {
        instance = null;
    }
}