package project.game.context.api.constant;

/**
 * Constants related to game entities and their properties.
 */
public interface IEntityConstants {

    // Player constants
    float PLAYER_START_X();

    float PLAYER_START_Y();

    float PLAYER_WIDTH();

    float PLAYER_HEIGHT();

    // NPC constants
    float TRASH_START_X();

    float TRASH_START_Y();

    float TRASH_WIDTH();

    float TRASH_HEIGHT();

    int NUM_TRASHES();

    // Rock constants
    float ROCK_WIDTH();

    float ROCK_HEIGHT();

    int NUM_ROCKS();

    // Monster constants
    float MONSTER_START_X();

    float MONSTER_START_Y();

    float MONSTER_WIDTH();

    float MONSTER_HEIGHT();

    float BOAT_BOUNCE_FORCE();

}