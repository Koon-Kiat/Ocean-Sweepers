package project.game.userdefined;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;

/**
 * The OptionsScene class represents the game scene where users can adjust
 * options.
 */
public class OptionsScene extends Scene {

    private final Skin skin;
    private final Table tableScene;
    private final TextButton returnButton;

    public OptionsScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(sceneManager, inputManager);
        //this.sceneManager = sceneManager;

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Audio management can use this table
        tableScene = new Table();

        // Create the "Return" button for scene navigation
        returnButton = new TextButton("Return", skin);

        tableScene.setFillParent(true);
        tableScene.add(returnButton).padBottom(10);
        tableScene.row();

        sceneUIManager.getStage().addActor(tableScene);

        inputManager.addButtonClickListener(returnButton, () -> {
            sceneManager.setScene("menu");
        });
    }

    @Override
    public void create() {
    }

}
