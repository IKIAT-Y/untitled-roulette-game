package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.state.RunState;

public class ExtraChange extends Card {

    RunState runState = Roulette.getInstance().getRunState();

    public ExtraChange() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Extra Change");
        tooltip.setDescription("Start each round with extra chips equal to 10% of the quota");
    }

    @Override
    public void roundStartEffect() {

        triggerDisplay();
        runState.addChips(Roulette.getInstance().getRoundManager().getCurrentConfig().getQuota()/10);
    }
}
