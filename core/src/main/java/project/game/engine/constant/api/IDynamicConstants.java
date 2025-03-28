package project.game.engine.constant.api;

/**
 * Interface for constants that can be dynamically added at runtime
 */
public interface IDynamicConstants {

    /**
     * Adds a dynamic constant to the system at runtime.
     * This method handles both registering the constant definition and setting its
     * value.
     * 
     * @param key          The constant key/name
     * @param category     The category for the constant
     * @param type         The class type of the constant
     * @param defaultValue The default/initial value for the constant
     * @return True if the constant was added successfully, false otherwise
     */
    boolean addDynamicConstant(String key, String category, Class<?> type, Object defaultValue);

    Object getRawValue(String key);
}
