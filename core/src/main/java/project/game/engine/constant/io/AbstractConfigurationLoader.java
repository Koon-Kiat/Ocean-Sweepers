package project.game.engine.constant.io;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import project.game.engine.constant.api.IConfigurationLoader;
import project.game.engine.constant.base.AbstractConfigurableConstants;
import project.game.engine.constant.model.ConstantDefinition;

/**
 * Abstract base class for configuration loading.
 * Provides template methods for loading and saving configurations.
 */
public abstract class AbstractConfigurationLoader implements IConfigurationLoader {

    private final Map<Class<?>, Function<Object, Boolean>> typeCheckerMap;

    protected AbstractConfigurationLoader() {
        this.typeCheckerMap = initializeTypeCheckers();
    }

    private Map<Class<?>, Function<Object, Boolean>> initializeTypeCheckers() {
        Map<Class<?>, Function<Object, Boolean>> checkers = new HashMap<>();

        Function<Object, Boolean> numberChecker = value -> value != null
                && (Number.class.isAssignableFrom(value.getClass()));

        checkers.put(Float.class, numberChecker);
        checkers.put(float.class, numberChecker);
        checkers.put(Long.class, numberChecker);
        checkers.put(long.class, numberChecker);
        checkers.put(Integer.class, numberChecker);
        checkers.put(int.class, numberChecker);

        checkers.put(Boolean.class, value -> value != null && Boolean.class.isAssignableFrom(value.getClass()));
        checkers.put(boolean.class, checkers.get(Boolean.class));

        checkers.put(String.class, value -> value != null && String.class.isAssignableFrom(value.getClass()));

        return checkers;
    }

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
        for (String key : constants.getRegistry().getAllKeys()) {
            validationMap.put(key, constants.getRegistry().get(key));
        }

        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            validateAndSetValue(entry.getKey(), entry.getValue(), validationMap, profileName, constants);
        }

        // Set default values for missing constants
        for (String key : constants.getRegistry().getAllKeys()) {
            if (!constants.hasValue(profileName, key)) {
                ConstantDefinition def = constants.getRegistry().get(key);
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
        for (String key : constants.getRegistry().getAllKeys()) {
            Object value = constants.getRawValue(key);
            configData.put(key, value);
        }
        return configData;
    }

    protected boolean isValueTypeValid(Object value, Class<?> expectedType) {
        if (value == null) {
            return false;
        }

        // Use the type checker if available
        Function<Object, Boolean> checker = typeCheckerMap.get(expectedType);
        if (checker != null) {
            return checker.apply(value);
        }

        // Fallback to class check
        return expectedType.isAssignableFrom(value.getClass());
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