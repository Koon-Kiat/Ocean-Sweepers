package project.game.engine.audio.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import project.game.engine.api.audio.IAudioConfig;

public class AudioConfig implements IAudioConfig {
    
    private final Preferences prefs = Gdx.app.getPreferences("AudioSettings");

    @Override
    public void saveSoundEnabled(boolean enabled) {
        prefs.putBoolean("isSoundEnabled", enabled);
        prefs.flush();
    }

    @Override
    public boolean isSoundEnabled() {
        return prefs.getBoolean("isSoundEnabled", true);
    }

    @Override
    public void saveMusicVolume(float volume) {
        prefs.putFloat("musicVolume", volume);
        prefs.flush();
    }

    @Override
    public float getMusicVolume() {
        return prefs.getFloat("musicVolume", 0.2f);
    }

    @Override
    public void saveSoundVolume(float volume) {
        prefs.putFloat("soundVolume", volume);
        prefs.flush();
    }

    @Override
    public float getSoundVolume() {
        return prefs.getFloat("soundVolume", 0.2f);
    }
}
