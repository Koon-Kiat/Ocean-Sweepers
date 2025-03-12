package project.game.engine.constant.loader;

import project.game.common.config.constant.DefaultProfileManager;
import project.game.engine.constant.api.IConstantsRegistry;
import project.game.engine.constant.api.IProfileManager;

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