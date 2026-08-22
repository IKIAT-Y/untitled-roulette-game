package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.state.RunState;

public class ExtraChange extends Card {

    RunState runState = Roulette.getInstance().getRunState();

    public ExtraChange() {
        super(Rarity.COMMON);
    }

    @Override
    public void roundStartEffect() {
        System.out.println("ExtraChange card effect triggered");
        runState.addChips((int) Roulette.getInstance().getRoundManager().getCurrentConfig().getQuota()/10);
    }
}
