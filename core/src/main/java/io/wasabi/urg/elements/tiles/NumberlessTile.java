package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;

import io.wasabi.urg.Roulette;

public class NumberlessTile extends TileType {
    private static final Color NUMBERLESS_COLOUR = new Color(0.75f, 0.75f, 0.75f, 1f);

    public NumberlessTile() {
        this.betMultiplier = 1f;
        this.flatBonus = 50f;
        tooltip.setTitle("NUMBERLESS");
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("This tile no longer counts as any number. Gain an extra 50 chips when landed on");
        tooltip.addType("NUMBERLESS", Color.BLACK, NUMBERLESS_COLOUR);
    }

    @Override
    protected void updateTooltipTitle() {
        tooltip.setTitle("NUMBERLESS");
    }

    @Override
    public int getNumber() {
        return -1;
    }
}
