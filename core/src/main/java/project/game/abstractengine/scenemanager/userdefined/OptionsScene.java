package project.game.abstractengine.scenemanager.userdefined;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;

public class OptionsScene extends Scene {

    private final Skin skin;
    private final Table tableScene;
    private final TextButton returnButton;

    /**
     * @brief Constructor for the OptionsScene class.
     * 
     *        Initializes and draws the Options Scene "return" button moves to the
     *        main menu scene Main Menu --> Options --> Main Menu
     */
    public OptionsScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Audio management can use this table
        tableScene = new Table();
        inputManager = new SceneIOManager();

        // Scene change buttons. Return goes to last scene
        returnButton = new TextButton("Return", skin);

        tableScene.setFillParent(true);
        tableScene.add(returnButton).padBottom(10);
        tableScene.row();

        stage.addActor(tableScene);

        inputManager.addButtonClickListener(returnButton, () -> {
            sceneManager.setScene("menu");
        });
    }

    @Override
    public void create() {
    }

}
