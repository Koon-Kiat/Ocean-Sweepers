package project.game.context.constant;

import project.game.common.logging.core.GameLogger;
import project.game.context.api.constant.IGameConstants;
import project.game.engine.constant.AbstractConfigurableConstants;

/**
 * Game-specific implementation of configurable constants.
 */
public class ConfigurableGameConstants extends AbstractConfigurableConstants implements IGameConstants {
    // Implementing missing methods from IEntityConstants
    
    private static final GameLogger LOGGER = new GameLogger(ConfigurableGameConstants.class);
    private static ConfigurableGameConstants instance;

    private ConfigurableGameConstants() {
        // Initialize with game-specific registry
        super(ConstantsRegistry.getInstance());
    }

    @Override
    protected void initializeConfiguration(String configSource) {
        try {
            GameConfigurationLoader.getInstance().loadConfiguration(configSource, "default", this);
        } catch (Exception e) {
            LOGGER.fatal("Failed to initialize configuration from: " + configSource, e);
            throw new RuntimeException("Failed to initialize configuration", e);
        }
    }

    public static synchronized ConfigurableGameConstants init(String configFile) {
        if (instance == null) {
            instance = new ConfigurableGameConstants();
            instance.initializeConfiguration(configFile);
        }
        return instance;
    }

    public static ConfigurableGameConstants getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigurableGameConstants not initialized. Call init() first.");
        }
        return instance;
    }

    // Movement Constants
    @Override
    public float PLAYER_SPEED() {
        return getFloatValue("PLAYER_SPEED");
    }

    @Override
    public float NPC_SPEED() {
        return getFloatValue("NPC_SPEED");
    }

    @Override
    public float DEFAULT_SPEED() {
        return getFloatValue("DEFAULT_SPEED");
    }

    @Override
    public float AMPLITUDE() {
        return getFloatValue("AMPLITUDE");
    }

    @Override
    public float FREQUENCY() {
        return getFloatValue("FREQUENCY");
    }

    @Override
    public float MIN_DURATION() {
        return getFloatValue("MIN_DURATION");
    }

    @Override
    public float MAX_DURATION() {
        return getFloatValue("MAX_DURATION");
    }

    // Physics Constants
    @Override
    public float PIXELS_TO_METERS() {
        return getFloatValue("PIXELS_TO_METERS");
    }

    @Override
    public long COLLISION_ACTIVE_DURATION() {
        return getLongValue("COLLISION_ACTIVE_DURATION");
    }

    // Screen Constants
    @Override
    public float GAME_WIDTH() {
        return getFloatValue("GAME_WIDTH");
    }

    @Override
    public float GAME_HEIGHT() {
        return getFloatValue("GAME_HEIGHT");
    }

    // Entity Constants
    @Override
    public float BUCKET_START_X() {
        return getFloatValue("BUCKET_START_X");
    }

    @Override
    public float BUCKET_START_Y() {
        return getFloatValue("BUCKET_START_Y");
    }

    @Override
    public float BUCKET_WIDTH() {
        return getFloatValue("BUCKET_WIDTH");
    }

    @Override
    public float BUCKET_HEIGHT() {
        return getFloatValue("BUCKET_HEIGHT");
    }

    @Override
    public float DROP_START_X() {
        return getFloatValue("DROP_START_X");
    }

    @Override
    public float DROP_START_Y() {
        return getFloatValue("DROP_START_Y");
    }

    @Override
    public float DROP_WIDTH() {
        return getFloatValue("DROP_WIDTH");
    }

    @Override
    public float DROP_HEIGHT() {
        return getFloatValue("DROP_HEIGHT");
    }

    @Override
    public float MONSTER_HEIGHT() {
        return getFloatValue("MONSTER_HEIGHT");
    }

    @Override
    public float MONSTER_WIDTH() {
        return getFloatValue("MONSTER_WIDTH");
    }

    @Override
    public float TRASH_HEIGHT() {
        return getFloatValue("TRASH_HEIGHT");
    }

    @Override
    public float TRASH_WIDTH() {
        return getFloatValue("TRASH_WIDTH");
    }

    @Override
    public int NUM_TRASHES() {
        return getIntValue("NUM_TRASHES");
    }

    @Override
    public float ROCK_HEIGHT() {
        return getFloatValue("ROCK_HEIGHT");
    }

    @Override
    public float ROCK_WIDTH() {
        return getFloatValue("ROCK_WIDTH");
    }

    @Override
    public int NUM_ROCKS() {
        return getIntValue("NUM_ROCKS");
    }

    // Collision Constants
    @Override
    public float MONSTER_BASE_IMPULSE() {
        return getFloatValue("MONSTER_BASE_IMPULSE");
    }

    @Override
    public float ROCK_BASE_IMPULSE() {
        return getFloatValue("ROCK_BASE_IMPULSE");
    }

    @Override
    public float BOAT_BOUNCE_FORCE() {
        return getFloatValue("BOAT_BOUNCE_FORCE");
    }
}