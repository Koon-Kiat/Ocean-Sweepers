package project.game.context.decorator;

import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

public abstract class MovementStrategyDecorator implements IMovementStrategy {
    
    protected final IMovementStrategy wrappedStrategy;

    public MovementStrategyDecorator(IMovementStrategy strategy) {
        this.wrappedStrategy = strategy;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        wrappedStrategy.move(movable, deltaTime);
    }

    public IMovementStrategy getWrappedStrategy() {
        return wrappedStrategy;
    }
}