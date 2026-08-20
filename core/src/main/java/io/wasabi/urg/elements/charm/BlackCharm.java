package io.wasabi.urg.elements.charm;

import java.util.List;

import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.TileType;

public class BlackCharm extends AbstractCharm {

    public BlackCharm() {
        super();
        tooltip.setTitle("Black Charm");
        tooltip.setDescription("Choose two tiles, turn them into black tiles.");
    }
    
    @Override
    public boolean requiresTileSelection() {
        return true;
    }

    @Override
    public int getRequiredTileSelections() {
        return 2;
    }

    @Override
    public void beforeSpinEffect() {
        List<Tile> selectedTiles = getSelectedTiles();
        for (Tile tile : selectedTiles) {
            tile.setColor(TileType.TileColour.BLACK);
        }
    }
}
