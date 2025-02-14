package project.game.abstractengine.entitysystem.entitymanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;

public class EntityManager {

	private List<Entity> entityList;
	private Set<String> entityIDs;

	public EntityManager() {
		this.entityList = new ArrayList<>();
		this.entityIDs = new HashSet<>();

	}

	public boolean addEntity(Entity entity) {
		if (entityIDs.contains(entity.getID())) {
			System.out.println("Duplicate ID: " + entity.getID());
			return false;
		}
		entityList.add(entity);
		entityIDs.add(entity.getID());
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
		for (Entity entity : entityList) {
			if (entity.isActive() && entity instanceof IRenderable) {
				IRenderable renderableEntity = (IRenderable) entity;
				renderableEntity.render(batch);
			}
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
							collidableA.onCollision(entityB);
							collidableB.onCollision(entityA);
						}
					}
				}
			}
		}
	}

}
