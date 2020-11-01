package com.obstacleavoid.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.obstacleavoid.config.GameConfig;

public class PlayerActor extends ActorBase {

    // -- constructor --

    public PlayerActor() {
        setCollisionRadius(GameConfig.PLAYER_BOUNDS_RADIUS);
        setSize(GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
    }

    // -- public methods --
    @Override
    public void act(float delta) {
        super.act(delta);
        update();
    }


    // -- private methods --
    private void update() {
        float xSpeed = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xSpeed = GameConfig.MAX_PLAYER_X_SPEED;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xSpeed = -GameConfig.MAX_PLAYER_X_SPEED;
        }

        setX(getX() + xSpeed);
        blockPlayerFromLeavingTheWorld();
    }


    private void blockPlayerFromLeavingTheWorld() {
        float playerX = MathUtils.clamp(getX(),
                0,
                GameConfig.WORLD_WIDTH - getWidth());

        setPosition(playerX, getY());

    }
}
