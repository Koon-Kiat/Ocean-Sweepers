package project.game.engine.api.scene;

public interface IScene {

    void render(float delta);

    void show();

    void hide();

    void dispose();

    void resize(int width, int height);

    void resetScene();

}