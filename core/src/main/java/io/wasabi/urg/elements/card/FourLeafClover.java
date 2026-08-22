package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class FourLeafClover extends Card {
    public FourLeafClover() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("Four Leaf Clover");
        tooltip.setDescription("Landing on a 7 pays out [#A37800]7 [BLACK]tickets");
    }

    @Override
    public void afterSpinEffect() {
        Tile landedTile = Roulette.getInstance().getRunState().getLastTile();
        if (landedTile != null && landedTile.getNumber() == 7) {
            Roulette.getInstance().getRunState().addTickets(7);
            triggerDisplay();
        }
    }
}
