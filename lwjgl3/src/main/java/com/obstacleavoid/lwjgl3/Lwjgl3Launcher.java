package com.obstacleavoid.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.config.GameConfig;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	public static void main(String[] args) {

		createApplication();
	}

	private static Lwjgl3Application createApplication() {
		return new Lwjgl3Application(new ObstacleAvoidGame(), configuration());
	}

	private static Lwjgl3ApplicationConfiguration configuration(){
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setWindowedMode((int) GameConfig.WIDTH, (int) GameConfig.HEIGHT);
		return configuration;
	}
}