package project.game.engine.constant.base;

import project.game.common.config.constant.DefaultProfileManager;
import project.game.engine.constant.api.IConstantsRegistry;
import project.game.engine.constant.api.IProfileManager;
import project.game.engine.constant.model.ConstantDefinition;

/**
 * An abstract base class for configurable constants that provides profile-based
 * configuration management.
 */
public abstract class AbstractConfigurableConstants {

    protected final IProfileManager profileManager;
    protected final IConstantsRegistry registry;

    protected AbstractConfigurableConstants(IConstantsRegistry registry) {
        this.registry = registry;
        this.profileManager = new DefaultProfileManager(registry);
    }

    /**
     * Adds a dynamic constant to the system at runtime.
     * This method handles both registering the constant definition and setting its value.
     * 
     * @param key          The constant key/name
     * @param category     The category for the constant
     * @param type         The class type of the constant
     * @param defaultValue The default/initial value for the constant
     * @return True if the constant was added successfully, false otherwise
     */
    public boolean addDynamicConstant(String key, String category, Class<?> type, Object defaultValue) {
        try {
            // Check if the constant already exists
            if (registry.containsKey(key)) {
                // Just update the value if it exists
                setValue(key, defaultValue);
                return true;
            }
            
            // Add the constant to the registry (cast to access the addConstant method)
            ConstantDefinition definition = new ConstantDefinition(key, category, type, defaultValue);
            registry.register(key, definition);
            
            // Set the value in the current profile
            setValue(key, defaultValue);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public IConstantsRegistry getRegistry() {
        return registry;
    }

    public void createProfile(String profileName) {
        profileManager.createProfile(profileName);
    }

    public boolean setProfile(String profileName) {
        return profileManager.setProfile(profileName);
    }

    public void setValue(String key, Object value) {
        profileManager.setValue(key, value);
    }

    public boolean hasValue(String profileName, String key) {
        return profileManager.hasValue(profileName, key);
    }

    public Object getRawValue(String key) {
        return profileManager.getRawValue(key);
    }

    protected Float getFloatValue(String key) {
        return profileManager.getFloatValue(key);
    }

    protected Integer getIntValue(String key) {
        return profileManager.getIntValue(key);
    }

    protected Long getLongValue(String key) {
        return profileManager.getLongValue(key);
    }

    protected Boolean getBooleanValue(String key) {
        return profileManager.getBooleanValue(key);
    }

    protected String getStringValue(String key) {
        return profileManager.getStringValue(key);
    }

    /**
     * Abstract method to initialize configuration from a source.
     * Implementation should handle loading configuration from a specific source.
     */
    protected abstract void initializeConfiguration(String configSource);
}