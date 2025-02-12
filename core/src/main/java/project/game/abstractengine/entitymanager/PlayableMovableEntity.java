package project.game.abstractengine.entitymanager;

import project.game.Direction;
import project.game.abstractengine.movementmanager.PlayerMovementManager;

public class PlayableMovableEntity extends MovableEntity {
	private PlayerMovementManager playerMovementManager;
	
	public PlayableMovableEntity(Entity entity, float speed, PlayerMovementManager playerMovementManager) {
		super(entity, speed);
		this.playerMovementManager = playerMovementManager;
	}
	
	public void updateMovement() {
		if (super.getEntity().isActive()) {
			this.playerMovementManager.updateMovement();
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
