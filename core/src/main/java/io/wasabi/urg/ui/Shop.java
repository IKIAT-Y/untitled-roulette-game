package io.wasabi.urg.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

public class Shop extends InputAdapter {
    private static final float WIDTH = 1200f;
    private static final float HEIGHT = 650f;
    private static final float CENTER_X = -WIDTH / 2f;
    private static final float OFFSCREEN_Y = -1500f;
    private static final int OFFER_COUNT = 4;
    private static final int CHARM_OFFER_COUNT = 3;
    private static final int REROLL_PRICE = 5;
    private static final float OFFER_START_X = -300f;
    private static final float OFFER_SPACING = 160f;
    private static final float CHARM_OFFER_START_X = -260f;
    private static final float CHARM_OFFER_SPACING = 160f;
    // Shop appearance settings — adjust these to resize the price text/button.
    private static final float PRICE_FONT_SCALE = 0.6f;
    private static final float REROLL_BUTTON_WIDTH = 260f;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final Viewport viewport;
    private final RunState runState;
    private final List<Card> offers = new ArrayList<>();
    private final List<AbstractCharm> charmOffers = new ArrayList<>();

    private final Rectangle continueButton = new Rectangle();
    private final Rectangle rerollButton = new Rectangle();
    private final Rectangle buyBox = new Rectangle();
    private final Rectangle sellBox = new Rectangle();

    private Tween tween;
    private float y = OFFSCREEN_Y;
    private boolean visible;
    private boolean continueRequested;

    private Card draggedCard;
    private boolean draggingOffer;
    private AbstractCharm draggedCharm;
    private boolean draggingCharmOffer;
    private final Vector2 dragOffset = new Vector2();

    public Shop(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, Viewport viewport) {
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.viewport = viewport;
        this.runState = Roulette.getInstance().getRunState();
    }

