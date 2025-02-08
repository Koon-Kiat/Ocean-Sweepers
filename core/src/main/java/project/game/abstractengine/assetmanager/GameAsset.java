package project.game.abstractengine.assetmanager;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

public class GameAsset implements Disposable {
	
	private static GameAsset instance;
	private AssetManager assetManager;
	
	private GameAsset() {
		assetManager = new AssetManager();
	}
	
	public static GameAsset getInstance() {
		if (instance == null) {
			instance = new GameAsset();
		}
		return instance;
	}
	
	public void loadTextureAssets(String filePath) {
		assetManager.load(filePath, Texture.class);
	}
	
	public void loadSoundAssets(String filePath) {
		assetManager.load(filePath, Sound.class);
	}
	
	public void loadMusicAssets(String filePath) {
		assetManager.load(filePath, Music.class);
	}
	
	public void loadFontAssets(String filePath) {
		assetManager.load(filePath, BitmapFont.class);
	}
	
	public boolean isLoaded() {
		return assetManager.update();
	}
	
	public <T> T getAsset(String filePath, Class<T> type) {
		return assetManager.get(filePath, type);
	}
	
	@Override
	public void dispose() {
		assetManager.dispose();
	}
	
	
	
}
