package io.wasabi.urg.elements.tiles;

import java.util.EnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.ui.Tooltip;

public abstract class TileType {
    protected  static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    protected static final PolygonSpriteBatch POLY_BATCH = RENDERER_MANAGER.getPolygonSpriteBatch();
    protected static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    public enum TileColour {
        BLACK, RED, GREEN,
    }

    protected static EnumMap<TileColour, Integer> tileColourMap = new EnumMap<TileColour, Integer>(TileColour.class) {{
        put(TileColour.BLACK, 0x000000FF);
        put(TileColour.RED, 0xFF0000FF);
        put(TileColour.GREEN, 0x00AA00FF);
    }};

    protected float betMultiplier = 1.0f; // multiplier for bets on this tile

    private int number;
    protected Tooltip tooltip = new Tooltip(0.5f, 0.5f);
    protected Texture texture;
    protected TileColour colour;
    protected PolygonRegion region;
    protected float textureColor = Color.WHITE_FLOAT_BITS;

    public TileType() {
        tooltip.setDescriptionVisible(false);
    }

    public void setColour(TileColour colour) {
        this.colour = colour;

        if (texture != null) texture.dispose();

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(tileColourMap.get(colour));
        pix.fill();
        texture = new Texture(pix);
        pix.dispose();

        updateTooltipTitle();
    }

    private void updateTooltipTitle() {
        tooltip.setTitle(String.format("[#%08X]%s %d", tileColourMap.get(colour), colour.toString().toUpperCase(), number));
    }

    public void setRegion(float[] vertices, short[] indices) {
        region = new PolygonRegion(new TextureRegion(texture), vertices, indices);
    }

    public void drawTextures() {
        POLY_BATCH.draw(region, 0, 0);
    }

    /**
     * Used to draw AFTER the polygon sprite batch has ended
     * Used for overlays on the existing region textures.
     */
    public void drawOverlay() {

    }

    public void onLanded() {

    }

    protected  float[] textureWrapVertices(float[] vertices, TextureRegion texRegion) {
        final int regionVerticesLength = vertices.length;
        final int vertCount = regionVerticesLength / 2;

        float u = texRegion.getU(), v = texRegion.getV();
        float uvWidth = texRegion.getU2() - u;
        float uvHeight = texRegion.getV2() - v;

        float[] resultVerts = new float[vertCount * 5];
        int vertexIndex = 0;

        boolean bottom = false;
        for (int i = 0; i < regionVerticesLength; i += 2) {
            resultVerts[vertexIndex++] = vertices[i];
            resultVerts[vertexIndex++] = vertices[i + 1];
            resultVerts[vertexIndex++] = textureColor;
            resultVerts[vertexIndex++] = u + uvWidth * ((float) i / regionVerticesLength);
            resultVerts[vertexIndex++] = bottom ? v : v + uvHeight;
            bottom = !bottom;
        }
        return resultVerts;
    }

    public boolean isRed() { return colour == TileColour.RED; }
    public boolean isBlack() { return colour == TileColour.BLACK; }
    public boolean isGreen() { return colour == TileColour.GREEN; }

    public Texture getTexture() { return texture; }
    public PolygonRegion getRegion() { return region; }
    public TileColour getColour() { return colour; }
    public int getNumber() { return number; }
    public void setNumber(int number) {
        this.number = number;
        updateTooltipTitle();
    }
    public Tooltip getTooltip() { return tooltip; }

    public void setBetMultiplier(float betMultiplier) { this.betMultiplier = betMultiplier; }
    public float getBetMultiplier() { return betMultiplier; }
}
