package io.wasabi.urg.elements.betting;

import io.wasabi.urg.elements.game.Tile;

/**
 * A wager placed on a single {@link BetZone}. Kept separate from {@link Chip}
 * so the visual
 * chip stack and the logical wager can change independently (e.g. animating
 * chips without
 * touching payout math).
 */
public class Bet {
    private final BetZone zone;
    private int amount;

    public Bet(BetZone zone, int amount) {
        this.zone = zone;
        this.amount = amount;
    }

    public void addAmount(int extra) {
        this.amount += extra;
    }

    public boolean wins(Tile winningTile) {
        return zone.getCoveredTiles().contains(winningTile);
    }

    /**
     * Total amount returned (stake + profit) if this bet wins, using the "total
     * return"
     * multiplier convention already used in {@link BetType} (e.g. STRAIGHT = 36f,
     * not 35f).
     */
    public int payout(Tile winningTile) {
        if (!wins(winningTile)) {
            return 0;
        }
        return Math.round(amount * zone.getType().payoutMultiplier);
    }

    public BetZone getZone() {
        return zone;
    }

    public int getAmount() {
        return amount;
    }
}
