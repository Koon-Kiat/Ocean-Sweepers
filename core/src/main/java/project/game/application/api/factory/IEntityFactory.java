package project.game.application.api.factory;

/**
 * Base interface for all entity factories.
 * 
 * @param <T> The type of object to be created by this factory
 */
public interface IEntityFactory<T> {
    T createEntity(float x, float y);
}