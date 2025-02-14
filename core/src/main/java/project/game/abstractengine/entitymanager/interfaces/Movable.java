package project.game.abstractengine.entitymanager.interfaces;

public interface Movable {
	
	void updateMovement();
	
	float getSpeed();
	
	void setSpeed(float speed);
}
