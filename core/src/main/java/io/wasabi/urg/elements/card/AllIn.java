package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class AllIn extends Card {
    public AllIn() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("All In");
        tooltip.setDescription("Gain [RED]2x [BLACK]payout if you bet all your chips");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        triggerDisplay();
        return chipBalance > 0 && totalStaked == chipBalance ? 2f : 1f;
    }
}
