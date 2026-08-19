package io.wasabi.urg.elements.card;

import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class GreenCard extends Card {

    public GreenCard() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("GreenCard card effect triggered");
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getType().isGreen()) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 3f);
            }
        }
    }
}
