package io.wasabi.urg.elements.card;

import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class OddCard extends Card {

    public OddCard() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Odd Card");
        tooltip.setDescription("Odd numbered tiles give [RED]1.5x[BLACK] payout");
    }

    @Override
    public void roundStartEffect() {
        triggerDisplay();
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getNumber() % 2 != 0) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
