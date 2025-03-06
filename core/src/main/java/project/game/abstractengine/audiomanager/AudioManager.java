package project.game.abstractengine.audiomanager;

import project.game.abstractengine.interfaces.IAudioConfig;
import project.game.abstractengine.interfaces.IMusic;
import project.game.abstractengine.interfaces.ISound;
import project.game.audioui.AudioUI;

public class AudioManager {
    private static volatile AudioManager instance;  // Singleton instance

    private final IMusic MusicManager;
    private final ISound SoundManager;
    private final IAudioConfig AudioConfig;
    private AudioUI AudioUI;  // Can be null if UI is not needed

    // Private constructor to prevent direct instantiation
    private AudioManager(IMusic musicManager, ISound soundManager, IAudioConfig audioConfig) {
        this.MusicManager = musicManager;
        this.SoundManager = soundManager;
        this.AudioConfig = audioConfig;
        this.AudioUI = null;  // Initially null
    }

    // Public method to get the singleton instance of AudioManager with parameters
    public static AudioManager getInstance(IMusic musicManager, ISound soundManager, IAudioConfig config) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager(musicManager, soundManager, config);
                }
            }
        }
        return instance;
    }
    
    // Setter for AudioUI
    public void setAudioUI(AudioUI audioUI) {
        this.AudioUI = audioUI;
    }

    public void loadMusicTracks(String... tracks) {
        MusicManager.loadMusicTracks(tracks);  // Calls MusicManager's method
    }

    public void loadSoundEffects(String[] paths, String[] keys) {
        SoundManager.loadSoundEffects(paths, keys);  // Calls SoundManager's method
    }

    public void playMusic(String trackName) {
        MusicManager.playMusic(trackName);
    }

    public void stopMusic() {
        MusicManager.stopMusic();
    }

    public void setMusicVolume(float volume) {
        MusicManager.setMusicVolume(volume);
    }

    public void playSoundEffect(String key) {
        SoundManager.playSoundEffect(key);
    }

    public void setSoundEnabled(boolean enabled) {
        SoundManager.setSoundEnabled(enabled);
    }

    public void showVolumeControls() {
        if (AudioUI != null) {
            AudioUI.showSettings();
        }
    }

    public void hideVolumeControls() {
        if (AudioUI != null) {
            AudioUI.hideSettings();
        }
    }

    public void dispose() {
        MusicManager.dispose();
        SoundManager.dispose();
    }
}
