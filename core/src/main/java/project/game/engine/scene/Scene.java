package project.game.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import project.game.engine.api.scene.IScene;
import project.game.engine.io.SceneIOManager;

/**
 * Abstract class for creating scenes in the game.
 */
public abstract class Scene implements Screen, IScene {
    // protected Stage stage;
    protected SceneUIManager sceneUIManager;
    protected SceneIOManager inputManager;
    protected SceneManager sceneManager;

    public Scene(SceneManager sceneManager, SceneIOManager inputManager) {
        this.sceneManager = sceneManager;
        this.inputManager = inputManager;
        this.sceneUIManager = new SceneUIManager(new ScreenViewport());
        initialize();
    }

    public final void initialize() {
        create();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sceneUIManager.update(delta);
        sceneUIManager.render();
    }

    @Override
    public void show() {
        Gdx.app.log("Scene", "Showing: " + this.getClass().getSimpleName());
        Gdx.input.setInputProcessor(sceneUIManager.getStage());
    }

    @Override
    public void pause() {
        Gdx.app.log("Scene", "Paused: " + this.getClass().getSimpleName());
    }

    @Override
    public void resume() {
        Gdx.app.log("Scene", "Resumed: " + this.getClass().getSimpleName());
    }

    @Override
    public void hide() {
        Gdx.app.log("Scene", "Hidden: " + this.getClass().getSimpleName());
    }

    @Override
    public void dispose() {
        Gdx.app.log("Scene", "Disposed: " + this.getClass().getSimpleName());
        sceneUIManager.dispose();
    }

    @Override
    public void resize(int width, int height) {
        sceneUIManager.resize(width, height);
    }

    @Override
    public void resetScene() {
        create();
    }

    protected abstract void create();

}
