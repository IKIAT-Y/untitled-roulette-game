package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class BetScreenButton {

    private final Texture texture; // Uses whatever texture you pass in
    private final Rectangle bounds;
    private boolean isPressed;
    private final ClickAction action; // Stores the custom scene-changing logic

    // Functional interface to define what the button does
    public interface ClickAction {
        void execute();
    }

    /**
     * Constructor for the BetScreenButton class. It takes in coordinates following
     * the bottom-left origin convention, rather than texture's top-left origin
     * convention.
     * 
     * @param texture
     * @param x       Left corner of the button
     * @param y       Bottom corner of the button
     * @param width
     * @param height
     * @param action
     */
    public BetScreenButton(Texture texture, float x, float y, float width, float height, ClickAction action) {
        this.texture = texture;
        this.bounds = new Rectangle(x, y, width, height);
        this.action = action;
        this.isPressed = false;
    }

    public void update() {
        if (!Gdx.input.isTouched()) {
            if (isPressed) {
                isPressed = false;
                action.execute();
            }
            isPressed = false;
            return;
        }

        float touchX = Gdx.input.getX();
        float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

        isPressed = bounds.contains(touchX, touchY);
    }

    public void draw(SpriteBatch batch) {
        if (isPressed) {
            batch.setColor(0.7f, 0.7f, 0.7f, 1f);
        }

        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        batch.setColor(1f, 1f, 1f, 1f);
    }
}