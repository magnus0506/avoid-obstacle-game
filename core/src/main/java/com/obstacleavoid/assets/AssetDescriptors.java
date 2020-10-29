package com.obstacleavoid.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class AssetDescriptors {

    public static final AssetDescriptor<BitmapFont> FONT =
            new AssetDescriptor<BitmapFont>(AssetPaths.UI_FONT, BitmapFont.class);

    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
            new AssetDescriptor<>(AssetPaths.GAME_PLAY, TextureAtlas.class);

    public static final AssetDescriptor<TextureAtlas> UI =
            new AssetDescriptor<>(AssetPaths.UI,TextureAtlas.class);

    public AssetDescriptors() {
    }
}
