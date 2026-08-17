package io.wasabi.urg.elements.card;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.state.RunState;

public class ExtraChange extends Card {

    RunState runState = Roulette.getInstance().getRunState();

    public ExtraChange() {
        super(Rarity.UNCOMMON);
    }

    @Override
    public void roundStartEffect() {
        // Temporary test effect
        System.out.println("before round triggered");
        runState.addChips(5);
    }

    @Override
    public void beforeSpinEffect() {
        System.out.println("before spin triggered");
        runState.addChips(5);
        System.out.println("Chips: " + runState.getChips());
    }

    @Override
    public void afterSpinEffect() {
        System.out.println("after spin triggered");
        // Example effect: Add 5 chips if tile is odd
        if (runState.getLastTile() != null &&
                runState.getLastTile().getNumber() % 2 != 0) {
            runState.addChips(5);
            System.out.println("Added 5 chips for landing on an odd tile.");
            System.out.println("Chips: " + runState.getChips());
        }
    }

    @Override
    public void render() {
        // Implement rendering logic for Extra Change card
    }
}
