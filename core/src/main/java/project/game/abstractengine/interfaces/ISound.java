package project.game.abstractengine.interfaces;

public interface  ISound {
    void loadSoundEffects(String[] soundFiles, String[] keys);
    void playSoundEffect(String key);
    void stopAllSounds();
    void setSoundEnabled(boolean enabled);  // 🔄 New: Enable or disable sound globally
    void dispose();

}
