package project.game.context.constant;

import project.game.engine.constant.AbstractConstantsRegistry;
import project.game.engine.constant.ConstantDefinition;

/**
 * Game-specific implementation of the constants registry.
 * Extends the abstract base registry and adds game-specific constant
 * categories.
 */
public class ConstantsRegistry extends AbstractConstantsRegistry {
    // Categories
    public static final String CATEGORY_MOVEMENT = "movement";
    public static final String CATEGORY_PHYSICS = "physics";
    public static final String CATEGORY_SCREEN = "screen";
    public static final String CATEGORY_ENTITY = "entity";

    private static final ConstantsRegistry INSTANCE = new ConstantsRegistry();

    private ConstantsRegistry() {
        registerAllConstants();
    }

    public static ConstantsRegistry getInstance() {
        return INSTANCE;
    }

    private void registerAllConstants() {
        registerMovementConstants();
        registerPhysicsConstants();
        registerScreenConstants();
        registerEntityConstants();
    }

    private void registerMovementConstants() {
        register(new ConstantDefinition("PLAYER_SPEED", CATEGORY_MOVEMENT, Float.class, 1000.0f));
        register(new ConstantDefinition("NPC_SPEED", CATEGORY_MOVEMENT, Float.class, 200.0f));
        register(new ConstantDefinition("DEFAULT_SPEED", CATEGORY_MOVEMENT, Float.class, 600.0f));
        register(new ConstantDefinition("AMPLITUDE", CATEGORY_MOVEMENT, Float.class, 100.0f));
        register(new ConstantDefinition("FREQUENCY", CATEGORY_MOVEMENT, Float.class, 5.0f));
        register(new ConstantDefinition("MIN_DURATION", CATEGORY_MOVEMENT, Float.class, 1.0f));
        register(new ConstantDefinition("MAX_DURATION", CATEGORY_MOVEMENT, Float.class, 3.0f));
    }

    private void registerPhysicsConstants() {
        register(new ConstantDefinition("PIXELS_TO_METERS", CATEGORY_PHYSICS, Float.class, 32.0f));
        register(new ConstantDefinition("IMPULSE_STRENGTH", CATEGORY_PHYSICS, Float.class, 5.0f));
        register(new ConstantDefinition("COLLISION_ACTIVE_DURATION", CATEGORY_PHYSICS, Long.class, 1000L));
    }

    private void registerScreenConstants() {
        register(new ConstantDefinition("GAME_WIDTH", CATEGORY_SCREEN, Float.class, 1920.0f));
        register(new ConstantDefinition("GAME_HEIGHT", CATEGORY_SCREEN, Float.class, 1080.0f));
    }

    private void registerEntityConstants() {
        register(new ConstantDefinition("BUCKET_START_X", CATEGORY_ENTITY, Float.class, 400.0f));
        register(new ConstantDefinition("BUCKET_START_Y", CATEGORY_ENTITY, Float.class, 400.0f));
        register(new ConstantDefinition("BUCKET_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("BUCKET_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("DROP_START_X", CATEGORY_ENTITY, Float.class, 0.0f));
        register(new ConstantDefinition("DROP_START_Y", CATEGORY_ENTITY, Float.class, 0.0f));
        register(new ConstantDefinition("DROP_WIDTH", CATEGORY_ENTITY, Float.class, 50.0f));
        register(new ConstantDefinition("DROP_HEIGHT", CATEGORY_ENTITY, Float.class, 50.0f));
    }
}