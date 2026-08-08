package io.wasabi.urg.elements.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Wheel {
    private Vector2 position = new Vector2();
    private float rotation; // in Degrees
    private float radius;
    private float tileSize;

    // placeholder, i think this should be actually stored inside the Player's class
    public List<Tile> tiles = new ArrayList<>();

    public Wheel() {
        // Testing
        radius = 80;
        tileSize = 25;

        for (int i = 0; i < 37; i++) {
            Tile tile = new Tile(i, position, radius, tileSize);
            tile.setSize(0.5f + MathUtils.random.nextFloat());
            tiles.add(tile);
        }
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void setPosition(Vector2 vec) {
        this.position.x = vec.x;
        this.position.y = vec.y;
    }

    public void setRotation(float rot) {
        this.rotation = rot;
    }

    public void setSize(float radius, float tileSize) {
        this.radius = radius;
        this.tileSize = tileSize;
    }

    private float getBaseTileAngle() {
        float ang;
        float total = 0;

        for (Tile tile : tiles) {
            total += tile.getSize();
        }
        ang = 360f / total;

        return ang;
    }

    public void render(ShapeRenderer shapeRenderer, PolygonSpriteBatch polyBatch) {
        // placeholder render function
        float r1 = radius;
        float r2 = radius + tileSize;

        float ang = getBaseTileAngle();
        float angc = rotation;

        shapeRenderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.circle(position.x, position.y, r1);
        shapeRenderer.circle(position.x, position.y, r2);
        shapeRenderer.end();

        for (Tile tile : tiles) {
            tile.setDegrees(ang);
            tile.setRotation(angc);
            float inc = tile.render(shapeRenderer, polyBatch);
            angc += inc;
        }
    }

    public void dispose() {

    }
}
