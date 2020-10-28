package com.obstacleavoid;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Logger;
import com.obstacleavoid.screen.game.GameScreen;
import com.obstacleavoid.screen.loading.LoadingScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ObstacleAvoidGame extends Game {

	private AssetManager assetManager;
	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_ERROR);

		assetManager = new AssetManager();
		assetManager.getLogger().setLevel(Logger.ERROR);

		setScreen(new GameScreen(this));
	}

	@Override
	public void dispose() {
		assetManager.dispose();
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}
}