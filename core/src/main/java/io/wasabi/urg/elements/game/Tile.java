package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Tile {
    private int number;
    private float size = 1; // multiplier

    private Vector2 position;
    private float degrees;
    private float rotation = 0.0f;
    private float radius;
    private float height;

    private Texture tex;
    private PolygonRegion region;

    public Tile(int number, Vector2 position, float radius, float height) {
        this.number = number;
        this.position = position;
        this.radius = radius;
        this.height = height;

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        if (number % 2 == 0) {
            pix.setColor(0xFF0000FF);
        } else {
            pix.setColor(0x000000FF);
        }
        pix.fill();

        tex = new Texture(pix);
    }

    public void update() {
        float x = position.x;
        float y = position.y;
        float r1 = radius;
        float r2 = radius + height;
        float radians = degrees * MathUtils.degreesToRadians * size;

        int segments = Math.max(1, (int) (6 * (float) Math.cbrt(r2)));
        float radInc = radians / segments;

        float rot = rotation;

        float[] vertices = new float[segments * 4];
        short[] tris = new short[segments * 11];

        for (int i = 0; i < segments; i++) {
            int v = i * 4;
            int ind = i * 2;
            int t = i * 6;
            vertices[v] = x + r1 * MathUtils.cos(rot);
            vertices[v+1] = y + r1 * MathUtils.sin(rot);
            vertices[v+2] = x + r2 * MathUtils.cos(rot);
            vertices[v+3] = y + r2 * MathUtils.sin(rot);

            if (i < segments - 1) {
                tris[t] = (short) ind;
                tris[t + 1] = (short) (ind + 2);
                tris[t + 2] = (short) (ind + 3);
                tris[t + 3] = (short) ind;
                tris[t + 4] = (short) (ind + 3);
                tris[t + 5] = (short) (ind + 1);
            }

            rot += radInc;
        }
        region = new PolygonRegion(new TextureRegion(tex), vertices, tris);
    }

    public float render(ShapeRenderer shapeRenderer, PolygonSpriteBatch polyBatch) {
        update();

        float radians = degrees * MathUtils.degreesToRadians * size;

        polyBatch.begin();
        polyBatch.draw(region, 0, 0);
        polyBatch.end();

        return radians;
    }

    public float getSize() { return size; }

    public void setPosition(Vector2 position) { this.position = position; }
    public void setDegrees(float degrees) { this.degrees = degrees; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public void setRadius(float radius) { this.radius = radius; }
    public void setSize(float size) { this.size = size; }
}
