package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

import java.util.List;

public class GreenCard extends Card {

    public GreenCard() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("GreenCard card effect triggered");
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getColor() == 2) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 3f);
            }
        }
    }
}
