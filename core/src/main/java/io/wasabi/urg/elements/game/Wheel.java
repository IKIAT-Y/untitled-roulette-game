package io.wasabi.urg.elements.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.screens.GameScreen;

public class Wheel {
    private float posX;
    private float posY;
    private float rotation; // in Degrees
    private float radius;
    private float tileSize;

    private final GameScreen screen;

    // placeholder, i think this should be actually stored inside the Player's class
    public List<Tile> tiles = new ArrayList<>();

    public Wheel(GameScreen screen) {
        this.screen = screen;

        // Testing
        radius = 80;
        tileSize = 25;

        for (int i = 0; i < 37; i++) {
            Tile tile = new Tile(screen, i);
            tile.size = 0.5f + MathUtils.random.nextFloat();
            tiles.add(tile);
        }
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
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
            total += tile.size;
        }
        ang = 360f / total;

        return ang;
    }

    public void render() {
        // placeholder render function
        float r1 = radius;
        float r2 = radius + tileSize;

        float ang = getBaseTileAngle();
        float angc = rotation;

        screen.shapeRenderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        screen.shapeRenderer.circle(posX, posY, r1);
        screen.shapeRenderer.circle(posX, posY, r2);
        screen.shapeRenderer.end();

        for (Tile tile : tiles) {
            tile.render(posX, posY, angc, ang, radius, tileSize);
            angc += ang * MathUtils.degreesToRadians * tile.size;
        }
    }

    public void dispose() {

    }
}
