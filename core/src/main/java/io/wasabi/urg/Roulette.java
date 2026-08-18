package io.wasabi.urg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.screens.GameScreen;
import io.wasabi.urg.state.RunState;

public class Roulette extends Game {
    private static final Roulette INSTANCE = new Roulette();
    private final RunState runState = new RunState();
    private final RoundManager roundManager = new RoundManager(runState);
    private final SoundManager soundManager = SoundManager.getInstance();

    // Renderers
    private RendererManager rendererManager;

    private Viewport viewport;
    private OrthographicCamera camera;

    private Roulette() {}

    public static Roulette getInstance() {
        return INSTANCE;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(640, 480, camera); // change this depending on actual game size at launch (?)

        rendererManager = RendererManager.getInstance();
        rendererManager.initialize(this);

        FontManager.getInstance().initialize(this);

        soundManager.initialize();

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
        soundManager.dispose();
    }

    public RunState getRunState() { return runState; }
    public Viewport getViewport() { return viewport; }
    public RoundManager getRoundManager() { return roundManager; }
}
