package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class Jackpot extends Card {
    private int lastCountedRound = -1;
    private boolean jackpotRound;

    public Jackpot() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        int roundNumber = io.wasabi.urg.Roulette.getInstance()
                .getRoundManager().getOverallRoundNumber();
        if (roundNumber != lastCountedRound) {
            lastCountedRound = roundNumber;
            jackpotRound = roundNumber % 7 == 0;
        }
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return jackpotRound ? 7f : 1f;
    }
}
