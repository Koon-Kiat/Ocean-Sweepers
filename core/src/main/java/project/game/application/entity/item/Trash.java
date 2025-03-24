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
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
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
        // Handler for trash-trash collisions
        registerTrashCollisionHandler(Trash.class, Trash::handleTrashCollision);
        // Register collision handler for SeaTurtle
        registerTrashCollisionHandler(SeaTurtle.class, Trash::handleSeaTurtleCollision);
        // Register collision handler for Rock
        registerTrashCollisionHandler(Rock.class, Trash::handleRockCollision);
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

        // When collision becomes active, sync position between physics body and entity
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position
        getEntity().setX(physX);
        getEntity().setY(physY);
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    @Override
    public String getTexturePath() {
        return "trash1.png";
    }

    public boolean isActive() {
        return entity.isActive();
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    public void removeFromManager(EntityManager entityManager) {
        entityManager.removeRenderableEntity(this);
	}

    @Override
    public void collideWith(Object other) {
        if (other instanceof ICollidableVisitor) {
            onCollision((ICollidableVisitor) other);
        }
    }

    @Override
    public void collideWithBoundary() {
        // Get current velocity
        com.badlogic.gdx.math.Vector2 velocity = body.getLinearVelocity();
        float speed = velocity.len();

        // Get position to determine which boundary was hit
        float x = entity.getX();
        float y = entity.getY();
        float bounceMultiplier = 0.8f; // Maintain 80% of speed after bounce

        // Get game boundaries
        float gameWidth = GameConstantsFactory.getConstants().GAME_WIDTH();
        float gameHeight = GameConstantsFactory.getConstants().GAME_HEIGHT();

        // Determine which boundary was hit and reverse appropriate velocity component
        if (x <= 0 || x >= gameWidth) {
            velocity.x = -velocity.x * bounceMultiplier;
        }
        if (y <= 0 || y >= gameHeight) {
            velocity.y = -velocity.y * bounceMultiplier;
        }

        // Ensure minimum speed after bounce
        float minSpeed = 2.0f;
        if (velocity.len() < minSpeed) {
            velocity.nor().scl(minSpeed);
        }

        // Apply the new velocity
        body.setLinearVelocity(velocity);

        // Keep damping low to maintain movement
        body.setLinearDamping(0.2f);
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
        bodyDef.linearDamping = 0.05f;

        Body newBody = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius((width / 2) / pixelsToMeters);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.3f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.8f;

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

    private void handleTrashCollision(ICollidableVisitor other) {
        if (other == null || !(other instanceof Trash)) {
            return;
        }
    
        // Get the other Trash object
        Trash otherTrash = (Trash) other;
    
        // Get the positions of both Trash objects
        float trash1X = getBody().getPosition().x;
        float trash1Y = getBody().getPosition().y;
        float trash2X = otherTrash.getBody().getPosition().x;
        float trash2Y = otherTrash.getBody().getPosition().y;
    
        // Calculate direction from trash2 to trash1
        float dx = trash1X - trash2X;
        float dy = trash1Y - trash2Y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
    
        if (distance > 0.0001f) {
            dx /= distance;
            dy /= distance;
    
            // Apply a repulsion force to both trash objects
            float repulsionForce = GameConstantsFactory.getConstants().BOAT_BOUNCE_FORCE() * 3; // Adjust as needed
            getBody().applyLinearImpulse(dx * repulsionForce, dy * repulsionForce, trash1X, trash1Y, true);
            otherTrash.getBody().applyLinearImpulse(-dx * repulsionForce, -dy * repulsionForce, trash2X, trash2Y, true);
        }
    
        // Ensure minimum speed after collision
        float minSpeed = 3.0f;
        com.badlogic.gdx.math.Vector2 velocity1 = getBody().getLinearVelocity();
        if (velocity1.len() < minSpeed) {
            velocity1.x = dx * minSpeed;
            velocity1.y = dy * minSpeed;
            getBody().setLinearVelocity(velocity1);
        }
        com.badlogic.gdx.math.Vector2 velocity2 = otherTrash.getBody().getLinearVelocity();
        if (velocity2.len() < minSpeed) {
            velocity2.x = -dx * minSpeed;
            velocity2.y = -dy * minSpeed;
            otherTrash.getBody().setLinearVelocity(velocity2);
        }
    
        // Reduce linear damping to maintain post-collision movement
        getBody().setLinearDamping(0.01f);
        otherTrash.getBody().setLinearDamping(0.01f);
    }

    private void handleSeaTurtleCollision(ICollidableVisitor seaTurtle) {
        // SeaTurtle collision handling
        // SeaTurtle is not affected by trash collisions
    }

     /**
     * Handle collision with a rock
     */
    private void handleRockCollision(ICollidableVisitor rock) {
        if (rock == null || !(rock instanceof Rock)) {
            return;
        }

        // Get the Rock object
        Rock currentRock = (Rock) rock;

        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
        float trashX = getBody().getPosition().x;
        float trashY = getBody().getPosition().y;
        float rockX = currentRock.getBody().getPosition().x;
        float rockY = currentRock.getBody().getPosition().y;

        float dx = trashX - rockX;
        float dy = trashY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        // LOGGER.info("go distance: " + distance);

        if (distance > 0.0001f) {
            dx /= distance;
            dy /= distance;
            float bounceForce = GameConstantsFactory.getConstants().BOAT_BOUNCE_FORCE() * 2;
            getBody().applyLinearImpulse(dx * bounceForce, dy * bounceForce, trashX, trashY, true);
        }

        // Ensure minimum speed after collision
        float minSpeed = 3.0f;
        com.badlogic.gdx.math.Vector2 velocity = getBody().getLinearVelocity();
        if (velocity.len() < minSpeed) {
            velocity.nor().scl(minSpeed);
            getBody().setLinearVelocity(velocity);
        }

        // Reduce linear damping to maintain post-collision movement
        getBody().setLinearDamping(0.01f);
    }

}