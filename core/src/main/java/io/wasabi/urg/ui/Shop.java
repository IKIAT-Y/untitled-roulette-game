package io.wasabi.urg.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

public class Shop extends InputAdapter {
    private static final float WIDTH = 680f;
    private static final float HEIGHT = 900f;
    private static final float CENTER_Y = -HEIGHT / 2f;
    private static final float OFFSCREEN_X = -1500f;
    private static final float OFFSCREEN_Y = -1500f;

    private static final int OFFER_COUNT = 4;
    private static final int CHARM_OFFER_COUNT = 3;
    private static final int REROLL_PRICE = 5;

    private static final float OFFER_START_Y = 155f;
    private static final float OFFER_TARGET_WIDTH = 620f;
    private static final float CHARM_OFFER_START_Y = OFFER_START_Y - 250f;
    // Shop appearance settings — adjust these to resize the price text/button.
    private static final float PRICE_FONT_SCALE = 0.6f;

    private static final BitmapFont FONT = FontManager.getInstance().getFontByName("Placeholder");
    private static final BitmapFont FONT_64PX = FontManager.getInstance().getFontByName("Terminus64PXBold");

    private static final Texture PATCH_TEXTURE = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));

    private final SpriteBatch spriteBatch;
    private final Viewport viewport;
    private final RunState runState;
    private final List<Card> offers = new ArrayList<>();
    private final List<AbstractCharm> charmOffers = new ArrayList<>();
    private final NinePatch patch = new NinePatch(PATCH_TEXTURE, 10, 10, 10, 10);

    private final Rectangle continueButton = new Rectangle();
    private final Rectangle rerollButton = new Rectangle();
    private final Rectangle buyBox = new Rectangle();
    private final Rectangle sellBox = new Rectangle();

    private Tween tween;
    private Tween tweenY;
    private float x = OFFSCREEN_X;
    private float y = OFFSCREEN_Y;
    private boolean visible;
    private boolean continueRequested;

    private Card draggedCard;
    private boolean draggingOffer;
    private AbstractCharm draggedCharm;
    private boolean draggingCharmOffer;
    private final Vector2 dragOffset = new Vector2();

    public Shop(SpriteBatch spriteBatch, Viewport viewport) {
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
        tween = new Tween(0.9f, OFFSCREEN_X, -95f, Tween.TweenStyle.CIRCULAR, Tween.TweenDirection.OUT);
        tweenY = new Tween(0.7f, OFFSCREEN_Y, 0, Tween.TweenStyle.CIRCULAR, Tween.TweenDirection.OUT);
    }

    public void hide() {
        returnOffersToPool();
        returnCharmOffersToPool();
        draggedCard = null;
        draggedCharm = null;
        //visible = false; // stop sudden disappearance of shop when tweening out
        tween = new Tween(1f, x, OFFSCREEN_X, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
        tweenY = new Tween(1f, y, OFFSCREEN_Y, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
    }

    public void update(float delta) {
        if (visible && tween != null && !tween.isComplete()) {
            x = tween.update(delta);
            y = tweenY.update(delta);
        }
    }

    public void render() {
        if (!visible) return;

        float left = x - WIDTH / 2f;
        float bottom = y - HEIGHT / 2f;
        layoutControls(bottom, left);
        layoutOffers(left);
        layoutCharmOffers(left);

        // shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // shapeRenderer.setColor(0.10f, 0.10f, 0.13f, 1f);
        // shapeRenderer.rect(CENTER_Y, bottom - 300f, WIDTH, HEIGHT + 300f);
        // shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 1f);
        // shapeRenderer.rect(CENTER_Y, bottom + HEIGHT - 90f, WIDTH, 90f);

        // shapeRenderer.setColor(0.22f, 0.22f, 0.27f, 1f);
        // for (Card card : offers) {
        //     shapeRenderer.rect(card.getX() - 12f, card.getY() - 42f, 120f, 182f);
        // }
        // shapeRenderer.setColor(0.27f, 0.22f, 0.27f, 1f);
        // for (AbstractCharm charm : charmOffers) {
        //     shapeRenderer.rect(charm.getX() - 8f, charm.getY() - 8f, charm.getWidth() + 16f, charm.getHeight() + 16f);
        // }

        // shapeRenderer.setColor(0.22f, 0.50f, 0.28f, 1f);
        // shapeRenderer.rect(buyBox.x, buyBox.y, buyBox.width, buyBox.height);
        // shapeRenderer.setColor(0.55f, 0.25f, 0.25f, 1f);
        // shapeRenderer.rect(sellBox.x, sellBox.y, sellBox.width, sellBox.height);
        // shapeRenderer.setColor(0.35f, 0.35f, 0.65f, 1f);
        // shapeRenderer.rect(rerollButton.x, rerollButton.y, rerollButton.width, rerollButton.height);
        // shapeRenderer.setColor(0.25f, 0.60f, 0.30f, 1f);
        // shapeRenderer.rect(continueButton.x, continueButton.y, continueButton.width, continueButton.height);
        // shapeRenderer.end();

        spriteBatch.begin();
        spriteBatch.setTransformMatrix(new com.badlogic.gdx.math.Matrix4().setToTranslation(0, 0, 0));

        // white outline
        spriteBatch.setColor(1, 1, 1, 1);
        patch.draw(spriteBatch, left - 4f, CENTER_Y, WIDTH + 8, HEIGHT);

        // black inner
        spriteBatch.setColor(0.10f, 0.10f, 0.13f, 1f);
        patch.draw(spriteBatch, left, CENTER_Y + 2.5f, WIDTH, HEIGHT - 5);

        // buy box
        float boxPadding = 3f;
        spriteBatch.setColor(1, 1, 1, 1);
        patch.draw(spriteBatch, buyBox.x - boxPadding/2, buyBox.y - boxPadding/2, buyBox.width + boxPadding, buyBox.height + boxPadding);
        spriteBatch.setColor(0.4f, 0.6f, 0.4f, 1);
        patch.draw(spriteBatch, buyBox.x, buyBox.y, buyBox.width, buyBox.height);

        // sell box
        spriteBatch.setColor(1, 1, 1, 1);
        patch.draw(spriteBatch, sellBox.x - boxPadding / 2, sellBox.y - boxPadding / 2, sellBox.width + boxPadding, sellBox.height + boxPadding);
        spriteBatch.setColor(0.6f, 0.4f, 0.4f, 1);
        patch.draw(spriteBatch, sellBox.x, sellBox.y, sellBox.width, sellBox.height);

        // continue
        spriteBatch.setColor(1, 1, 1, 1);
        patch.draw(spriteBatch, continueButton.x - boxPadding / 2, continueButton.y - boxPadding / 2, continueButton.width + boxPadding, continueButton.height + boxPadding);
        spriteBatch.setColor(0.2f, 0.6f, 0.2f, 1);
        patch.draw(spriteBatch, continueButton.x, continueButton.y, continueButton.width, continueButton.height);

        // REROLL
        spriteBatch.setColor(1, 1, 1, 1);
        patch.draw(spriteBatch, rerollButton.x - boxPadding / 2, rerollButton.y - boxPadding / 2, rerollButton.width + boxPadding, rerollButton.height + boxPadding);
        spriteBatch.setColor(0.6f, 0.5f, 0.2f, 1);
        patch.draw(spriteBatch, rerollButton.x, rerollButton.y, rerollButton.width, rerollButton.height);

        spriteBatch.setColor(1, 1, 1, 1);

        FONT_64PX.draw(spriteBatch, "SHOP", left + 30f, HEIGHT / 2 - 30f);

        GlyphLayout layout = new GlyphLayout();
        layout.setText(FONT, "BUY", Color.WHITE, buyBox.width, Align.center, false);
        FONT.draw(spriteBatch, "BUY", buyBox.x, buyBox.y + buyBox.height / 2f + layout.height / 2f, buyBox.width, Align.center, false);

        layout.setText(FONT, "SELL", Color.WHITE, buyBox.width, Align.center, false);
        FONT.draw(spriteBatch, "SELL", sellBox.x, sellBox.y + sellBox.height / 2f + layout.height / 2f, sellBox.width, Align.center, false);

        String rerollText = "REROLL - " + REROLL_PRICE;
        layout.setText(FONT, rerollText, Color.WHITE, rerollButton.width, Align.center, false);
        FONT.draw(spriteBatch, rerollText, rerollButton.x, rerollButton.y + rerollButton.height / 2f + layout.height / 2f, rerollButton.width, Align.center, false);

        layout.setText(FONT, "CONTINUE", Color.WHITE, continueButton.width, Align.center, false);
        FONT.draw(spriteBatch, "CONTINUE", continueButton.x, continueButton.y + continueButton.height / 2f + layout.height / 2f, continueButton.width, Align.center, false);

        for (Card card : offers) {
            if (card == draggedCard) continue;
            renderCard(card);
        }
        for (AbstractCharm charm : charmOffers) {
            if (charm == draggedCharm) continue;
            renderCharm(charm);
        }
        spriteBatch.end();
    }

    public void renderCard(Card card) {
        card.render();
        float previousScaleX = FONT.getData().scaleX;
        float previousScaleY = FONT.getData().scaleY;
        FONT.getData().setScale(PRICE_FONT_SCALE);
        FONT.draw(spriteBatch, card.getPrice() + " TICKETS", card.getX() - 12f, card.getY() - 18f, 120f, Align.center, false);
        FONT.getData().setScale(previousScaleX, previousScaleY);
    }

    public void renderCharm(AbstractCharm charm) {
        charm.render();
        float previousScaleX = FONT.getData().scaleX;
        float previousScaleY = FONT.getData().scaleY;
        FONT.getData().setScale(PRICE_FONT_SCALE);
        FONT.draw(spriteBatch, charm.getPrice() + " TICKETS", charm.getX() - 8f, charm.getY() - 12f, charm.getWidth() + 16f, Align.center, false);
        FONT.getData().setScale(previousScaleX, previousScaleY);
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

        for (int i = charmOffers.size() - 1; i >= 0; i--) {
            AbstractCharm charm = charmOffers.get(i);
            if (charm.contains(world.x, world.y)) {
                beginCharmDrag(charm, true, world);
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

    public void finishInventoryCardDrag(int x, int y, Card card) {
        Vector2 world = screenToWorld(x, y);
        draggedCard = card;
        draggingOffer = false;
        finishCardDrag(world);
    }

    public void finishInventoryCharmDrag(int x, int y, AbstractCharm charm) {
        Vector2 world = screenToWorld(x, y);
        draggedCharm = charm;
        draggingCharmOffer = false;
        finishCharmDrag(world);
    }

    private void finishCardDrag(Vector2 world) {
        Card card = draggedCard;
        boolean droppedInTarget = draggingOffer ? buyBox.contains(world) : sellBox.contains(world);
        card.setDragging(false);
        if (droppedInTarget) {
            if (draggingOffer) buy(card);
            else sell(card);
        }
        draggedCard = null;
    }

    private void finishCharmDrag(Vector2 world) {
        AbstractCharm charm = draggedCharm;
        boolean droppedInTarget = draggingCharmOffer ? buyBox.contains(world) : sellBox.contains(world);
        charm.setDragging(false);
        if (droppedInTarget) {
            if (draggingCharmOffer) buyCharm(charm);
            else sellCharm(charm);
        }
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

    private void layoutControls(float bottom, float left) {
        buyBox.set(280f, bottom + 150f, 225f, 300f);
        sellBox.set(550f, bottom + 150f, 225f, 300f);
        rerollButton.set(left + WIDTH / 2 - OFFER_TARGET_WIDTH / 2, -HEIGHT / 2f + 30f, OFFER_TARGET_WIDTH, 65f);
        continueButton.set(280f, bottom + 30f, 495f, 65f);
    }

    private void layoutOffers(float left) {
        int size = OFFER_COUNT; // offers.size();
        float spacing = (OFFER_TARGET_WIDTH - (size * 96f)) / size;
        float targetX = spacing / 2 + left + WIDTH / 2 - OFFER_TARGET_WIDTH / 2;
        for (int i = 0; i < offers.size(); i++) {
            Card card = offers.get(i);
            if (!card.isDragging()) {
                card.setPosition(targetX, OFFER_START_Y);
            }
            targetX += spacing + card.getWidth();
        }
    }

    private void layoutCharmOffers(float left) {
        int size = CHARM_OFFER_COUNT; // offers.size();
        float spacing = (OFFER_TARGET_WIDTH - (size * 64f)) / size;
        float targetX = spacing / 2 + left + WIDTH / 2 - OFFER_TARGET_WIDTH / 2;
        for (int i = 0; i < charmOffers.size(); i++) {
            AbstractCharm charm = charmOffers.get(i);
            if (!charm.isDragging()) {
                charm.setPosition(targetX, CHARM_OFFER_START_Y);
            }
            targetX += spacing + charm.getWidth();
        }
    }

    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0f));
        return new Vector2(world.x, world.y);
    }

    public List<Card> getOffers() { return offers; }
    public List<AbstractCharm> getCharmOffers() { return charmOffers; }
    public boolean isVisible() { return visible; }

    public Card getDraggedCard() { return draggedCard; }
    public AbstractCharm getDraggedCharm() { return draggedCharm; }
}
