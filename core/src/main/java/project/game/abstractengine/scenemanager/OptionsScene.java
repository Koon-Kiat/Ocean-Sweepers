package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import project.game.abstractengine.iomanager.SceneIOManager;

public class OptionsScene extends Scene {
    
    private Skin skin;
    private Table tableScene, tableRebind;
    private TextButton backButton, returnButton;
    private TextButton rebindUP, rebindDOWN, rebindLEFT, rebindRIGHT;
    private SceneManager sceneManager;
    private final SceneIOManager inputManager;
    private Window rebindWindow;

    public OptionsScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        tableScene = new Table();
        tableRebind = new Table();
        inputManager = new SceneIOManager();
        rebindWindow = new Window("Rebind...", skin);

        // Scene change buttons. Return goes to last scene

        //backButton = new TextButton("BACK", skin);
        returnButton = new TextButton("Return", skin);
        
        // tableScene.add(backButton).bottom().width(100);
        // tableScene.row();
        tableScene.setFillParent(true);
        tableScene.add(returnButton).padBottom(-300);
        tableScene.row();

        // Rebind buttons

        rebindUP = new TextButton("Rebind UP", skin);
        rebindDOWN = new TextButton("Rebind DOWN", skin);
        rebindLEFT = new TextButton("Rebind LEFT", skin);
        rebindRIGHT = new TextButton("Rebind RIGHT", skin);

        tableRebind.setFillParent(true);
        tableRebind.add(rebindUP).fillX().pad(5);
        tableRebind.row();
        tableRebind.add(rebindDOWN).fillX().pad(5);
        tableRebind.row();
        tableRebind.add(rebindLEFT).fillX().pad(5);
        tableRebind.row();
        tableRebind.add(rebindRIGHT).fillX().pad(5);

        stage.addActor(tableScene);
        stage.addActor(tableRebind);

        inputManager.addClickListener(returnButton, () -> {
            sceneManager.setScene("menu");
        });

        rebindWindow.setSize(100,100);
        rebindWindow.setPosition(100,100);


        // For rebinding keys
        inputManager.addClickListener(rebindUP, () -> {

        });

        inputManager.addClickListener(rebindDOWN, () -> {
            
        });

        inputManager.addClickListener(rebindLEFT, () -> {
            
        });

        inputManager.addClickListener(rebindRIGHT, () -> {
            
        });

        

    }

    private void rebindKey() {
        // Implementation of the rebindKey method
        // Onclick of UP/DOWN/LEFT/RIGHT: Window shows up
        // Window has a textfield for input
        // Input saved
        // Output logged
    }

    @Override
    public void create() {
        // Implementation of the create method
    }
    
}
