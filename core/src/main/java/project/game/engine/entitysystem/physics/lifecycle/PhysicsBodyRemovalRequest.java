package project.game.engine.entitysystem.physics.lifecycle;

import com.badlogic.gdx.physics.box2d.Body;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.engine.entitysystem.entity.base.Entity;

/**
 * Represents a request to safely remove a Box2D body and associated entity
 * outside of the physics step.
 */
public class PhysicsBodyRemovalRequest {
    
    private final Body body;
    private final Entity entity;
    private final IEntityRemovalListener removalListener;

    public PhysicsBodyRemovalRequest(Body body, Entity entity, IEntityRemovalListener removalListener) {
        this.body = body;
        this.entity = entity;
        this.removalListener = removalListener;
    }

    public Body getBody() {
        return body;
    }

    public Entity getEntity() {
        return entity;
    }

    public IEntityRemovalListener getRemovalListener() {
        return removalListener;
    }
}