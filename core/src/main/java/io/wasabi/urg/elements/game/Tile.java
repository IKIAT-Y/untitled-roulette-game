package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.screens.GameScreen;

public class Tile {
    private GameScreen screen;

    public int number;
    public float size = 1; // multiplier

    public Tile(GameScreen screen, int number) {
        this.screen = screen;
        this.number = number;
    }

    public Tile(GameScreen screen, int number, float size) {
        this.screen = screen;
        this.number = number;
        this.size = size;
    }

    public void render(float x, float y, float rotation, float degrees, float dist, float height) {
        float r1 = dist;
        float r2 = dist + height;

        float xa1 = x + r1 * MathUtils.cos(rotation);
        float xa2 = x + r2 * MathUtils.cos(rotation);
        float ya1 = y + r1 * MathUtils.sin(rotation);
        float ya2 = y + r2 * MathUtils.sin(rotation);
        rotation += degrees * MathUtils.degreesToRadians * size;
        float xb1 = x + r1 * MathUtils.cos(rotation);
        float xb2 = x + r2 * MathUtils.cos(rotation);
        float yb1 = y + r1 * MathUtils.sin(rotation);
        float yb2 = y + r2 * MathUtils.sin(rotation);

        screen.shapeRenderer.begin(ShapeType.Line);
        screen.shapeRenderer.line(xa1, ya1, xa2, ya2);
        screen.shapeRenderer.line(xb2, yb2, xb1, yb1);
        screen.shapeRenderer.end();
    }
}
