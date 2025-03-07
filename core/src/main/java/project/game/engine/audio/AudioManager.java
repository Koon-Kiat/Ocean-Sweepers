package project.game.engine.audio;

import project.game.application.ui.AudioUI;
import project.game.engine.api.audio.IAudioConfig;
import project.game.engine.api.audio.IMusic;
import project.game.engine.api.audio.ISound;

public class AudioManager {
    
    private static volatile AudioManager instance;
    private final IMusic musicManager;
    private final ISound soundManager;
    private final IAudioConfig audioConfig;
    private AudioUI audioUI;

    private AudioManager(IMusic musicManager, ISound soundManager, IAudioConfig audioConfig) {
        this.musicManager = musicManager;
        this.soundManager = soundManager;
        this.audioConfig = audioConfig;
        this.audioUI = null;
    }

    // Public method to get the singleton instance of AudioManager with parameters
    @SuppressWarnings("DoubleCheckedLocking")
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

    public void setAudioUI(AudioUI audioUI) {
        this.audioUI = audioUI;
    }

    public void loadMusicTracks(String... tracks) {
        musicManager.loadMusicTracks(tracks);
    }

    public void loadSoundEffects(String[] paths, String[] keys) {
        soundManager.loadSoundEffects(paths, keys);
    }

    public void playMusic(String trackName) {
        musicManager.playMusic(trackName);
    }

    public void stopMusic() {
        musicManager.stopMusic();
    }

    public void setMusicVolume(float volume) {
        musicManager.setMusicVolume(volume);
    }

    public void playSoundEffect(String key) {
        soundManager.playSoundEffect(key);
    }

    public void setSoundEnabled(boolean enabled) {
        soundManager.setSoundEnabled(enabled);
    }

    public void showVolumeControls() {
        if (audioUI != null) {
            audioUI.showSettings();
        }
    }

    public void hideVolumeControls() {
        if (audioUI != null) {
            audioUI.hideSettings();
        }
    }

    public void dispose() {
        musicManager.dispose();
        soundManager.dispose();
    }
}
