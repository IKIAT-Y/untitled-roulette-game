package io.wasabi.urg.elements.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.GoldTile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.elements.tiles.VoidTile;

public class GoldenTicket extends Card {
    private static final int ENCHANTED_TILE_COUNT = 2;
    private final Map<Tile, TileType> enchantedTiles = new LinkedHashMap<>();
    private int activeRound = -1;

    public GoldenTicket() {
        super(Rarity.COMMON);
        tooltip.setTitle("Golden Ticket");
        tooltip.setDescription(
                "At round start, enchant 2 tiles. Gain 4 [#FFCB1FFF]TICKETS "
                        + "[BLACK]when landing on an enchanted tile");
    }

    @Override
    public void roundStartEffect() {
        int roundNumber = Roulette.getInstance().getRoundManager().getOverallRoundNumber();
        if (roundNumber != activeRound) {
            activeRound = roundNumber;
            restoreTiles();
        }

        List<Tile> tiles = new ArrayList<>(Roulette.getInstance().getRunState().getTiles());
        tiles.removeIf(tile -> tile.getType() instanceof GoldTile
                || tile.getType() instanceof VoidTile);
        Collections.shuffle(tiles);
        for (Tile tile : tiles.subList(0, Math.min(ENCHANTED_TILE_COUNT, tiles.size()))) {
            TileType originalType = tile.getType();
            enchantedTiles.put(tile, originalType);

            GoldTile goldType = new GoldTile();
            goldType.setColour(originalType.getColour());
            goldType.setNumber(originalType.getNumber());
            goldType.setBetMultiplier(originalType.getBetMultiplier());
            tile.setType(goldType);
        }
    }

    @Override
    public void roundEndEffect() {
        restoreTiles();
    }

    private void restoreTiles() {
        for (Map.Entry<Tile, TileType> entry : enchantedTiles.entrySet()) {
            if (entry.getKey().getType() instanceof GoldTile) {
                entry.getKey().setType(entry.getValue());
            }
        }
        enchantedTiles.clear();
    }
}
