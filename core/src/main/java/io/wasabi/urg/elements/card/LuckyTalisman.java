package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class LuckyTalisman extends Card {
    private float mult = 1.0f;

    public LuckyTalisman() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("Lucky Talisman");
        tooltip.setDescription("All tiles gain [RED]+0.1x [BLACK]payout every time a charm is used\nCurrently [RED]1.0x");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        triggerDisplay();
        return mult;
    }

    @Override
    public void charmConsumedEffect() {
        mult += 0.1f;
        tooltip.setDescription(String.format("All tiles gain [RED]+0.1x [BLACK]payout every time a charm is used\nCurrently [RED]%.1fx", mult));
    }

}
