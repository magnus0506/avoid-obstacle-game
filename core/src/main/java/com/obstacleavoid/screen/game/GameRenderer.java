package com.obstacleavoid.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.assets.AssetPaths;
import com.obstacleavoid.assets.RegionNames;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.entity.Background;
import com.obstacleavoid.entity.Obstacle;
import com.obstacleavoid.entity.Player;
import com.obstacleavoid.util.GdxUtils;
import com.obstacleavoid.util.ViewportUtils;
import com.obstacleavoid.util.debug.DebugCameraController;

public class GameRenderer implements Disposable {

    // -- attributes --
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;

    private OrthographicCamera hudCamera;
    private Viewport hudViewport;

    private SpriteBatch batch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private DebugCameraController debugCameraController;
    private final GameController controller;

    private AssetManager assetManager;
    private TextureRegion playerRegion;
    private TextureRegion obstacleRegion;
    private TextureRegion backgroundRegion;


    // -- constructor --
    public GameRenderer(SpriteBatch batch, AssetManager assetManager, GameController controller) {
        this.assetManager = assetManager;
        this.batch = batch;
        this.controller = controller;
        init();
    }

    // -- init --
    private void init() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, hudCamera);
        batch = new SpriteBatch();


        font = assetManager.get(AssetDescriptors.FONT);

//        debugCameraController = new DebugCameraController();
//        debugCameraController.setStartPosition(GameConfig.WORLD_CENTER_X, GameConfig.WORLD_CENTER_Y);

        TextureAtlas gamePlayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);


        playerRegion = gamePlayAtlas.findRegion(RegionNames.PLAYER);
        obstacleRegion = gamePlayAtlas.findRegion(RegionNames.OBSTACLE);
        backgroundRegion = gamePlayAtlas.findRegion(RegionNames.BACKGROUND);


    }

    // -- public methods --
    public void render(float delta) {
        batch.totalRenderCalls = 0;

//        debugCameraController.handleDebugInput(delta);
//        debugCameraController.applyTo(camera);

        if(Gdx.input.isTouched() && !controller.isGameOver()){
            Vector2 screenTouch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 worldTouch = viewport.unproject(new Vector2(screenTouch));

            Player player = controller.getPlayer();
            worldTouch.x = MathUtils.clamp(worldTouch.x,
                    0 + player.getWidth() / 2f,
                    (GameConfig.WORLD_WIDTH - player.getWidth() / 2f));
            player.setX(worldTouch.x - player.getWidth() / 2f);
        }

        GdxUtils.clearScreen();

        renderGameplay();
        renderUI();

        renderDebug();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        ViewportUtils.debugPixelPermit(viewport);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    private void renderGameplay(){
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // draw background
        Background background = controller.getBackground();

        batch.draw(backgroundRegion,
                background.getX(),background.getY(),
                background.getWidth(),background.getHeight());

        // draw player
        Player player = controller.getPlayer();
        batch.draw(playerRegion,
                player.getX(), player.getY(),
                player.getWidth(), player.getHeight()
        );

        // draw obstacles
        for (Obstacle obstacle : controller.getObstacles()){
            batch.draw(obstacleRegion,
                    obstacle.getX(),obstacle.getY(),
                    obstacle.getWidth(), obstacle.getHeight()
            );
        }


        batch.end();
    }

    private void renderUI() {
        hudViewport.apply();
        batch.setProjectionMatrix(hudCamera.combined);

        batch.begin();

        String livesText = "Lives: " + controller.getLives();
        layout.setText(font, livesText);
        font.draw(batch, livesText,
                20,
                GameConfig.HUD_HEIGHT - layout.height);

        String scoreText = "Score: " + controller.getScore();
        layout.setText(font, scoreText);

        font.draw(batch, scoreText,
                20,
//                GameConfig.HUD_HEIGHT - layout.height
        1);

        batch.end();
    }

    private void renderDebug() {
        viewport.apply();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        drawDebug();

        renderer.end();

//        ViewportUtils.drawGrid(viewport, renderer);
    }

    private void drawDebug() {
        Player player = controller.getPlayer();
        player.drawDebug(renderer);

        Array.ArrayIterator<Obstacle> obstacles = new Array.ArrayIterator<>(controller.getObstacles());
        for (Obstacle obstacle : obstacles) {
            obstacle.drawDebug(renderer);
        }
    }
}
