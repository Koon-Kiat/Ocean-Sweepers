package project.game.engine.constant.api;

import java.util.Set;

import project.game.engine.constant.model.ConstantDefinition;

/**
 * Interface defining the contract for a constants registry.
 */
public interface IConstantsRegistry {
    
    /**
     * Register a new constant definition
     */
    void register(String key, ConstantDefinition definition);

    /**
     * Get a constant definition by key
     */
    ConstantDefinition get(String key);

    /**
     * Get all registered constant keys
     */
    Set<String> getAllKeys();

    /**
     * Check if a key exists in the registry
     */
    boolean containsKey(String key);

    /**
     * Get all keys for a specific category
     */
    Set<String> getKeysByCategory(String category);
}