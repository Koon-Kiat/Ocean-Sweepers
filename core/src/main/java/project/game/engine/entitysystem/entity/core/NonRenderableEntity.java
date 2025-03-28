package project.game.engine.entitysystem.entity.core;

import project.game.engine.entitysystem.entity.base.Entity;

/**
 * NonRenderableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that are non-renderable.
 */
public abstract class NonRenderableEntity extends Entity {

	public NonRenderableEntity(float x, float y, float width, float height, boolean active) {
		super(x, y, width, height, active);
	}

	public abstract void update();
}
