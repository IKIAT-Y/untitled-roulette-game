package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class MetallicTile extends TileType {
    private static final int METALLIC_COLOUR = 0x999999FF;

    public MetallicTile() {
        super();
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("Gain [RED]1.5x [BLACK]winnings from this tile");
        tooltip.addType("METALLIC", Color.WHITE, new Color(METALLIC_COLOUR));
    }

    @Override
    public void setColour(TileColour colour) {
        super.setColour(colour);
        texture.dispose();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(METALLIC_COLOUR);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }
}
