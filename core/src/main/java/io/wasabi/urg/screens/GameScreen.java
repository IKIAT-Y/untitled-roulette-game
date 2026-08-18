package io.wasabi.urg.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.betting.BetScreenButton;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.managers.SoundManager;

import java.util.List;
import java.util.Random;

public class GameScreen implements Screen {
    private final Roulette game;

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
    private Texture betButtonTexture;
    private BetScreenButton betButton;
    private float baseWindowWidth;
    private float baseWindowHeight;
    private float baseButtonWidth;
    private float baseButtonHeight;
    private Vector2 wheelCenter = new Vector2(-120f, 0);

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
        this.spriteBatch = RendererManager.getInstance().getSpriteBatch();

        this.ticketTexture = new Texture(Gdx.files.internal("ticket.png"));
        this.world = new World(new Vector2(0f, 0f), true);

        
        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);

        // Wheel boundaries and frets for bouncing
        // this.frets = new Frets(world, wheelCenter, 100f, 115f, 37, 1f);
        // this.boundary = new WheelBoundary(world, wheelCenter, 100f, 150f);

        spin();
    }
    
    private void spin() {
        // Move this to launch method when the player presses the spin button
        float startAngleRad = 0f;
        float initialSpeed = new Random().nextFloat() * (1000f) + 5000f;
        float outerTrackRadius = 400f;
        float innerWheelRadius = 300f;
        Roulette.getInstance().getRunState().triggerCardEffects("beforeSpin");
        wheel.spin();
        SoundManager.getInstance().playSound("spin1");
        ball.launch(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
        wheel.spin(4.5f, -10f);
    }

    private void handleWheelClick() {
        if (!Gdx.input.justTouched() || ball.getState() != Ball.State.STOPPED) {
            return;
        }

        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        game.getCamera().unproject(touch);

        if (wheel.containsPoint(new Vector2(touch.x, touch.y))) {
            spin();
        }
    }
    private void renderTicketCounter() {
        int tickets = game.getRunState().getTickets();

        float padding = 80f;
        float iconSize = 32f;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float x = screenWidth - iconSize - padding;
        //placeholder y
        float y = padding - 50f;

        uiProjection.setToOrtho2D(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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

        batch.end();
        renderTicketCounter();
    }

    @Override
    public void resize(int width, int height) {
        updateBetButtonLayout();
    }

    @Override
    public void show() {
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
                    game.setScreen(new BettingScreen(game));
                });
        updateBetButtonLayout();
    }

    private void updateBetButtonLayout() {
        if (betButton == null || betButtonTexture == null) {
            return;
        }

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float widthScale = screenWidth / Math.max(1f, baseWindowWidth);
        float heightScale = screenHeight / Math.max(1f, baseWindowHeight);
        float scale = Math.max(0.5f, Math.min(2.5f, Math.min(widthScale, heightScale)));

        float btnWidth = baseButtonWidth * scale;
        float btnHeight = baseButtonHeight * scale;

        betButton.setSize(btnWidth, btnHeight);
        betButton.setPosition((screenWidth - btnWidth) / 2f, 0);
        com.badlogic.gdx.Gdx.input.setInputProcessor(
            new io.wasabi.urg.managers.CardInputHandler(game.getRunState(), game.getViewport())
        );
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

    public Wheel getWheel() {return wheel;}
}
