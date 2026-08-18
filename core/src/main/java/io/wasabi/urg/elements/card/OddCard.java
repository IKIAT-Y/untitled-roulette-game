package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

import java.util.List;

public class OddCard extends Card{

    public OddCard() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("OddCard card effect triggered");
        List<Tile> tiles = Roulette.getInstance().getScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getNumber() % 2 != 0) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
