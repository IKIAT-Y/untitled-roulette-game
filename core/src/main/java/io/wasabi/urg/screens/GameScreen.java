package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Frets;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.elements.game.WheelBoundary;

public class GameScreen implements Screen {
    private final Roulette game;

    private ShapeRenderer shapeRenderer;

    // Physics
    private World world;

    // Elements
    private Ball ball;
    private Wheel wheel;
    private Frets frets;
    private WheelBoundary boundary;

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = game.getShapeRenderer();

        this.world = new World(new Vector2(0f, 0f), true);

        this.wheel = new Wheel();
        wheel.setPosition(-120f, 0);

        Vector2 wheelCenter = new Vector2(-120f, 0);
        this.ball = new Ball(world, 6f, wheelCenter);

        // Wheel boundaries and frets for bouncing
        this.frets = new Frets(world, wheelCenter, 100f, 115f, 37, 1f);
        this.boundary = new WheelBoundary(world, wheelCenter, 100f, 150f, ball);

        float startAngleRad = 0f;
        float initialSpeed = 2000f;
        float outerTrackRadius = 200f;
        float innerWheelRadius = 150f;
        ball.launch(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
    }

    @Override
    public void render(float delta) {
        // TODO: game screen rendering
        // includes the roulette wheel & the ui
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);

        ball.update(delta);
        ball.render(shapeRenderer);

        wheel.render(shapeRenderer);

        boundary.update(delta);
        boundary.render(shapeRenderer);
        frets.render(shapeRenderer);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

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
        boundary.dispose();
        frets.dispose();
        world.dispose();
    }
}
