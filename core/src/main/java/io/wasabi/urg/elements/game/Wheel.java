package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.screens.GameScreen;

public class Wheel {
    private float posX;
    private float posY;

    private final GameScreen screen;

    public Wheel(GameScreen screen) {
        this.screen = screen;
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
    }

    public void render() {
        // placeholder render function
        int nums = 37;
        float r1 = 100f;
        float r2 = 150f;

        float ang = 360f/nums;
        float angc = 0.0f;

        for (int i = 0; i < nums; i++) {
            float xa1 = posX + r1 * MathUtils.cos(angc);
            float xa2 = posX + r2 * MathUtils.cos(angc);
            float ya1 = posY + r1 * MathUtils.sin(angc);
            float ya2 = posY + r2 * MathUtils.sin(angc);
            angc += ang * MathUtils.degreesToRadians;
            float xb1 = posX + r1 * MathUtils.cos(angc);
            float xb2 = posX + r2 * MathUtils.cos(angc);
            float yb1 = posY + r1 * MathUtils.sin(angc);
            float yb2 = posY + r2 * MathUtils.sin(angc);

            float[] vertices = new float[] {xa1, ya1, xa2, ya2, xb2, yb2, xb1, yb1};

            screen.shapeRenderer.begin(ShapeType.Line);
            screen.shapeRenderer.polygon(vertices);
            screen.shapeRenderer.end();
        }
    }

    public void dispose() {

    }
}
