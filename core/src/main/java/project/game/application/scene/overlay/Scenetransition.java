package project.game.application.scene.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;

public class Scenetransition {
    private SceneManager sceneManager;
    private Skin skin;

    public Scenetransition(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        skin = new Skin(Gdx.files.internal("uiskin.json"));
    }

    // Method to show the popup and transition to the next scene
    public void showLevelCompletePopup(Scene currentScene) {
        // Pause the current scene (freeze game activities)
        pauseCurrentScene(currentScene);

        // Create the level complete popup window
        Window popupMenu = new Window("Level Complete", skin);
        popupMenu.setSize(300, 200);
        popupMenu.setPosition(Gdx.graphics.getWidth() / 2 - popupMenu.getWidth() / 2, Gdx.graphics.getHeight() / 2 - popupMenu.getHeight() / 2);

        // Create a button to continue to the next game scene
        TextButton continueButton = new TextButton("Next Level", skin);
        continueButton.addListener(event -> {
            if (event.isHandled()) {
                // Transition to the next scene, e.g., "game2"
                sceneManager.setScene("game2");
                return true;
            }
            return false;
        });

        // Add the continue button to the popup menu
        popupMenu.add(continueButton).fillX().padTop(20);
        popupMenu.row();

        // Add the popup menu to the stage
        currentScene.getSceneUIManager().getStage().addActor(popupMenu);
    }

    // Method to pause the current scene and stop game logic
    private void pauseCurrentScene(Scene currentScene) {
        if (currentScene != null) {
            // Pause the scene if it is a GameScene
            currentScene.pause();

            // Optionally, disable input and hide UI elements
            currentScene.getSceneUIManager().getStage().getActors().forEach(actor -> actor.setVisible(false));

            // Pause any gameplay logic (for example, player movement, NPCs, etc.)
            // You can stop or pause timers, movements, animations, etc. in your game logic here.
        }
    }
}
