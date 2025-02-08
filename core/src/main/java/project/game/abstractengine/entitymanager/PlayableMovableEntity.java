package project.game.abstractengine.entitymanager;

import project.game.Direction;
import project.game.abstractengine.movementmanager.PlayerMovementManager;

public class PlayableMovableEntity extends MovableEntity {
	private PlayerMovementManager playerMovementManager;
	
	public PlayableMovableEntity(float speed, PlayerMovementManager playerMovementManager) {
		super(speed);
		this.playerMovementManager = playerMovementManager;
	}
	
	public void updateMovement() {
		if (super.isActive()) {
			playerMovementManager.updateMovement();
		}
	}
	
	public void update() {
		updateMovement();
	}
	
	public void setDeltaTime(float deltaTime) {
		this.playerMovementManager.setDeltaTime(deltaTime);
	}
	
	public void updateDirection(Direction direction) {
		this.playerMovementManager.setDirection(direction);
	}
}
