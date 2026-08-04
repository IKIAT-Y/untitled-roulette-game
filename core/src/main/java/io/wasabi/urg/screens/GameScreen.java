package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;

public class GameScreen implements Screen {

    final Roulette game;

    public GameScreen(final Roulette game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        // TODO: game screen rendering
        // includes the roulette wheel & the ui

        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
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

    }
}
