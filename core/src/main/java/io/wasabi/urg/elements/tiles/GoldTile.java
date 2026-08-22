package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import io.wasabi.urg.Roulette;

public class GoldTile extends TileType {
    private static final int GOLD_COLOUR = 0xFFCB1FFF;

    public GoldTile() {
        super();
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("Gain 4 [#FFCB1FFF]TICKETS [BLACK]when landed on");
        tooltip.addType("GOLDEN", Color.WHITE, new Color(GOLD_COLOUR));
    }

    @Override
    public void setColour(TileColour colour) {
        super.setColour(colour);
        texture.dispose();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(GOLD_COLOUR);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void onLanded() {
        Roulette.getInstance().getRunState().addTickets(4);
    }
}
