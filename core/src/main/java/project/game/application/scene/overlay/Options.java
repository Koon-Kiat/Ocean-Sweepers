package project.game.application.scene.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import project.game.application.scene.main.GameScene;
import project.game.common.logging.core.GameLogger;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;

@SuppressWarnings("unused")
public class Options extends Scene {

    private static final GameLogger LOGGER = new GameLogger(Options.class);
    private final GameScene gameScene;
    private Skin skin;
    private Window popupMenu;
    private Window rebindMenu;
    private boolean isPaused = true;

    // Button to control main menu visibility.
    private TextButton mainMenuButton;

    public Options(SceneManager sceneManager, GameScene gameScene, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.gameScene = gameScene;
    }

    public Window getPopupMenu() {
        return popupMenu;
    }

    public Window getRebindMenu() {
        return rebindMenu;
    }

    public void setMainMenuButtonVisibility(boolean isVisible) {
        mainMenuButton.setVisible(isVisible);
    }

    public Stage getStage() {
        return sceneUIManager.getStage();
    }

    public void setPopupMenu(Window popupMenu) {
        this.popupMenu = popupMenu;
    }

    public void render() {
        sceneUIManager.getStage().act();
        sceneUIManager.getStage().draw();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Constructs an Options scene.
     */
    @Override
    public void create() {
        LOGGER.info("Options inputManager instance: {0}", System.identityHashCode(inputManager));

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        BitmapFont customFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));

        // Create a custom window style with the new font
        Window.WindowStyle customWindowStyle = new Window.WindowStyle(
                skin.get(Window.WindowStyle.class).titleFont,
                Color.WHITE,
                skin.get(Window.WindowStyle.class).background);

        // Create a custom button style with the new font
        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle(
                skin.get(TextButton.TextButtonStyle.class).up,
                skin.get(TextButton.TextButtonStyle.class).down,
                skin.get(TextButton.TextButtonStyle.class).checked,
                customFont);

        // Create a custom label style with the new font
        Label.LabelStyle customLabelStyle = new Label.LabelStyle(
                customFont, Color.WHITE);

        popupMenu = new Window("Options", skin);
        popupMenu.setSize(200, 100);
        popupMenu.setPosition(400, 270);
        popupMenu.setVisible(false);

        // Configure the popup menu.
        popupMenu.setModal(true);
        popupMenu.setMovable(false);
        popupMenu.setKeepWithinStage(true);

        // Log touch events on the popup menu.
        inputManager.addWindowTouchDownListener(popupMenu, (event, x, y, pointer, button) -> {
            LOGGER.info("Popup menu touched at ({0}, {1})", new Object[] { x, y });
        });

        // Create buttons with custom style
        TextButton rebindButton = new TextButton("REBIND KEYS", skin);
        rebindButton.setStyle(customButtonStyle);
        mainMenuButton = new TextButton("MAIN MENU", skin);
        mainMenuButton.setStyle(customButtonStyle);

        inputManager.addButtonClickListener(rebindButton, () -> {
            LOGGER.info("Rebind keys selected");
            popupMenu.setVisible(false);
            rebindMenu.setVisible(true);
        });

        inputManager.addButtonClickListener(mainMenuButton, () -> {
            LOGGER.info("Return to main menu selected");
            popupMenu.setVisible(false);
        });

        Table table = new Table();
        table.add(rebindButton).fillX().pad(5);
        popupMenu.add(table);
        sceneUIManager.getStage().addActor(popupMenu);

        // Rebind Menu Creation
        rebindMenu = new Window("REBIND", skin);
        rebindMenu.getTitleLabel().setStyle(customLabelStyle);

        rebindMenu.setSize(600, 400);
        // Calculate center position based on screen dimensions
        float centerX = Gdx.graphics.getWidth() / 2f - rebindMenu.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f - rebindMenu.getHeight() / 2f;

