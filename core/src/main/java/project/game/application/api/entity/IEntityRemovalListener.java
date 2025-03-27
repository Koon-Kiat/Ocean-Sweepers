package project.game.application.api.entity;

import project.game.engine.entitysystem.entity.base.Entity;

public interface IEntityRemovalListener {
    
    void onEntityRemove(Entity entity);
}
