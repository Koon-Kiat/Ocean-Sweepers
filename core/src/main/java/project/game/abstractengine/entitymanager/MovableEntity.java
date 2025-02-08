package project.game.abstractengine.entitymanager;

import project.game.abstractengine.entitymanager.interfaces.Movable;


public abstract class MovableEntity extends Entity implements Movable {
	private float speed;
	
	public MovableEntity(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return this.speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
}
