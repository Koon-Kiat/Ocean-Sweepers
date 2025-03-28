package project.game.engine.entitysystem.entity.base;

import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.entitysystem.entity.api.IEntityManager;
import project.game.engine.entitysystem.entity.management.EntityManager;

public class Entity implements IEntityManager {

	private static AtomicInteger idCounter = new AtomicInteger(0);
	private final Vector2 position;
	private String id;
	private float width;
	private float height;
	private boolean active;

	public Entity() {
		this.id = generateUniqueID();
		this.position = new Vector2(100, 100);
		this.width = 100;
		this.height = 100;
		this.active = true;
	}

	public Entity(float x, float y, float width, float height, boolean active) {
		this.id = generateUniqueID();
		this.position = new Vector2(x, y);
		this.width = width;
		this.height = height;
		this.active = active;
	}

	public String getID() {
		return this.id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public float getX() {
		return this.position.x;
	}

	public void setX(float x) {
		this.position.x = x;
	}

	public float getY() {
		return this.position.y;
	}

	public void setY(float y) {
		this.position.y = y;
	}

	public Vector2 getVector() {
		return this.position;
	}

	public float getWidth() {
		return this.width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return this.height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	private String generateUniqueID() {
		return "E" + idCounter.getAndIncrement();
	}

	@Override
	public void removeFromManager(EntityManager entityManager) {
		entityManager.removeEntity(this);
	}

}