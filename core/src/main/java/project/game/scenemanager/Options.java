package project.game.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Options extends Scene{
    private Skin skin;
    private Stage stage;
    private Window popupMenu;
    private SceneManager sceneManager;
    private Window rebindMenu;
    private TextButton mainMenuButton; // Added to make main menu button visible/invisible. Main menu button not needed in the main menu, only in other scenes like game scene or fail state scene.
    private boolean isPaused = true;

    public Options(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void create() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();
        popupMenu = new Window("Options", skin);
        popupMenu.setSize(200, 200);
        popupMenu.setPosition(400, 270);

        popupMenu.setVisible(false);
        popupMenu.setModal(true); // Ensure that the popup menu blocks input to other UI elements
        popupMenu.setMovable(false); // Ensure that the popup menu cannot be moved
        popupMenu.setKeepWithinStage(true); // Ensure that the popup menu stays within the bounds of the stage

        // Prevent popup from dismissing when clicked outside
        popupMenu.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        TextButton rebindButton = new TextButton("Rebind Keys", skin);
        mainMenuButton = new TextButton("Main Menu", skin);
        TextButton closeButton = new TextButton("Close", skin);

        // Rebind Keys
        rebindButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("'Rebind keys' selected");
                popupMenu.setVisible(false);
                rebindMenu.setVisible(true);
            }
        });

        // Main Menu
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("'Return to main menu' selected");
                popupMenu.setVisible(false);
                sceneManager.setScene("mainMenu");
            }
        });

        // Close Button

        // Issue: Closing in game scene does not unpause game
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("'Close' selected");
                setPaused(false);
                popupMenu.setVisible(false);
            }
        });

        Table table = new Table();
        table.add(rebindButton).fillX().pad(5);
        table.row();
        table.add(mainMenuButton).fillX().pad(5);
        table.row();
        table.add(closeButton).fillX().pad(5);

        popupMenu.add(table);

        stage.addActor(popupMenu);

        // Rebind Menu Creation

        rebindMenu = new Window("Rebind", skin);
        rebindMenu.setSize(200, 200);
        rebindMenu.setPosition(400, 270);
        rebindMenu.setVisible(false);
        rebindMenu.setModal(true); // Ensure that the popup menu blocks input to other UI elements
        rebindMenu.setMovable(false); // Ensure that the popup menu cannot be moved
        rebindMenu.setKeepWithinStage(true); // Ensure that the popup menu stays within the bounds of the stage

        rebindMenu.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }
        });


        TextButton rebindButton1 = new TextButton("Up", skin);
        TextButton rebindButton2 = new TextButton("Down", skin);
        TextButton rebindButton3 = new TextButton("Left", skin);
        TextButton rebindButton4 = new TextButton("Right", skin);
        TextButton rebindButton5 = new TextButton("Return", skin);

        // IO integration here ***

        //

        // Close rebind menu
        rebindButton5.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("'Return' selected");
                rebindMenu.setVisible(false);
                popupMenu.setVisible(true);
            }
        });

        Table rebindTable = new Table();
        rebindTable.add(rebindButton1).fillX().pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton2).fillX().pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton3).fillX().pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton4).fillX().pad(5);
        rebindTable.row();
        rebindTable.add(rebindButton5).fillX().pad(5);

        rebindMenu.add(rebindTable);

        stage.addActor(rebindMenu);

    }

    public Window getPopupMenu() {
        return popupMenu;
    }

    public Window getRebindMenu() {
        return rebindMenu;
    }

    public void setMainMenuButtonVisibility (boolean isVisible){
        mainMenuButton.setVisible(isVisible);
    }

    public Stage getStage() {
        return stage;
    }

    public void setPopupMenu(Window popupMenu) {
        this.popupMenu = popupMenu;
    }

    public void render() {
        stage.act();
        stage.draw();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
}
