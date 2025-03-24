package project.game.application.api.constant;

/**
 * Constants related to physics and collision detection.
 */
public interface IPhysicsConstants {

    float PIXELS_TO_METERS();

    long COLLISION_ACTIVE_DURATION();

    float SEA_TURTLE_BASE_IMPULSE();

    float ROCK_BASE_IMPULSE();

    float TRASH_BASE_IMPULSE();

    float BOAT_BOUNCE_FORCE();

    float TRASH_ROCK_BOUNCE_FORCE();
}