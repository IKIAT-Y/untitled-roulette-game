package io.wasabi.urg.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.elements.betting.BetScreenButton;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.SpinButton;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.CardInputHandler;
import io.wasabi.urg.managers.CharmInputHandler;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.CardLayout;
import io.wasabi.urg.ui.CharmLayout;
import io.wasabi.urg.ui.GameOver;
import io.wasabi.urg.ui.QuotaTracker;
import io.wasabi.urg.ui.RoundInfoPanel;
import io.wasabi.urg.ui.RoundResult;
import io.wasabi.urg.ui.Shop;
import io.wasabi.urg.ui.Tooltip;

public class GameScreen implements Screen {
    private static final int STARTING_CHIPS = 100;
    private static final float START_ANGLE_RAD = 0f;
    private static final float OUTER_TRACK_RADIUS = 325f;
    private static final float INNER_WHEEL_RADIUS = 300f;
    private static final float MIN_INITIAL_SPEED = 5000f;
    private static final float INITIAL_SPEED_RANGE = 1000f;
    private static final float WHEEL_SPIN_DURATION = 6.0f;
    private static final float WHEEL_SPIN_SPEED = -10f;
    private final Roulette game;

    private enum GameState {
        ROUND,
        RESULT,
        SHOP,
        GAME_OVER
    }

    private GameState gameState;

    // Renderers
    private final Background background;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    // Matrices for UI rendering
    private final Matrix4 uiProjection = new Matrix4();
    private final Matrix4 uiTransform = new Matrix4();

    // Physics
    private final World world;

    // Miscellaneous
    private List<GameObject> particles = new ArrayList<GameObject>();

    // Elements
    private Ball ball;
    private Wheel wheel;
    private Vector2 wheelCenter = new Vector2(0f, 0);

    // UI
    private RoundResult roundResult;
    private Shop shop;
    private GameOver gameOver;
    private QuotaTracker quotaTracker;
    private RoundInfoPanel roundInfoPanel;

    // Handlers
    private CardInputHandler cardInputHandler = new CardInputHandler(Roulette.getInstance().getRunState(),
            Roulette.getInstance().getViewport());
    private CharmInputHandler charmInputHandler = new CharmInputHandler(Roulette.getInstance().getRunState(),
            Roulette.getInstance().getViewport());
    private InputMultiplexer inputMultiplexer = new InputMultiplexer(cardInputHandler, charmInputHandler);

    // Betting
    private Texture betButtonTexture;
    private BetScreenButton betButton;
    private float baseWindowWidth;
    private float baseWindowHeight;
    private float baseButtonWidth;
    private float baseButtonHeight;
    private final Rectangle debugWinButton = new Rectangle();

    // Middle-mouse drag-to-rotate
    private boolean draggingWheelRotation = false;
    private float lastWheelRotationAngle = 0f;

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
        this.spriteBatch = RendererManager.getInstance().getSpriteBatch();
        this.background = new Background(spriteBatch);

        this.world = new World(new Vector2(0f, 0f), true);

