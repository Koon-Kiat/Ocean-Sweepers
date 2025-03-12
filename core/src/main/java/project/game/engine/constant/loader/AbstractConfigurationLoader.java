package project.game.engine.constant.loader;

import java.util.HashMap;
import java.util.Map;

import project.game.engine.constant.api.IConfigurationLoader;
import project.game.engine.constant.core.ConstantDefinition;

/**
 * Abstract base class for configuration loading.
 * Provides template methods for loading and saving configurations.
 */
public abstract class AbstractConfigurationLoader implements IConfigurationLoader {

    @Override
    public boolean loadConfiguration(String source, String profileName, AbstractConfigurableConstants constants) {
        try {
            constants.createProfile(profileName);
            Map<String, Object> configData = readConfigurationData(source);
            if (configData == null) {
                return false;
            }

            validateAndLoadConfig(configData, profileName, constants);
            return true;

        } catch (Exception e) {
            handleError("Error loading configuration", e);
            return false;
        }
    }

    @Override
    public boolean saveConfiguration(String destination, AbstractConfigurableConstants constants) {
        try {
            Map<String, Object> configData = collectConfigurationData(constants);
            return writeConfigurationData(destination, configData);
        } catch (Exception e) {
            handleError("Error saving configuration", e);
            return false;
        }
    }

    protected void validateAndLoadConfig(Map<String, Object> configData, String profileName,
            AbstractConfigurableConstants constants) {
        Map<String, ConstantDefinition> validationMap = new HashMap<>();
        for (String key : constants.registry.getAllKeys()) {
            validationMap.put(key, constants.registry.get(key));
        }

        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            validateAndSetValue(entry.getKey(), entry.getValue(), validationMap, profileName, constants);
        }

        // Set default values for missing constants
        for (String key : constants.registry.getAllKeys()) {
            if (!constants.hasValue(profileName, key)) {
                ConstantDefinition def = constants.registry.get(key);
                handleWarning("Missing constant " + key + " in profile " + profileName +
                        ". Using default value: " + def.getDefaultValue());
                constants.setValue(key, def.getDefaultValue());
            }
        }
    }

    protected void validateAndSetValue(String key, Object value, Map<String, ConstantDefinition> validationMap,
            String profileName, AbstractConfigurableConstants constants) {
        ConstantDefinition def = validationMap.get(key);
        if (def == null) {
            handleWarning("Unknown constant in config: " + key);
            return;
        }

        if (!isValueTypeValid(value, def.getType())) {
            handleWarning("Type mismatch for constant " + key + ". Expected " + def.getType().getSimpleName());
            return;
        }

        constants.setValue(key, value);
    }

    protected Map<String, Object> collectConfigurationData(AbstractConfigurableConstants constants) {
        Map<String, Object> configData = new HashMap<>();
        for (String key : constants.registry.getAllKeys()) {
            Object value = constants.getRawValue(key);
            configData.put(key, value);
        }
        return configData;
    }

    protected boolean isValueTypeValid(Object value, Class<?> expectedType) {
        if (value == null) {
            return false;
        }

        if (expectedType == Float.class || expectedType == float.class) {
            return value instanceof Number;
        }
        if (expectedType == Long.class || expectedType == long.class) {
            return value instanceof Number;
        }
        if (expectedType == Integer.class || expectedType == int.class) {
            return value instanceof Number;
        }
        if (expectedType == Boolean.class || expectedType == boolean.class) {
            return value instanceof Boolean;
        }
        if (expectedType == String.class) {
            return value instanceof String;
        }

        return expectedType.isInstance(value);
    }

    /**
     * Handle warning messages. Implementations can define their logging strategy.
     */
    protected abstract void handleWarning(String message);

    /**
     * Handle error messages. Implementations can define their error handling
     * strategy.
     */
    protected abstract void handleError(String message, Exception e);
}