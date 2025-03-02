package project.game.engine.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract registry for constants that allows dynamic registration of new
 * constants.
 * This provides a base layer that can be extended for different projects.
 */
public abstract class AbstractConstantsRegistry {
    protected static final Map<String, ConstantDefinition> constants = new HashMap<>();

    protected static void register(ConstantDefinition definition) {
        constants.put(definition.getKey(), definition);
    }

    public static ConstantDefinition get(String key) {
        return constants.get(key);
    }

    public static Set<String> getKeysByCategory(String category) {
        return constants.values().stream()
                .filter(def -> def.getCategory().equals(category))
                .map(ConstantDefinition::getKey)
                .collect(Collectors.toSet());
    }

    public static Set<String> getAllKeys() {
        return constants.keySet();
    }

    /**
     * Add a new constant at runtime
     * 
     * @param key          The constant key
     * @param category     The category it belongs to
     * @param type         The data type
     * @param defaultValue The default value
     */
    public static void addConstant(String key, String category, Class<?> type, Object defaultValue) {
        register(new ConstantDefinition(key, category, type, defaultValue));
    }
}