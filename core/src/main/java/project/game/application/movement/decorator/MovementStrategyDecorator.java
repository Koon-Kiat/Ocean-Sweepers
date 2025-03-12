package project.game.application.movement.decorator;

import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;

public abstract class MovementStrategyDecorator implements IMovementStrategy {

    protected final IMovementStrategy wrappedStrategy;

    public MovementStrategyDecorator(IMovementStrategy strategy) {
        this.wrappedStrategy = strategy;
    }

    public IMovementStrategy getWrappedStrategy() {
        return wrappedStrategy;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        wrappedStrategy.move(movable, deltaTime);
    }
}