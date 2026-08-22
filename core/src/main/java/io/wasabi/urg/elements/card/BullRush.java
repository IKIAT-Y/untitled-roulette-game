package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.state.RunState;

public class BullRush extends Card {

    public BullRush() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("Bull Rush");
        tooltip.setDescription(
            "Landing on [RED]6 [BLACK]grants the payout immediately and grants a free re-spin"
        );
    }

    @Override
    public void afterSpinEffect() {
        RunState runState = Roulette.getInstance().getRunState();
        Tile landedTile = runState.getLastTile();

        if (landedTile != null && landedTile.getNumber() == 6) {
            runState.requestFreeSpin();
        }
    }
}