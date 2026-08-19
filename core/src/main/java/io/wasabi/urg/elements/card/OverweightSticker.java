package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;

public class OverweightSticker extends Card {
    private static final float WEIGHT_MULTIPLIER = 1.3f;

    public OverweightSticker() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        Roulette.getInstance().getGameScreen().getBall().setWeightMultiplier(WEIGHT_MULTIPLIER);
    }
}
