package project.game.application.entity.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.player.Boat;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class Trash implements ISpriteRenderable, ICollidableVisitor {

    private static final GameLogger LOGGER = new GameLogger(Trash.class);
    private final Entity entity;
    private final World world;
    private final Body body;
    private TextureRegion[] sprites;
    private int currentSpriteIndex;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private IEntityRemovalListener removalListener;
    private CollisionManager collisionManager;

    // Type-based collision handler registry
    private static final Map<Class<?>, BiConsumer<Trash, ICollidableVisitor>> TRASH_COLLISION_HANDLERS = new ConcurrentHashMap<>();

    static {
        // Register collision handler for Boat
        registerTrashCollisionHandler(Boat.class, Trash::handleBoatCollision);
    }

    /**
     * Register a handler for a specific type of collidable entity for Trash
     * 
     * @param <T>     Type of collidable
     * @param clazz   Class of collidable
     * @param handler Function to handle collision with the collidable
     */
    public static <T extends ICollidableVisitor> void registerTrashCollisionHandler(
            Class<T> clazz, BiConsumer<Trash, ICollidableVisitor> handler) {
        TRASH_COLLISION_HANDLERS.put(clazz, handler);
    }

    public Trash(Entity entity, World world, TextureRegion sprite) {
        this.entity = entity;
        this.world = world;
        this.sprites = new TextureRegion[] { sprite };
        this.currentSpriteIndex = 0;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
    }

    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    @Override
    public String getTexturePath() {
        return "trash1.png"; // Default texture path for fallback
    }

    public boolean isActive() {
        return entity.isActive();
    }

    @Override
    public void collideWith(Object other) {
        if (other instanceof ICollidableVisitor) {
            onCollision((ICollidableVisitor) other);
        }
    }

    @Override
    public void collideWithBoundary() {
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
    }

    @Override
    public boolean checkCollision(Entity other) {
        // Always return true to ensure collision is checked using Box2D
        return true;
    }

    @Override
    public void onCollision(ICollidableVisitor other) {
        LOGGER.info("{0} collided with {1}",
                new Object[] { getEntity().getClass().getSimpleName(),
                        other == null ? "boundary" : other.getClass().getSimpleName() });

        if (other != null) {
            // Dispatch to appropriate collision handler based on entity type
            dispatchCollisionHandling(other);
        }
    }

    /**
     * Dispatches collision handling to the appropriate registered handler
     * 
     * @param other The other entity involved in the collision
     */
    private void dispatchCollisionHandling(ICollidableVisitor other) {
        // Get other entity's class and find a matching handler
        Class<?> otherClass = other.getClass();

        // Look for a handler for this specific class or its superclasses
        for (Map.Entry<Class<?>, BiConsumer<Trash, ICollidableVisitor>> entry : TRASH_COLLISION_HANDLERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(otherClass)) {
                entry.getValue().accept(this, other);
                return;
            }
        }
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
        }
        return collisionActive;
    }

    @Override
    public TextureRegion getCurrentSprite() {
        if (!hasSprites()) {
            return null;
        }
        return sprites[currentSpriteIndex];
    }

    @Override
    public void updateSpriteIndex() {
        // Trash currently uses a single sprite
    }

    @Override
    public void setSprites(TextureRegion[] sprites) {
        this.sprites = sprites;
    }

    @Override
    public void setCurrentSpriteIndex(int index) {
        if (hasSprites() && index >= 0 && index < sprites.length) {
            this.currentSpriteIndex = index;
        }
    }

    @Override
    public boolean hasSprites() {
        return sprites != null && sprites.length > 0;
    }

    @Override
    public int getSpritesCount() {
        return hasSprites() ? sprites.length : 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isActive() && getCurrentSprite() != null) {
            float renderX = entity.getX() - entity.getWidth() / 2;
            float renderY = entity.getY() - entity.getHeight() / 2;
            batch.draw(getCurrentSprite(), renderX, renderY, entity.getWidth(), entity.getHeight());
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public final Body createBody(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float centerX = (x + width / 2) / pixelsToMeters;
        float centerY = (y + height / 2) / pixelsToMeters;
        bodyDef.position.set(centerX, centerY);
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;
        bodyDef.linearDamping = 0.8f;

        Body newBody = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius((width / 2) / pixelsToMeters);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.1f;

        Filter filter = new Filter();
        filter.categoryBits = 0x0004; // Trash category
        filter.maskBits = -1; // Collide with everything
        fixtureDef.filter.categoryBits = filter.categoryBits;
        fixtureDef.filter.maskBits = filter.maskBits;

        newBody.createFixture(fixtureDef);
        shape.dispose();
        newBody.setUserData(this);
        return newBody;
    }

    private void handleBoatCollision(ICollidableVisitor boat) {
        // Check if the boat covers up half of the trash
        float boatX = boat.getEntity().getX();
        float boatY = boat.getEntity().getY();
        float boatWidth = boat.getEntity().getWidth();
        float boatHeight = boat.getEntity().getHeight();

        float trashX = entity.getX();
        float trashY = entity.getY();
        float trashWidth = entity.getWidth();
        float trashHeight = entity.getHeight();

        boolean isCovered = (boatX < trashX + trashWidth / 2 && boatX + boatWidth > trashX + trashWidth / 2) &&
                (boatY < trashY + trashHeight / 2 && boatY + boatHeight > trashY + trashHeight / 2);

        if (isCovered) {
            // Always mark as inactive regardless of collisionManager availability
            // to prevent further collision processing
            entity.setActive(false);

            // Check if this entity should be removed
            String entityType = this.getClass().getSimpleName();
            if (!Boat.isEntityPermanent(entityType)) {
                if (collisionManager != null) {
                    collisionManager.scheduleBodyRemoval(getBody(), entity, removalListener);
                } else {
                    // If collisionManager isn't available, just log an error
                    // but don't try to destroy the body directly
                    LOGGER.error("CollisionManager not set in Trash object - cannot safely remove trash");
                    // Still notify the removal listener if available
                    if (removalListener != null) {
                        removalListener.onEntityRemove(entity);
                    }
                }
            }
        }
    }

}