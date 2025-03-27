package project.game.engine.scene.management;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ScoreManager {
    
    private static ScoreManager scoreInstance;
    private int score;
    private Label scoreLabel; 
    private boolean winState = false; 

    public ScoreManager() {
        score = 0;
    }

    public static ScoreManager getInstance() {
        if (scoreInstance == null) {
            scoreInstance = new ScoreManager();
        }
        return scoreInstance;
    }

    public void addScore(int points) {
        this.score += points;
        updateScoreLabel();
    }

    public void subtractScore(int points) {
        this.score -= points;
        updateScoreLabel();
    }

    public void multiplyScore(float multiplier) {
        this.score *= multiplier;
        updateScoreLabel();
    }

    public void resetScore() {
        score = 0;
        updateScoreLabel();
    }

    public int getScore() {
        return score;
    }

    public void setScoreLabel(Label label) {
        this.scoreLabel = label;
        updateScoreLabel();
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }

    private void updateScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }
    
    public void setWinState(boolean state) {
        this.winState = state;
    }
    
    public boolean hasWon() {
        return winState;
    }
}
