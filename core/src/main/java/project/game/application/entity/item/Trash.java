package project.game.application.entity.item;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.core.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;

public class Trash extends CollidableEntity implements IRenderable {

    private static final GameLogger LOGGER = new GameLogger(Trash.class);
    private final String texturePath;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private IEntityRemovalListener removalListener;

    public Trash(Entity entity, World world, String texturePath) {
        super(entity, world);
        this.texturePath = texturePath;
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
    }

    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;
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
        bodyDef.type = BodyDef.BodyType.StaticBody;
        float centerX = (x + width / 2) / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float centerY = (y + height / 2) / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        bodyDef.position.set(centerX, centerY);
        bodyDef.fixedRotation = true;
        bodyDef.allowSleep = false;

        Body newBody = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        float radius = Math.min(width, height) / 1.8f / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.5f;

        // Set up collision filtering
        Filter filter = new Filter();
        filter.categoryBits = 0x0004;
        filter.maskBits = -1 & ~0x0008;
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
        if (isActive() && CustomAssetManager.getInstance().isLoaded()) {
            // Render the entity using offset for BOX2D body
            float renderX = entityX() - entityWidth() / 2;
            float renderY = entityY() - entityHeight() / 2;
            Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
            if (texture != null) {
                batch.draw(texture, renderX, renderY, entityWidth(), entityHeight());
            }

        }
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

        if (other != null && other instanceof Boat) {
            // Check if the boat covers up half of the trash
            float boatX = other.getEntity().getX();
            float boatY = other.getEntity().getY();
            float boatWidth = other.getEntity().getWidth();
            float boatHeight = other.getEntity().getHeight();

            float trashX = getEntity().getX();
            float trashY = getEntity().getY();
            float trashWidth = getEntity().getWidth();
            float trashHeight = getEntity().getHeight();

            boolean isCovered = (boatX < trashX + trashWidth / 2 && boatX + boatWidth > trashX + trashWidth / 2) &&
                    (boatY < trashY + trashHeight / 2 && boatY + boatHeight > trashY + trashHeight / 2);

            if (isCovered) {
                // Dispose of the trash or make it disappear
                super.getEntity().setActive(false);
                getWorld().destroyBody(getBody());

                if (removalListener != null) {
                    removalListener.onEntityRemove(getEntity());
                }
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