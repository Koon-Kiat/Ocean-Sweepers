package project.game.context.core;

import java.util.HashMap;
import java.util.Map;

import project.game.common.logging.GameLogger;

/**
 * A configurable implementation of game constants that uses the Strategy
 * pattern.
 * Uses the ConstantsRegistry for dynamic constant management.
 */
public class ConfigurableGameConstants implements IGameConstants {

    private static final GameLogger LOGGER = new GameLogger(ConfigurableGameConstants.class);
    private static ConfigurableGameConstants INSTANCE;

    private String currentProfile = "default";
    private final Map<String, Map<String, Object>> profiles = new HashMap<>();

    private ConfigurableGameConstants() {
        // Private constructor - initialization must happen through init()
    }

    public static synchronized ConfigurableGameConstants init(String configFile) {
        if (INSTANCE == null) {
            INSTANCE = new ConfigurableGameConstants();
            boolean loaded = GameConfigurationLoader.loadConfiguration(configFile, "default");
            if (!loaded) {
                LOGGER.fatal("Failed to load default configuration from: {0}", configFile);
                throw new IllegalStateException("Failed to load default configuration");
            }
        }
        return INSTANCE;
    }

    public static ConfigurableGameConstants getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ConfigurableGameConstants not initialized. Call init() first.");
        }
        return INSTANCE;
    }

    public void createProfile(String profileName) {
        if (!profiles.containsKey(profileName)) {
            profiles.put(profileName, new HashMap<>());

            // Initialize with default values from registry
            Map<String, Object> profileValues = profiles.get(profileName);
            for (String key : ConstantsRegistry.getAllKeys()) {
                ConstantDefinition def = ConstantsRegistry.get(key);
                profileValues.put(key, def.getDefaultValue());
            }
        }
    }

    public boolean setProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profileName;
            return true;
        }
        return false;
    }

    public void setValue(String key, Object value) {
        ConstantDefinition def = ConstantsRegistry.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown constant key: " + key);
        }
        if (!def.getType().isInstance(value)) {
            throw new IllegalArgumentException("Invalid type for constant " + key +
                    ". Expected " + def.getType().getSimpleName() +
                    " but got " + value.getClass().getSimpleName());
        }
        profiles.get(currentProfile).put(key, value);
    }

    private Object getValue(String key) {
        Map<String, Object> currentValues = profiles.get(currentProfile);
        if (currentValues.containsKey(key)) {
            return currentValues.get(key);
        } else if (!currentProfile.equals("default") && profiles.get("default").containsKey(key)) {
            return profiles.get("default").get(key);
        }
        ConstantDefinition def = ConstantsRegistry.get(key);
        if (def != null) {
            return def.getDefaultValue();
        }
        throw new IllegalArgumentException("Constant key not found: " + key);
    }

    /**
     * Check if a value exists for a given key in a profile
     */
    public boolean hasValue(String profileName, String key) {
        Map<String, Object> profileValues = profiles.get(profileName);
        return profileValues != null && profileValues.containsKey(key);
    }

    /**
     * Get the raw value for a key from the current profile
     */
    public Object getRawValue(String key) {
        return getValue(key);
    }

    // Helper methods for type conversion
    private float getFloatValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    private long getLongValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    // IMovementConstants implementation
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

    // IPhysicsConstants implementation
    @Override
    public float PIXELS_TO_METERS() {
        return getFloatValue("PIXELS_TO_METERS");
    }

    @Override
    public float IMPULSE_STRENGTH() {
        return getFloatValue("IMPULSE_STRENGTH");
    }

    @Override
    public long COLLISION_ACTIVE_DURATION() {
        return getLongValue("COLLISION_ACTIVE_DURATION");
    }

    // IEntityConstants implementation
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

    // IScreenConstants implementation
    @Override
    public float GAME_WIDTH() {
        return getFloatValue("GAME_WIDTH");
    }

    @Override
    public float GAME_HEIGHT() {
        return getFloatValue("GAME_HEIGHT");
    }
}