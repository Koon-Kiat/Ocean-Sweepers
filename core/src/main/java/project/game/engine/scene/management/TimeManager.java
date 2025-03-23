package project.game.engine.scene.management;

import java.util.Timer;
import java.util.TimerTask;

public class TimeManager {
    private int hours;
    private int minutes;
    private int seconds;
    private Timer timer;
    private boolean isRunning;

    // Constructor initializes time to 0
    public TimeManager() {
        resetTime();
    }

    // Set a specific time
    public void setTime(int hours, int minutes, int seconds) {
        this.hours = Math.max(0, hours);
        this.minutes = Math.max(0, minutes);
        this.seconds = Math.max(0, seconds);
        normalizeTime();
    }

    // Add time
    public void addTime(int hours, int minutes, int seconds) {
        this.hours += hours;
        this.minutes += minutes;
        this.seconds += seconds;
        normalizeTime();
    }

    // Subtract time
    public void subtractTime(int hours, int minutes, int seconds) {
        int totalSeconds = getTotalSeconds() - (hours * 3600 + minutes * 60 + seconds);
        totalSeconds = Math.max(0, totalSeconds); // Prevent negative time
        setTimeFromSeconds(totalSeconds);
    }

    // Reset time to 00:00:00
    public void resetTime() {
        stopCountdown();
        this.hours = 0;
        this.minutes = 0;
        this.seconds = 0;
    }

    // Get the current time as a formatted string
    public String getTime() {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Get total time in seconds
    public int getTotalSeconds() {
        return (hours * 3600) + (minutes * 60) + seconds;
    }

    // Start countdown
    public void startCountdown(Runnable onTimeUp) {
        if (isRunning) return; // Prevent multiple timers

        timer = new Timer();
        isRunning = true;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getTotalSeconds() > 0) {
                    subtractTime(0, 0, 1);
                    System.out.println("Time Left: " + getTime());
                } else {
                    stopCountdown();
                    onTimeUp.run();
                }
            }
        }, 1000, 1000);
    }

    // Stop countdown
    public void stopCountdown() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        isRunning = false;
    }

    // Normalize time to handle overflow
    private void normalizeTime() {
        if (seconds >= 60) {
            minutes += seconds / 60;
            seconds %= 60;
        }
        if (minutes >= 60) {
            hours += minutes / 60;
            minutes %= 60;
        }
    }

    // Convert total seconds into hours, minutes, and seconds
    private void setTimeFromSeconds(int totalSeconds) {
        this.hours = totalSeconds / 3600;
        this.minutes = (totalSeconds % 3600) / 60;
        this.seconds = totalSeconds % 60;
    }
}