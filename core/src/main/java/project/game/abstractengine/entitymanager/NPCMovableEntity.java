package project.game.abstractengine.entitymanager;

import project.game.Direction;
import project.game.abstractengine.movementmanager.NPCMovementManager;

public class NPCMovableEntity extends MovableEntity {
	private NPCMovementManager npcMovementManager;
	
	public NPCMovableEntity(float speed, NPCMovementManager npcMovementManager) {
		super(speed);
		this.npcMovementManager = npcMovementManager;
	}
	
	public void updateMovement() {
		npcMovementManager.updateMovement();
	}
	
	public void update() {
		updateMovement();
	}
	
	public void setDeltaTime(float deltaTime) {
		this.npcMovementManager.setDeltaTime(deltaTime);
	}
	
	public void updateDirection(Direction direction) {
		this.npcMovementManager.setDirection(direction);
	}
}
