package project.game.constants;

public class GameConstants {
    // Collision constants
    public static final float PIXELS_TO_METERS = 32f;
    public static final long COLLISION_ACTIVE_DURATION = 1000; // in milliseconds

    // Screen constants
    public static final float GAME_WIDTH = 1920;
    public static final float GAME_HEIGHT = 1080;

    // Speed constants
    public static final float PLAYER_SPEED = 600f;
    public static final float NPC_SPEED = 400f;

    // Player constants
    public static final float BUCKET_START_X = 400f;
    public static final float BUCKET_START_Y = 400f;
    public static final float BUCKET_WIDTH = 50f;
    public static final float BUCKET_HEIGHT = 50f;

    // NPC constants
    public static final float DROP_START_X = 0f;
    public static final float DROP_START_Y = 0f;
    public static final float DROP_WIDTH = 50f;
    public static final float DROP_HEIGHT = 50f;

    private GameConstants() {

    }
}
