package io.wasabi.urg.elements.card;
import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class BlackCard extends Card {

    public BlackCard() {
        super(Rarity.COMMON);
        this.price = 4;
        this.sellPrice = 2;
        tooltip.setTitle("Black Card");
        tooltip.setDescription("Black tiles give [RED]1.5x [BLACK]payout");
    }

    @Override
    public void roundStartEffect() {
        triggerDisplay();
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getType().isBlack()) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
