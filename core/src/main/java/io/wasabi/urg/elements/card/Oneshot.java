package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;

public class Oneshot extends Card {
    public Oneshot() {
        super(Rarity.RARE);
        tooltip.setTitle("Oneshot");
        tooltip.setMinWidth(200f);
        tooltip.setDescription("Set your spins to [RED]1\n[BLACK]All card effects trigger [RED]TWICE\n[#888888](overrides other cards' effects on spin count)");
    }

    @Override
    public int getAdditionalEffectTriggers() {
        return 1;
    }

    @Override
    public void afterCardEffects(String effectType) {
        if ("roundStart".equals(effectType)) {
            Roulette.getInstance().getRoundManager().setSpinsRemaining(1);
        }
        triggerDisplay();
    }
}
