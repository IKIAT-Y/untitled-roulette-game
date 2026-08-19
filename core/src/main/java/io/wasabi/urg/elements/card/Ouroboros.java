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
        boostedTile = Roulette.getInstance().getRunState().getLastTile();
    }

    @Override
    public void roundEndEffect() { boostedTile = null; }
}
