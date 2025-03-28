package project.game.common.config.api;

/**
 * Constants related to entity movement behaviors.
 */
public interface IMovementConstants {

    // Speed related constants
    float DEFAULT_SPEED();

    float PLAYER_SPEED();

    float NPC_SPEED();

    // Movement pattern constants
    float AMPLITUDE();

    float FREQUENCY();

    // Duration constants
    float MIN_DURATION();

    float MAX_DURATION();
}