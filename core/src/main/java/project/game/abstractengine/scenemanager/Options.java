// package project.game.abstractengine.scenemanager;

// import java.util.HashMap;

// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.Input;
// import com.badlogic.gdx.scenes.scene2d.InputEvent;
// import com.badlogic.gdx.scenes.scene2d.InputListener;
// import com.badlogic.gdx.scenes.scene2d.Stage;
// import com.badlogic.gdx.scenes.scene2d.ui.Skin;
// import com.badlogic.gdx.scenes.scene2d.ui.Table;
// import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
// import com.badlogic.gdx.scenes.scene2d.ui.TextField;
// import com.badlogic.gdx.scenes.scene2d.ui.Window;
// import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import project.game.Direction;
import project.game.abstractengine.iomanager.SceneIOManager;

public class Options extends Scene {
    private Skin skin;
    private Stage stage;
    private Window popupMenu;
    private SceneManager sceneManager;
    private Window rebindMenu;
    private TextButton mainMenuButton; // Added to make main menu button visible/invisible. Main menu button not needed
                                       // in the main menu, only in other scenes like game scene or fail state scene.
    private boolean isPaused = true;
    private GameScene gameScene;

    public Options(SceneManager sceneManager, GameScene gameScene, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
        this.gameScene = gameScene;
    }

