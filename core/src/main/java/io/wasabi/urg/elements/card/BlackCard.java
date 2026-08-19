package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

import java.util.List;

public class BlackCard extends Card {

    public BlackCard() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("BlackCard card effect triggered");
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getColor() == 1) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
