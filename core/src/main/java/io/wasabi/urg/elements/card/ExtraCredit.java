package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.state.RunState;

public class ExtraCredit extends Card {

    public ExtraCredit() { super(Rarity.COMMON); }

    @Override
    public void roundStartEffect() {
        System.out.println("ExtraCredit card effect triggered");
        RoundManager roundManager = Roulette.getInstance().getRoundManager();
        roundManager.setSpinsRemaining(roundManager.getSpinsRemaining() + 1);
    }
}
