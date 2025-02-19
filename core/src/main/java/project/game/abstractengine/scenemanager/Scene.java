package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import project.game.abstractengine.iomanager.SceneIOManager;

public abstract class Scene extends SceneManager implements Screen {
    protected Stage stage;
    protected SceneIOManager inputManager;
    protected SceneManager sceneManager;

    public Scene(SceneIOManager inputManager) {
        this.inputManager = inputManager;
        this.stage = new Stage(new ScreenViewport());
        this.sceneManager = new SceneManager();
        initialize();
    }

    public final void initialize() {
        create();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.app.log("Scene", "Showing: " + this.getClass().getSimpleName());
        Gdx.input.setInputProcessor(stage);
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
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void resetScene() {
        create();
    }

    protected abstract void create();

}
