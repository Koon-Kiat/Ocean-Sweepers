package project.game.context.api.entity;

import project.game.engine.entitysystem.entity.Entity;

public interface IEntityRemovalListener {
    void onEntityRemove(Entity entity);
}
