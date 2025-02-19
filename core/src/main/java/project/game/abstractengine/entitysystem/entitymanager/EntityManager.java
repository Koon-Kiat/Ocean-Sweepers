package project.game.abstractengine.entitysystem.entitymanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;

public class EntityManager {

	private final List<IRenderable> renderables;
	private final List<Entity> entityList;
	private final Set<String> entityIDs;
	private static final Logger LOGGER = Logger.getLogger(EntityManager.class.getName());

	public EntityManager() {
		this.renderables = new ArrayList<>();
		this.entityList = new ArrayList<>();
		this.entityIDs = new HashSet<>();

	}

	public boolean addRenderableEntity(IRenderable renderable) {
		renderables.add(renderable);

		if (renderable instanceof Entity) {
			Entity entity = (Entity) renderable;
			if (entityIDs.contains(entity.getID())) {
				LOGGER.log(Level.WARNING, "Duplicate ID: {0}", entity.getID());
				return false;
			}
			entityIDs.add(entity.getID());
			entityList.add(entity);
		}
		return true;
	}

	public boolean addEntity(Entity entity) {
		if (entityIDs.contains(entity.getID())) {
			LOGGER.log(Level.WARNING, "Duplicate ID: {0}", entity.getID());
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

	public List<Entity> getEntities() {
		return entityList;
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
			if (entityA instanceof ICollidable) {
				ICollidable collidableA = (ICollidable) entityA;
				for (int j = i + 1; j < entityList.size(); j++) {
					Entity entityB = entityList.get(j);
					if (entityB instanceof ICollidable) {
						ICollidable collidableB = (ICollidable) entityB;
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
