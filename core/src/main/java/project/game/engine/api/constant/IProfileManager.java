package project.game.engine.api.constant;

/**
 * Interface for managing configuration profiles
 */
public interface IProfileManager {
    void createProfile(String profileName);

    boolean setProfile(String profileName);

    void setValue(String key, Object value);

    Object getValue(String key);

    boolean hasValue(String profileName, String key);

    Object getRawValue(String key);

    // Type-safe getters
    Float getFloatValue(String key);

    Integer getIntValue(String key);

    Long getLongValue(String key);

    Boolean getBooleanValue(String key);

    String getStringValue(String key);
}