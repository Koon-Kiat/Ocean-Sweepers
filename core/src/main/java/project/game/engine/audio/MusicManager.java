package project.game.engine.audio;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import project.game.common.logging.core.GameLogger;
import project.game.engine.api.audio.IMusic;

public class MusicManager implements IMusic {
    private static final GameLogger LOGGER = new GameLogger(MusicManager.class);
    private static volatile MusicManager instance;
    private final Map<String, Music> musicTracks = new HashMap<>();

    private MusicManager() {
    }

    @SuppressWarnings("DoubleCheckedLocking")
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

    @Override
    public void loadMusicTracks(String... tracks) {
        for (String track : tracks) {
            loadSingleTrack(track);
        }
    }

    public void unloadMusic(String trackName) {
        if (musicTracks.containsKey(trackName)) {
            Music music = musicTracks.get(trackName);
            if (music != null) {
                music.stop();
                music.dispose();
                LOGGER.info("Unloaded Music: {0}", trackName);
            }
            musicTracks.remove(trackName);
        } else {
            LOGGER.warn("Attempted to unload non-existent music: {0}", trackName);
        }
    }

    @Override
    public void playMusic(String trackName) {
        if (musicTracks.containsKey(trackName)) {
            musicTracks.get(trackName).play();
        } else {
            LOGGER.warn("Music track not found: {0}", trackName);
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

    private String extractTrackKey(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        return (lastDotIndex != -1) ? filePath.substring(0, lastDotIndex) : filePath;
    }

    private void loadSingleTrack(String track) {
        try {
            if (!isValidTrack(track)) {
                LOGGER.fatal("File not found: {0}", track);
                return;
            }

            String trackKey = extractTrackKey(track);
            Music music = Gdx.audio.newMusic(Gdx.files.internal(track));
            music.setLooping(true);
            musicTracks.put(trackKey, music);
            LOGGER.info("Successfully loaded music: {0}", trackKey);
        } catch (Exception e) {
            LOGGER.warn("Failed to Load Music: {0} - Error: {1}", new Object[] { track, e.getMessage() });
        }
    }

    private boolean isValidTrack(String track) {
        return track != null && !track.isEmpty() && Gdx.files.internal(track).exists();
    }
}