package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Frets;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.elements.game.WheelBoundary;
import io.wasabi.urg.managers.GameManager;

public class GameScreen implements Screen {

    private final Roulette game;

    private final GameManager gameManager;

    public GameScreen(final Roulette game) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        gameManager.setScreen(this);

        Wheel wheel = new Wheel();
        wheel.setPosition(-120f, 0);

        Vector2 wheelCenter = new Vector2(-120f, 0);
        Ball ball = new Ball(gameManager.getWorld(), 6f, wheelCenter);

        // Wheel boundaries and frets for bouncing
        Frets frets = new Frets(gameManager.getWorld(), wheelCenter, 100f, 115f, 37, 1f);
        WheelBoundary boundary = new WheelBoundary(gameManager.getWorld(), wheelCenter, 100f, 150f, ball);

        gameManager.addGameObject(ball);
        gameManager.addGameObject(wheel);
        gameManager.addGameObject(boundary);
        gameManager.addGameObject(frets);

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
        gameManager.render(delta);
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
        gameManager.dispose();
    }
}
