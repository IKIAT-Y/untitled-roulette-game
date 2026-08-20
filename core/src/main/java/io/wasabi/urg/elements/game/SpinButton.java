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

//Circular button drawn at the centre of the roulette wheel.*/
public final class SpinButton {

    private static final Color ENABLED_COLOR = Color.GREEN;
    private static final Color DISABLED_COLOR = Color.RED;
    private static final Color TEXT_COLOR = Color.BLACK;

    private final Vector2 center;
    private final float radius;
    private final ClickAction action;
    private final GlyphLayout textLayout = new GlyphLayout();
    private final Vector3 touchPosition = new Vector3();

    public interface ClickAction {
        void execute();
    }

    public SpinButton(Vector2 center, float radius, ClickAction action) {
        this.center = new Vector2(center);
        this.radius = radius;
        this.action = action;
    }

    public void setPosition(Vector2 position) {
        center.set(position);
    }

    //Checks for a click only when the button is currently enabled.*/
    public void update(boolean enabled, OrthographicCamera camera) {
        if (!enabled || !Gdx.input.justTouched()) {
            return;
        }

        touchPosition.set(Gdx.input.getX(), Gdx.input.getY(),0f);

        camera.unproject(touchPosition);

        float dx = touchPosition.x - center.x;
        float dy = touchPosition.y - center.y;

        if (dx * dx + dy * dy <= radius * radius) {
            action.execute();
        }
    }

    // Determines the colour of the button based on state.
    public void draw(boolean enabled,ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, BitmapFont font) {

        // Save the colour that was being used before SpinButton changes it.
        Color previousShapeColor = shapeRenderer.getColor().cpy();

        shapeRenderer.setColor(enabled ? ENABLED_COLOR : DISABLED_COLOR);

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.circle(center.x, center.y, radius);
        shapeRenderer.end();

        // Restore the old ShapeRenderer colour.
        shapeRenderer.setColor(previousShapeColor);

        Color previousColor = font.getColor().cpy();
        font.setColor(TEXT_COLOR);

        textLayout.setText(font, enabled ? "SPIN" : "PLACE A BET TO SPIN");

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