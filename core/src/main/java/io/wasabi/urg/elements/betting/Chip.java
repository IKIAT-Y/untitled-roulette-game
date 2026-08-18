package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * A single draggable chip. Purely visual/interaction state.
 */
public class Chip {
    private final ChipDenomination denomination;
    private final Vector2 position;
    private final float radius;
    private boolean dragging;
    private Bet bet; // null while in the tray / mid-drag

    public Chip(ChipDenomination denomination, float x, float y, float radius) {
        this.denomination = denomination;
        this.position = new Vector2(x, y);
        this.radius = radius;
        this.dragging = false;
    }

    public boolean contains(Vector2 point) {
        return position.dst2(point) <= (radius * radius);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public ChipDenomination getDenomination() {
        return denomination;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }

    public void draw(SpriteBatch batch, Texture texture) {
        batch.begin();
        batch.draw(texture, position.x - radius, position.y - radius, radius * 2, radius * 2);
        batch.end();
    }
}
