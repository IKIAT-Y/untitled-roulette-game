package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;

public class Oneshot extends Card {
    public Oneshot() { super(Rarity.RARE); }

    @Override
    public int getAdditionalEffectTriggers() {
        return 1;
    }

    @Override
    public void afterCardEffects(String effectType) {
        if ("roundStart".equals(effectType)) {
            Roulette.getInstance().getRoundManager().setSpinsRemaining(1);
        }
    }
}
