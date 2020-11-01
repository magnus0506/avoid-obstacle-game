package com.obstacleavoid.screen.loading;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.screen.menu.MenuScreen;
import com.obstacleavoid.util.GdxUtils;

public class LoadingScreen implements Screen {
    private static final Logger log = new Logger(LoadingScreen.class.getName(), Logger.DEBUG);

    // -- constants --
    private static final float PROGRESS_BAR_WIDTH = GameConfig.WIDTH / 2f;
    private static final float PROGRESS_BAR_HEIGHT = 60;

    // -- attributes --
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;

    private float progress;
    private float waitTime = 0.75f;
    private boolean changeScreen;

    private final ObstacleAvoidGame game;
    private final AssetManager assetManager;


    // -- constructor --
    public LoadingScreen(ObstacleAvoidGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    // -- public methods --

    @Override
    public void show() {
        log.debug("show");
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        assetManager.load(AssetDescriptors.FONT);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.UISKIN);
        assetManager.load(AssetDescriptors.HIT_SOUND);
    }

    @Override
    public void render(float delta) {
        update(delta);

        GdxUtils.clearScreen();
        viewport.apply();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        draw();

        renderer.end();

        if (changeScreen) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public void hide() {
        log.debug("hide");
        dispose();
    }

    @Override
    public void dispose() {
        log.debug("dispose");
        renderer.dispose();
        renderer = null;
    }

    // -- private methods --

    private void update(float delta) {

        progress = assetManager.getProgress();

        // -- update returns true when all assets are loaded
        if (assetManager.update()) {
            waitTime -= delta;

            if (waitTime <= 0) {
                changeScreen = true;
            }
        }
    }

    private static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void draw() {

        renderer.rect(4, 3, 2, 4);
        float progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f;
        float progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f;

        renderer.rect(progressBarX, progressBarY,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
    }
}
