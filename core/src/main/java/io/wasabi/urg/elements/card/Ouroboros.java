package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class Ouroboros extends Card {
    private Tile boostedTile;

    public Ouroboros() { super(Rarity.UNCOMMON); }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return winningTile == boostedTile ? 3f : 1f;
    }

    @Override
    public void afterSpinEffect() {
        if (boostedTile != null) {
            boostedTile.removeIndicator(Tile.Indicator.OUROBOROS);
        }
        boostedTile = Roulette.getInstance().getRunState().getLastTile();
        if (boostedTile != null) {
            boostedTile.addIndicator(Tile.Indicator.OUROBOROS);
        }
    }

    @Override
    public void roundEndEffect() {
        if (boostedTile != null) {
            boostedTile.removeIndicator(Tile.Indicator.OUROBOROS);
            boostedTile = null;
        }
    }
}
