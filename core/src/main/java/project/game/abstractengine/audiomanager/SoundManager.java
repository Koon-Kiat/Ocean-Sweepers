package project.game.abstractengine.audiomanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import project.game.abstractengine.interfaces.ISound;


public class SoundManager implements ISound {
    private static volatile SoundManager instance; // Singleton instance
    private final Map<String, Sound> soundEffects = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(SoundManager.class.getName());
    private boolean soundEnabled = true;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) {
            synchronized (SoundManager.class) {
                if (instance == null) {
                    instance = new SoundManager();
                }
            }
        }
        return instance;
    }

     // Method to load sound effects
    @Override
    public void loadSoundEffects(String[] soundFiles, String[] keys) {
        if (soundFiles.length != keys.length) {
            LOGGER.log(Level.WARNING, "Mismatch between sound files and keys count.");
            return;
        }
        for (int i = 0; i < soundFiles.length; i++) {
            loadSoundEffect(keys[i], soundFiles[i]);
        }
    }

    // Individual loader for a single sound effect
    public void loadSoundEffect(String key, String filePath) {
    if (!Gdx.files.internal(filePath).exists()) {
        LOGGER.log(Level.WARNING, "Sound file not found at path: " + filePath);
        return;
    }

        Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));  // Load sound
        soundEffects.put(key, sound);  // Store sound in map with the key
        LOGGER.log(Level.INFO, "Successfully loaded sound effect: " + filePath + " with key: " + key);

    }
    
    @Override
    public void playSoundEffect(String key) {
        if (soundEnabled && soundEffects.containsKey(key)) {
            long id = soundEffects.get(key).play();  // Play with volume
            if (id == -1) {
                LOGGER.log(Level.WARNING, "Failed to play sound for key: " + key);
            } else {
                LOGGER.log(Level.INFO, "Playing sound effect for key: " + key);
            }
        } else if (!soundEnabled){
            LOGGER.log(Level.INFO, "Sound effects are disabled.");
        } else {
            LOGGER.log(Level.WARNING, "Sound effect not found for key: " + key);
        }
    }
    @Override
    public void stopAllSounds() {
        soundEffects.values().forEach(Sound::stop);
    }
    @Override
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    @Override
    public void dispose() {
        soundEffects.values().forEach(Sound::dispose);
        soundEffects.clear();
    }

}
