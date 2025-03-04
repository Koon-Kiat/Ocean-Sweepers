package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SceneUIManager {
    private Stage stage;

    public SceneUIManager(Viewport viewport) {
        this.stage = new Stage(viewport);
    }

    public Stage getStage() {
        return stage;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    } 
}
