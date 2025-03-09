package project.game.abstractengine.interfaces;

public interface  IAudioConfig {
    void saveSoundEnabled(boolean enabled);
    boolean isSoundEnabled();

    void saveMusicVolume(float volume);
    float getMusicVolume();

    void saveSoundVolume(float volume);
    float getSoundVolume();
}
