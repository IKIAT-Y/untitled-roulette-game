package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class Infinite extends Card {
    private float multiplier = 1f;

    public Infinite() {
        super(Rarity.RARE);
        tooltip.setTitle("Infinite");
        tooltip.setMinWidth(200f);
        tooltip.setDescription("All tiles gain [RED]+0.2x [BLACK]payout every time an 8 is scored\nCurrently [RED]1.0x");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return multiplier;
    }

    @Override
    public void afterSpinEffect() {
        Tile landedTile = io.wasabi.urg.Roulette.getInstance().getRunState().getLastTile();
        if (landedTile != null && landedTile.getNumber() == 8) {
            multiplier += 0.2f;
            tooltip.setDescription(String.format("All tiles gain [RED]+0.2x [BLACK]payout every time an 8 is scored\nCurrently [RED]%.1fx", multiplier));
            triggerDisplay();
        }
    }
}
