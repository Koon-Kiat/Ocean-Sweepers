package project.game.engine.api.audio;

public interface ISound {
    void loadSoundEffects(String[] soundFiles, String[] keys);

    void playSoundEffect(String key);

    void stopAllSounds();

    void setSoundEnabled(boolean enabled);

    void dispose();

}
