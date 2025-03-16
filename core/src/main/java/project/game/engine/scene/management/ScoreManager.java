package project.game.engine.scene.management;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ScoreManager {
    
    private static ScoreManager scoreInstance;
    private int score;
    private Label scoreLabel; 

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
    }

    public void subtractScore(int points) {
        this.score -= points;
    }

    public void multiplyScore(int multiplier) {
        this.score *= multiplier;
    }

    public void resetScore() {
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScoreLabel(Label label) {
        this.scoreLabel = label;
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }

    public void draw() {
        
    }
}
