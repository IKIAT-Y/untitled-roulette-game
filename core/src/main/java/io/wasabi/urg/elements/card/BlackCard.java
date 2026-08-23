package io.wasabi.urg.elements.card;
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
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        if (winningTile.isBlack()) {
            triggerDisplay();
            return 1.5f;
        } else {
            return 1.0f;
        }
    }
}
