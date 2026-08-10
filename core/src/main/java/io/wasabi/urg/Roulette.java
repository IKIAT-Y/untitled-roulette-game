package io.wasabi.urg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.screens.GameScreen;
import io.wasabi.urg.state.RunState;

public class Roulette extends Game {

    private final RunState runState = new RunState();

    // Renderers
    private ShapeRenderer shapeRenderer;

    private Viewport viewport;
    private OrthographicCamera camera;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(640, 480, camera); // change this depending on actual game size at launch (?)

        shapeRenderer = new ShapeRenderer();

        this.setScreen(new GameScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    public RunState getRunState() { return runState; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
}
