package project.game.context.constant;

import java.util.HashMap;
import java.util.Map;

import project.game.engine.api.constant.IConstantsRegistry;
import project.game.engine.api.constant.IProfileManager;
import project.game.engine.constant.ConstantDefinition;

/**
 * Default implementation of profile management
 */
public class DefaultProfileManager implements IProfileManager {
    private final IConstantsRegistry registry;
    private String currentProfile = "default";
    private final Map<String, Map<String, Object>> profiles = new HashMap<>();

    public DefaultProfileManager(IConstantsRegistry registry) {
        this.registry = registry;
        initializeDefaultProfile();
    }

    private void initializeDefaultProfile() {
        createProfile("default");
    }

    @Override
    public final void createProfile(String profileName) {
        if (!profiles.containsKey(profileName)) {
            profiles.put(profileName, new HashMap<>());
            Map<String, Object> profileValues = profiles.get(profileName);
            for (String key : registry.getAllKeys()) {
                ConstantDefinition def = registry.get(key);
                profileValues.put(key, def.getDefaultValue());
            }
        }
    }

    @Override
    public boolean setProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profileName;
            return true;
        }
        return false;
    }

    @Override
    public void setValue(String key, Object value) {
        ConstantDefinition def = registry.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown constant key: " + key);
        }
        if (!isValueTypeValid(value, def.getType())) {
            throw new IllegalArgumentException("Invalid type for constant " + key +
                    ". Expected " + def.getType().getSimpleName() +
                    " but got " + value.getClass().getSimpleName());
        }
        profiles.get(currentProfile).put(key, value);
    }

    @Override
    public Object getValue(String key) {
        Map<String, Object> currentValues = profiles.get(currentProfile);
        if (currentValues.containsKey(key)) {
            return currentValues.get(key);
        } else if (!currentProfile.equals("default") && profiles.get("default").containsKey(key)) {
            return profiles.get("default").get(key);
        }
        ConstantDefinition def = registry.get(key);
        if (def != null) {
            return def.getDefaultValue();
        }
        throw new IllegalArgumentException("Constant key not found: " + key);
    }

    @Override
    public boolean hasValue(String profileName, String key) {
        Map<String, Object> profileValues = profiles.get(profileName);
        return profileValues != null && profileValues.containsKey(key);
    }

    @Override
    public Object getRawValue(String key) {
        return getValue(key);
    }

    @Override
    public Float getFloatValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Integer getIntValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Long getLongValue(String key) {
        Object value = getValue(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Boolean getBooleanValue(String key) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a boolean");
    }

    @Override
    public String getStringValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a string");
    }

    private boolean isValueTypeValid(Object value, Class<?> expectedType) {
        if (value == null)
            return false;
        if (expectedType == Float.class || expectedType == float.class)
            return value instanceof Number;
        if (expectedType == Long.class || expectedType == long.class)
            return value instanceof Number;
        if (expectedType == Integer.class || expectedType == int.class)
            return value instanceof Number;
        if (expectedType == Boolean.class || expectedType == boolean.class)
            return value instanceof Boolean;
        if (expectedType == String.class)
            return value instanceof String;
        return expectedType.isInstance(value);
    }
}