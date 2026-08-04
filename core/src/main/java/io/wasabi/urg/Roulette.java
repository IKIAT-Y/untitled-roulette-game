package io.wasabi.urg;

import com.badlogic.gdx.Game;

import io.wasabi.urg.screens.GameScreen;

public class Roulette extends Game {

    @Override
    public void create() {
        this.setScreen(new GameScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {

    }
}
