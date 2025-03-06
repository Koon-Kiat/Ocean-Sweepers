package project.game.engine.entitysystem.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.common.logging.core.GameLogger;
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;

/**
 * EntityManager class that manages entities and renderables
 */
public class EntityManager {

	private static final GameLogger LOGGER = new GameLogger(EntityManager.class);
	private final List<IRenderable> renderables;
	private final List<Entity> entityList;
	private final Set<String> entityIDs;

	public EntityManager() {
		this.renderables = new ArrayList<>();
		this.entityList = new ArrayList<>();
		this.entityIDs = new HashSet<>();

	}

	public List<Entity> getEntities() {
		return entityList;
	}

	public boolean addRenderableEntity(IRenderable renderable) {
		renderables.add(renderable);

		if (renderable instanceof Entity) {
			Entity entity = (Entity) renderable;
			if (entityIDs.contains(entity.getID())) {
				LOGGER.warn("Duplicate ID: {0}", entity.getID());
				return false;
			}
			entityIDs.add(entity.getID());
			entityList.add(entity);
		}
		return true;
	}

	public void removeRenderableEntity(IRenderable renderable) {
		renderables.remove(renderable);

		if (renderable instanceof Entity) {
			Entity entity = (Entity) renderable;
			entityIDs.remove(entity.getID());
			entityList.remove(entity);
		}
	}

	public boolean addEntity(Entity entity) {
		if (entityIDs.contains(entity.getID())) {
			LOGGER.warn("Duplicate ID: {0}", entity.getID());
			return false;
		}
		entityIDs.add(entity.getID());
		entityList.add(entity);
		return true;
	}

	public void removeEntity(Entity entity) {
		entityList.remove(entity);
		entityIDs.remove(entity.getID());
	}

	public void draw(SpriteBatch batch) {
		// Iterate over renderables instead of entityList
		for (IRenderable renderable : renderables) {
			renderable.render(batch);
		}
	}

	public void checkCollision() {
		for (int i = 0; i < entityList.size(); i++) {
			Entity entityA = entityList.get(i);
			if (entityA instanceof ICollidableVisitor) {
				ICollidableVisitor collidableA = (ICollidableVisitor) entityA;
				for (int j = i + 1; j < entityList.size(); j++) {
					Entity entityB = entityList.get(j);
					if (entityB instanceof ICollidableVisitor) {
						ICollidableVisitor collidableB = (ICollidableVisitor) entityB;
						if (collidableA.checkCollision(entityB)) {
							collidableA.onCollision(collidableB);
							collidableB.onCollision(collidableA);

						}
					}
				}
			}
		}
	}
}
