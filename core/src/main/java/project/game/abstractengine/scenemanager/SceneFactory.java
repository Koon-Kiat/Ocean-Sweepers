package project.game.abstractengine.scenemanager;

import java.util.Map;
import java.util.function.Supplier;

public class SceneFactory {
    private final SceneManager sceneManager;
    private final Map<String, Supplier<Scene>> sceneCreators;

    public SceneFactory(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        sceneCreators = Map.of(
            "menu", () -> new MainMenuScene(sceneManager),
            "game", () -> new GameScene(sceneManager),
            "options", () -> new OptionsScene(sceneManager),
            "gameover", () -> new GameOverScene(sceneManager)
        );
    }

    public void createAndRegisterScenes() {
        sceneCreators.forEach((name, creator) -> {
            Scene scene = creator.get();
            sceneManager.addScene(name, scene);
            System.out.println("Registered scene: " + name);
        });
    }

    public MainMenuScene createMainMenuScene() {
        return new MainMenuScene(sceneManager);
    }

    public GameScene createGameScene() {
        return new GameScene(sceneManager);
    }

    public OptionsScene createOptionsScene() {
        return new OptionsScene(sceneManager);
    }

    public GameOverScene createGameOverScene() {
        return new GameOverScene(sceneManager);
    }
}
