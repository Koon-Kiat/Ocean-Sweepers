package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import project.game.abstractengine.iomanager.SceneIOManager;

public class OptionsScene extends Scene {
    
    private Skin skin;
    private Table tableScene;
    private TextButton returnButton;

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
        // Implementation of the create method
    }
    
}
