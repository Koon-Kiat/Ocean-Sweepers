package project.game.abstractengine.audiomanager;

import project.game.abstractengine.interfaces.IAudioConfig;
import project.game.abstractengine.interfaces.IMusic;
import project.game.abstractengine.interfaces.ISound;

/**
 * AudioManager is a singleton class that manages the audio settings and
 * playback of music and sound effects in the game.
 * 
 * It uses LibGDX's audio classes to load and play music and sound effects.
 */
public class AudioManager {
    private static volatile AudioManager instance; // Singleton instance

    private final IMusic MusicManager;
    private final ISound SoundManager;
    private AudioUIManager AudioUIManager;
    private final IAudioConfig AudioConfig;

    public AudioManager(IMusic MusicManager, ISound SoundManager, IAudioConfig AudioConfig, AudioUIManager AudioUIManager) {
        this.MusicManager = MusicManager;
        this.SoundManager = SoundManager;
        this.AudioConfig = AudioConfig;
        this.AudioUIManager = AudioUIManager;
    }

    //Public method to get the singleton instance of AudioManager
    public static AudioManager getInstance(IMusic MusicManager, ISound SoundManager, IAudioConfig AudioConfig, AudioUIManager AudioUIManager) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager(MusicManager, SoundManager, AudioConfig, AudioUIManager);
                }
            }
        }
        return instance;
    }

    public void playMusic(String trackName) {
        MusicManager.playMusic(trackName);
    }

    public void playSoundEffect(String key) {
        SoundManager.playSoundEffect(key);
    }

    public void setMusicVolume(float volume) {
        MusicManager.setMusicVolume(volume);
    }

    public void setSoundEnabled(boolean enabled) {
        SoundManager.setSoundEnabled(enabled);
    }

    public void showVolumeControls(){
        if (AudioUIManager != null) {
            AudioUIManager.showSettings();
        }
    }

    public void hideVolumeControls(){
        if (AudioUIManager != null) {
            AudioUIManager.hideSettings();
        }
    }
    public void stopMusic() {
        MusicManager.stopMusic();
    }

    public void setAudioUIManager(AudioUIManager audioUIManager) {
        this.AudioUIManager = audioUIManager;
    }

    public void dispose() {
        MusicManager.dispose();
        SoundManager.dispose();
    }
}