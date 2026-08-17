package io.wasabi.urg.elements.tiles;

import java.util.EnumMap;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public abstract class TileType {
    public enum TileColour {
        BLACK, RED, GREEN
    }

    protected static EnumMap<TileColour, Integer> tileColourMap = new EnumMap<TileColour, Integer>(TileColour.class) {{
        put(TileColour.BLACK, 0x000000FF);
        put(TileColour.RED, 0xFF0000FF);
        put(TileColour.GREEN, 0x00AA00FF);
    }};

    protected Texture texture;
    protected TileColour colour;

    public TileType() {

    }

    public void setColour(TileColour colour) {
        this.colour = colour;
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor((int) tileColourMap.get(colour));
        pix.fill();
        texture = new Texture(pix);
    }

    public Texture getTexture() { return texture; }
}
