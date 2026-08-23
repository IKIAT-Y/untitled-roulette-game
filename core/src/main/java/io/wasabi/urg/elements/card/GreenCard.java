package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class GreenCard extends Card {

    public GreenCard() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Green Card");
        tooltip.setDescription("[GREEN]Green [BLACK]tiles give [RED]3x [BLACK]payout");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        if (winningTile.isGreen()) {
            triggerDisplay();
            return 3f;
        } else {
            return 1.0f;
        }
    }
}
