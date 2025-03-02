package project.game.context.api.constant;

/**
 * Constants related to entity movement behaviors.
 */
public interface IMovementConstants {
    // Speed related constants
    float PLAYER_SPEED();

    float NPC_SPEED();

    float DEFAULT_SPEED();

    // Movement pattern constants
    float AMPLITUDE();

    float FREQUENCY();

    // Duration constants
    float MIN_DURATION();

    float MAX_DURATION();
}