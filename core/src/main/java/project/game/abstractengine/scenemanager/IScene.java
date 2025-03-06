package project.game.abstractengine.scenemanager;

public interface IScene {
    void resetScene();
    void show();
    void hide();
    void render(float delta);
    void resize(int width, int height);
    void dispose();
}