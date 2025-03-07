package project.game.application.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import project.game.engine.api.audio.IAudioConfig;
import project.game.engine.api.audio.IAudioUI;
import project.game.engine.audio.AudioManager;

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

        settingsWindow = new Window("Audio Settings", skin);
        settingsWindow.setSize(400, 250);
        settingsWindow.setPosition(stage.getWidth() / 2f - 200, stage.getHeight() / 2f - 125);

        createVolumeControls();
        createSoundToggle();

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

    private void createVolumeControls() {
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

        settingsWindow.add(new Label("Music Volume", skin)).pad(10);
        settingsWindow.row();
        settingsWindow.add(musicSlider).width(300).padBottom(20);
        settingsWindow.row();
    }

    private void createSoundToggle() {
        soundToggle = new CheckBox("Enable Sound Effects", skin);
        soundToggle.setChecked(config.isSoundEnabled());

        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = soundToggle.isChecked();
                config.saveSoundEnabled(enabled);
                audioManager.setSoundEnabled(enabled);
            }
        });

        settingsWindow.add(soundToggle).padTop(20);
    }
}
