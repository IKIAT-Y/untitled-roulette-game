package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.managers.RoundManager;

public class ExtraCredit extends Card {

    public ExtraCredit() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Extra Credit");
        tooltip.setDescription("+1 spin each round");
    }

    @Override
    public void roundStartEffect() {
        triggerDisplay();
        RoundManager roundManager = Roulette.getInstance().getRoundManager();
        roundManager.setSpinsRemaining(roundManager.getSpinsRemaining() + 1);
    }
}
