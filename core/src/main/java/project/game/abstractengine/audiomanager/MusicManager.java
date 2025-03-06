package project.game.abstractengine.audiomanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import project.game.abstractengine.interfaces.IMusic;

public class MusicManager implements IMusic {
    private static volatile MusicManager instance;  // Singleton instance
    private final Map<String, Music> musicTracks = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(MusicManager.class.getName());

    // Private constructor to prevent instantiation
    private MusicManager() {}

    // Public method to get the singleton instance of MusicManager
    public static MusicManager getInstance() {
        if (instance == null) {
            synchronized (MusicManager.class) {
                if (instance == null) {
                    instance = new MusicManager();
                }
            }
        }
        return instance;
    }

    // Abstract method to load music with no hardcoded paths
    @Override
    public void loadMusicTracks(String... tracks) {
        for (String track : tracks) {
            loadSingleTrack(track);
        }
    }

    // Method to unload music by track name
    public void unloadMusic(String trackName) {
        if (musicTracks.containsKey(trackName)) {
            Music music = musicTracks.get(trackName);
            if (music != null) {
                music.stop();
                music.dispose();  // Properly unload from memory
                LOGGER.log(Level.INFO, "Unloaded Music: {0}", trackName);
            }
            musicTracks.remove(trackName);
        } else {
            LOGGER.log(Level.WARNING, "Attempted to unload non-existent music: {0}", trackName);
        }
    }

    private String extractTrackKey(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        return (lastDotIndex != -1) ? filePath.substring(0, lastDotIndex) : filePath;
    }
    

    // Private method to load a single track with no context
    private void loadSingleTrack(String track) {
        try {
            if (!isValidTrack(track)) {
                LOGGER.log(Level.SEVERE, "File not found: {0}", track);
                return;
            }
    
            String trackKey = extractTrackKey(track);
            Music music = Gdx.audio.newMusic(Gdx.files.internal(track));
            music.setLooping(true);
            musicTracks.put(trackKey, music);
            LOGGER.log(Level.INFO, "Successfully loaded music: {0}", trackKey);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to Load Music: {0} - Error: {1}", new Object[]{track, e.getMessage()});
        }
    }
    /** 
     * Validates if a track exists before attempting to load. 
     */
    private boolean isValidTrack(String track) {
        return track != null && !track.isEmpty() && Gdx.files.internal(track).exists();
    }

    @Override
    public void playMusic(String trackName) {
        if (musicTracks.containsKey(trackName)) {
            musicTracks.get(trackName).play();
        } else {
            LOGGER.log(Level.WARNING, "Music track not found: {0}", trackName);
        }
    }

    @Override
    public void stopMusic() {
        musicTracks.values().forEach(Music::stop);
    }

    @Override
    public void setMusicVolume(float volume) {
        musicTracks.values().forEach(music -> music.setVolume(volume));
    }

    @Override
    public boolean isPlaying(String trackName) {
        return musicTracks.containsKey(trackName) && musicTracks.get(trackName).isPlaying();
    }

    @Override
    public void dispose() {
        musicTracks.values().forEach(Music::dispose);
        musicTracks.clear();
    }
}