        this.gameState = GameState.ROUND;

        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);

        this.roundResult = new RoundResult(shapeRenderer, spriteBatch);
        this.shop = new Shop(spriteBatch, game.getViewport());
        this.gameOver = new GameOver(shapeRenderer, spriteBatch, game.getViewport());
        this.inputMultiplexer.addProcessor(2, shop);
        this.quotaTracker = new QuotaTracker(shapeRenderer, spriteBatch, game.getRunState(), game.getRoundManager());
        this.roundInfoPanel = new RoundInfoPanel(-700f, 250f);
        roundInfoPanel.updateRoundType();
    }

    public void spin() {
        launchSpin(false);
    }

    public void freeSpin() {
        launchSpin(true);
    }

    private void launchSpin(boolean free) {
        float initialSpeed = new Random().nextFloat() * INITIAL_SPEED_RANGE + MIN_INITIAL_SPEED;
        Roulette.getInstance().getRunState().triggerEffects("beforeSpin");
        SoundManager.getInstance().playSound("spin1");
        ball.setVisible(true);

        if (free) {
            ball.launchFree(START_ANGLE_RAD, initialSpeed, OUTER_TRACK_RADIUS, INNER_WHEEL_RADIUS);
        } else {
            ball.launch(START_ANGLE_RAD, initialSpeed, OUTER_TRACK_RADIUS, INNER_WHEEL_RADIUS);
        }

        wheel.spin(WHEEL_SPIN_DURATION, WHEEL_SPIN_SPEED);
        quotaTracker.onSpinStarted();
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
        } else if (gameState == GameState.GAME_OVER && gameOver.isVisible()) {
            if (Gdx.input.justTouched()) {
                restartGame();
            }
        }
    }

    public void enterResultScreen(int chips, int quota, int baseReward, int unusedSpinBonus, int totalReward) {
        this.gameState = GameState.RESULT;
        wheel.shiftOutOfScreen();
        ball.setVisible(false);
        roundInfoPanel.hide();
        quotaTracker.hide();
        roundResult.show(quota, chips, baseReward, unusedSpinBonus, totalReward);
    }

    public void showGameOver() {
        this.gameState = GameState.GAME_OVER;
        inputMultiplexer.removeProcessor(cardInputHandler);
        inputMultiplexer.removeProcessor(charmInputHandler);
        inputMultiplexer.removeProcessor(shop);
        gameOver.show();
    }

    public void restartGame() {
        game.getRunState().reset(STARTING_CHIPS);
        game.getRoundManager().reset();
        wheel.reset();
        gameOver.hide();
        enterRoundScreen();
        inputMultiplexer.addProcessor(0, cardInputHandler);
        inputMultiplexer.addProcessor(1, charmInputHandler);
        inputMultiplexer.addProcessor(2, shop);
        this.gameState = GameState.ROUND;
    }

    public Ball getBall() {
        return ball;
    }

    private void enterShopScreen() {
        gameState = GameState.SHOP;

        roundResult.hide();

        roundInfoPanel.setRoundType("Shop");
        roundInfoPanel.setRoundInfo("Enhance your run!", "Drag-n-drop items to the BUY/SELL squares");

        roundInfoPanel.show();
        quotaTracker.show();
        shop.show();
    }

    public void enterRoundScreen() {
        gameState = GameState.ROUND;

        inputMultiplexer.addProcessor(0, cardInputHandler);
        inputMultiplexer.addProcessor(1, charmInputHandler);

        shop.hide();

        Roulette.getInstance().getRoundManager().startRound();

        roundInfoPanel.show();
        quotaTracker.show();

        roundInfoPanel.updateRoundType();

        wheel.shiftIntoScreen();
    }

    @Override
    public void render(float delta) {
        // TODO: game screen rendering
        // includes the roulette wheel & the ui
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        // ShapeRenderer renders
        background.render(delta);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        wheel.render(delta);

        ball.update(delta);
        ball.render();

        // couple of checks if button can be pressed
        SpinButton.State spinButtonState;

        if (ball.getState() != Ball.State.STOPPED) {
            spinButtonState = SpinButton.State.SPINNING;
        } else if (game.getRunState().getActiveBets().isEmpty()) {
            spinButtonState = SpinButton.State.NO_BET;
        } else {
            spinButtonState = SpinButton.State.READY;
        }

        wheel.updateSpinButton(spinButtonState, game.getCamera());

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
        handleTileSelectionInput();
        handleWheelRotationInput();

        quotaTracker.update(delta);

        roundInfoPanel.update(delta);
        roundInfoPanel.render();

        SpriteBatch batch = RendererManager.getInstance().getSpriteBatch();
        batch.begin();
        batch.setTransformMatrix(new Matrix4().setToTranslation(0, 0, 0));

        // Card Inventory Rendering
        List<Card> cards = game.getRunState().getOwnedCards();
        Card draggedCard = null;
        float worldWidth = game.getViewport().getWorldWidth();
        float worldHeight = game.getViewport().getWorldHeight();

        CardLayout.renderRightBackPanel(batch, worldWidth, worldHeight);
        CardLayout.renderSlotPanels(batch, worldWidth);
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Vector2 slot = CardLayout.getSlotPosition(i, cards.size(), worldWidth);
            card.setTargetPosition(slot.x, slot.y);
            card.update(delta);
            if (card.isDragging()) {
                draggedCard = card;
                continue;
            }
            card.render();
        }

        // Charm Inventory Rendering
        List<AbstractCharm> charms = game.getRunState().getOwnedCharms();
        AbstractCharm draggedCharm = null;

        CharmLayout.renderRightBackPanel(batch, worldWidth, worldHeight);
        CharmLayout.renderSlotPanels(batch, worldWidth);
        for (int i = 0; i < charms.size(); i++) {
            AbstractCharm c = charms.get(i);
            Vector2 slot = CharmLayout.getSlotPosition(i, charms.size(), worldWidth);
            c.setTargetPosition(slot.x, slot.y);
            c.update(delta);
            if (c.isDragging()) {
                draggedCharm = c;
                continue;
            }
            c.render();
        }

        // draw dragged elements at the end
        if (draggedCard != null)
            draggedCard.render();
        if (draggedCharm != null)
            draggedCharm.render();
        if (shop.getDraggedCard() != null)
            shop.renderCard(shop.getDraggedCard());
        if (shop.getDraggedCharm() != null)
            shop.renderCharm(shop.getDraggedCharm());

        batch.end();

        quotaTracker.render();
        // render tooltip at very end
        Tooltip activeTooltip = game.getRunState().getActiveTooltip();
        if (activeTooltip != null) {
            activeTooltip.render();
        }

        if (gameState == GameState.ROUND) {
            quotaTracker.render();
            renderDebugWinButton();
        }

        // render all particles
        for (GameObject particle : particles) {
            particle.update(delta);
            particle.render();
        }

        gameOver.update(delta);
        gameOver.render();
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
                    if (canBet()) {
                        game.setScreen(Roulette.getInstance().getBettingScreen());
                    }
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

    private void handleTileSelectionInput() {
        if (gameState != GameState.ROUND) {
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.getRunState().clearSelectedTiles();
            SoundManager.getInstance().playSound("tileDeselect");
            return;
        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        Vector2 touchPoint = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        game.getViewport().unproject(touchPoint);

        Tile tile = wheel.getTileAt(touchPoint);
        if (tile != null) {
            game.getRunState().toggleTileSelection(tile);
            SoundManager.getInstance().playSound("tileSelect");
        }
    }

    private void handleWheelRotationInput() {
        if (gameState != GameState.ROUND || wheel.isSpinning() || !Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            draggingWheelRotation = false;
            return;
        }

        Vector2 touchPoint = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        game.getViewport().unproject(touchPoint);

        Vector2 wheelPos = wheel.getPosition();
        float currentAngleDeg = MathUtils.atan2(touchPoint.y - wheelPos.y, touchPoint.x - wheelPos.x)
                * MathUtils.radiansToDegrees;

        if (draggingWheelRotation) {
            float deltaDeg = wrapDegrees(currentAngleDeg - lastWheelRotationAngle);
            wheel.rotateBy(deltaDeg);
        }

        draggingWheelRotation = true;
        lastWheelRotationAngle = currentAngleDeg;
    }

    private float wrapDegrees(float degrees) {
        degrees %= 360f;
        if (degrees > 180f)
            degrees -= 360f;
        if (degrees < -180f)
            degrees += 360f;
        return degrees;
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

    private boolean canBet() {
        return gameState != GameState.SHOP
                && !wheel.isSpinning();
    }

    public void addParticle(GameObject particle) {
        particles.add(particle);
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
    }

    public Wheel getWheel() {
        return wheel;
    }

    public World getWorld() {
        return world;
    }

    public Shop getShop() {
        return shop;
    }
}