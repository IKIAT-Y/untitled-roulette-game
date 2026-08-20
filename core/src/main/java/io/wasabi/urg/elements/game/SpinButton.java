package io.wasabi.urg.elements.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.wasabi.urg.Roulette;

//Circular button drawn at the centre of the roulette wheel.*/
public final class SpinButton {

    public enum State {
    NO_BET,
    READY,
    SPINNING
    }
    //green, bet available
    private static final Color ENABLED_COLOR = new Color(0x81D681FF);
    //red, bet not available
    private static final Color DISABLED_COLOR = new Color(0xD68181FF);
    //gray, bet in progress
    private static final Color WAITING_COLOR = new Color(0xD3D3D3FF);
    private static final Color TEXT_COLOR = Color.BLACK;

    private final Vector2 center;
    private final float radius;
    private final GlyphLayout textLayout = new GlyphLayout();
    private final Vector3 touchPosition = new Vector3();

    public interface ClickAction {
        void execute();
    }

    public SpinButton(Vector2 center, float radius) {
        this.center = new Vector2(center);
        this.radius = radius;
    }

    public void setPosition(Vector2 position) {
        center.set(position);
    }

    //Checks for a click only when the button is currently enabled.*/
    public void update(State state, OrthographicCamera camera) {
        if (state != State.READY || !Gdx.input.justTouched()) {
            return;
        }

        touchPosition.set(Gdx.input.getX(), Gdx.input.getY(),0f);

        camera.unproject(touchPosition);

        float dx = touchPosition.x - center.x;
        float dy = touchPosition.y - center.y;

        if (dx * dx + dy * dy <= radius * radius) {
            Roulette.getInstance().getGameScreen().spin();
        }
    }

    // Determines the colour of the button based on state.
    public void draw(State state, ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, BitmapFont font) {

        Color previousShapeColor = shapeRenderer.getColor().cpy();
        //Three states of colour the button can be in
        if (state == State.READY) {
            shapeRenderer.setColor(ENABLED_COLOR);
        }
        else if (state == State.SPINNING) {
            shapeRenderer.setColor(WAITING_COLOR);
        }
        else {
            shapeRenderer.setColor(DISABLED_COLOR);
        }

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.circle(center.x, center.y,radius);
        shapeRenderer.end();

        shapeRenderer.setColor(previousShapeColor);

        Color previousColor = font.getColor().cpy();

        font.setColor(TEXT_COLOR);

        //Text displayed on the button based on state.
        if (state == State.READY) {
            textLayout.setText(font, "SPIN");
        }
        else if (state == State.SPINNING) {
            textLayout.setText(font, "SPIN IN PROGRESS...");
        }
        else {
            textLayout.setText(font, "PLACE A BET TO SPIN");
        }

        spriteBatch.begin();

        font.draw(
                spriteBatch,
                textLayout,
                center.x - textLayout.width / 2f,
                center.y + textLayout.height / 2f);

        spriteBatch.end();

        font.setColor(previousColor);
    }
}
