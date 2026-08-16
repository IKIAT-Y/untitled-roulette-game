package io.wasabi.urg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.screens.GameScreen;
import io.wasabi.urg.state.RunState;

public class Roulette extends Game {
    private static final Roulette INSTANCE = new Roulette();
    private final RunState runState = new RunState();

    // Renderers
    private RendererManager rendererManager;

    private Viewport viewport;
    private OrthographicCamera camera;

    private Roulette() {}

    public static Roulette getInstance() {
        return INSTANCE;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(640, 480, camera); // change this depending on actual game size at launch (?)

        rendererManager = RendererManager.getInstance();
        rendererManager.initialize(this);

        FontManager.getInstance().initialize(this);

        this.setScreen(new GameScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        rendererManager.applyViewport(viewport);

        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    public RunState getRunState() { return runState; }
}
