package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;

import io.wasabi.urg.Roulette;

public class NumberlessTile extends TileType {
    private static final Color NUMBERLESS_COLOUR = new Color(0.75f, 0.75f, 0.75f, 1f);
    private final TileType originalType;

    public NumberlessTile(TileType originalType) {
        this.originalType = originalType;
        this.betMultiplier = 0f;
        tooltip.setTitle("NUMBERLESS");
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("This tile no longer counts as any number. Gain an extra 250 chips when landed on");
        tooltip.addType("NUMBERLESS", Color.BLACK, NUMBERLESS_COLOUR);
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
        originalType.drawOutline();
    }

    @Override
    public void onLanded() {
        Roulette.getInstance().getRunState().addChips(250);
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
    public int getNumber() { return -1; }

    @Override
    public void setBetMultiplier(float betMultiplier) { }

    @Override
    public float getBetMultiplier() { return 0f; }

    public TileType getOriginalType() { return originalType; }
}
