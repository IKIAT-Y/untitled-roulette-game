package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class Infinite extends Card {
    private float multiplier = 1f;

    public Infinite() { super(Rarity.RARE); }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return multiplier;
    }

    @Override
    public void afterSpinEffect() {
        Tile landedTile = io.wasabi.urg.Roulette.getInstance().getRunState().getLastTile();
        if (landedTile != null && landedTile.getNumber() == 8) {
            multiplier += 0.2f;
            triggerDisplay();
        }
    }
}
