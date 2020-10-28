package com.obstacleavoid.screen.loading;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.screen.game.GameScreen;
import com.obstacleavoid.util.GdxUtils;

public class LoadingScreen extends ScreenAdapter {

    // -- constants --
    private static final float PROGRESS_BAR_WIDTH = GameConfig.WIDTH / 2f;
    private static final float PROGRESS_BAR_HEIGHT = 60;

    // -- attributes --
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;

    private float progress;
    private float waitTime = 0.75f;

    private ObstacleAvoidGame game;
    private AssetManager assetManager;

    private BitmapFont font;


    // -- constructor --
    public LoadingScreen(ObstacleAvoidGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager(); ;
    }

    // -- public methods --

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        assetManager.load(AssetDescriptors.FONT);
        assetManager.load(AssetDescriptors.GAMEPLAY);

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
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height,true);
    }


    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    // -- private methods --

    private void update(float delta){
        waitMillis(400);

        progress = assetManager.getProgress();

        // -- update returns true when all assets are loaded
        if(assetManager.update()){
            waitTime -= delta;

            if(waitTime <= 0){
                game.setScreen(new GameScreen(game));
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

    private void draw(){
        float progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f;
        float progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f;

        renderer.rect(progressBarX,progressBarY,
                progress * PROGRESS_BAR_WIDTH,PROGRESS_BAR_HEIGHT);
    }
}
