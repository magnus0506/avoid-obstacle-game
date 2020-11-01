package com.obstacleavoid.screen.menu;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Logger;
import com.obstacleavoid.ObstacleAvoidGame;
import com.obstacleavoid.assets.AssetDescriptors;
import com.obstacleavoid.assets.RegionNames;
import com.obstacleavoid.common.GameManager;
import com.obstacleavoid.config.DifficultyLevel;

public class OptionsScreen extends MenuScreenBase {
    private static final Logger log = new Logger(OptionsScreen.class.getName(), Logger.DEBUG);

    private ButtonGroup<CheckBox> checkBoxButtonGroup;
    private CheckBox easy;
    private CheckBox medium;
    private CheckBox hard;

    public OptionsScreen(ObstacleAvoidGame game) {
        super(game);
    }

    @Override
    protected Actor createUi() {

        Table table = new Table();
        table.defaults().pad(15);

        TextureAtlas gamePlayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        Skin uiSkin = assetManager.get(AssetDescriptors.UISKIN);

        TextureRegion backgroundRegion = gamePlayAtlas.findRegion(RegionNames.BACKGROUND);
        table.setBackground(new TextureRegionDrawable(backgroundRegion));

        // label
        Label label = new Label("DIFFICULTY", uiSkin);

        easy = checkBox(DifficultyLevel.EASY.name(), uiSkin);
        medium = checkBox(DifficultyLevel.MEDIUM.name(), uiSkin);
        hard = checkBox(DifficultyLevel.HARD.name(), uiSkin);
        checkBoxButtonGroup = new ButtonGroup<CheckBox>(easy, medium, hard);

        DifficultyLevel difficultyLevel = GameManager.INSTANCE.getDifficultyLevel();
        checkBoxButtonGroup.setChecked(difficultyLevel.name());

        TextButton backButton = new TextButton("BACK", uiSkin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                back();
            }
        });

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                difficultyChanged();
            }
        };

        easy.addListener(listener);
        medium.addListener(listener);
        hard.addListener(listener);

        Table contentTable = new Table(uiSkin);
        contentTable.defaults().pad(10);
        contentTable.setBackground(RegionNames.PANEL);

        contentTable.add(label).row();
        contentTable.add(easy).row();
        contentTable.add(medium).row();
        contentTable.add(hard).row();
        contentTable.add(backButton);

        table.add(contentTable);
        table.center();
        table.setFillParent(true);
        table.pack();

        return table;
    }

    private void back() {
        log.debug("back");
        game.setScreen(new MenuScreen(game));
    }

    private void difficultyChanged() {
        log.debug("difficultyChanged()");
        CheckBox checked = checkBoxButtonGroup.getChecked();
        if (checked == easy) {
            log.debug("easy");
            GameManager.INSTANCE.setDifficultyLevel(DifficultyLevel.EASY);
        } else if (checked == medium) {
            log.debug("medium");
            GameManager.INSTANCE.setDifficultyLevel(DifficultyLevel.MEDIUM);
        } else if (checked == hard) {
            log.debug("hard");
            GameManager.INSTANCE.setDifficultyLevel(DifficultyLevel.HARD);
        }
    }

    private static CheckBox checkBox(String text, Skin skin){
        CheckBox checkBox = new CheckBox(text, skin);
        checkBox.left().pad(10);
        checkBox.getLabelCell().pad(8);
        return checkBox;
    }
}
