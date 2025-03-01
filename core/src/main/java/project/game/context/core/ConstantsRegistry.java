package project.game.context.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registry for game constants that allows dynamic registration of new
 * constants.
 * This centralizes constant definitions and makes the system more extensible.
 */
public class ConstantsRegistry {
    private static final Map<String, ConstantDefinition> constants = new HashMap<>();

    // Categories
    public static final String CATEGORY_MOVEMENT = "movement";
    public static final String CATEGORY_PHYSICS = "physics";
    public static final String CATEGORY_SCREEN = "screen";
    public static final String CATEGORY_ENTITY = "entity";

    static {
        // Register all default constants
        registerMovementConstants();
        registerPhysicsConstants();
        registerScreenConstants();
        registerEntityConstants();
    }

    private static void registerMovementConstants() {
        register(new ConstantDefinition("PLAYER_SPEED", CATEGORY_MOVEMENT, Float.class, 600.0f));
        register(new ConstantDefinition("NPC_SPEED", CATEGORY_MOVEMENT, Float.class, 400.0f));
        register(new ConstantDefinition("DEFAULT_SPEED", CATEGORY_MOVEMENT, Float.class, 600.0f));
        register(new ConstantDefinition("AMPLITUDE", CATEGORY_MOVEMENT, Float.class, 100.0f));
        register(new ConstantDefinition("FREQUENCY", CATEGORY_MOVEMENT, Float.class, 5.0f));
        register(new ConstantDefinition("MIN_DURATION", CATEGORY_MOVEMENT, Float.class, 1.0f));
        register(new ConstantDefinition("MAX_DURATION", CATEGORY_MOVEMENT, Float.class, 3.0f));
    }

    private static void registerPhysicsConstants() {
        register(new ConstantDefinition("PIXELS_TO_METERS", CATEGORY_PHYSICS, Float.class, 32.0f));
        register(new ConstantDefinition("IMPULSE_STRENGTH", CATEGORY_PHYSICS, Float.class, 5.0f));
        register(new ConstantDefinition("COLLISION_ACTIVE_DURATION", CATEGORY_PHYSICS, Long.class, 1000L));
    }

    private static void registerScreenConstants() {
        register(new ConstantDefinition("GAME_WIDTH", CATEGORY_SCREEN, Float.class, 1920.0f));
        register(new ConstantDefinition("GAME_HEIGHT", CATEGORY_SCREEN, Float.class, 1080.0f));
    }

    private static void registerEntityConstants() {
        register(new ConstantDefinition("BUCKET_START_X", CATEGORY_ENTITY, Float.class, 400.0f));
        register(new ConstantDefinition("BUCKET_START_Y", CATEGORY_ENTITY, Float.class, 400.0f));
        register(new ConstantDefinition("BUCKET_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("BUCKET_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("DROP_START_X", CATEGORY_ENTITY, Float.class, 0.0f));
        register(new ConstantDefinition("DROP_START_Y", CATEGORY_ENTITY, Float.class, 0.0f));
        register(new ConstantDefinition("DROP_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("DROP_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f));
    }

    public static void register(ConstantDefinition definition) {
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