        // Set position to center
        rebindMenu.setPosition(centerX, centerY);
        rebindMenu.setVisible(false);

        // Ensure that the popup menu blocks input to other UI elements
        rebindMenu.setModal(true);

        // Ensure that the popup menu cannot be moved
        rebindMenu.setMovable(true);

        // Ensure that the popup menu stays within the bounds of the stage
        rebindMenu.setKeepWithinStage(true);

        inputManager.addButtonClickListener(rebindMenu, () -> {
            LOGGER.info("Window clicked!");
        });

        // Debug log for popup menu touch event
        inputManager.addWindowTouchDownListener(rebindMenu, (event, x, y, pointer, button) -> {
        });

        TextButton rebindButton1 = new TextButton("Up", skin);
        TextButton rebindButton2 = new TextButton("Down", skin);
        TextButton rebindButton3 = new TextButton("Left", skin);
        TextButton rebindButton4 = new TextButton("Right", skin);
        TextButton confirmButton = new TextButton("Confirm", skin);

        // Apply the custom style to all other buttons
        rebindButton1.setStyle(customButtonStyle);
        rebindButton2.setStyle(customButtonStyle);
        rebindButton3.setStyle(customButtonStyle);
        rebindButton4.setStyle(customButtonStyle);
        confirmButton.setStyle(customButtonStyle);

        TextField.TextFieldStyle customTextFieldStyle = new TextField.TextFieldStyle(
                customFont,
                Color.WHITE,
                skin.get(TextField.TextFieldStyle.class).cursor,
                skin.get(TextField.TextFieldStyle.class).selection,
                skin.get(TextField.TextFieldStyle.class).background);

        final TextField textField1 = new TextField("", skin);
        textField1.setAlignment(1);
        textField1.setMessageText("Press a key...");

        final TextField textField2 = new TextField("", skin);
        textField2.setAlignment(1);
        textField2.setMessageText("Press a key...");

        final TextField textField3 = new TextField("", skin);
        textField3.setAlignment(1);
        textField3.setMessageText("Press a key...");

        final TextField textField4 = new TextField("", skin);
        textField4.setAlignment(1);
        textField4.setMessageText("Press a key...");

        textField1.setStyle(customTextFieldStyle);
        textField2.setStyle(customTextFieldStyle);
        textField3.setStyle(customTextFieldStyle);
        textField4.setStyle(customTextFieldStyle);

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

        // Close rebind menu
        inputManager.addButtonClickListener(confirmButton, () -> {
            String upKeyString = textField1.getText().toUpperCase();
            String downKeyString = textField2.getText().toUpperCase();
            String leftKeyString = textField3.getText().toUpperCase();
            String rightKeyString = textField4.getText().toUpperCase();

            if (upKeyString.isEmpty() || downKeyString.isEmpty() ||
                    leftKeyString.isEmpty() || rightKeyString.isEmpty()) {
                LOGGER.warn("No keys set");
                return;
            }
            inputManager.promptForKeyBindings(upKeyString, downKeyString, leftKeyString, rightKeyString);
        });

        Table rebindTable = new Table();
        rebindTable.add(rebindButton1).fillX().pad(5);
        rebindTable.add(textField1).width(300).pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton2).fillX().pad(5);
        rebindTable.add(textField2).width(300).pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton3).fillX().pad(5);
        rebindTable.add(textField3).width(300).pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton4).fillX().pad(5);
        rebindTable.add(textField4).width(300).pad(5);
        rebindTable.row();
        rebindTable.add(confirmButton).colspan(2).center().pad(5);

        Label exitHintLabel = new Label("Hit 'P' again to exit this menu", skin);
        rebindTable.row();
        rebindTable.add(exitHintLabel).colspan(2).center().pad(5);
        // Apply custom style to exit hint label
        exitHintLabel.setStyle(customLabelStyle);
        rebindMenu.add(rebindTable);
        sceneUIManager.getStage().addActor(rebindMenu);
    }
}
