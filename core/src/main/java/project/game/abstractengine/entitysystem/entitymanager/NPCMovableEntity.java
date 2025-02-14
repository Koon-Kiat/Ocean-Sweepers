package project.game.abstractengine.entitysystem.entitymanager;

import project.game.Direction;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;

public class NPCMovableEntity extends MovableEntity {
	private NPCMovementManager npcMovementManager;

	public NPCMovableEntity(Entity entity, float speed, NPCMovementManager npcMovementManager) {
		super(entity, speed);
		this.npcMovementManager = npcMovementManager;
	}

	@Override
	public void updateMovement() {
		npcMovementManager.updateMovement();
	}

	public void update() {
		updateMovement();
	}

	public void updateDirection(Direction direction) {
		this.npcMovementManager.setDirection(direction);
	}
}
