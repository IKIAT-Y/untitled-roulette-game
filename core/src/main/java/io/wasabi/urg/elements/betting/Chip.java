package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.math.Vector2;

public class Chip {
    private ChipDenomination denomination;
    private Vector2 position;
    private boolean dragging;
    private float radius;

    public Chip(float x, float y, float radius) {
        this.position = new Vector2(x, y);
        this.radius = radius;
        this.dragging = false;
    }

    public boolean contains(Vector2 point) {
        return position.dst2(point) <= (radius * radius);
    }
}
