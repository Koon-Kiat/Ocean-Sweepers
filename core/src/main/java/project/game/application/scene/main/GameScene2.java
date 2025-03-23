package project.game.application.scene.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;
import project.game.engine.scene.management.TimeManager;

public class GameScene2 extends Scene implements IEntityRemovalListener {
    
    private HealthManager healthManager;
    private ScoreManager scoreManager;
    private TimeManager timer;    

    private GameScene gameScene;
    private Texture heartTexture;
    private SpriteBatch batch;
    private Skin skin;

    public GameScene2(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.gameScene = new GameScene(sceneManager, inputManager); // Composition of GameScene
        this.healthManager = HealthManager.getInstance(heartTexture);
        this.scoreManager = ScoreManager.getInstance();
        this.timer = new TimeManager(0, 5);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        if (gameScene != null) {
            // gameScene.initPopUpMenu();
            // gameScene.displayMessage();
            gameScene.create();
        }
    }

    @Override
    public void render(float deltaTime) {
        gameScene.timer.stop();
        timer.update(deltaTime);

        if (timer.isTimeUp()) {
            timer.stop();
            sceneManager.setScene("gameover");
            return;
        }

        gameScene.render(deltaTime);
        //gameScene.input();
        batch.begin();
        skin.getFont("default-font").draw(batch, String.format("Time: %02d:%02d", 
            timer.getMinutes(), timer.getSeconds()), 200, sceneUIManager.getStage().getHeight() - 60);
        batch.end();    
    }

    @Override
    public void show() {
        super.show();
        timer.resetTime();
        timer.start();
        gameScene.setShowTimer(false); // Hide timer in GameScene
        gameScene.show();
    }

    @Override
    public void onEntityRemove(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
