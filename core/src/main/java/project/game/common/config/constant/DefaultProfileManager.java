package project.game.common.config.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import project.game.engine.constant.api.IConstantsRegistry;
import project.game.engine.constant.api.IProfileManager;
import project.game.engine.constant.model.ConstantDefinition;

/**
 * Default implementation of profile management
 */
public class DefaultProfileManager implements IProfileManager {

    private final IConstantsRegistry registry;
    private final Map<String, Map<String, Object>> profiles = new HashMap<>();
    private String currentProfile = "default";

    // Type checker map
    private final Map<Class<?>, Function<Object, Boolean>> typeCheckers = new HashMap<>();

    public DefaultProfileManager(IConstantsRegistry registry) {
        this.registry = registry;
        initializeTypeCheckers();
        initializeDefaultProfile();
    }

    private void initializeTypeCheckers() {
        // Register type checkers for each supported type - ensuring null safety
        typeCheckers.put(Float.class, value -> {
            if (value == null)
                return false;
            Class<?> valueClass = value.getClass();
            return valueClass == Float.class || valueClass == Double.class || Number.class.isAssignableFrom(valueClass);
        });

        typeCheckers.put(float.class, typeCheckers.get(Float.class));

        typeCheckers.put(Integer.class, value -> {
            if (value == null)
                return false;
            Class<?> valueClass = value.getClass();
            return valueClass == Integer.class || Number.class.isAssignableFrom(valueClass);
        });

        typeCheckers.put(int.class, typeCheckers.get(Integer.class));

        typeCheckers.put(Long.class, value -> {
            if (value == null)
                return false;
            Class<?> valueClass = value.getClass();
            return valueClass == Long.class || Number.class.isAssignableFrom(valueClass);
        });

        typeCheckers.put(long.class, typeCheckers.get(Long.class));

        typeCheckers.put(Boolean.class, value -> {
            if (value == null)
                return false;
            return value.getClass() == Boolean.class;
        });

        typeCheckers.put(boolean.class, typeCheckers.get(Boolean.class));

        typeCheckers.put(String.class, value -> {
            if (value == null)
                return false;
            return value.getClass() == String.class;
        });
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
                    " but got " + (value != null ? value.getClass().getSimpleName() : "null"));
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
        if (checkType(value, Number.class)) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Integer getIntValue(String key) {
        Object value = getValue(key);
        if (checkType(value, Number.class)) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Long getLongValue(String key) {
        Object value = getValue(key);
        if (checkType(value, Number.class)) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Constant " + key + " is not a number");
    }

    @Override
    public Boolean getBooleanValue(String key) {
        Object value = getValue(key);
        if (checkType(value, Boolean.class)) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a boolean");
    }

    @Override
    public String getStringValue(String key) {
        Object value = getValue(key);
        if (checkType(value, String.class)) {
            return (String) value;
        }
        throw new IllegalArgumentException("Constant " + key + " is not a string");
    }

    private boolean isValueTypeValid(Object value, Class<?> expectedType) {
        if (value == null)
            return false;

        // Use registered type checker if available
        Function<Object, Boolean> typeChecker = typeCheckers.get(expectedType);
        if (typeChecker != null) {
            return typeChecker.apply(value);
        }

        // Fallback to standard class check for other types
        return expectedType.isAssignableFrom(value.getClass());
    }

    // Helper method for type checking with better readability
    private boolean checkType(Object value, Class<?> expectedType) {
        if (value == null)
            return false;

        if (expectedType == Number.class) {
            Class<?> valueClass = value.getClass();
            return valueClass == Integer.class ||
                    valueClass == Long.class ||
                    valueClass == Float.class ||
                    valueClass == Double.class ||
                    Number.class.isAssignableFrom(valueClass);
        }

        return expectedType.isAssignableFrom(value.getClass());
    }

    private void initializeDefaultProfile() {
        createProfile("default");
    }
}