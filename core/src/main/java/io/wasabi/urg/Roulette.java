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
    private final float MIN_WORLD_WIDTH = 640f; // Minimum width of the game world
    private final float MIN_WORLD_HEIGHT = 480f; // Minimum height of the game world

    // Renderers
    private RendererManager rendererManager;

    private Viewport viewport;
    private OrthographicCamera camera;

    private Roulette() {
    }

    public static Roulette getInstance() {
        return INSTANCE;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(MIN_WORLD_WIDTH, MIN_WORLD_HEIGHT, camera); // change this depending on actual
                                                                                  // game size at launch (?)

        rendererManager = RendererManager.getInstance();
        rendererManager.initialize(this);

        FontManager.getInstance().initialize(this);

        // Card testing
        runState.addCard(new io.wasabi.urg.elements.card.ExtraChange());

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

    public RunState getRunState() {
        return runState;
    }

    public Viewport getViewport() {
        return INSTANCE.viewport;
    }

    public OrthographicCamera getCamera() {
        return INSTANCE.camera;
    }

    public float getWorldWidth() {
        return MIN_WORLD_WIDTH;
    }

    public float getWorldHeight() {
        return MIN_WORLD_HEIGHT;
    }
}
