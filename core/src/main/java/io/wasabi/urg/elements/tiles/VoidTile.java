package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class VoidTile extends TileType {
    private static final Color RIM_COLOR = new Color(0.65f, 0.15f, 0.9f, 1f);
    private final TileType originalType;
    private final Tile tile;

    public VoidTile(TileType originalType, Tile tile) {
        this.originalType = originalType;
        this.tile = tile;
        tooltip.setTitle("VOID " + originalType.getNumber());
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("Gain [RED]3x [BLACK]payout. This tile is destroyed when landed on");
        tooltip.addType("VOID", Color.WHITE, RIM_COLOR);
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        originalType.setRegion(vertices, indices);
    }

    @Override
    public void drawTextures() {
        originalType.drawTextures();
    }

    @Override
    public void drawOverlay() {
        originalType.drawOverlay();
    }

    @Override
    public void drawOutline() {
        PolygonRegion region = getRegion();
        if (region == null) {
            return;
        }

        float[] vertices = region.getVertices();
        ShapeRenderer renderer = RENDERER_MANAGER.getShapeRenderer();
        Gdx.gl.glLineWidth(7f);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(RIM_COLOR);
        for (int i = 0; i + 7 < vertices.length; i += 4) {
            float middleAX = (vertices[i] + vertices[i + 2]) / 2f;
            float middleAY = (vertices[i + 1] + vertices[i + 3]) / 2f;
            float middleBX = (vertices[i + 4] + vertices[i + 6]) / 2f;
            float middleBY = (vertices[i + 5] + vertices[i + 7]) / 2f;
            renderer.line(vertices[i + 2], vertices[i + 3], vertices[i + 6], vertices[i + 7]);
            renderer.line(middleAX, middleAY, middleBX, middleBY);
        }

        int lastOuter = vertices.length - 2;
        float firstMiddleX = (vertices[0] + vertices[2]) / 2f;
        float firstMiddleY = (vertices[1] + vertices[3]) / 2f;
        float lastMiddleX = (vertices[vertices.length - 4] + vertices[lastOuter]) / 2f;
        float lastMiddleY = (vertices[vertices.length - 3] + vertices[lastOuter + 1]) / 2f;
        renderer.line(firstMiddleX, firstMiddleY, vertices[2], vertices[3]);
        renderer.line(lastMiddleX, lastMiddleY, vertices[lastOuter], vertices[lastOuter + 1]);
        renderer.end();
        Gdx.gl.glLineWidth(1f);
    }

    @Override
    public void onLanded() {
        originalType.onLanded();
        Roulette.getInstance().getRunState().removeTile(tile);
    }

    @Override
    public boolean isRed() { return originalType.isRed(); }

    @Override
    public boolean isBlack() { return originalType.isBlack(); }

    @Override
    public boolean isGreen() { return originalType.isGreen(); }

    @Override
    public PolygonRegion getRegion() { return originalType.getRegion(); }

    @Override
    public TileColour getColour() { return originalType.getColour(); }

    @Override
    public int getNumber() { return originalType.getNumber(); }

    @Override
    public void setBetMultiplier(float betMultiplier) {
        originalType.setBetMultiplier(betMultiplier / 3f);
    }

    @Override
    public float getBetMultiplier() { return originalType.getBetMultiplier() * 3f; }

    public TileType getOriginalType() { return originalType; }
}
