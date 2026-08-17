package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.BettingTable;
import io.wasabi.urg.managers.RendererManager;

public class BettingScreen implements Screen {

    private ShapeRenderer shapeRenderer;

    // Elements
    private BettingTable bettingTable;

    public BettingScreen(final Roulette game) {
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        bettingTable.dispose();
    }
}