    public void show() {
        returnOffersToPool();
        returnCharmOffersToPool();
        drawOffers();
        drawCharmOffers();
        visible = true;
        continueRequested = false;
        tween = new Tween(1f, OFFSCREEN_Y, 0f, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public void hide() {
        returnOffersToPool();
        returnCharmOffersToPool();
        draggedCard = null;
        draggedCharm = null;
        //visible = false; // stop sudden disappearance of shop when tweening out
        tween = new Tween(1f, y, OFFSCREEN_Y, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
    }

    public void update(float delta) {
        if (visible && tween != null && !tween.isComplete()) {
            y = tween.update(delta);
        }
    }

    public void render() {
        if (!visible) return;

        float bottom = y - HEIGHT / 2f;
        layoutControls(bottom);
        layoutOffers(bottom);
        layoutCharmOffers(bottom);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.10f, 0.13f, 1f);
        shapeRenderer.rect(CENTER_X, bottom - 300f, WIDTH, HEIGHT + 300f);
        shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 1f);
        shapeRenderer.rect(CENTER_X, bottom + HEIGHT - 90f, WIDTH, 90f);

        shapeRenderer.setColor(0.22f, 0.22f, 0.27f, 1f);
        for (Card card : offers) {
            shapeRenderer.rect(card.getX() - 12f, card.getY() - 42f, 120f, 182f);
        }
        shapeRenderer.setColor(0.27f, 0.22f, 0.27f, 1f);
        for (AbstractCharm charm : charmOffers) {
            shapeRenderer.rect(charm.getX() - 8f, charm.getY() - 8f, charm.getWidth() + 16f, charm.getHeight() + 16f);
        }

        shapeRenderer.setColor(0.22f, 0.50f, 0.28f, 1f);
        shapeRenderer.rect(buyBox.x, buyBox.y, buyBox.width, buyBox.height);
        shapeRenderer.setColor(0.55f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(sellBox.x, sellBox.y, sellBox.width, sellBox.height);
        shapeRenderer.setColor(0.35f, 0.35f, 0.65f, 1f);
        shapeRenderer.rect(rerollButton.x, rerollButton.y, rerollButton.width, rerollButton.height);
        shapeRenderer.setColor(0.25f, 0.60f, 0.30f, 1f);
        shapeRenderer.rect(continueButton.x, continueButton.y, continueButton.width, continueButton.height);
        shapeRenderer.end();

        spriteBatch.begin();
        spriteBatch.setTransformMatrix(new com.badlogic.gdx.math.Matrix4().setToTranslation(0, 0, 0));
        BitmapFont font = FontManager.getInstance().getFontByName("Placeholder");
        font.draw(spriteBatch, "SHOP", -30f, bottom + HEIGHT - 30f);
        font.draw(spriteBatch, "BUY", buyBox.x + 55f, buyBox.y + 85f);
        font.draw(spriteBatch, "SELL", sellBox.x + 50f, sellBox.y + 85f);
        font.draw(spriteBatch, "REROLL - " + REROLL_PRICE, rerollButton.x + 30f, rerollButton.y + 43f);
        font.draw(spriteBatch, "CONTINUE", continueButton.x + 75f, continueButton.y + 43f);

        for (Card card : offers) {
            card.render();
            float previousScaleX = font.getData().scaleX;
            float previousScaleY = font.getData().scaleY;
            font.getData().setScale(PRICE_FONT_SCALE);
            font.draw(spriteBatch, card.getPrice() + " TICKETS", card.getX() + 12f, card.getY() - 18f);
            font.getData().setScale(previousScaleX, previousScaleY);
        }
        for (AbstractCharm charm : charmOffers) {
            charm.render();
            float previousScaleX = font.getData().scaleX;
            float previousScaleY = font.getData().scaleY;
            font.getData().setScale(PRICE_FONT_SCALE);
            font.draw(spriteBatch, charm.getPrice() + " TICKETS", charm.getX() + 8f, charm.getY() - 12f);
            font.getData().setScale(previousScaleX, previousScaleY);
        }
        spriteBatch.end();
    }

    public boolean handleInput() {
        if (!continueRequested) return false;
        continueRequested = false;
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!visible) return false;
        Vector2 world = screenToWorld(screenX, screenY);

        for (int i = offers.size() - 1; i >= 0; i--) {
            Card card = offers.get(i);
            if (card.contains(world.x, world.y)) {
                beginCardDrag(card, true, world);
                return true;
            }
        }

        List<Card> ownedCards = runState.getOwnedCards();
        for (int i = ownedCards.size() - 1; i >= 0; i--) {
            Card card = ownedCards.get(i);
            if (card.contains(world.x, world.y)) {
                beginCardDrag(card, false, world);
                return true;
            }
        }

        for (int i = charmOffers.size() - 1; i >= 0; i--) {
            AbstractCharm charm = charmOffers.get(i);
            if (charm.contains(world.x, world.y)) {
                beginCharmDrag(charm, true, world);
                return true;
            }
        }

        List<AbstractCharm> ownedCharms = runState.getOwnedCharms();
        for (int i = ownedCharms.size() - 1; i >= 0; i--) {
            AbstractCharm charm = ownedCharms.get(i);
            if (charm.contains(world.x, world.y)) {
                beginCharmDrag(charm, false, world);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!visible) return false;
        Vector2 world = screenToWorld(screenX, screenY);

        if (draggedCard != null) {
            draggedCard.setPosition(world.x - dragOffset.x, world.y - dragOffset.y);
            if (!draggingOffer) {
                List<Card> ownedCards = runState.getOwnedCards();
                int currentIndex = ownedCards.indexOf(draggedCard);
                int closestIndex = CardLayout.getClosestIndex(
                    draggedCard.getX(), ownedCards.size(), viewport.getWorldWidth());
                if (closestIndex != currentIndex) {
                    runState.reorderCard(draggedCard, closestIndex);
                }
            }
            return true;
        }

        if (draggedCharm != null) {
            draggedCharm.setPosition(world.x - dragOffset.x, world.y - dragOffset.y);
            if (!draggingCharmOffer) {
                List<AbstractCharm> ownedCharms = runState.getOwnedCharms();
                int currentIndex = ownedCharms.indexOf(draggedCharm);
                int closestIndex = CharmLayout.getClosestIndex(
                    draggedCharm.getX(), ownedCharms.size(), viewport.getWorldWidth());
                if (closestIndex != currentIndex) {
                    runState.reorderCharm(draggedCharm, closestIndex);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!visible) return false;
        Vector2 world = screenToWorld(screenX, screenY);

        if (draggedCard != null) {
            finishCardDrag(world);
            return true;
        }
        if (draggedCharm != null) {
            finishCharmDrag(world);
            return true;
        }
        if (continueButton.contains(world)) {
            continueRequested = true;
            return true;
        }
        if (rerollButton.contains(world)) {
            reroll();
            return true;
        }
        return false;
    }

    private void beginCardDrag(Card card, boolean offer, Vector2 world) {
        draggedCard = card;
        draggingOffer = offer;
        card.setDragging(true);
        dragOffset.set(world.x - card.getX(), world.y - card.getY());
    }

    private void beginCharmDrag(AbstractCharm charm, boolean offer, Vector2 world) {
        draggedCharm = charm;
        draggingCharmOffer = offer;
        charm.setDragging(true);
        dragOffset.set(world.x - charm.getX(), world.y - charm.getY());
    }

    private void finishCardDrag(Vector2 world) {
        Card card = draggedCard;
        boolean droppedInTarget = draggingOffer ? buyBox.contains(world) : sellBox.contains(world);
        if (droppedInTarget) {
            if (draggingOffer) buy(card);
            else sell(card);
        }
        card.setDragging(false);
        draggedCard = null;
    }

    private void finishCharmDrag(Vector2 world) {
        AbstractCharm charm = draggedCharm;
        boolean droppedInTarget = draggingCharmOffer ? buyBox.contains(world) : sellBox.contains(world);
        if (droppedInTarget) {
            if (draggingCharmOffer) buyCharm(charm);
            else sellCharm(charm);
        }
        charm.setDragging(false);
        draggedCharm = null;
    }

    private void buy(Card card) {
        if (!runState.canAddCard(card) || runState.getTickets() < card.getPrice()) return;
        if (runState.spendTickets(card.getPrice()) && runState.addCard(card)) {
            offers.remove(card);
        }
    }

    private void sell(Card card) {
        if (runState.removeCard(card)) {
            runState.addTickets(card.getSellPrice());
            Roulette.getInstance().getCardPool().returnCard(card);
        }
    }

    private void buyCharm(AbstractCharm charm) {
        if (!runState.canAddCharm(charm) || runState.getTickets() < charm.getPrice()) return;
        if (runState.spendTickets(charm.getPrice()) && runState.addCharm(charm)) {
            charmOffers.remove(charm);
        }
    }

    private void sellCharm(AbstractCharm charm) {
        if (runState.removeCharm(charm)) {
            runState.addTickets(charm.getSellPrice());
            Roulette.getInstance().getCharmPool().returnCharm(charm);
        }
    }

    private void reroll() {
        if (!runState.spendTickets(REROLL_PRICE)) return;

        List<Card> previousOffers = new ArrayList<>(offers);
        offers.clear();
        drawOffers();
        for (Card card : previousOffers) {
            Roulette.getInstance().getCardPool().returnCard(card);
        }

        List<AbstractCharm> previousCharmOffers = new ArrayList<>(charmOffers);
        charmOffers.clear();
        drawCharmOffers();
        for (AbstractCharm charm : previousCharmOffers) {
            Roulette.getInstance().getCharmPool().returnCharm(charm);
        }
    }

    private void drawOffers() {
        int attempts = 0;
        while (offers.size() < OFFER_COUNT && attempts++ < 30) {
            Card card = Roulette.getInstance().getCardPool().getRandomCard();
            if (card == null) break;
            if (runState.ownsCardType(card)) {
                Roulette.getInstance().getCardPool().returnCard(card);
                continue;
            }
            offers.add(card);
        }
    }

    private void drawCharmOffers() {
        int attempts = 0;
        while (charmOffers.size() < CHARM_OFFER_COUNT && attempts++ < 30) {
            AbstractCharm charm = Roulette.getInstance().getCharmPool().getRandomCharm();
            if (charm == null) break;
            if (runState.ownsCharmType(charm) || offersContainType(charmOffers, charm)) {
                Roulette.getInstance().getCharmPool().returnCharm(charm);
                continue;
            }
            charmOffers.add(charm);
        }
    }

    private boolean offersContainType(List<AbstractCharm> offerList, AbstractCharm charm) {
        for (AbstractCharm offered : offerList) {
            if (offered.getClass() == charm.getClass()) {
                return true;
            }
        }
        return false;
    }

    private void returnOffersToPool() {
        for (Card card : offers) Roulette.getInstance().getCardPool().returnCard(card);
        offers.clear();
    }

    private void returnCharmOffersToPool() {
        for (AbstractCharm charm : charmOffers) Roulette.getInstance().getCharmPool().returnCharm(charm);
        charmOffers.clear();
    }

    private void layoutControls(float bottom) {
        buyBox.set(560f, bottom + 245f, 160f, 150f);
        sellBox.set(-720f, bottom + 245f, 160f, 150f);
        rerollButton.set(-290f, bottom - 75f, REROLL_BUTTON_WIDTH, 65f);
        continueButton.set(30f, bottom - 75f, 240f, 65f);
    }

    private void layoutOffers(float bottom) {
        for (int i = 0; i < offers.size(); i++) {
            Card card = offers.get(i);
            if (!card.isDragging()) {
                card.setPosition(OFFER_START_X + i * OFFER_SPACING, bottom + 380f);
            }
        }
    }

    private void layoutCharmOffers(float bottom) {
        for (int i = 0; i < charmOffers.size(); i++) {
            AbstractCharm charm = charmOffers.get(i);
            if (!charm.isDragging()) {
                charm.setPosition(CHARM_OFFER_START_X + i * CHARM_OFFER_SPACING, bottom + 170f);
            }
        }
    }

    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0f));
        return new Vector2(world.x, world.y);
    }

    public boolean isVisible() { return visible; }
}
