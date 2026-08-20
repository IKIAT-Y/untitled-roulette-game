package io.wasabi.urg.screens;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.betting.BetScreenButton;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.CardInputHandler;
import io.wasabi.urg.managers.CharmInputHandler;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.CardLayout;
import io.wasabi.urg.ui.CharmLayout;
import io.wasabi.urg.ui.QuotaTracker;
import io.wasabi.urg.ui.RoundResult;
import io.wasabi.urg.ui.Shop;

public class GameScreen implements Screen {
    private final Roulette game;

    private enum GameState {
        ROUND,
        RESULT,
        SHOP
    }

    private GameState gameState;

    // Renderers
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    private final Texture ticketTexture;
    // Matrices for UI rendering
    private final Matrix4 uiProjection = new Matrix4();
    private final Matrix4 uiTransform = new Matrix4();
    // Physics
    private final World world;

    // Elements
    private Ball ball;
    private Wheel wheel;
    private Vector2 wheelCenter = new Vector2(0f, 0);

    // UI
    private RoundResult roundResult;
    private Shop shop;
    private QuotaTracker quotaTracker;

    // Handlers
    private CardInputHandler cardInputHandler = new CardInputHandler(Roulette.getInstance().getRunState(), Roulette.getInstance().getViewport());
    private CharmInputHandler charmInputHandler = new CharmInputHandler(Roulette.getInstance().getRunState(), Roulette.getInstance().getViewport());
    private InputMultiplexer inputMultiplexer = new InputMultiplexer(cardInputHandler, charmInputHandler);
    
    // Betting
    private Texture betButtonTexture;
    private BetScreenButton betButton;
    private float baseWindowWidth;
    private float baseWindowHeight;
    private float baseButtonWidth;
    private float baseButtonHeight;
    private final Rectangle debugWinButton = new Rectangle();

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
        this.spriteBatch = RendererManager.getInstance().getSpriteBatch();

        this.ticketTexture = new Texture(Gdx.files.internal("ticket.png"));
        this.world = new World(new Vector2(0f, 0f), true);

