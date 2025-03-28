package project.game.engine.audio.sound;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import project.game.common.logging.core.GameLogger;
import project.game.engine.audio.api.ISound;

public class SoundManager implements ISound {

    private static final GameLogger LOGGER = new GameLogger(SoundManager.class);
    private static volatile SoundManager instance;
    private final Map<String, Sound> soundEffects = new HashMap<>();
    private boolean soundEnabled = true;

    private SoundManager() {
    }

    @SuppressWarnings("DoubleCheckedLocking")
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

    // Individual loader for a single sound effect
    public void loadSoundEffect(String key, String filePath) {
        if (!Gdx.files.internal(filePath).exists()) {
            LOGGER.warn("Sound file not found at path: {0}", filePath);
            return;
        }

        Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
        soundEffects.put(key, sound);
        LOGGER.info("Successfully loaded sound effect: " + filePath + " with key: " + key);

    }

    // Method to load sound effects
    @Override
    public void loadSoundEffects(String[] soundFiles, String[] keys) {
        if (soundFiles.length != keys.length) {
            LOGGER.warn("Mismatch between sound files and keys count.");
            return;
        }
        for (int i = 0; i < soundFiles.length; i++) {
            loadSoundEffect(keys[i], soundFiles[i]);
        }
    }

    @Override
    public void playSoundEffect(String key) {
        if (soundEnabled && soundEffects.containsKey(key)) {
            long id = soundEffects.get(key).play();
            if (id == -1) {
                LOGGER.warn("Failed to play sound effect with key: " + key);
            }
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
