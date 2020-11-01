package com.obstacleavoid.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.common.GameManager;
import com.obstacleavoid.config.DifficultyLevel;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.entity.Background;
import com.obstacleavoid.entity.Obstacle;
import com.obstacleavoid.entity.Player;

public class GameController {

    // -- constants --
    private static final Logger log = new Logger(GameController.class.getName(), Logger.DEBUG);

    // -- attributes --
    private ObstacleAvoidGame game;
    private Player player;
    private final Array<Obstacle> obstacles = new Array<>();
    private Background background;
    private float obstacleTimer;
    private float scoreTimer;
    private int lives = GameConfig.LIVES_ON_START;
    private int score;
    private Pool<Obstacle> obstaclePool;
    public Sound hit;
    private final float startPlayerX = (GameConfig.WORLD_WIDTH  - GameConfig.PLAYER_SIZE) / 2f ;
    private final float startPlayerY = 1 - GameConfig.PLAYER_SIZE / 2f;

    private final AssetManager assetManager;
    // -- constructor --
    public GameController(ObstacleAvoidGame game) {
        this.game = game;
        assetManager = game.getAssetManager();
        init();
    }

    // -- init --
    private void init() {
        player = new Player();

        // set position
        player.setPosition(startPlayerX,startPlayerY);

        // create obstacle pool
        obstaclePool = Pools.get(Obstacle.class,10);

        // create background
        background = new Background();
        background.setPosition(0,0);
        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        hit = assetManager.get(AssetDescriptors.HIT_SOUND);
    }

    // -- public methods --
    public void update(float delta) {
        if (isGameOver()) {
            return;
        }
        updatePlayer();
        updateObstacles(delta);

        updateScore(delta);
        if (isPlayerCollidingWithObstacle()) {
            log.debug("Collision detected");
            lives--;

            if (isGameOver()){
                log.debug("Game over");
                GameManager.INSTANCE.setHighScore(score);
            } else {
                restart();
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Obstacle> getObstacles() {
        return obstacles;
    }


    public Background getBackground() {
        return background;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    // -- private methods --

    private void restart(){
        obstaclePool.freeAll(obstacles);
        obstacles.clear();
    }

    private void updatePlayer() {
        float xSpeed = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xSpeed = GameConfig.MAX_PLAYER_X_SPEED;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xSpeed = -GameConfig.MAX_PLAYER_X_SPEED;
        }

        player.setX(player.getX() + xSpeed);
        blockPlayerFromLeavingTheWorld();
    }


    private void blockPlayerFromLeavingTheWorld() {
        float playerX = MathUtils.clamp(player.getX(),
                0,
                GameConfig.WORLD_WIDTH);

        player.setPosition(playerX, player.getY());

    }

    private boolean isPlayerCollidingWithObstacle() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isNotHit() && obstacle.isPlayerColliding(player)) {
                hit.play();
                return true;
            }
        }

        return false;
    }

    private void updateObstacles(float delta) {
        for (Obstacle obstacle : obstacles) {
            obstacle.update();
        }

        createNewObstacle(delta);
        removePassedObstacles();
    }


    private void createNewObstacle(float delta) {
        obstacleTimer += delta;

        if (obstacleTimer >= GameConfig.OBSTACLE_SPAWN_TIME) {
            float min = 0;
            float max = GameConfig.WORLD_WIDTH - GameConfig.OBSTACLE_SIZE;

            float obstacleX = MathUtils.random(min, max);
            float obstacleY = GameConfig.WORLD_HEIGHT;

            Obstacle obstacle = obstaclePool.obtain();
            DifficultyLevel difficultyLevel = GameManager.INSTANCE.getDifficultyLevel();
            obstacle.setYSpeed(difficultyLevel.getObstacleSpeed());
            obstacle.setPosition(obstacleX, obstacleY);

            obstacles.add(obstacle);
            obstacleTimer = 0f;
        }
    }


    private void removePassedObstacles(){
        if(obstacles.size > 0){
            Obstacle first = obstacles.first();

            float minObstacleY = -GameConfig.OBSTACLE_SIZE;

            if(first.getY() < minObstacleY){
                obstacles.removeValue(first, true);
                obstaclePool.free(first);
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
}
