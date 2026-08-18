package io.wasabi.urg.elements.card;

import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.TileType;

public class BlackCard extends Card {

    public BlackCard() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("BlackCard card effect triggered");
        List<Tile> tiles = Roulette.getInstance().getScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getColor() == TileType.TileColour.BLACK) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
