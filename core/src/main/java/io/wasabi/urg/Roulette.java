package io.wasabi.urg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.screens.GameScreen;
import io.wasabi.urg.state.RunState;

import java.util.ArrayList;
import java.util.List;

public class Roulette extends Game {
    private static final Roulette INSTANCE = new Roulette();
    private GameScreen gameScreen;
    private final RunState runState = new RunState();
    private final RoundManager roundManager = new RoundManager(runState);
    private final SoundManager soundManager = SoundManager.getInstance();

    private List<Card> commonCards = new ArrayList<>();
    private List<Card> uncommonCards = new ArrayList<>();
    private List<Card> rareCards = new ArrayList<>();

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

        initializeCardPool();

        // Card testing
        runState.addCard(getRandomCard());
        System.out.println("Added card: " + runState.getOwnedCards().get(0).getClass().getSimpleName());

        this.gameScreen = new GameScreen(this);
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
    }

    public Card getRandomCard() {
        // 5% chance for rare 25% chance for uncommon 70% chance for common
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
        if (getScreen() != null) {
            getScreen().dispose();
        }
        soundManager.dispose();
    }

    @Override
    public GameScreen getScreen() { return gameScreen; }
    public RunState getRunState() { return runState; }
    public Viewport getViewport() { return viewport; }
    public RoundManager getRoundManager() { return roundManager; }
}
