package io.wasabi.urg.elements.betting;

import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.state.RunState;

/**
 * Staged result of one {@link RunState#resolveActiveBetsDetailed()} call, in
 * the exact order {@code WinAnimation} reveals them:
 * stake -> flat bonus -> tile multiplier -> global multiplier -> final total.
 *
 * {@code flatBonus} is the winning tile's flat bonus (see {@code Tile#getFlatBonus()}),
 * summed once per winning bet to mirror {@code winningStake}. It's for display only:
 * {@link RunState#resolveActiveBetsDetailed()} already folds each winning bet's flat
 * bonus into its own payout (see {@code Bet#payout}) before {@code finalTotal} is
 * computed, so this field must never be added into a payout total again.
 */
public final class WinBreakdown {
    private final int totalStaked;
    private final int rawPayout;
    private final int winningStake;
    private final float payoutMultiplier; // sum of Bet#payout(winningTile) before any multipliers
    private final float flatBonus;
    private final float tileMultiplier;
    private final float globalMultiplier;
    private final int finalTotal;
    private final Tile winningTile;

    public WinBreakdown(int totalStaked, int rawPayout, int winningStake, float payoutMultiplier, float flatBonus, float tileMultiplier,
                        float globalMultiplier, int finalTotal, Tile winningTile) {
        this.totalStaked = totalStaked;
        this.rawPayout = rawPayout;
        this.winningStake = winningStake;
        this.payoutMultiplier = payoutMultiplier;
        this.flatBonus = flatBonus;
        this.tileMultiplier = tileMultiplier;
        this.globalMultiplier = globalMultiplier;
        this.finalTotal = finalTotal;
        this.winningTile = winningTile;
    }

    public int getTotalStaked() { return totalStaked; }

    public int getRawPayout() { return rawPayout; }

    public int getWinningStake() {
        return winningStake;
    }

    public float getPayoutMultiplier() { return payoutMultiplier; }

    public float getFlatBonus() {
        return flatBonus;
    }

    public float getTileMultiplier() {
        return tileMultiplier;
    }

    public float getGlobalMultiplier() {
        return globalMultiplier;
    }

    public int getFinalTotal() {
        return finalTotal;
    }

    public Tile getWinningTile() {
        return winningTile;
    }

    /** Nothing was won (no bets, or nothing covered the winning tile). */
    public boolean isEmpty() {
        return rawPayout <= 0 && flatBonus <= 0;
    }
}
