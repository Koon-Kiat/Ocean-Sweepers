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
    //method to show the popup and transition to the next scene
    public void showLevelCompletePopup(Scene currentScene) {
        Window popupMenu = new Window("Level Complete", skin);
        popupMenu.setSize(300, 200);
        popupMenu.setPosition(Gdx.graphics.getWidth() / 2 - popupMenu.getWidth() / 2, Gdx.graphics.getHeight() / 2 - popupMenu.getHeight() / 2);
        
        //Create a button to continue to the next game scene
        TextButton continueButton = new TextButton("Next Level", skin);
        continueButton.addListener(event -> {
            if (event.isHandled()) {
                // Transition to the next scene, here you can set the next scene to be "GameScene2" or any other scene
                sceneManager.setScene("game2");  // Change "gameScene2" to the appropriate scene name
                return true;
            }
            return false;
        });
        //Add the continue button to the popup menu
        popupMenu.add(continueButton).fillX().padTop(20);
        popupMenu.row();

        //Add the popup menu to the stage
        currentScene.getSceneUIManager().getStage().addActor(popupMenu);
    }


}
