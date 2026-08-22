package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class FourLeafClover extends Card {
    public FourLeafClover() { super(Rarity.UNCOMMON); }

    @Override
    public void afterSpinEffect() {
        Tile landedTile = Roulette.getInstance().getRunState().getLastTile();
        if (landedTile != null && landedTile.getNumber() == 7) {
            Roulette.getInstance().getRunState().addTickets(7);
            triggerDisplay();
        }
    }
}
