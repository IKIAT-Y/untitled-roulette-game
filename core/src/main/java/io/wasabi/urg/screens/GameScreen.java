package io.wasabi.urg.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Ball;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;

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
    private Vector2 wheelCenter = new Vector2(-120f, 0);

    public GameScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();

        this.world = new World(new Vector2(0f, 0f), true);

        
        this.wheel = new Wheel(world, wheelCenter);
        this.ball = new Ball(world, 6f, wheelCenter);
        spin();

        // Wheel boundaries and frets for bouncing
        //this.frets = new Frets(world, wheelCenter, 100f, 115f, 37, 1f);
        //this.boundary = new WheelBoundary(world, wheelCenter, 100f, 150f);
    }
    
    private void spin() {
        float startAngleRad = 0f;
        float initialSpeed = new Random().nextFloat() * (1000f) + 1500f;
        float outerTrackRadius = 200f;
        float innerWheelRadius = 150f;
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
        world.dispose();
    }
}
