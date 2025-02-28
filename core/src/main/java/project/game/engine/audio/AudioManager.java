package project.game.engine.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * AudioManager is a singleton class that manages the audio settings and
 * playback of music and sound effects in the game.
 * 
 * It uses LibGDX's audio classes to load and play music and sound effects.
 */
public class AudioManager {
    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());
    private float setsoundVolume = 0.2f;
    private float setmusicVolume = 0.1f;
    private boolean isSoundEnabled = true;
    private final boolean musicEnable = true;
    private final Map<String, Sound> soundEffects;
    private final Map<String, Music> musicTrack;

    // UI Components
    private final Stage stage;
    private final Skin skin;
    private Slider musicSlider;
    private Label musicLabel;
    private Label soundLabel;
    private Window volumeWindow;
    private static volatile AudioManager instance;
    public boolean isPaused = false;

    public AudioManager(Stage stage) {
        this.stage = stage;

        // Create the background music
        soundEffects = new HashMap<>();
        musicTrack = new HashMap<>();

        Preferences prefs = Gdx.app.getPreferences("GameSettings");
        isSoundEnabled = prefs.getBoolean("isSoundEnabled", true);
        setsoundVolume = isSoundEnabled ? 0.2f : 0f;

        loadSoundEffects();
        loadMusicTracks();

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        createVolumeControls();
    }

    @SuppressWarnings("DoubleCheckedLocking")
    public static AudioManager getInstance(Stage stage) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    // Double-checked locking with volatile instance
                    instance = new AudioManager(stage);
                }
            }
        }
        return instance;
    }

    private void loadMusicTracks() {
        // Create an array of music tracks can add on more songs here
        String[] tracks = { "BackgroundMusic.mp3" };

        for (String track : tracks) {
            // Remove the file extension from the track name
            String trackName = track.replace(".mp3", "");
            if (!Gdx.files.internal(track).exists()) {
                LOGGER.log(Level.WARNING, "Failed to Load Music: {0}", track);
                continue;
            }

            // Load the music track
            Music music = Gdx.audio.newMusic(Gdx.files.internal(track));

            if (music == null) {
                LOGGER.log(Level.WARNING, "Failed to Load Music: {0}", track);
            } else {
                LOGGER.log(Level.INFO, "Loaded Music: {0}", track);
                music.setLooping(true);
                music.setVolume(setmusicVolume);
                musicTrack.put(trackName, music);
            }
        }
    }

    private void loadSoundEffects() {
        // Create an array of sound effect names
        String[] soundNames = { "Boinkeffect.mp3", "Selection.mp3", "Watercollision.mp3" };

        // Create an array of keys for the sound effects
        String[] keys = { "keybuttons", "selection", "drophit" };

        for (int i = 0; i < soundNames.length; i++) {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(soundNames[i]));

            if (sound == null) {
                LOGGER.log(Level.WARNING, "Failed to Load Sound Effect: {0}", soundNames[i]);
            } else {
                LOGGER.log(Level.INFO, "Loaded Sound Effect: {0}", soundNames[i]);
                soundEffects.put(keys[i], sound);
            }
        }
    }

    private void toggleSoundEffects(boolean isEnabled) {
        isSoundEnabled = isEnabled;
        setsoundVolume = isSoundEnabled ? 0.2f : 0f;

        if (!isSoundEnabled) {
            stopAllSounds();
        }
        Preferences prefs = Gdx.app.getPreferences("AudioSettings");
        prefs.putBoolean("isSoundEnabled", isSoundEnabled);
        prefs.flush();
        LOGGER.log(Level.INFO, "Sound Effects Enabled: {0} | Volume: {1}",
                new Object[] { isSoundEnabled, setsoundVolume });
    }

    public void playMusic(String trackName) {
        Music music = musicTrack.get(trackName);
        if (music != null && musicEnable) {
            music.setVolume(setmusicVolume);
            music.play();
        }
    }

    public void playSoundEffect(String soundEffect) {
        if (!isSoundEnabled || setsoundVolume == 0) {
            return;
        }
        Sound sound = soundEffects.get(soundEffect);
        if (sound != null) {
            long soundId = sound.play();
            sound.setVolume(soundId, setsoundVolume);
        }
    }

    private void stopAllSounds() {
        for (Sound sound : soundEffects.values()) {
            sound.stop();
        }
    }

    public void stopMusic() {
        for (Music music : musicTrack.values()) {
            music.stop();
        }
    }

    public void setmusicVolume(float musicvolume) {
        this.setmusicVolume = musicvolume;
        for (Music music : musicTrack.values()) {
            music.setVolume(setmusicVolume);
        }
        LOGGER.log(Level.INFO, "Updated Music Volume: {0}", musicvolume);
    }

    public void setLooping(boolean isLooping) {
        for (Music music : musicTrack.values()) {
            music.setLooping(isLooping);
        }
    }

    public boolean isPlaying(String trackName) {
        Music music = musicTrack.get(trackName);
        return music != null && music.isPlaying();
    }

    public boolean isSoundEnabled() {
        return isSoundEnabled;
    }

    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            showVolumeControls();
            Gdx.input.setInputProcessor(stage);
        } else {
            hideVolumeControls();
            Gdx.input.setInputProcessor(null);
        }
        LOGGER.log(Level.INFO, "Audio Paused: {0}", isPaused);
    }

    public void hideVolumeControls() {
        volumeWindow.setVisible(false);
    }

    // UI Pop-Up for Volume Controls
    private void createVolumeControls() {
        volumeWindow = new Window("Audio Settings", skin);
        volumeWindow.setSize(400, 250);
        volumeWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 100);
        musicLabel = new Label("Music Volume", skin);
        musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(setmusicVolume);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setmusicVolume(musicSlider.getValue());
            }
        });

        // Sound Effects Volume Toggle
        soundLabel = new Label("Sound Effects Volume", skin);
        final CheckBox soundToggle = new CheckBox("Enable Sound Effects", skin);
        soundToggle.setChecked(isSoundEnabled);
        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isChecked = soundToggle.isChecked();
                toggleSoundEffects(isChecked);
                LOGGER.log(Level.INFO, "Sound Effects Enabled: {0}", isChecked);
            }
        });

        // Add components to the window
        Table table = new Table();
        table.add(musicLabel).padBottom(10);
        table.row();
        table.add(musicSlider).width(250).padBottom(20);
        table.row();
        table.add(soundLabel).padBottom(10);
        table.row();
        table.add(soundToggle).padBottom(20);
        volumeWindow.add(table);
        stage.addActor(volumeWindow);
        volumeWindow.setVisible(false);
    }

    public void showVolumeControls() {
        volumeWindow.setVisible(true);
    }

    // Dispose of the audios
    public void dispose() {
        for (Music music : musicTrack.values()) {
            music.stop();
            music.dispose();
        }
        for (Sound sound : soundEffects.values()) {
            sound.dispose();
        }
        soundEffects.clear();
    }
}
