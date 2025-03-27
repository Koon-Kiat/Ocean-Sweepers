package project.game.engine.scene.management;

public class TimeManager {
    
    private static TimeManager instance;
    private final int initialMinutes;
    private final int initialSeconds;
    private float timeLeft;
    private boolean isRunning;

    public TimeManager(int minutes, int seconds) {
        this.initialMinutes = minutes;
        this.initialSeconds = seconds;
        this.timeLeft = (minutes * 60) + seconds;
        this.isRunning = false;
    }

    public static TimeManager getInstance(int minutes, int seconds) {
        if (instance == null) {
            instance = new TimeManager(minutes, seconds);
        }
        return instance;
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void resetTime() {
        this.timeLeft = (initialMinutes * 60) + initialSeconds;
    }

    public void update(float deltaTime) {
        if (isRunning && timeLeft > 0) {
            timeLeft -= deltaTime;
        }
    }

    public boolean isTimeUp() {
        return timeLeft <= 0;
    }

    public int getMinutes() {
        return (int) (timeLeft / 60);
    }

    public int getSeconds() {
        return (int) (timeLeft % 60);
    }

    public float getRemainingTime() {
        return timeLeft;
    }
}
