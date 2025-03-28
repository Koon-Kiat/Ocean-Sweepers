package project.game.application.entity.api;

import project.game.engine.entitysystem.entity.base.Entity;

public interface IEntityRemovalListener {

    void onEntityRemove(Entity entity);
}