    public void create() {
        System.out.println("[DEBUG] Options inputManager instance: " + System.identityHashCode(inputManager));
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();
        popupMenu = new Window("Options", skin);
        popupMenu.setSize(200, 200);
        popupMenu.setPosition(400, 270);

//         popupMenu.setVisible(false);
//         popupMenu.setModal(true); // Ensure that the popup menu blocks input to other UI elements
//         popupMenu.setMovable(false); // Ensure that the popup menu cannot be moved
//         popupMenu.setKeepWithinStage(true); // Ensure that the popup menu stays within the bounds of the stage

//         // Debug log for popup menu touch event
//         popupMenu.addListener(new ClickListener() {
//             @Override
//             public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                 System.out.println("[DEBUG] PopupMenu touched at (" + x + ", " + y + ")");
//                 event.stop();
//                 return super.touchDown(event, x, y, pointer, button);
//             }
//         });

//         TextButton rebindButton = new TextButton("Rebind Keys", skin);
//         mainMenuButton = new TextButton("Main Menu", skin);
//         TextButton closeButton = new TextButton("Close", skin);

//         inputManager.addClickListener(rebindButton, () -> {
//             System.out.println("'Rebind keys' selected");
//             popupMenu.setVisible(false);
//             rebindMenu.setVisible(true);
//         });

//         inputManager.addClickListener(mainMenuButton, () -> {
//             System.out.println("'Return to main menu' selected");
//             popupMenu.setVisible(false);
//         });

//         inputManager.addClickListener(closeButton, () -> {
//             System.out.println("'Close' selected");
//             // setPaused(false);
//             // ((GameScene) sceneManager.getCurrentScene()).closePopupMenu();
//             gameScene.closePopupMenu();
//             // popupMenu.setVisible(false);
//         });

//         Table table = new Table();
//         table.add(rebindButton).fillX().pad(5);
//         table.row();
//         table.add(mainMenuButton).fillX().pad(5);
//         table.row();
//         table.add(closeButton).fillX().pad(5);

//         popupMenu.add(table);

//         stage.addActor(popupMenu);

//         // Rebind Menu Creation

//         rebindMenu = new Window("Rebind", skin);
//         rebindMenu.setSize(400, 400);
//         rebindMenu.setPosition(400, 270);
//         rebindMenu.setVisible(false);
//         rebindMenu.setModal(true); // Ensure that the popup menu blocks input to other UI elements
//         rebindMenu.setMovable(true); // Ensure that the popup menu cannot be moved
//         rebindMenu.setKeepWithinStage(true); // Ensure that the popup menu stays within the bounds of the stage

//         rebindMenu.addListener(new ClickListener() {
//             @Override
//             public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                 event.stop();
//                 return super.touchDown(event, x, y, pointer, button);
//             }
//         });

//         TextButton rebindButton1 = new TextButton("Up", skin);
//         TextButton rebindButton2 = new TextButton("Down", skin);
//         TextButton rebindButton3 = new TextButton("Left", skin);
//         TextButton rebindButton4 = new TextButton("Right", skin);
//         TextButton returnButton = new TextButton("Return", skin);
//         TextButton confirmButton = new TextButton("Confirm", skin);

//         final TextField textField1 = new TextField("", skin);
//         textField1.setAlignment(1); // Set alignment to center (1)
//         textField1.setMessageText("Press a key...");

//         final TextField textField2 = new TextField("", skin);
//         textField2.setAlignment(1); // Set alignment to center (1)
//         textField2.setMessageText("Press a key...");

//         final TextField textField3 = new TextField("", skin);
//         textField3.setAlignment(1); // Set alignment to center (1)
//         textField3.setMessageText("Press a key...");

//         final TextField textField4 = new TextField("", skin);
//         textField4.setAlignment(1); // Set alignment to center (1)
//         textField4.setMessageText("Press a key...");

//         InputListener textFieldListener = new InputListener() {
//             @Override
//             public boolean keyTyped(InputEvent event, char character) {
//                 TextField textField = (TextField) event.getTarget();
//                 textField.setText(String.valueOf(Character.toUpperCase(character)));
//                 return true;
//             }

//             @Override
//             public boolean keyDown(InputEvent event, int keycode) {
//                 TextField textField = (TextField) event.getTarget();
//                 if (keycode == Input.Keys.ENTER) {
//                     System.out.println("TextField content: " + textField.getText());
//                     return true;
//                 }

//                 // Handle arrow keys
//                 if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN ||
//                         keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
//                     textField.setText(Input.Keys.toString(keycode).toUpperCase());
//                     return true;
//                 }

//                 return false;
//             }
//         };

//         textField1.addListener(textFieldListener);
//         textField2.addListener(textFieldListener);
//         textField3.addListener(textFieldListener);
//         textField4.addListener(textFieldListener);

//         // IO integration here ***

//         //

//         // Close rebind menu
//         inputManager.addClickListener(returnButton, () -> {
//             System.out.println("'Return' selected");
//             rebindMenu.setVisible(false);
//             popupMenu.setVisible(true);
//         });

        inputManager.addClickListener(confirmButton, () -> {
            String upKeyString = textField1.getText().toUpperCase();
            String downKeyString = textField2.getText().toUpperCase();
            String leftKeyString = textField3.getText().toUpperCase();
            String rightKeyString = textField4.getText().toUpperCase();

            if (upKeyString.isEmpty() || downKeyString.isEmpty() ||
                    leftKeyString.isEmpty() || rightKeyString.isEmpty()) {
                System.out.println("[DEBUG] No keys set");
                return;
            }

            // Call promptForKeyBindings with the key strings
            inputManager.promptForKeyBindings(upKeyString, downKeyString, leftKeyString, rightKeyString);

            rebindMenu.setVisible(false);
            popupMenu.setVisible(true);
        });

//         Table rebindTable = new Table();
//         rebindTable.add(rebindButton1).fillX().pad(5);
//         rebindTable.add(textField1).width(150).pad(5);
//         rebindTable.row();
//         rebindTable.add(rebindButton2).fillX().pad(5);
//         rebindTable.add(textField2).width(150).pad(5);
//         rebindTable.row();
//         rebindTable.add(rebindButton3).fillX().pad(5);
//         rebindTable.add(textField3).width(150).pad(5);
//         rebindTable.row();
//         rebindTable.add(rebindButton4).fillX().pad(5);
//         rebindTable.add(textField4).width(150).pad(5);
//         rebindTable.row();
//         rebindTable.add(returnButton).fillX().pad(5);
//         rebindTable.add(confirmButton).fillX().pad(5);

//         rebindMenu.add(rebindTable);

//         stage.addActor(rebindMenu);
//     }

//     public Window getPopupMenu() {
//         return popupMenu;
//     }

//     public Window getRebindMenu() {
//         return rebindMenu;
//     }

//     public void setMainMenuButtonVisibility(boolean isVisible) {
//         mainMenuButton.setVisible(isVisible);
//     }

//     public Stage getStage() {
//         return stage;
//     }

//     public void setPopupMenu(Window popupMenu) {
//         this.popupMenu = popupMenu;
//     }

//     public void render() {
//         stage.act();
//         stage.draw();
//     }

//     public boolean isPaused() {
//         return isPaused;
//     }

//     public void setPaused(boolean paused) {
//         this.isPaused = paused;
//     }

// }
