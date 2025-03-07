package project.game.common.config.constant;

import project.game.engine.constant.core.AbstractConstantsRegistry;
import project.game.engine.constant.core.ConstantDefinition;

/**
 * Game-specific implementation of the constants registry.
 * Extends the abstract base registry and adds game-specific constant
 * categories.
 */
public class ConstantsRegistry extends AbstractConstantsRegistry {

    // Categories
    public static final String CATEGORY_PHYSICS = "physics";
    public static final String CATEGORY_SCREEN = "screen";
    public static final String CATEGORY_ENTITY = "entity";
    public static final String CATEGORY_MOVEMENT = "movement";

    private static final ConstantsRegistry INSTANCE = new ConstantsRegistry();

    private ConstantsRegistry() {
        registerAllConstants();
    }

    public static ConstantsRegistry getInstance() {
        return INSTANCE;
    }

    private void registerAllConstants() {
        registerPhysicsConstants();
        registerScreenConstants();
        registerEntityConstants();
        registerMovementConstants();

    }

    private void registerPhysicsConstants() {
        ConstantDefinition def;

        def = new ConstantDefinition("PIXELS_TO_METERS", CATEGORY_PHYSICS, Float.class, 32.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("COLLISION_ACTIVE_DURATION", CATEGORY_PHYSICS, Long.class, 1000L);
        register(def.getKey(), def);

        // Entity-specific impulse strength constants
        def = new ConstantDefinition("MONSTER_BASE_IMPULSE", CATEGORY_PHYSICS, Float.class, 1.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("ROCK_BASE_IMPULSE", CATEGORY_PHYSICS, Float.class, 2.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("BOAT_BOUNCE_FORCE", CATEGORY_PHYSICS, Float.class, 0.1f);
        register(def.getKey(), def);

    }

    private void registerScreenConstants() {
        ConstantDefinition def;

        def = new ConstantDefinition("GAME_WIDTH", CATEGORY_SCREEN, Float.class, 1920.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("GAME_HEIGHT", CATEGORY_SCREEN, Float.class, 1080.0f);
        register(def.getKey(), def);
    }

    private void registerEntityConstants() {
        ConstantDefinition def;

        def = new ConstantDefinition("PLAYER_START_X", CATEGORY_ENTITY, Float.class, 400.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("PLAYER_START_Y", CATEGORY_ENTITY, Float.class, 400.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("PLAYER_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("PLAYER_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("TRASH_START_X", CATEGORY_ENTITY, Float.class, 0.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("TRASH_START_Y", CATEGORY_ENTITY, Float.class, 0.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("TRASH_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("TRASH_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);
        def = new ConstantDefinition("NUM_TRASHES", CATEGORY_ENTITY, Integer.class, 10);
        register(def.getKey(), def);

        def = new ConstantDefinition("ROCK_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("ROCK_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("NUM_ROCKS", CATEGORY_ENTITY, Integer.class, 10);
        register(def.getKey(), def);

        def = new ConstantDefinition("MONSTER_START_X", CATEGORY_ENTITY, Float.class, 0.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("MONSTER_START_Y", CATEGORY_ENTITY, Float.class, 0.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("MONSTER_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("MONSTER_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f);
        register(def.getKey(), def);

    }

    private void registerMovementConstants() {
        ConstantDefinition def;

        def = new ConstantDefinition("DEFAULT_SPEED", CATEGORY_MOVEMENT, Float.class, 600.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("PLAYER_SPEED", CATEGORY_MOVEMENT, Float.class, 1000.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("NPC_SPEED", CATEGORY_MOVEMENT, Float.class, 200.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("AMPLITUDE", CATEGORY_MOVEMENT, Float.class, 100.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("FREQUENCY", CATEGORY_MOVEMENT, Float.class, 5.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("MIN_DURATION", CATEGORY_MOVEMENT, Float.class, 1.0f);
        register(def.getKey(), def);

        def = new ConstantDefinition("MAX_DURATION", CATEGORY_MOVEMENT, Float.class, 3.0f);
        register(def.getKey(), def);
    }
}