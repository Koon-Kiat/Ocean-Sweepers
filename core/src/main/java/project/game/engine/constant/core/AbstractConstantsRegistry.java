package project.game.engine.constant.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import project.game.engine.api.constant.IConstantsRegistry;

/**
 * Base implementation of the constants registry.
 */
public class AbstractConstantsRegistry implements IConstantsRegistry {

    private final Map<String, ConstantDefinition> registry = new HashMap<>();

    public void addConstant(String key, String category, Class<?> type, Object defaultValue) {
        register(key, new ConstantDefinition(key, category, type, defaultValue));
    }

    @Override
    public void register(String key, ConstantDefinition definition) {
        if (registry.containsKey(key)) {
            throw new IllegalArgumentException("Constant already registered: " + key);
        }
        registry.put(key, definition);
    }

    @Override
    public ConstantDefinition get(String key) {
        return registry.get(key);
    }

    @Override
    public Set<String> getAllKeys() {
        return registry.keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return registry.containsKey(key);
    }

    @Override
    public Set<String> getKeysByCategory(String category) {
        return registry.values().stream()
                .filter(def -> def.getCategory().equals(category))
                .map(ConstantDefinition::getKey)
                .collect(Collectors.toSet());
    }
}