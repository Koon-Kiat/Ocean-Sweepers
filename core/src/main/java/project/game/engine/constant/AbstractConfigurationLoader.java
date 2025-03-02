package project.game.engine.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for configuration loading.
 * Provides core functionality for loading and saving configurations,
 * while allowing specific implementations to define how to handle file I/O.
 */
public abstract class AbstractConfigurationLoader {

    /**
     * Load configuration from a source.
     * 
     * @param source      The source identifier (could be a file path, URL, etc.)
     * @param profileName The name of the profile to load into
     * @return true if loading was successful, false otherwise
     */
    protected boolean loadConfiguration(String source, String profileName, AbstractConfigurableConstants constants) {
        try {
            // Create the profile
            constants.createProfile(profileName);

            // Get configuration data
            Map<String, Object> configData = readConfigurationData(source);
            if (configData == null) {
                return false;
            }

            // Validate against registry before loading
            Map<String, ConstantDefinition> validationMap = new HashMap<>();
            for (String key : AbstractConstantsRegistry.getAllKeys()) {
                validationMap.put(key, AbstractConstantsRegistry.get(key));
            }

            // Process all values
            for (Map.Entry<String, Object> entry : configData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                ConstantDefinition def = validationMap.get(key);
                if (def == null) {
                    handleWarning("Unknown constant in config: " + key);
                    continue;
                }

                if (!isValueTypeValid(value, def.getType())) {
                    handleWarning("Type mismatch for constant " + key + ". Expected " + def.getType().getSimpleName());
                    continue;
                }

                constants.setValue(key, value);
            }

            // Verify all required constants are present
            for (String key : AbstractConstantsRegistry.getAllKeys()) {
                if (!constants.hasValue(profileName, key)) {
                    ConstantDefinition def = AbstractConstantsRegistry.get(key);
                    handleWarning("Missing constant " + key + " in profile " + profileName +
                            ". Using default value: " + def.getDefaultValue());
                    constants.setValue(key, def.getDefaultValue());
                }
            }

            return true;

        } catch (Exception e) {
            handleError("Error loading configuration", e);
            return false;
        }
    }

    /**
     * Save configuration to a destination.
     * 
     * @param destination The destination identifier (could be a file path, URL,
     *                    etc.)
     * @return true if saving was successful, false otherwise
     */
    protected boolean saveConfiguration(String destination, AbstractConfigurableConstants constants) {
        try {
            Map<String, Object> configData = new HashMap<>();

            // Collect all current values
            for (String key : AbstractConstantsRegistry.getAllKeys()) {
                Object value = constants.getRawValue(key);
                configData.put(key, value);
            }

            // Write the configuration data
            return writeConfigurationData(destination, configData);

        } catch (Exception e) {
            handleError("Error saving configuration", e);
            return false;
        }
    }

    /**
     * Read configuration data from the source.
     * Implementations should define how to read from their specific source.
     */
    protected abstract Map<String, Object> readConfigurationData(String source) throws Exception;

    /**
     * Write configuration data to the destination.
     * Implementations should define how to write to their specific destination.
     */
    protected abstract boolean writeConfigurationData(String destination, Map<String, Object> data) throws Exception;

    /**
     * Handle warning messages. Implementations can define their logging strategy.
     */
    protected abstract void handleWarning(String message);

    /**
     * Handle error messages. Implementations can define their error handling
     * strategy.
     */
    protected abstract void handleError(String message, Exception e);

    /**
     * Validate if a value matches the expected type
     */
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
}