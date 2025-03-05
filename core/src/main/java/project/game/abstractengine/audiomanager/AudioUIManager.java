package project.game.abstractengine.audiomanager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import project.game.abstractengine.interfaces.IAudioConfig;
import project.game.abstractengine.interfaces.IAudioUI;

public class AudioUIManager implements IAudioUI {
    private final AudioManager audioManager;
    private final IAudioConfig config;
    private final Skin skin;
    private final Stage stage;
    private final Window settingsWindow;

    // UI Components
    private Slider musicSlider;
    private CheckBox soundToggle;

    public AudioUIManager(AudioManager audioManager, IAudioConfig config, Stage stage) {
        this.audioManager = audioManager;
        this.config = config;
        this.stage = stage;
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Create UI components
        settingsWindow = new Window("Audio Settings", skin);
        settingsWindow.setSize(400, 250);  // Adjusted height to fit new layout
        settingsWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 125);

        createVolumeControls();
        createSoundToggle();

        stage.addActor(settingsWindow);
        settingsWindow.setVisible(false);  // Initially hidden
    }

    // Create music volume slider
    private void createVolumeControls() {
        musicSlider = new Slider(0f, 1f, 0.01f, false, skin);

        // Get saved volume or default to 0.5 if none exists
        float savedVolume = config.getMusicVolume();
        System.out.println("Saved volume from config: " + savedVolume);

        // Only default to 0.5 if the saved volume is negative or not set
        if (savedVolume < 0f) {  
            savedVolume = 0.5f;
            System.out.println("Saved volume is negative or not set, defaulting to 0.5");
        }

// Set the slider to the saved volume directly
musicSlider.setValue(savedVolume);
System.out.println("Slider set to saved volume: " + savedVolume);

        musicSlider.setValue(savedVolume);

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = musicSlider.getValue();
                System.out.println("Music Slider Moved: " + volume);

                if (audioManager != null) {
                    audioManager.setMusicVolume(volume);      // Apply volume immediately
                    config.saveMusicVolume(volume);           // Save volume to config
                } else {
                    System.out.println("AudioManager is null!");
                }
            }
        });

        settingsWindow.add(new Label("Music Volume", skin)).pad(10);
        settingsWindow.row();
        settingsWindow.add(musicSlider).width(300).padBottom(20);
        settingsWindow.row();
    }

    // Create sound toggle checkbox
    private void createSoundToggle() {
        soundToggle = new CheckBox("Enable Sound Effects", skin);
        soundToggle.setChecked(config.isSoundEnabled());
        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = soundToggle.isChecked();
                config.saveSoundEnabled(enabled);  // Save the toggle state to config
                if (audioManager != null) {
                    audioManager.setSoundEnabled(enabled);  // Enable/Disable sound effects
                } else {
                    System.out.println("AudioManager is null!");
                }
            }
        });
    
        settingsWindow.add(soundToggle).padTop(20);
    }

    public void restoreUIInteractivity() {
        musicSlider.setTouchable(Touchable.enabled);
        soundToggle.setTouchable(Touchable.enabled);
        settingsWindow.setTouchable(Touchable.enabled);
        System.out.println("Audio settings UI re-enabled for interaction.");
    }


    @Override
    public void showSettings() {
        if (audioManager != null) {
            // Fetch saved volume from config
            float savedVolume = config.getMusicVolume();
            System.out.println("Fetched saved volume from config: " + savedVolume);

            // Set the slider to match the saved volume directly
            musicSlider.setValue(savedVolume);
            System.out.println("Slider set to saved volume: " + savedVolume);

            // Ensure sound toggle reflects saved state
            soundToggle.setChecked(config.isSoundEnabled());

            restoreUIInteractivity();
        } else {
            System.out.println("AudioManager is null!");
        }

        settingsWindow.setVisible(true);
        Gdx.input.setInputProcessor(stage);
    }


    @Override
    public void hideSettings() {
        settingsWindow.setVisible(false);

    if (audioManager != null) {
        float savedMusicVolume = config.getMusicVolume();

        System.out.println("Applying saved music volume: " + savedMusicVolume);

        audioManager.setMusicVolume(savedMusicVolume);
        audioManager.setSoundEnabled(config.isSoundEnabled());

        // // Check if music is not playing, start it
        // if (!MusicManager.getInstance().isPlaying("BackgroundMusic")) {
        //     audioManager.playMusic("BackgroundMusic");
        // }
    } else {
        System.out.println("AudioManager is null when closing settings!");
    }
}
}
