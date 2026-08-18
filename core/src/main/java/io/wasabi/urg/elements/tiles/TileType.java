package io.wasabi.urg.elements.tiles;

import java.util.EnumMap;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.wasabi.urg.managers.RendererManager;

public abstract class TileType {
    protected  static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    protected static final PolygonSpriteBatch POLY_BATCH = RENDERER_MANAGER.getPolygonSpriteBatch();
    protected static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    public enum TileColour {
        BLACK, RED, GREEN
    }

    protected static EnumMap<TileColour, Integer> tileColourMap = new EnumMap<TileColour, Integer>(TileColour.class) {{
        put(TileColour.BLACK, 0x000000FF);
        put(TileColour.RED, 0xFF0000FF);
        put(TileColour.GREEN, 0x00AA00FF);
    }};

    protected float betMultiplier = 1.0f; // multiplier for bets on this tile

    protected Texture texture;
    protected TileColour colour;
    protected PolygonRegion region;

    public TileType() {

    }

    public void setColour(TileColour colour) {
        this.colour = colour;
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor((int) tileColourMap.get(colour));
        pix.fill();
        texture = new Texture(pix);
    }

    public void setRegion(float[] vertices, short[] indices) {
        region = new PolygonRegion(new TextureRegion(texture), vertices, indices);
    }

    public void drawTextures() {
        POLY_BATCH.draw(region, 0, 0);
    }

    public void onLanded() {

    }

    public Texture getTexture() { return texture; }
    public PolygonRegion getRegion() { return region; }
    public TileColour getColour() { return colour; }

    public void setBetMultiplier(float betMultiplier) { this.betMultiplier = betMultiplier; }
    public float getBetMultiplier() { return betMultiplier; }
}
