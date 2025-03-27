package project.game.application.movement.composite;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.application.movement.api.StrategyType;
import project.game.application.movement.strategy.ConstantMovementStrategy;
import project.game.application.movement.strategy.ZigZagMovemenStrategy;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.movement.api.ICompositeMovementStrategy;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.CompositeMovementStrategy;

/**
 * A composite movement strategy that simulates ocean currents.
 * 
 * Combines constant directional flow with zigzag oscillation for realistic
 * water movement effects.
 */
public class OceanCurrentStrategy extends AbstractMovementStrategy {

    private final ICompositeMovementStrategy compositeStrategy;
    private final ConstantMovementStrategy constantStrategy;
    private final ZigZagMovemenStrategy zigzagStrategy;
    // Removed unused fields constantWeight and zigzagWeight

    /**
     * Creates an ocean current movement with specified parameters.
     * 
     * @param baseSpeed      Speed for constant directional flow
     * @param zigSpeed       Speed for zigzag oscillation component
     * @param amplitude      Amplitude of zigzag oscillation
     * @param frequency      Frequency of zigzag oscillation
     * @param constantWeight Weight for constant movement (0.0-1.0)
     * @param zigzagWeight   Weight for zigzag movement (0.0-1.0)
     * @param lenientMode    Whether to use lenient mode
     */
    public OceanCurrentStrategy(
            float baseSpeed,
            float zigSpeed,
            float amplitude,
            float frequency,
            float constantWeight,
            float zigzagWeight,
            boolean lenientMode) {

        super(OceanCurrentStrategy.class, lenientMode);

        try {
            // Create constant movement for directional flow
            this.constantStrategy = new ConstantMovementStrategy(baseSpeed, lenientMode);

            // Create zigzag movement for wave oscillation
            this.zigzagStrategy = new ZigZagMovemenStrategy(zigSpeed, amplitude, frequency, lenientMode);

            // Combine them with specified weights
            List<IMovementStrategy> additionalStrategies = new ArrayList<>();
            additionalStrategies.add(zigzagStrategy);
            float[] weights = new float[] { constantWeight, zigzagWeight };

            this.compositeStrategy = new CompositeMovementStrategy(
                    constantStrategy, additionalStrategies, weights);

            logger.info("OceanCurrentStrategy created with: baseSpeed={0}, zigSpeed={1}, amplitude={2}, " +
                    "frequency={3}, weights=[{4},{5}]",
                    baseSpeed, zigSpeed, amplitude, frequency, constantWeight, zigzagWeight);

        } catch (Exception e) {
            String msg = "Failed to create OceanCurrentStrategy: " + e.getMessage();
            logger.error(msg);
            throw new MovementException(msg, e);
        }
    }

    /**
     * Creates a randomized ocean current movement with parameters within specified
     * ranges.
     * 
     * @param minBaseSpeed   Minimum constant speed
     * @param maxBaseSpeed   Maximum constant speed
     * @param minZigSpeed    Minimum zigzag speed
     * @param maxZigSpeed    Maximum zigzag speed
     * @param minAmplitude   Minimum zigzag amplitude
     * @param maxAmplitude   Maximum zigzag amplitude
     * @param minFrequency   Minimum zigzag frequency
     * @param maxFrequency   Maximum zigzag frequency
     * @param constantWeight Weight for constant movement
     * @param zigzagWeight   Weight for zigzag movement
     * @param lenientMode    Whether to use lenient mode
     * @return A new OceanCurrentStrategy instance with randomized parameters
     */
    public static OceanCurrentStrategy createRandomized(
            float minBaseSpeed, float maxBaseSpeed,
            float minZigSpeed, float maxZigSpeed,
            float minAmplitude, float maxAmplitude,
            float minFrequency, float maxFrequency,
            float constantWeight, float zigzagWeight,
            boolean lenientMode) {

        try {
            // Generate random parameters within specified ranges
            float baseSpeed = MathUtils.random(minBaseSpeed, maxBaseSpeed);
            float zigSpeed = MathUtils.random(minZigSpeed, maxZigSpeed);
            float amplitude = MathUtils.random(minAmplitude, maxAmplitude);
            float frequency = MathUtils.random(minFrequency, maxFrequency);

            // Create the ocean current movement with randomized parameters
            return new OceanCurrentStrategy(
                    baseSpeed, zigSpeed, amplitude, frequency,
                    constantWeight, zigzagWeight, lenientMode);

        } catch (Exception e) {
            if (lenientMode) {
                // Create a default ocean current with minimal parameters if random generation
                // fails
                return new OceanCurrentStrategy(
                        minBaseSpeed, minZigSpeed, minAmplitude, minFrequency,
                        constantWeight, zigzagWeight, lenientMode);
            }
            throw new MovementException("Failed to create RandomizedOceanCurrentMovement", e);
        }
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.OCEAN_CURRENT;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            if (movable == null) {
                if (lenientMode) {
                    logger.warn("Entity is null in OceanCurrentStrategy.move; skipping movement");
                    return;
                } else {
                    throw new MovementException("Entity cannot be null in OceanCurrentStrategy");
                }
            }

            // Delegate to the composite strategy
            compositeStrategy.move(movable, deltaTime);

        } catch (MovementException e) {
            handleMovementException(e, "Error in OceanCurrentStrategy.move");
        } catch (Exception e) {
            handleMovementException(e, "Unexpected error in OceanCurrentStrategy.move");
        }
    }
}