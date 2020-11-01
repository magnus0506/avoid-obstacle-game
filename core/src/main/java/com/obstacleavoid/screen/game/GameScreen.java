package com.obstacleavoid.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.assets.RegionNames;
import com.obstacleavoid.common.GameManager;
import com.obstacleavoid.config.DifficultyLevel;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.entity.ObstacleActor;
import com.obstacleavoid.entity.PlayerActor;
import com.obstacleavoid.screen.menu.MenuScreen;
import com.obstacleavoid.util.GdxUtils;
import com.obstacleavoid.util.ViewportUtils;
import com.obstacleavoid.util.debug.DebugCameraController;

public class GameScreen extends ScreenAdapter {

    // -- constants --
    private static final float PADDING = 20.0f;

    // -- attributes --
    private final ObstacleAvoidGame game;
    private final AssetManager assetManager;
    private final SpriteBatch batch;
    private final GlyphLayout layout = new GlyphLayout();

    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private ShapeRenderer renderer;

    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private BitmapFont font;

    private float obstacleTimer;
    private float scoreTimer;
    private int lives = GameConfig.LIVES_ON_START;
    private int score;
    private Sound hitSound;

    private float startPlayerX = (GameConfig.WORLD_WIDTH - GameConfig.PLAYER_SIZE) / 2f;
    private float startPlayery = GameConfig.PLAYER_SIZE / 2f;

    private DebugCameraController debugCameraController;
    private TextureRegion obstacleRegion;
    private TextureRegion backgroundRegion;


    private PlayerActor player;
    private final Array<ObstacleActor> obstacles = new Array<>();
    private final Pool<ObstacleActor> obstaclePool = Pools.get(ObstacleActor.class);

    // -- constructor --
    public GameScreen(ObstacleAvoidGame game) {
        this.game = game;
        assetManager = game.getAssetManager();
        batch = game.getBatch();
    }

    // -- public methods --

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        stage = new Stage(viewport, batch);
        stage.setDebugAll(false);


        renderer = new ShapeRenderer();

        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera);
        font = assetManager.get(AssetDescriptors.FONT);

        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition(GameConfig.WORLD_CENTER_X, GameConfig.WORLD_CENTER_Y);

        hitSound = assetManager.get(AssetDescriptors.HIT_SOUND);

        TextureAtlas gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        TextureRegion playerRegion = gameplayAtlas.findRegion(RegionNames.PLAYER);
        obstacleRegion = gameplayAtlas.findRegion(RegionNames.OBSTACLE);
        backgroundRegion = gameplayAtlas.findRegion(RegionNames.BACKGROUND);

        Image background = new Image(backgroundRegion);
        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        player = new PlayerActor();
        player.setPosition(startPlayerX, startPlayery);
        player.setRegion(playerRegion);

        stage.addActor(background);
        stage.addActor(player);
    }

    @Override
    public void render(float delta) {
        debugCameraController.handleDebugInput(delta);
        debugCameraController.applyTo(camera);


        if (Gdx.input.isTouched() && !isGameOver()) {
            Vector2 screenTouch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 worldTouch = viewport.unproject(new Vector2(screenTouch));


            worldTouch.x = MathUtils.clamp(worldTouch.x,
                    0 + player.getWidth() / 2f,
                    (GameConfig.WORLD_WIDTH - player.getWidth() / 2f));
            player.setX(worldTouch.x - player.getWidth() / 2f);
        }
        update(delta);

        GdxUtils.clearScreen();

        viewport.apply();
        renderGameplay();

        uiViewport.apply();
        renderUi();

        viewport.apply();
//        renderDebug();

        if (isGameOver()) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);

        ViewportUtils.debugPixelPermit(viewport);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }


    // -- private methods --

    private void update(float delta) {
        if (isGameOver()) {
            return;
        }

        createNewObstacle(delta);
        removePassedObstacles();

        updateScore(delta);

        if (!isGameOver() && isPlayerCollidingWithObstacle()) {
            lives--;

            if (isGameOver()) {
                GameManager.INSTANCE.setHighScore(score);
            } else {
                restart();
            }
        }
    }

    private void updateScore(float delta) {
        scoreTimer += delta;

        if (scoreTimer >= GameConfig.SCORE_RATE) {
            score += 1;
            scoreTimer = 0.0f;
        }
    }

    private void createNewObstacle(float delta) {
        obstacleTimer += delta;

        if (obstacleTimer >= GameConfig.OBSTACLE_SPAWN_TIME) {
            float min = 0;
            float max = GameConfig.WORLD_WIDTH - GameConfig.OBSTACLE_SIZE;

            float obstacleX = MathUtils.random(min, max);
            float obstacleY = GameConfig.WORLD_HEIGHT;

            ObstacleActor obstacle = obstaclePool.obtain();
            DifficultyLevel difficultyLevel = GameManager.INSTANCE.getDifficultyLevel();
            obstacle.setYSpeed(difficultyLevel.getObstacleSpeed());
            obstacle.setPosition(obstacleX, obstacleY);
            obstacle.setRegion(obstacleRegion);

            obstacles.add(obstacle);
            stage.addActor(obstacle);
            obstacleTimer = 0f;
        }
    }


    private void removePassedObstacles() {
        if (obstacles.size > 0) {
            ObstacleActor first = obstacles.first();

            float minObstacleY = -GameConfig.OBSTACLE_SIZE;

            if (first.getY() < minObstacleY) {
                obstacles.removeValue(first, true);

                first.remove();

                obstaclePool.free(first);
            }
        }
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    private void restart() {
        for (int i = 0; i < obstacles.size; i++) {
            ObstacleActor obstacle = obstacles.get(i);

            // remove from stage
            obstacle.remove();

            // return to obstacle pool
            obstaclePool.free(obstacle);

            // remove from array
            obstacles.removeIndex(i);
        }
    }

    private boolean isPlayerCollidingWithObstacle() {
        for (ObstacleActor obstacle : obstacles) {
            if (obstacle.isNotHit() && obstacle.isPlayerColliding(player)) {
                hitSound.play();
                return true;
            }
        }

        return false;
    }

    private void renderGameplay() {
        batch.setProjectionMatrix(camera.combined);

        stage.act();
        stage.draw();
    }

    private void renderUi() {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        String livesText = "LIVES: " + lives;
        layout.setText(font, livesText);
        font.draw(batch, layout, 20, GameConfig.HUD_HEIGHT - layout.height);

        String scoreText = "SCORE: " + score;
        layout.setText(font, scoreText);
        font.draw(batch, layout,
                GameConfig.HUD_WIDTH - layout.width - PADDING,
                GameConfig.HUD_HEIGHT - layout.height
        );

        batch.end();
    }

    private void renderDebug() {
        ViewportUtils.drawGrid(viewport, renderer);
    }
}
