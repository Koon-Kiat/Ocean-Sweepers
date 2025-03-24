package project.game.application.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import project.game.engine.audio.api.IAudioConfig;
import project.game.engine.audio.api.IAudioUI;
import project.game.engine.audio.management.AudioManager;

public class AudioUI implements IAudioUI {

    private final AudioManager audioManager;
    private final IAudioConfig config;
    private final Skin skin;
    private final Stage stage;
    private final Window settingsWindow;
    private Slider musicSlider;
    private CheckBox soundToggle;

    public AudioUI(AudioManager audioManager, IAudioConfig config, Stage stage, Skin skin) {
        this.audioManager = audioManager;
        this.config = config;
        this.stage = stage;
        this.skin = skin;

        // Load the same custom font used in MainMenuScene
        BitmapFont customFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));

        // Create custom window style with the upheaval font
        Window.WindowStyle customWindowStyle = new Window.WindowStyle(
                customFont,
                Color.WHITE,
                skin.get(Window.WindowStyle.class).background);

        // Create custom label style
        Label.LabelStyle customLabelStyle = new Label.LabelStyle(customFont, Color.WHITE);

        // Create custom checkbox style
        CheckBox.CheckBoxStyle customCheckBoxStyle = new CheckBox.CheckBoxStyle(
                skin.get(CheckBox.CheckBoxStyle.class).checkboxOff,
                skin.get(CheckBox.CheckBoxStyle.class).checkboxOn,
                customFont,
                Color.WHITE);

        // Create window with custom style
        settingsWindow = new Window("AUDIO SETTINGS", customWindowStyle);
        settingsWindow.setSize(600, 250);
        settingsWindow.setPosition(stage.getWidth() / 2f - 200, stage.getHeight() / 2f - 125);

        // Pass the custom styles to these methods
        createVolumeControls(customLabelStyle);
        createSoundToggle(customCheckBoxStyle, customLabelStyle);

        stage.addActor(settingsWindow);
        settingsWindow.setVisible(false);
    }

    public void restoreUIInteractivity() {
        musicSlider.setValue(config.getMusicVolume());
        soundToggle.setChecked(config.isSoundEnabled());
    }

    @Override
    public void showSettings() {
        settingsWindow.setVisible(true);
    }

    @Override
    public void hideSettings() {
        settingsWindow.setVisible(false);
    }

    // Update these methods to accept style parameters
    private void createVolumeControls(Label.LabelStyle labelStyle) {
        // Keep using the skin for the slider as requested
        musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(config.getMusicVolume());

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = musicSlider.getValue();
                audioManager.setMusicVolume(volume);
                config.saveMusicVolume(volume);
            }
        });

        // Use custom label style here
        settingsWindow.add(new Label("MUSIC VOLUME", labelStyle)).pad(10);
        settingsWindow.row();
        settingsWindow.add(musicSlider).width(300).padBottom(20);
        settingsWindow.row();
    }

    private void createSoundToggle(CheckBox.CheckBoxStyle checkBoxStyle, Label.LabelStyle labelStyle) {
        // Create a table to hold checkbox and label with custom spacing
        Table checkboxRow = new Table();

        // Create the checkbox without text
        CheckBox checkbox = new CheckBox("", checkBoxStyle);
        checkbox.setChecked(config.isSoundEnabled());

        // Create separate label
        Label label = new Label("ENABLE SOUND EFFECTS", labelStyle);

        // Add both to table with desired spacing
        checkboxRow.add(checkbox).padRight(20); 
        checkboxRow.add(label);

        // Add listener to checkbox
        checkbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = checkbox.isChecked();
                config.saveSoundEnabled(enabled);
                audioManager.setSoundEnabled(enabled);
            }
        });

        // Add the custom row to settings window
        settingsWindow.add(checkboxRow).padTop(50);

        // Store reference to checkbox
        soundToggle = checkbox;
    }

    public Table createAudioSettingsTable() {
        Table table = new Table();

        Label musicLabel = new Label("MUSIC VOLUME", skin);
        musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(config.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = musicSlider.getValue();
                audioManager.setMusicVolume(volume);
                config.saveMusicVolume(volume);
            }
        });

        Label soundLabel = new Label("ENABLE SOUND EFFECTS", skin);
        soundToggle = new CheckBox("", skin);
        soundToggle.setChecked(config.isSoundEnabled());
        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = soundToggle.isChecked();
                audioManager.setSoundEnabled(enabled);
                config.saveSoundEnabled(enabled);
            }
        });

        // Organize UI elements
        table.add(musicLabel).top().pad(5);
        table.row();
        table.add(musicSlider).width(300).padBottom(10);
        table.row();
        table.add(soundLabel).left().pad(5);
        table.add(soundToggle).padBottom(10);

        return table;
    }
}
