package project.game.application.entity.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.core.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.movement.type.PlayerMovementManager;

public class Boat extends CollidableEntity implements IRenderable {

    private static final GameLogger LOGGER = new GameLogger(Boat.class);
    private final PlayerMovementManager movementManager;
    private String texturePath;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
	private int lastDirectionIndex = 2; // Default to DOWN (index 2)
    
    // New fields for directional sprites
    private TextureRegion[] directionalSprites;
    private boolean useDirectionalSprites = false;

    /**
     * Constructor for single texture boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
        super(entity, world);
        this.movementManager = movementManager;
        this.texturePath = texturePath;
        this.useDirectionalSprites = false;
    }
    
    /**
     * Constructor for directional sprites boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, TextureRegion[] directionalSprites) {
        super(entity, world);
        this.movementManager = movementManager;
        this.texturePath = null;
        this.directionalSprites = directionalSprites;
        this.useDirectionalSprites = true;
    }

    public PlayerMovementManager getMovementManager() {
        return this.movementManager;
    }

    /**
     * Set the collision to be active for a certain duration.
     */
    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;

        // When collision becomes active, sync positions to prevent desynchronization
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position and movement manager
        getEntity().setX(physX);
        getEntity().setY(physY);
        if (movementManager != null) {
            movementManager.setX(physX);
            movementManager.setY(physY);
        }
    }

    @Override
    public boolean isActive() {
        return super.getEntity().isActive();
    }

    @Override
    public Entity getEntity() {
        return super.getEntity();
    }

    @Override
    public Body getBody() {
        return super.getBody();
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
        bodyDef.linearDamping = 0.5f;
        bodyDef.bullet = true;
        bodyDef.allowSleep = false;

        Body newBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (width / 2) / pixelsToMeters,
                (height / 2) / pixelsToMeters);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 10.0f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.0f;

        Filter filter = new Filter();
        filter.categoryBits = 0x0001;
        filter.maskBits = -1;
        fixtureDef.filter.categoryBits = filter.categoryBits;
        fixtureDef.filter.maskBits = filter.maskBits;

        newBody.createFixture(fixtureDef);
        shape.dispose();
        newBody.setUserData(this);
        return newBody;
    }

    @Override
    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        
        // Calculate render coordinates (centered on Box2D body)
        float renderX = entityX() - entityWidth() / 2;
        float renderY = entityY() - entityHeight() / 2;
        
        if (useDirectionalSprites && directionalSprites != null) {
            // Use directional sprites
            TextureRegion currentSprite = getDirectionalSprite();
            batch.draw(currentSprite, renderX, renderY, entityWidth(), entityHeight());
        } else if (CustomAssetManager.getInstance().isLoaded()) {
            // Use single texture
            Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
            batch.draw(texture, renderX, renderY, entityWidth(), entityHeight());
        }
    }

    /**
     * Get the appropriate directional sprite based on movement
     */
    private TextureRegion getDirectionalSprite() {
        // Get velocity from movement manager
        Vector2 velocity = movementManager.getVelocity();
        
        // Only update direction if actually moving
        if (velocity.x != 0 || velocity.y != 0) {
            // Determine predominant direction
            if (Math.abs(velocity.y) > Math.abs(velocity.x)) {
                // Vertical movement is stronger
                if (velocity.y > 0) {
                    lastDirectionIndex = 0; // UP
                } else {
                    lastDirectionIndex = 2; // DOWN
                }
            } else {
                // Horizontal movement is stronger
                if (velocity.x > 0) {
                    lastDirectionIndex = 1; // RIGHT
                } else {
                    lastDirectionIndex = 3; // LEFT
                }
            }
        }
        
        // Return the last direction sprite (either just updated or preserved from before)
        return directionalSprites[lastDirectionIndex];
    }
    
    
    @Override
    public boolean checkCollision(Entity other) {
        // Use Box2D for collision detection
        return true;
    }

    @Override
    public void onCollision(ICollidableVisitor other) {
        // Only handle collisions with actual entities, not boundaries
        if (other != null) {
            // Log normal entity collisions
            LOGGER.info("{0} collided with {1}",
                    new Object[] { getEntity().getClass().getSimpleName(),
                            other.getClass().getSimpleName() });

            if (other instanceof Rock) {
                setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
                float boatX = getBody().getPosition().x;
                float boatY = getBody().getPosition().y;
                float rockX = other.getBody().getPosition().x;
                float rockY = other.getBody().getPosition().y;

                float dx = boatX - rockX;
                float dy = boatY - rockY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                LOGGER.info("go distance: " + distance);

                if (distance > 0.0001f) {
                    dx /= distance;
                    dy /= distance;
                    float bounceForce = GameConstantsFactory.getConstants().BOAT_BOUNCE_FORCE();
                    LOGGER.info("boat bounce force: " + bounceForce);
                    LOGGER.info("dy dx: " + dx);
                    getBody().applyLinearImpulse(dx * bounceForce, dy * bounceForce, boatX, boatY, true);
                }
            } else if (other instanceof Trash) {
                Trash trash = (Trash) other;
                trash.getEntity().setActive(false);
                getWorld().destroyBody(trash.getBody());
            }
        }
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
            // Important: Clear any lingering velocity when exiting collision state
            getBody().setLinearVelocity(0, 0);
        }
        return collisionActive;
    }

    private float entityX() {
        return super.getEntity().getX();
    }

    private float entityY() {
        return super.getEntity().getY();
    }

    private float entityWidth() {
        return super.getEntity().getWidth();
    }

    private float entityHeight() {
        return super.getEntity().getHeight();
    }
}