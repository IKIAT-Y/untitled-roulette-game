package io.wasabi.urg.elements.betting;

import io.wasabi.urg.elements.game.Tile;

/**
 * Staged result of one {@link RunState#resolveActiveBetsDetailed()} call, in
 * the exact order {@code WinAnimation} reveals them:
 * stake -> flat bonus -> tile multiplier -> global multiplier -> final total.
 *
 * {@code flatBonus} is a stub (always 0 today) reserved for per-tile flat
 * increases that aren't implemented yet — wiring it in later only means
 * making {@link RunState#resolveActiveBetsDetailed()} compute a nonzero
 * value; this class and {@code WinAnimation} already know how to show it.
 */
public final class WinBreakdown {
    private final int totalStaked;
    private final int rawPayout;
    private final int winningStake;
    private final float payoutMultiplier; // sum of Bet#payout(winningTile) before any multipliers
    private final int flatBonus; // TODO: not implemented yet, see class javadoc
    private final float tileMultiplier;
    private final float globalMultiplier;
    private final int finalTotal;
    private final Tile winningTile;

    public WinBreakdown(int totalStaked, int rawPayout, int winningStake, float payoutMultiplier, int flatBonus, float tileMultiplier,
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

    public int getFlatBonus() {
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
        System.out.println("payoutMultiplier: " + payoutMultiplier + ", flatBonus: " + flatBonus);
        return rawPayout <= 0 && flatBonus <= 0;
    }
}
