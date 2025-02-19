package project.game.abstractengine.audiomanager;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class AudioManager {
    private float setsoundVolume = 0.2f; //Set default sound effects
    private float setmusicVolume = 0.2f; //Set default music volume
    private boolean isSoundEnabled = true; //Set sound effects to be enabled by default
    private final boolean musicEnable = true; //Set music to be enabled by default
    private final Map<String, Sound> soundEffects; //Create a hashmap to store the sound effects
    private final Map<String, Music> musicTrack; //Create a hashmap to store the music tracks

    //UI Components
    private final Stage stage;
    private final Skin skin;
    private Slider musicSlider;
    private Label musicLabel;
    private Label soundLabel;
    private Window volumeWindow;
    private TextButton settingsButton;
    private static volatile AudioManager instance;
    public boolean isPaused = false;

    public AudioManager(Stage stage){
        this.stage = stage; //Use an external stage so it is available in the main game
        //Create the background music
        soundEffects = new HashMap<>(); //Create a new hashmap to store the sound effects
        musicTrack = new HashMap<>(); //Create a new hashmap to store the music tracks

        Preferences prefs = Gdx.app.getPreferences("GameSettings");
        isSoundEnabled = prefs.getBoolean("isSoundEnabled", true); // ✅ Load last saved setting
        setsoundVolume = isSoundEnabled ? 0.2f : 0f; // ✅ Load last saved setting

        loadSoundEffects(); //Load the sound effects
        loadMusicTracks(); //Load the music tracks

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        createVolumeControls();
    }
    public static AudioManager getInstance(Stage stage){
        if(instance == null){
            synchronized (AudioManager.class){
                if(instance == null){
                    instance = new AudioManager(stage);
                }
            }
        }
        return instance;
    }

    private void loadMusicTracks(){
        String[] tracks = {"BackgroundMusic.mp3"}; //Create an array of music tracks can add on more songs here
        // Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("BackgroundMusic.mp3"));
        // backgroundMusic.setLooping(true);
        // backgroundMusic.setVolume(setmusicVolume);
        // musicTrack.put("background", backgroundMusic);
        for(String track : tracks){
            String trackName = track.replace(".mp3", ""); //Remove the file extension from the track name
            if(!Gdx.files.internal(track).exists()){
                System.out.println("Failed to Load Music: " + track);
                continue;
            }

            Music music = Gdx.audio.newMusic(Gdx.files.internal(track)); //Load the music track
            
            if(music == null){
                System.out.println("Failed to Load Music: " + track);
            }else{
                System.out.println("Loaded Music: " + track);
                music.setLooping(true); //Set the music to loop
                music.setVolume(setmusicVolume); //Set the volume of the music
                musicTrack.put(trackName, music); //Add the music track to the hashmap
            }
        }
    }

    private void loadSoundEffects(){ //Load the sound effects
        String[] soundNames = {"Boinkeffect.mp3", "Selection.mp3", "Watercollision.mp3"}; //Create an array of sound effect names
        String[] keys = {"keybuttons", "selection", "watercollision"}; //Create an array of keys for the sound effects
        for(int i = 0; i < soundNames.length; i++){
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(soundNames[i])); //Load the sound effect
        
            if(sound == null){
                System.out.println("Failed to Load Sound Effect: " + soundNames[i]);
            }else{
                System.out.println("Loaded Sound Effect: " + soundNames[i]);
                soundEffects.put(keys[i], sound); //Add the sound effect to the hashmap
            }
        // soundEffects.put("collision", Gdx.audio.newSound(Gdx.files.internal("Boinkeffect.mp3"))); //Load the explosion sound effect
        }
    }
    private void toggleSoundEffects(boolean isEnabled){ //Toggle the sound effects
        isSoundEnabled = isEnabled; //Set the sound effects to be enabled or disabled
        setsoundVolume = isSoundEnabled ? 0.2f : 0f; //If sound effects are enabled set the volume to 0.2 otherwise set it to 0

        if(!isSoundEnabled){
            stopAllSounds();
        }
        Preferences prefs = Gdx.app.getPreferences("AudioSettings");
        prefs.putBoolean("soundEnabled", isSoundEnabled);
        prefs.flush();
        System.out.println("[Debug] Sound Effects Enabled: " + isSoundEnabled + " | Volume: " + isSoundEnabled);
    }
    public void playMusic(String trackName){
        Music music = musicTrack.get(trackName); //Get the music track from the hashmap
        if(music != null && musicEnable){ //If the music track exists
            music.setVolume(setmusicVolume);
            music.play(); //Play the music
        }
    }
    public void playSoundEffect(String soundEffect) { // Play a sound effect
        if (!isSoundEnabled || setsoundVolume == 0) { // If sound effects are disabled
            return; // Exit the method
        }
        Sound sound = soundEffects.get(soundEffect); 
        if (sound != null) { // If the sound effect exists and the volume is greater than 0
            long soundId = sound.play(); // Play the sound effect
            sound.setVolume(soundId, setsoundVolume); // Correctly applies volume
            // System.out.println("[DEBUG] Playing Sound: " + soundEffect + " | Volume: " + isSoundEnabled);
        }
    }
    private void stopAllSounds(){
        for(Sound sound : soundEffects.values()){
            sound.stop();
        }
    }

    public void stopMusic(){
        for(Music music : musicTrack.values()){
            music.stop();
        }
    }
    public void setmusicVolume(float musicvolume){
        this.setmusicVolume = musicvolume; //Set the music volume
        for(Music music : musicTrack.values()){
            music.setVolume(setmusicVolume);
        }
        System.out.println("Updated Music Volume: " + musicvolume);
    }
    public void setLooping(boolean isLooping){
        for(Music music : musicTrack.values()){
            music.setLooping(isLooping);
        }
    }
    public boolean isPlaying(String trackName){
        Music music = musicTrack.get(trackName); //Get the music track from the hashmap
        return music != null && music.isPlaying();
    }
    public boolean isSoundEnabled(){
        return isSoundEnabled;
    } //Return whether sound effects are enabled

    public void togglePause(){ //Toggle the pause state after pressing 'm'
        isPaused = !isPaused;
        if(isPaused){
            showVolumeControls();
            Gdx.input.setInputProcessor(stage);
        } else {
            hideVolumeControls();
            Gdx.input.setInputProcessor(null);
        }
        System.out.println("[Debug] Audio Paused: " + isPaused);
    }
    
    public void hideVolumeControls(){
        volumeWindow.setVisible(false);
    }

    // UI Pop-Up for Volume Controls
    private void createVolumeControls(){
        volumeWindow = new Window("Audio Settings", skin);
        volumeWindow.setSize(400, 250);
        volumeWindow.setPosition(Gdx.graphics.getWidth() /2f - 200, Gdx.graphics.getHeight()/ 2f-100);//Center the window
    
        musicLabel = new Label("Music Volume", skin);
        musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(setmusicVolume);
        musicSlider.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                setmusicVolume(musicSlider.getValue());
            }
        });

        //Sound Effects Volume Toggle
        soundLabel = new Label("Sound Effects Volume", skin);
        final CheckBox soundToggle = new CheckBox("Enable Sound Effects", skin); //Create a checkbox to enable/disable sound effects
        soundToggle.setChecked(isSoundEnabled); //Set the default value of the checkbox
        soundToggle.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                boolean isChecked = soundToggle.isChecked();
                toggleSoundEffects(isChecked);
                System.out.println("[Debug] Checkbox Changed: " + isChecked);
            }
        });
        
        // Close Button
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                volumeWindow.setVisible(false);
                togglePause(); //Unpause the game upon close
            }
        });

        //Add components to the window
        Table table = new Table();
        table.add(musicLabel).padBottom(10); // Add the music label to the window
        table.row();
        table.add(musicSlider).width(250).padBottom(20); //Add the music slider to the window
        table.row();
        table.add(soundLabel).padBottom(10); //Add the sound label to the window
        table.row();
        table.add(soundToggle).padBottom(20); //Add the sound slider to the window
        table.row();
        table.add(closeButton).padTop(10); //Add the close button to the top of the window
        
        volumeWindow.add(table);
        stage.addActor(volumeWindow);
        volumeWindow.setVisible(false); //Hide the window by default
    }
    public void showVolumeControls(){
        volumeWindow.setVisible(true);
    }

    //Dispose of the audios
    public void dispose(){
        for(Music music : musicTrack.values()){
            music.stop();
            music.dispose();
        }
        for(Sound sound : soundEffects.values()){
            sound.dispose();
        }
        soundEffects.clear();
    }

}
