package project.game.abstractengine.audiomanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import project.game.abstractengine.interfaces.IMusic;

public class MusicManager implements IMusic {
    private static volatile MusicManager instance; // Singleton instance
    private final Map<String, Music> musicTrack = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(MusicManager.class.getName());
    private MusicManager() {} // Private constructor to prevent instantiation

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
    // Load music tracks into the musicTrack map
    @Override
    public void loadMusicTracks(String... tracks) {
        for (String track : tracks) {
            loadSingleTrack(track);
        }
    }

    public void unloadMusic(String musicName) {
        if (musicTrack.containsKey(musicName)) {  // Corrected variable name
            Music music = musicTrack.get(musicName);
            if (music != null) {
                music.stop();
                music.dispose();  // Properly unload from memory
            }
            musicTrack.remove(musicName);
            LOGGER.log(Level.INFO, "Unloaded Music: {0}", musicName);
        } else {
            LOGGER.log(Level.WARNING, "Tried to unload non-existent music: {0}", musicName);
        }
    }
    
    

    //  Load a single music track (modular and reusable)
    private void loadSingleTrack(String track) {
        String trackName = track.replace(".mp3", "");  // Key: Track name without .mp3

        // Check if the file exists
        if (!Gdx.files.internal("assets/" + track).exists()) {
            LOGGER.log(Level.WARNING, "Failed to Load Music: {0}", track);
            return;
        }

        // Load and store music in ConcurrentHashMap
        Music music = Gdx.audio.newMusic(Gdx.files.internal("assets/" + track));
        music.setLooping(true);  // Optional: Make music loop by default
        musicTrack.put(trackName, music);  // Store in ConcurrentHashMap
        LOGGER.log(Level.INFO, "Loaded Music: {0}", track);
    }
    @Override
    public void playMusic(String trackName) {
        if (musicTrack.containsKey(trackName)) {
            musicTrack.get(trackName).play();
        }else{
            LOGGER.log(Level.WARNING, "Music track not found: {0}", trackName);
        }
    }

    @Override
    public void stopMusic() {
        musicTrack.values().forEach(Music::stop);
        
    }

    @Override
    public void setMusicVolume(float volume) {
        musicTrack.values().forEach(music -> {
            music.setVolume(volume);  // Set volume even if not playing
            System.out.println("Music volume set to: " + volume);
        });
    }

    @Override
    public boolean isPlaying(String trackName) {
        return musicTrack.containsKey(trackName) && musicTrack.get(trackName).isPlaying();
    }

    @Override
    public void dispose() {
        musicTrack.values().forEach(Music::dispose);
        musicTrack.clear();
    }
}
