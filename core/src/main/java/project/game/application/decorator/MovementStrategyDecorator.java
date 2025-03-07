package project.game.application.decorator;

import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

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