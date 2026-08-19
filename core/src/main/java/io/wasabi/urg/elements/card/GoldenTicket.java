package io.wasabi.urg.elements.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class GoldenTicket extends Card {
    private static final int ENCHANTED_TILE_COUNT = 2;
    private final List<Tile> enchantedTiles = new ArrayList<>();
    private int activeRound = -1;

    public GoldenTicket() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        int roundNumber = Roulette.getInstance().getRoundManager().getOverallRoundNumber();
        if (roundNumber != activeRound) {
            activeRound = roundNumber;
            enchantedTiles.clear();
        }

        List<Tile> tiles = new ArrayList<>(Roulette.getInstance().getRunState().getTiles());
        tiles.removeAll(enchantedTiles);
        Collections.shuffle(tiles);
        enchantedTiles.addAll(tiles.subList(0, Math.min(ENCHANTED_TILE_COUNT, tiles.size())));
    }

    @Override
    public void afterSpinEffect() {
        if (enchantedTiles.contains(Roulette.getInstance().getRunState().getLastTile())) {
            Roulette.getInstance().getRunState().addTickets(4);
        }
    }
}
