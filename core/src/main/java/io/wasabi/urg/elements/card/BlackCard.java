package io.wasabi.urg.elements.card;
import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class BlackCard extends Card {

    public BlackCard() {
        super(Rarity.COMMON);
        tooltip.setTitle("Black Card");
        tooltip.setDescription("Black tiles give [RED]1.5x [BLACK]payout");
    }

    @Override
    public void roundStartEffect() {
        System.out.println("BlackCard card effect triggered");
        triggerDisplay();
        List<Tile> tiles = Roulette.getInstance().getGameScreen().getWheel().getTiles();
        for (Tile tile : tiles) {
            if (tile.getType().isBlack()) {
                tile.setBetMultiplier(tile.getBetMultiplier() * 1.5f);
            }
        }
    }
}
