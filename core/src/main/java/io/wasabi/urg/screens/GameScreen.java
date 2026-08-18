package io.wasabi.urg.screens;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;

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
    private Vector2 wheelCenter = new Vector2(-120f, 0);

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
        this.spriteBatch = RendererManager.getInstance().getSpriteBatch();
        this.ticketTexture = new Texture(Gdx.files.internal("ticket.png"));
        this.world = new World(new Vector2(0f, 0f), true);


        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);
        spin();
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
        shapeRenderer.setColor(1f, 1f, 1f, 1f);

        handleWheelClick();
        wheel.render(delta);

        ball.update(delta);
        ball.render();

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

    }

    @Override
    public void show() {
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
        ball.dispose();
        wheel.dispose();
        world.dispose();
        ticketTexture.dispose();
    }

    public Wheel getWheel() {return wheel;}
}
