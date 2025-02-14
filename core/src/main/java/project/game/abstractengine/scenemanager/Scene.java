package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class Scene implements Screen {
    protected Stage stage;

    public Scene() {
        stage = new Stage(new ScreenViewport()); // Initialize stage
        create(); // Call abstract method for subclass-specific setup
    }

    protected abstract void create();

    //public abstract void update(float delta);

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(delta); // Update stage
        stage.draw(); // Draw stage elements
    } 

    @Override
    public void show() {
        Gdx.app.log("Scene", "Showing: " + this.getClass().getSimpleName());
        Gdx.input.setInputProcessor(stage); // Set input to this scene's stage
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
}
