package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.state.RunState;

public class ExtraChange extends Card {

    RunState runState = Roulette.getInstance().getRunState();

    public ExtraChange() {
        super(Rarity.COMMON);
        tooltip.setTitle("Extra Change");
        tooltip.setDescription("Start each round with extra chips equal to 10% of the quota");
    }

    @Override
    public void roundStartEffect() {
        //System.out.println("ExtraChange card effect triggered");
        triggerDisplay();
        runState.addChips((int) Roulette.getInstance().getRoundManager().getCurrentConfig().getQuota()/10);
    }
}