        this.gameState = GameState.ROUND;

        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);

        this.roundResult = new RoundResult(shapeRenderer, spriteBatch);
        this.shop = new Shop(shapeRenderer, spriteBatch, game.getViewport());
        this.inputMultiplexer.addProcessor(0, shop);
        this.quotaTracker = new QuotaTracker(shapeRenderer, spriteBatch, game.getRunState(), game.getRoundManager());

        //enterResultScreen(0, 0, 0, 0, 0);
        //enterShopScreen();
        //spin();
    }

    private void spin() {
        // Move this to launch method when the player presses the spin button
        float startAngleRad = 0f;
        float initialSpeed = new Random().nextFloat() * (1000f) + 5000f;
        float outerTrackRadius = 400f;
        float innerWheelRadius = 325f;
        Roulette.getInstance().getRunState().triggerCardEffects("beforeSpin");
        SoundManager.getInstance().playSound("spin1");
        ball.launch(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
        wheel.spin(6.0f, -10f);
        quotaTracker.onSpinStarted();
    }

    private void handleWheelClick() {
        if (gameState != GameState.ROUND
                || !Gdx.input.justTouched()
                || ball.getState() != Ball.State.STOPPED) {
            return;
        }

        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        game.getCamera().unproject(touch);

        if (wheel.containsPoint(new Vector2(touch.x, touch.y))) {
            spin();
        }
    }

    private void handleUIInput() {

        if (gameState == GameState.RESULT) {
            if (roundResult.handleInput()) {
                Roulette.getInstance().getRoundManager().awardTickets();
                enterShopScreen();
            }
        } else if (gameState == GameState.SHOP) {
            if (shop.handleInput()) {
                enterRoundScreen();
            }
        }
    }

    private void renderTicketCounter() {
        int tickets = game.getRunState().getTickets();

        float padding = 80f;
        float iconSize = 32f;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float x = screenWidth - iconSize - padding;
        // placeholder y
        float y = padding - 50f;

        uiProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        spriteBatch.setProjectionMatrix(uiProjection);
        uiTransform.idt();
        spriteBatch.setTransformMatrix(uiTransform);

        spriteBatch.begin();

        spriteBatch.draw(ticketTexture, x, y, iconSize, iconSize);

        FontManager.getInstance()
                .getFontByName("Placeholder")
                .draw(spriteBatch, Integer.toString(tickets), x + iconSize + 8f, y + 24f);

        spriteBatch.end();
    }

    public void enterResultScreen(int chips, int quota, int baseReward, int unusedSpinBonus, int totalReward) {
        this.gameState = GameState.RESULT;
        wheel.shiftOutOfScreen();
        roundResult.show(quota, chips, baseReward, unusedSpinBonus, totalReward);
    }

    public Ball getBall() {
        return ball;
    }

    private void enterShopScreen() {
        gameState = GameState.SHOP;

        roundResult.hide();
        shop.show();
    }

    public void enterRoundScreen() {
        gameState = GameState.ROUND;

        shop.hide();

        Roulette.getInstance().getRoundManager().startRound();
        wheel.shiftIntoScreen();
    }

    @Override
    public void render(float delta) {
        // TODO: game screen rendering
        // includes the roulette wheel & the ui
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        // ShapeRenderer renders
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        handleWheelClick();
        wheel.render(delta);

        ball.update(delta);
        ball.render();

        // SpriteBatch renders
        updateBetButtonLayout();
        betButton.update();
        betButton.draw(spriteBatch);

        roundResult.update(delta);
        roundResult.render();

        shop.update(delta);
        shop.render();

        handleUIInput();
        handleDebugWinInput();

        quotaTracker.update(delta);

        SpriteBatch batch = RendererManager.getInstance().getSpriteBatch();
        batch.begin();
        batch.setTransformMatrix(new Matrix4().setToTranslation(0, 0, 0));

        List<Card> cards = Roulette.getInstance().getRunState().getOwnedCards();
        float worldWidth = game.getViewport().getWorldWidth();

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Vector2 slot = CardLayout.getSlotPosition(i, cards.size(), worldWidth);
            card.setTargetPosition(slot.x, slot.y);
            card.update(delta);
            card.render();
        }

        // TEST CHARM

         List<AbstractCharm> charms = Roulette.getInstance().getRunState().getOwnedCharms();

        for (int i = 0; i < charms.size(); i++) {
            AbstractCharm c = charms.get(i);
            Vector2 slot = CharmLayout.getSlotPosition(i, charms.size(), worldWidth);
            c.setTargetPosition(slot.x, slot.y);
            c.update(delta);
            c.render();
        }

        batch.end();

        if (gameState == GameState.ROUND) {
            quotaTracker.render();
            renderDebugWinButton();
        }
        renderTicketCounter();
    }

    @Override
    public void resize(int width, int height) {
        updateBetButtonLayout();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);

        betButtonTexture = new Texture(Gdx.files.internal("buttons/TEX_BUTTON_64x32_BetUp.png"));

        float btnWidth = betButtonTexture.getWidth();
        float btnHeight = betButtonTexture.getHeight();
        baseWindowWidth = game.getWorldWidth();
        baseWindowHeight = game.getWorldHeight();
        baseButtonWidth = btnWidth;
        baseButtonHeight = btnHeight;

        betButton = new BetScreenButton(
                betButtonTexture,
                (game.getWorldWidth() - btnWidth) / 2f, 0,
                btnWidth, btnHeight,
                () -> {
                    // DO NOT CALL this.dispose() HERE, SOME ASSETS ARE STILL IN USE (e.g., the
                    // sprite batch)
                    game.setScreen(Roulette.getInstance().getBettingScreen());
                });
        updateBetButtonLayout();
    }

    private void updateBetButtonLayout() {
        if (betButton == null || betButtonTexture == null) {
            return;
        }

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float widthScale = screenWidth / Math.max(1f, baseWindowWidth) * 4f;
        float heightScale = screenHeight / Math.max(1f, baseWindowHeight) * 4f;
        float scale = Math.max(0.5f, Math.min(2.5f, Math.min(widthScale, heightScale)));

        float btnWidth = baseButtonWidth * scale;
        float btnHeight = baseButtonHeight * scale;

        betButton.setSize(btnWidth, btnHeight);
        betButton.setPosition((screenWidth - btnWidth) / 2f, 0);
    }

    private void handleDebugWinInput() {
        if (gameState != GameState.ROUND || !Gdx.input.justTouched()) {
            return;
        }

        float touchX = Gdx.input.getX();
        float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (!debugWinButton.contains(touchX, touchY)) {
            return;
        }

        int quota = game.getRoundManager().getCurrentConfig().getQuota();
        int missingChips = quota - game.getRunState().getChips();
        if (missingChips > 0) {
            game.getRunState().addChips(missingChips);
        }
        game.getRoundManager().advance();
    }

    private void renderDebugWinButton() {
        float buttonWidth = 250f;
        float buttonHeight = 55f;
        float buttonX = 20f;
        float buttonY = 75f;
        debugWinButton.set(buttonX, buttonY, buttonWidth, buttonHeight);

        Matrix4 previousShapeProjection = new Matrix4(shapeRenderer.getProjectionMatrix());
        Matrix4 previousSpriteProjection = new Matrix4(spriteBatch.getProjectionMatrix());
        Matrix4 previousSpriteTransform = new Matrix4(spriteBatch.getTransformMatrix());
        Matrix4 screenProjection = new Matrix4().setToOrtho2D(
                0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shapeRenderer.setProjectionMatrix(screenProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.65f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(screenProjection);
        spriteBatch.setTransformMatrix(new Matrix4().idt());
        spriteBatch.begin();
        FontManager.getInstance().getFontByName("Placeholder")
                .draw(spriteBatch, "DEBUG WIN", buttonX + 42f, buttonY + 35f);
        spriteBatch.end();

        shapeRenderer.setProjectionMatrix(previousShapeProjection);
        spriteBatch.setProjectionMatrix(previousSpriteProjection);
        spriteBatch.setTransformMatrix(previousSpriteTransform);
    }

    @Override
    public void hide() {
        com.badlogic.gdx.Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        ball.dispose();
        wheel.dispose();
        betButtonTexture.dispose();
        world.dispose();
        ticketTexture.dispose();
    }

    public Wheel getWheel() {
        return wheel;
    }
}
