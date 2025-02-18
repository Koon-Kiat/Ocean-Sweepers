package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import project.game.abstractengine.iomanager.SceneIOManager;

public class OptionsScene extends Scene {
    
    private Skin skin;
    private Table tableScene, tableRebind;
    private TextButton returnButton;
    private TextButton rebindUP, rebindDOWN, rebindLEFT, rebindRIGHT;
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

        final TextField textField1 = new TextField("", skin);
        textField1.setAlignment(1); // Set alignment to center (1)
        textField1.setMessageText("Press a key...");

        final TextField textField2 = new TextField("", skin);
        textField2.setAlignment(1); // Set alignment to center (1)
        textField2.setMessageText("Press a key...");

        final TextField textField3 = new TextField("", skin);
        textField3.setAlignment(1); // Set alignment to center (1)
        textField3.setMessageText("Press a key...");

        final TextField textField4 = new TextField("", skin);
        textField4.setAlignment(1); // Set alignment to center (1)
        textField4.setMessageText("Press a key...");

        InputListener textFieldListener = new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                TextField textField = (TextField) event.getTarget();
                textField.setText(String.valueOf(Character.toUpperCase(character)));
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                TextField textField = (TextField) event.getTarget();
                if (keycode == Input.Keys.ENTER) {
                    System.out.println("TextField content: " + textField.getText());
                    return true;
                }

                // Handle arrow keys
                if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN ||
                        keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
                    textField.setText(Input.Keys.toString(keycode).toUpperCase());
                    return true;
                }

                return false;
            }
        };

        textField1.addListener(textFieldListener);
        textField2.addListener(textFieldListener);
        textField3.addListener(textFieldListener);
        textField4.addListener(textFieldListener);

        tableRebind.setFillParent(true);
        tableRebind.add(rebindUP).fillX().pad(5);
        tableRebind.add(textField1).width(150).pad(5);
        tableRebind.row();
        tableRebind.add(rebindDOWN).fillX().pad(5);
        tableRebind.add(textField2).width(150).pad(5);
        tableRebind.row();
        tableRebind.add(rebindLEFT).fillX().pad(5);
        tableRebind.add(textField3).width(150).pad(5);
        tableRebind.row();
        tableRebind.add(rebindRIGHT).fillX().pad(5);
        tableRebind.add(textField4).width(150).pad(5);
        tableRebind.row();

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

    @Override
    public void create() {
        // Implementation of the create method
    }
    
}
