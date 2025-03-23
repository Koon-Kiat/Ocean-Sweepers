package project.game.application.scene.main;

import com.badlogic.gdx.graphics.Texture;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;

public class GameScene2 extends Scene implements IEntityRemovalListener {

    private static final GameLogger LOGGER = new GameLogger(GameScene2.class);
    private HealthManager healthManager;
    private ScoreManager scoreManager;

    private final GameScene gameScene;
    private Texture heartTexture;

    public GameScene2(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.gameScene = new GameScene(sceneManager, inputManager);
        this.healthManager = HealthManager.getInstance(heartTexture);
        this.scoreManager = ScoreManager.getInstance();
        LOGGER.info("GameScene2 created with composition of GameScene");
    }

    @Override
    public void create() {
        if (gameScene != null) {
            gameScene.create();
        }
    }

    @Override
    public void render(float deltaTime) {
        gameScene.render(deltaTime);
    }

    @Override
    public void show() {
        if (gameScene != null) {
            gameScene.show();
            LOGGER.info("GameScene2 shown (delegated to GameScene)");
        }
    }

    @Override
    public void hide() {
        if (gameScene != null) {
            gameScene.hide();
        }
    }

    @Override
    public void pause() {
        if (gameScene != null) {
            gameScene.pause();
        }
    }

    @Override
    public void resume() {
        if (gameScene != null) {
            gameScene.resume();
        }
    }

    @Override
    public void dispose() {
        if (gameScene != null) {
            gameScene.dispose();
        }

        LOGGER.info("GameScene2 disposed");
    }

    @Override
    public void onEntityRemove(Entity entity) {
        if (gameScene != null) {
            gameScene.onEntityRemove(entity);
        }
    }
}
