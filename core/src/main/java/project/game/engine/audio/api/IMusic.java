package project.game.engine.audio.api;

public interface IMusic {
    
    void loadMusicTracks(String... tracks);

    void playMusic(String trackName);

    void stopMusic();

    void setMusicVolume(float volume);

    boolean isPlaying(String trackName);

    void dispose();
}
