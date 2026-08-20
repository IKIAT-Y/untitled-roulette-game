package io.wasabi.urg;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.BlackCharm;
import io.wasabi.urg.elements.charm.RedCharm;
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

    private List<Card> commonCards = new ArrayList<>();
    private List<Card> uncommonCards = new ArrayList<>();
    private List<Card> rareCards = new ArrayList<>();

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

        initializeCardPool();

        runState.reset(100);

        // Card testing
        runState.addCard(getRandomCard());
        runState.addCard(getRandomCard());
        runState.addCard(getRandomCard());
        runState.addCard(getRandomCard());

        // Charm testing
        runState.addCharm(new BlackCharm());
        runState.addCharm(new RedCharm());

        this.gameScreen = new GameScreen(this);
        this.bettingScreen = new BettingScreen(this);

        this.setScreen(this.gameScreen);

        roundManager.startRound();
    }

    private void initializeCardPool() {
        // Common Cards
        commonCards.add(new io.wasabi.urg.elements.card.ExtraChange());
        commonCards.add(new io.wasabi.urg.elements.card.ExtraCredit());
        commonCards.add(new io.wasabi.urg.elements.card.BlackCard());
        commonCards.add(new io.wasabi.urg.elements.card.GreenCard());
        commonCards.add(new io.wasabi.urg.elements.card.OddCard());
        commonCards.add(new io.wasabi.urg.elements.card.Jackpot());
        commonCards.add(new io.wasabi.urg.elements.card.GoldenTicket());
        commonCards.add(new io.wasabi.urg.elements.card.OverweightSticker());

        // Uncommon Cards
        uncommonCards.add(new io.wasabi.urg.elements.card.AllIn());
        uncommonCards.add(new io.wasabi.urg.elements.card.FourLeafClover());
        uncommonCards.add(new io.wasabi.urg.elements.card.LuckyTalisman());
        uncommonCards.add(new io.wasabi.urg.elements.card.Ouroboros());

        // Rare Cards
        rareCards.add(new io.wasabi.urg.elements.card.Infinite());
        rareCards.add(new io.wasabi.urg.elements.card.MysteriousFragment());
        rareCards.add(new io.wasabi.urg.elements.card.Oneshot());
    }

    public Card getRandomCard() {
        // 5% chance for rare 25% chance for uncommon 70% chance for common
        // Moves down in rarity if the pool is empty for that rarity
        double roll = Math.random();

        if (roll < 0.05 && !rareCards.isEmpty()) {
            return getRareCard();
        } else if (roll < 0.3 && !uncommonCards.isEmpty()) {
            return getUncommonCard();
        } else {
            return getCommonCard();
        }
    }

    public Card getCommonCard() {
        if (commonCards.isEmpty()) {
            return null; // might need to add more error handling
        }
        int index = (int) (Math.random() * commonCards.size());

        Card card = commonCards.get(index);
        commonCards.remove(index);
        return card;
    }

    public Card getUncommonCard() {
        if (uncommonCards.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random() * uncommonCards.size());

        Card card = uncommonCards.get(index);
        uncommonCards.remove(index);
        return card;
    }

    public Card getRareCard() {
        if (rareCards.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random() * rareCards.size());

        Card card = rareCards.get(index);
        rareCards.remove(index);
        return card;
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
}
