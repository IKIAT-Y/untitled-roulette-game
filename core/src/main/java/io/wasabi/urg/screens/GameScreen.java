package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.managers.SoundManager;

import java.util.List;
import java.util.Random;

public class GameScreen implements Screen {
    private final Roulette game;

    // Renderers
    private final ShapeRenderer shapeRenderer;

    // Physics
    private final World world;

    // Elements
    private Ball ball;
    private Wheel wheel;

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();

        this.world = new World(new Vector2(0f, 0f), true);

        Vector2 wheelCenter = new Vector2(-120f, 0);
        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);

        // Move this to launch method when the player presses the spin button
        float startAngleRad = 0f;
        float initialSpeed = new Random().nextFloat() * (1000f) + 5000f;
        float outerTrackRadius = 400f;
        float innerWheelRadius = 300f;
        Roulette.getInstance().getRunState().triggerCardEffects("beforeSpin");
        SoundManager.getInstance().playSound("spin1");
        ball.launch(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
        wheel.spin(4.5f, -10f);
    }

    @Override
    public void render(float delta) {
        // TODO: game screen rendering
        // includes the roulette wheel & the ui
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);

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
    }

    public Wheel getWheel() {return wheel;}
}
