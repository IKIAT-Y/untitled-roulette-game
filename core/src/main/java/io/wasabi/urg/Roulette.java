package io.wasabi.urg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.elements.charm.BlackCharm;
import io.wasabi.urg.elements.charm.RedCharm;
import io.wasabi.urg.managers.CardPool;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.managers.TextureManager;
import io.wasabi.urg.screens.BettingScreen;
import io.wasabi.urg.screens.GameScreen;
import io.wasabi.urg.state.RunState;

public class Roulette extends Game {
    private static final Roulette INSTANCE = new Roulette();
    private GameScreen gameScreen;
    private BettingScreen bettingScreen;
    private final RunState runState = new RunState();
    private final float MIN_WORLD_WIDTH = 1600f; // Minimum width of the game world
    private final float MIN_WORLD_HEIGHT = 900f; // Minimum height of the game world
    private final RoundManager roundManager = new RoundManager(runState);
    private final SoundManager soundManager = SoundManager.getInstance();

    private CardPool cardPool;

    // Renderers
    private RendererManager rendererManager;

    private Viewport viewport;
    private OrthographicCamera camera;

    private Roulette() {
    }

    public static Roulette getInstance() {
        return INSTANCE;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(MIN_WORLD_WIDTH, MIN_WORLD_HEIGHT, camera); // change this depending on actual
                                                                                  // game size at launch (?)

        rendererManager = RendererManager.getInstance();
        rendererManager.initialize(this);

        FontManager.getInstance().initialize(this);

        soundManager.initialize();
        TextureManager.getInstance().initialize();

        cardPool = new CardPool();

        // Temporarily set to 10000 for testing purposes. Change to 100 for final release.
        int STARTING_MONEY = 100;
        runState.reset(STARTING_MONEY);

        // Card testing
        runState.addCard(cardPool.getRandomCard());

        // Charm testing
        runState.addCharm(new BlackCharm());
        runState.addCharm(new BlackCharm());
        runState.addCharm(new BlackCharm());
        runState.addCharm(new RedCharm());

        this.gameScreen = new GameScreen(this);
        this.bettingScreen = new BettingScreen(this);

        this.setScreen(this.gameScreen);

        roundManager.startRound();
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
        if (getGameScreen() != null) {
            getGameScreen().dispose();
        }
        soundManager.dispose();
        TextureManager.getInstance().dispose();
    }

    public RunState getRunState() {
        return runState;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public float getWorldWidth() {
        return MIN_WORLD_WIDTH;
    }

    public float getWorldHeight() {
        return MIN_WORLD_HEIGHT;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public BettingScreen getBettingScreen() { return bettingScreen; }

    public RoundManager getRoundManager() {
        return roundManager;
    }

    public CardPool getCardPool() {
        return cardPool;
    }
}
