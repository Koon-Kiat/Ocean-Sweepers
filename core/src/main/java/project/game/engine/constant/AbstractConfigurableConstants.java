package project.game.engine.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base class for configurable constants that uses the Strategy
 * pattern.
 * Projects can extend this class to implement their specific constants.
 */
public abstract class AbstractConfigurableConstants {
    private String currentProfile = "default";
    private final Map<String, Map<String, Object>> profiles = new HashMap<>();

    protected AbstractConfigurableConstants() {
        // Protected constructor - initialization must happen through init()
    }

    public void createProfile(String profileName) {
        if (!profiles.containsKey(profileName)) {
            profiles.put(profileName, new HashMap<>());

            // Initialize with default values from registry
            Map<String, Object> profileValues = profiles.get(profileName);
            for (String key : AbstractConstantsRegistry.getAllKeys()) {
                ConstantDefinition def = AbstractConstantsRegistry.get(key);
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
        ConstantDefinition def = AbstractConstantsRegistry.get(key);
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

    protected Object getValue(String key) {
        Map<String, Object> currentValues = profiles.get(currentProfile);
        if (currentValues.containsKey(key)) {
            return currentValues.get(key);
        } else if (!currentProfile.equals("default") && profiles.get("default").containsKey(key)) {
            return profiles.get("default").get(key);
        }
        ConstantDefinition def = AbstractConstantsRegistry.get(key);
        if (def != null) {
            return def.getDefaultValue();
        }
        throw new IllegalArgumentException("Constant key not found: " + key);
    }

    public boolean hasValue(String profileName, String key) {
        Map<String, Object> profileValues = profiles.get(profileName);
        return profileValues != null && profileValues.containsKey(key);
    }

    public Object getRawValue(String key) {
        return getValue(key);
    }

    // Helper methods for type conversion
    protected float getFloatValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    protected long getLongValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    protected int getIntValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    protected boolean getBooleanValue(String key) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a boolean");
    }

    protected String getStringValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a string");
    }
}