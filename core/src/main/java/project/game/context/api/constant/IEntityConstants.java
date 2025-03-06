package project.game.context.api.constant;

/**
 * Constants related to game entities and their properties.
 */
public interface IEntityConstants {
    
    // Player constants
    float BUCKET_START_X();

    float BUCKET_START_Y();

    float BUCKET_WIDTH();

    float BUCKET_HEIGHT();

    // NPC constants
    float DROP_START_X();

    float DROP_START_Y();

    float DROP_WIDTH();

    float DROP_HEIGHT();

    // Rock constants
    float ROCK_WIDTH();

    float ROCK_HEIGHT();

    int NUM_ROCKS();

    //Trash constants
    float TRASH_WIDTH();

    float TRASH_HEIGHT();

    int NUM_TRASHES();

    //Monster constants
    float MONSTER_WIDTH();

    float MONSTER_HEIGHT();

    float BOAT_BOUNCE_FORCE();

    
}